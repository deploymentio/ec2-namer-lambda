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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeTagsForResourceRequest;
import com.amazonaws.services.route53.model.CreateHealthCheckRequest;
import com.amazonaws.services.route53.model.CreateHealthCheckResult;
import com.amazonaws.services.route53.model.DeleteHealthCheckRequest;
import com.amazonaws.services.route53.model.HealthCheck;
import com.amazonaws.services.route53.model.HealthCheckConfig;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHealthChecksRequest;
import com.amazonaws.services.route53.model.ListHealthChecksResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.amazonaws.services.route53.model.Tag;
import com.amazonaws.services.route53.model.TagResourceType;
import com.deploymentio.ec2namer.DenamingRequest;
import com.deploymentio.ec2namer.InstanceNamingRequest;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;
import com.deploymentio.ec2namer.RequestedName;

public class DnsRegistrar implements Validator {

	protected AmazonRoute53 route53 = new AmazonRoute53Client();
	protected Ec2InstanceLookup instanceLookup = new Ec2InstanceLookup();
	
	public void deregister(DenamingRequest req, LambdaContext context) throws IOException {
		
		List<Change> changes = new ArrayList<Change>() ;
		
		// delete the additional names
		String zoneId = findHostedZoneId(context, req.getBaseDomain());
		String setId = req.getInstanceId() + "-" + req.createFqdn(req.getReservedName().getHostname());
		String marker = getReversedDomain(req.getEnvironment(), req.getBaseDomain());
		String justDomain = req.createFqdn("") + ".";
		
		Outer: do {
			
			ListResourceRecordSetsResult result = route53.listResourceRecordSets(
					new ListResourceRecordSetsRequest(zoneId)
						.withStartRecordType(RRType.CNAME)
						.withStartRecordName(marker));			
			marker = result.getNextRecordName();
			
			for (ResourceRecordSet set : result.getResourceRecordSets()) {
		
				// break out if we have strayed into another domain's records
				if (!set.getName().endsWith(justDomain) || !set.getType().equals(RRType.CNAME.name())) {
					break Outer;
				}
				
				String fullDomainName = StringUtils.replace(set.getName(), "\\052", "\052");
				for (String additionalName : req.getRequestedNames()) {
					
					String baseDomainInDnsSpeak = req.createFqdn(additionalName) + ".";
					if (fullDomainName.equals(baseDomainInDnsSpeak) && StringUtils.equals(setId, set.getSetIdentifier())) {
						changes.add(new Change(ChangeAction.DELETE, set));
						break;
					}
				}
			}

		} while(changes.size() < req.getRequestedNames().size() && !StringUtils.isEmpty(marker));
		
		// delete the reserved name record too
		changes.add(getDeleteChange(req, context, req.getReservedName().getHostname()));
		
		// execute the changes
		route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch().withChanges(changes))) ;

		// delete all health-checks for this instance
		deleteHealthChecks(req.getInstanceId(), context);
	}
	
	protected String getReversedDomain(String environment, String baseDomain) {
		String[] split = StringUtils.split(baseDomain, '.');
		ArrayUtils.reverse (split);
		return StringUtils.join(split, '.') + "." + environment + "." ;
	}
	
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
		
		// delete existing health-checks for this instance
		deleteHealthChecks(req.getInstanceId(), context);
		
		// create the additional names
		for (RequestedName additionalName : req.getRequestedNames()) {
			changes.add(getCreateChange(req, context, additionalName, name.getHostname()));
		}
		
		// execute the changes
		String zoneId = findHostedZoneId(context, req.getBaseDomain());
		route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch().withChanges(changes))) ;
	}
	
	protected Change getDeleteChange(InstanceNamingRequest req, LambdaContext context, String name) {
		return getChange(req, context, name, ChangeAction.DELETE);
	}
	
	protected Change getCreateChange(InstanceNamingRequest req, LambdaContext context, String name) {
		return getChange(req, context, name, ChangeAction.UPSERT);
	}
	
	protected Change getChange(InstanceNamingRequest req, LambdaContext context, String name, ChangeAction action) {
		Instance instance = instanceLookup.lookup(context, req.getInstanceId());
		boolean isVpc = req.isAlwaysUsePublicName() ? false : !StringUtils.isEmpty(instance.getVpcId());
		RRType recordType = isVpc ? RRType.A : RRType.CNAME;
		
		ResourceRecordSet set = new ResourceRecordSet(req.createFqdn(name) + ".", recordType);
		set.withTTL(60l) ;
		set.withResourceRecords(new ResourceRecord(isVpc ? instance.getPrivateIpAddress() : instance.getPublicDnsName()));
		
		return new Change(action, set);
	}
	
	protected Change getCreateChange(NamingRequest req, LambdaContext context, RequestedName additionalName, String assignedName) {

		String fullAssignedName = req.createFqdn(assignedName);
		String fullAdditionalName =  req.createFqdn(additionalName.getName());
		
		Instance instance = instanceLookup.lookup(context, req.getInstanceId());
		boolean usePublicHostname = req.isAlwaysUsePublicName() || StringUtils.isEmpty(instance.getVpcId());

		ResourceRecordSet set = new ResourceRecordSet(fullAdditionalName + ".", RRType.CNAME)
			.withTTL(60l)
			.withResourceRecords(new ResourceRecord(usePublicHostname ? instance.getPublicDnsName() : fullAssignedName))
			.withSetIdentifier(instance.getInstanceId() + "-" + fullAssignedName)
			.withWeight(Long.valueOf(additionalName.getWeight()));
		
		if (additionalName.isHealthChecked()) {
			
			String uuid = instance.getInstanceId() + "-" + UUID.randomUUID().toString();
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
	
	protected void deleteHealthChecks(String instanceId, LambdaContext context) {
		String marker = null;
		do {
			ListHealthChecksResult result = route53.listHealthChecks(new ListHealthChecksRequest().withMarker(marker));
			for (HealthCheck hc : result.getHealthChecks()) {
				if (hc.getCallerReference().startsWith(instanceId)) {
					String hcId = hc.getId();
					route53.deleteHealthCheck(new DeleteHealthCheckRequest().withHealthCheckId(hcId));
					context.log("Deleted health-check: Id=" + hcId + " FQDN=" + hc.getHealthCheckConfig().getFullyQualifiedDomainName());
				}
			}
			
		} while(!StringUtils.isEmpty(marker));
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
	
	protected String findHostedZoneId(LambdaContext context, String baseDomain) {
		String id = (String) context.get("hosted-zone");
		if (StringUtils.isEmpty(id)) {
			String lookingFor = baseDomain + ".";
			for (HostedZone zone : route53.listHostedZones().getHostedZones()) {
				if (lookingFor.equals(zone.getName())) {
					id = zone.getId();
					context.put("hosted-zone", id);
					context.log("Found hosted-zone: Id=" + zone.getId() + " Name=" + zone.getName());
				}
			}
		}
		return id;
	}
}
