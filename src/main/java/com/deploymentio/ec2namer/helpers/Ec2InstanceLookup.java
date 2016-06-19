package com.deploymentio.ec2namer.helpers;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.route53.model.RRType;
import com.deploymentio.ec2namer.LambdaContext;

public class Ec2InstanceLookup {

	protected AmazonEC2 ec2 = new AmazonEC2Client();
	
	public Instance lookup(LambdaContext context, String instanceId) {
		Instance inst = (Instance) context.get("inst");
		if (inst == null) {
			inst = getInstance(instanceId);
			context.put("inst", inst);
		}
		return inst;
	}
	
	public RRType getReservedNameRecordType(LambdaContext context, String instanceId, boolean alwaysUsePublicName) {
		Instance instance = lookup(context, instanceId);
		boolean useVpcPrivateIp = alwaysUsePublicName ? false : !StringUtils.isEmpty(instance.getVpcId());
		return useVpcPrivateIp ? RRType.A : RRType.CNAME;
	}

	public String getReservedNameRecordValue(LambdaContext context, String instanceId, boolean alwaysUsePublicName) {
		Instance instance = lookup(context, instanceId);
		boolean useVpcPrivateIp = alwaysUsePublicName ? false : !StringUtils.isEmpty(instance.getVpcId());
		return useVpcPrivateIp ? instance.getPrivateIpAddress() : instance.getPublicDnsName();
	}

	protected Instance getInstance(String instanceId) {
		DescribeInstancesResult result = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
		if (!result.getReservations().isEmpty()) {
			Reservation reservation = result.getReservations().get(0);
			if (!reservation.getInstances().isEmpty()) {
				return reservation.getInstances().get(0);				
			}
		}
		return null;
	}
}
