/*
 * Copyright 2016 - Deployment IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deploymentio.ec2namer;

import com.deploymentio.ec2namer.helpers.DnsRegistrar;
import com.deploymentio.ec2namer.helpers.NameReserver;

public class DenameFunction extends JsonLambdaFunction<InstanceEvent, Boolean, Boolean>{

	protected NameReserver reserver = new NameReserver();
	protected DnsRegistrar dnsRegistrar = new DnsRegistrar();

	@Override
	public Boolean error(LambdaContext context, String error) {
		return Boolean.FALSE;
	}
	
	public Boolean process(InstanceEvent evt, LambdaContext context) throws java.io.IOException {
		DenameRequest request = reserver.unreserve(evt.getDetails().getInstanceId(), context);
		if (request != null) {
			dnsRegistrar.deregister(request, context);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	@Override
	public boolean validate(InstanceEvent evt, LambdaContext context) {
		return "aws.ec2".equals(evt.getSource()) && "shutting-down".equals(evt.getDetails().getState());
	}
}
