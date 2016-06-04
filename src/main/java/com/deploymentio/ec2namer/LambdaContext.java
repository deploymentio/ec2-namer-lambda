package com.deploymentio.ec2namer;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

@SuppressWarnings("serial")
public class LambdaContext extends HashMap<String, Object> {

	private Context runtimeContext;

	public LambdaContext(Context runtimeContext) {
		super();
		this.runtimeContext = runtimeContext;
		
	}

	public LambdaLogger getLogger() {
		return runtimeContext.getLogger();
	}
	
	public Context getRuntimeContext() {
		return runtimeContext;
	}
}
