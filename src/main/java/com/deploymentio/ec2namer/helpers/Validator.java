package com.deploymentio.ec2namer.helpers;

import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;

public interface Validator {

	/**
	 * Validates that the request is valid
	 * 
	 * @param req
	 *            namer request
	 * @param context
	 *            the lambda function execution context
	 * @return <code>true</code> if request is valid, <code>false</code>
	 *         otherwise
	 */
	public boolean validate(NamingRequest req, LambdaContext context) ;
}
