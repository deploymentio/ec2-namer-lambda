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

package com.deploymentio.ec2namer;import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.deploymentio.ec2namer.helpers.DnsRegistrar;
import com.deploymentio.ec2namer.helpers.InstanceTagger;
import com.deploymentio.ec2namer.helpers.NameReserver;
import com.deploymentio.ec2namer.helpers.NamerRequestValidator;
import com.deploymentio.ec2namer.helpers.OsScriptGenerator;
import com.deploymentio.ec2namer.helpers.ReservedName;

/**
 * AWS Lambda function that names instances by reserving the next available name
 * in a DB, creating a DNS entry in Route53, tagging the EC2 instance based on
 * the assigned name, and returning a script which can be used by the caller to
 * set the hostname and set DNS resolution properly in the OS.
 */

public class NamerFunction extends JsonLambdaFunction<NamerRequest, NamerResponse> {

	protected NameReserver reserver = new NameReserver();
	protected DnsRegistrar dnsRegistrar = new DnsRegistrar();
	protected InstanceTagger tagger = new InstanceTagger();
	protected OsScriptGenerator scriptGenerator = new OsScriptGenerator();
	
	protected NamerRequestValidator validator = new NamerRequestValidator(
			reserver,
			dnsRegistrar,
			tagger,
			scriptGenerator);
	
	@Override
	public NamerResponse process(NamerRequest req, LambdaContext context) throws IOException {
		
		NamerResponse resp = new NamerResponse();
		
		// reserve the next available name in DB
		ReservedName name = reserver.reserve(req, context);
		if (name != null) {

			// copy the reserved named to the response
			resp.setHostname(name.getHostname());
			resp.setIndex(name.getIndex());
			
			// create the entries in route53
			dnsRegistrar.register(req, context, name);
			
			// create the tags on ec2 instance
			tagger.tag(req, context, name);
			
			// generate os script for setting hostname / name resolution
			String script = scriptGenerator.generate(req, context, name);
			resp.setOsScript(script);
			
			// setting the response as success
			resp.setSuccess(true);
		}
	
		return resp;
	}
	
	@Override
	public boolean validate(NamerRequest req, LambdaContext context) {
		return validator.validate(req, context);
	}
}
