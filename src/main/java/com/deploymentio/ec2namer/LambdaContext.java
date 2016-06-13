package com.deploymentio.ec2namer;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;

@SuppressWarnings("serial")
public class LambdaContext extends HashMap<String, Object> {

	private Context runtimeContext;

	public LambdaContext(Context runtimeContext) {
		super();
		this.runtimeContext = runtimeContext;
		
	}

	public void log(String msg) {
		runtimeContext.getLogger().log(msg);
	}
	
	public Context getRuntimeContext() {
		return runtimeContext;
	}
}
