package com.deploymentio.ec2namer.helpers;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.deploymentio.ec2namer.LambdaContext;

public abstract class DnsHelper {

	protected AmazonRoute53 route53 = new AmazonRoute53Client();
	
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
