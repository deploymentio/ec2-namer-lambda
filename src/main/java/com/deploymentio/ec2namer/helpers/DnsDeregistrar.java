package com.deploymentio.ec2namer.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.DeleteHealthCheckRequest;
import com.amazonaws.services.route53.model.HealthCheck;
import com.amazonaws.services.route53.model.ListHealthChecksRequest;
import com.amazonaws.services.route53.model.ListHealthChecksResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.deploymentio.ec2namer.DenamingRequest;
import com.deploymentio.ec2namer.LambdaContext;

public class DnsDeregistrar extends DnsHelper {
	
	public void deregister(DenamingRequest req, LambdaContext context) throws IOException {
		
		// delete the requested name records
		String zoneId = findHostedZoneId(context, req.getBaseDomain());
		List<Change> changes = getRequestedNameDeleteChanges(req, context, zoneId);
		
		// delete the reserved name record too
		Change change = getReservedNameDeleteChange(req, context, zoneId);
		if (change != null) {
			changes.add(change);
		}
		
		// execute the changes
		if (!changes.isEmpty()) {
			route53.changeResourceRecordSets(new ChangeResourceRecordSetsRequest(zoneId, new ChangeBatch().withChanges(changes))) ;
		}
		
		// delete all health-checks for this instance
		deleteHealthChecks(req.getInstanceId(), context);
	}
	
	private List<Change> getRequestedNameDeleteChanges(DenamingRequest req, LambdaContext context, String zoneId) {
		
		List<Change> changes = new ArrayList<Change>() ;
		
		String setId = req.getInstanceId() + "-" + req.createFqdn(req.getReservedName().getHostname());
		String marker = req.getEnvironment() + "." + req.getBaseDomain();
		String justDomain = "." + marker + ".";
		
		Outer: do {
			
			ListResourceRecordSetsResult result = route53.listResourceRecordSets(
					new ListResourceRecordSetsRequest(zoneId)
						.withStartRecordType(RRType.CNAME)
						.withStartRecordName(marker));			
			
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
						context.log("Marked for deletion: Name=" + set.getName() + " Value=" + set.getResourceRecords().get(0).getValue() + " Type=" + set.getType());
						break;
					}
				}
			}

			marker = result.getNextRecordName();
			
		} while(changes.size() < req.getRequestedNames().size() && !StringUtils.isEmpty(marker));

		return changes;
	}

	private Change getReservedNameDeleteChange(DenamingRequest req, LambdaContext context, String zoneId) {

		String recordName = req.createFqdn(req.getReservedName().getHostname());
		
		ListResourceRecordSetsResult result = route53.listResourceRecordSets(
				new ListResourceRecordSetsRequest(zoneId)
					.withMaxItems("1")
					.withStartRecordType(req.getReservedNameRecordType())
					.withStartRecordName(recordName));
		
		if (!result.getResourceRecordSets().isEmpty()) {
			ResourceRecordSet set = result.getResourceRecordSets().get(0);
			ResourceRecord record = set.getResourceRecords().get(0);
			if (record.getValue().equals(req.getReservedNameRecordValue())) {
				context.log("Marked for deletion: Name=" + set.getName() + " Value=" + record.getValue() + " Type=" + set.getType());
				return new Change(ChangeAction.DELETE, set);
			}
		}
		
		return null;
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
}
