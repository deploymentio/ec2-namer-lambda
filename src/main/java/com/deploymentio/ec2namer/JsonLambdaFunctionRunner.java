package com.deploymentio.ec2namer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class JsonLambdaFunctionRunner {

	public static void main(String[] args) {
		try {

			Context context = mock(Context.class);
			LambdaLogger logger = new LambdaLogger() {
				@Override
				public void log(String msg) {
					System.err.println(new Date() + " - " + msg);
				}
			};
			when(context.getLogger()).thenReturn(logger);
			
			JsonLambdaFunction func = (JsonLambdaFunction) Class.forName(args[0]).newInstance();
			func.handleRequest(System.in, System.out, context);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
