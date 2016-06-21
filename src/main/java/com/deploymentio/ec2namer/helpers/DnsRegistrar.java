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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeTagsForResourceRequest;
import com.amazonaws.services.route53.model.CreateHealthCheckRequest;
import com.amazonaws.services.route53.model.CreateHealthCheckResult;
import com.amazonaws.services.route53.model.HealthCheckConfig;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.amazonaws.services.route53.model.Tag;
import com.amazonaws.services.route53.model.TagResourceType;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;
import com.deploymentio.ec2namer.RequestedName;

public class DnsRegistrar extends DnsHelper implements Validator {

	protected Ec2InstanceLookup instanceLookup = new Ec2InstanceLookup();
	
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
	public void register(NamingRequest req, LambdaContext context, ReservedName name) throws IOException {
		
		List<Change> changes = new ArrayList<Change>() ;
		
		// lets create the first record
		changes.add(getCreateChange(req, context, name.getHostname()));
		
		// create the additional names
		for (RequestedName additionalName : req.getRequestedNames()) {
			changes.add(getCreateChange(req, context, additionalName, name.getHostname()));
		}
		
		// execute the changes
		String zoneId = findHostedZoneId(context, req.getBaseDomain());
		route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch().withChanges(changes))) ;
	}
	
	protected Change getCreateChange(NamingRequest req, LambdaContext context, String name) {
		RRType recordType = instanceLookup.getReservedNameRecordType(context, req.getInstanceId(), req.isAlwaysUsePublicName());
		
		ResourceRecordSet set = new ResourceRecordSet(req.createFqdn(name) + ".", recordType);
		set.withTTL(60l) ;
		set.withResourceRecords(new ResourceRecord(instanceLookup.getReservedNameRecordValue(context, req.getInstanceId(), req.isAlwaysUsePublicName())));
		
		return new Change(ChangeAction.UPSERT, set);
	}
	
	protected Change getCreateChange(NamingRequest req, LambdaContext context, RequestedName additionalName, String assignedName) {

		String fullAssignedName = req.createFqdn(assignedName);
		String fullAdditionalName =  req.createFqdn(additionalName.getName());
		
		Instance instance = instanceLookup.lookup(context, req.getInstanceId());
		boolean usePublicHostname = req.isAlwaysUsePublicName() || StringUtils.isEmpty(instance.getVpcId());

		ResourceRecordSet set = new ResourceRecordSet(fullAdditionalName + ".", RRType.CNAME)
			.withTTL(60l)
			.withResourceRecords(new ResourceRecord(usePublicHostname ? instance.getPublicDnsName() : fullAssignedName))
			.withSetIdentifier(req.getInstanceId() + "-" + fullAssignedName)
			.withWeight(Long.valueOf(additionalName.getWeight()));
		
		if (additionalName.isHealthChecked()) {
			
			String uuid = req.getInstanceId() + "-" + UUID.randomUUID().toString();
			String hcId = null;

			for (int i = 0; i < 5; i++) {
				try {
					
					if (i > 0) {
						Thread.sleep(5000);
					}

					CreateHealthCheckResult result = route53.createHealthCheck(new CreateHealthCheckRequest().withCallerReference(uuid).withHealthCheckConfig(new HealthCheckConfig()
						.withFullyQualifiedDomainName(fullAssignedName)
						.withIPAddress(instance.getPublicIpAddress())
						.withPort(additionalName.getHealthCheckPort())
						.withType(additionalName.getHealthCheckType())
						.withResourcePath(additionalName.getHealthCheckUri())));
					
					hcId = result.getHealthCheck().getId() ;
					context.log("Created health-check: Name=" + fullAdditionalName + " Protocol=" + additionalName.getHealthCheckType() + " Port=" + additionalName.getHealthCheckPort() + " Uri=" + additionalName.getHealthCheckUri() + " Id=" + hcId);

					// create tags on health-check
					route53.changeTagsForResource(new ChangeTagsForResourceRequest()
						.withResourceId(hcId)
						.withResourceType(TagResourceType.Healthcheck)
						.withAddTags(
							new Tag().withKey("Name").withValue(req.getEnvironment() + ":" + assignedName),
							new Tag().withKey("InstanceId").withValue(req.getInstanceId())));
					
					break;
					
				} catch (Exception e) {
					context.log("Can't create health-check: Attempt= " + (i+1) + " Name=" + fullAdditionalName + " Protocol=" + additionalName.getHealthCheckType() + " Port=" + additionalName.getHealthCheckPort() + " Uri=" + additionalName.getHealthCheckUri() + " Id=" + hcId + " Ref=" + uuid + " Error=" + e.getMessage());
				}
			}
			
			if (!StringUtils.isEmpty(hcId)) {
				set.setHealthCheckId(hcId);
			}
		}
		
		return new Change(ChangeAction.UPSERT, set);
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {
		
		if (StringUtils.isEmpty(req.getBaseDomain())) {
			context.log("BaseDomain is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getEnvironment())) {
			context.log("Environment is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getGroup())) {
			context.log("Group is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getInstanceId())) {
			context.log("InstanceId is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(findHostedZoneId(context, req.getBaseDomain()))) {
			context.log("HostedZone is not found: BaseDomain=" + req.getBaseDomain());
			return false;
		}
			
		return true;
	}
}
