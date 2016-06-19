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

package com.deploymentio.ec2namer.helpers;

import java.io.IOException;

import com.amazonaws.services.ec2.model.Instance;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;

public class OsScriptGenerator implements Validator {

	protected Ec2InstanceLookup instanceLookup = new Ec2InstanceLookup();

	/**
	 * Generates a script that the requester can run to set their hostname to
	 * the reserved name.
	 * 
	 * @param req
	 *            the namer request
	 * @param context
	 *            the lambda function execution context
	 * @param name
	 *            the reserved name for this instance
	 * @throws IOException
	 *             if the script cannot be generated
	 */
	public String generate(NamingRequest req, LambdaContext context, ReservedName name) throws IOException {
	
		Instance inst = instanceLookup.lookup(context, req.getInstanceId());
		StringBuilder b = new StringBuilder()
				
		/* append hostname to /etc/hosts */
		.append("printf '\\n# ec2-namer\\n%s %s %s\\n' '").append(inst.getPrivateIpAddress()).append("' '")
		.append(req.createFqdn(name.getHostname())).append("' '").append(name.getHostname())
		.append("' >> /etc/hosts\n")
		
		/* set hostname */
		.append("hostname ").append(name.getHostname()).append("\n")
		
		/* setup domain lookups */
		.append("printf 'prepend domain-name \"%s \";' '").append(req.getEnvironment()).append(".")
		.append(req.getBaseDomain()).append("' >> /etc/dhcp/dhclient.conf\n")

		/* restart dhcp */
		.append("ifdown eth0; ifup eth0\n");
		
		return b.toString();
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {
		return true;
	}
}
