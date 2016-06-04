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

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamerRequest;

public class DnsRegistrar implements Validator {

	protected AmazonRoute53 route53 = new AmazonRoute53Client();
	
	/**
	 * Registers the reserved name and all additional names along with their
	 * health-checks if requested. This will be done in Route53.
	 * 
	 * @param req
	 *            the namer request
	 * @param context
	 *            the lambda function execution context
	 * @param name
	 *            the reserved name for this instance
	 * @throws IOException
	 *             if the DNS name(s) cannot registered in Route53
	 */
	public void register(NamerRequest req, LambdaContext context, ReservedName name) throws IOException {
		
		// TODO: register the name in DNS, overwriting any existing entry
		
		String zoneId = (String) context.get("hosted-zone");
		
	}
	
	@Override
	public boolean validate(NamerRequest req, LambdaContext context) {
		
		if (StringUtils.isEmpty(req.getBaseDomain())) {
			context.getLogger().log("BaseDomain is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getEnvironment())) {
			context.getLogger().log("Environment is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getGroup())) {
			context.getLogger().log("Group is missing");
			return false;
		}
		
		ListHostedZonesResult result = route53.listHostedZones();
		String lookingFor = req.getBaseDomain() + ".";
		for (HostedZone zone : result.getHostedZones()) {
			if (lookingFor.equals(zone.getName())) {
				context.put("hosted-zone", zone.getId());
				return true;
			}
		}
		
		context.getLogger().log("HostedZone is not found: BaseDomain=" + req.getBaseDomain());
		return false;
	}
}
