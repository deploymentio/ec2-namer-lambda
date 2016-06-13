package com.deploymentio.ec2namer.helpers;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
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
