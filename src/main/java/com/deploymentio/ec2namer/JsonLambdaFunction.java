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

package com.deploymentio.ec2namer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An AWS lambda function handler that takes care of processing JSON requests.
 * The response of this lambda function is also converted to JSON. Extenders of
 * this object need to
 * <ul>
 * <li>validate that the request has all the information they need to process
 * the request</li>
 * <li>process the request</li>
 */

public abstract class JsonLambdaFunction<In, Out> implements RequestStreamHandler {

	protected ObjectMapper mapper = new ObjectMapper();

	/**
	 * Processes the lambda function request.
	 * 
	 * @param req the request
	 * @param context the lambda execution context
	 * @return the response that should be sent back
	 */
	public abstract Out process(In req, LambdaContext context) throws IOException;
	
	/**
	 * Validates if the request has all the needed parameters to
	 * process this function. This handler will not even attempt to process the
	 * request if it is not valid.
	 * 
	 * @param req the request
	 * @param context the lambda execution context
	 * @return <code>true</code> if request is valid, <code>false</code>
	 *         otherwise
	 */
	public abstract boolean validate(In req, LambdaContext context);


	/**
	 * The main entry point for all AWS Lambda functions that want to process
	 * JSON based requests.
	 * 
	 * @see com.amazonaws.services.lambda.runtime.RequestStreamHandler#handleRequest(java.io.InputStream,
	 *      java.io.OutputStream, com.amazonaws.services.lambda.runtime.Context)
	 */
	public void handleRequest(InputStream input, OutputStream output, Context runtimeContext) throws IOException {

		In req = null;
		Out resp = null;
		LambdaContext context = new LambdaContext(runtimeContext);

		try {
			// parse the request 
			ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
			Class<In> inputParamClass = (Class<In>) type.getActualTypeArguments()[0];
			req = mapper.readValue(input, inputParamClass);
			logIt(context, "Received: Request=" + mapper.writeValueAsString(req));
		} catch (JsonProcessingException e) {
			logIt(context, "Error parsing request: Error=" + e.getMessage());
			throw new IOException("Error parsing request: Error=" + e.getMessage());
		}

		if (req != null) {
			// validate the request
			if (validate(req, context)) {
				logIt(context, "Request is valid, attempting to process request");
				resp = process(req, context);
			} else {
				logIt(context, "Request is not valid");
				throw new IOException("Request is not valid");
			}
		}

		if (resp != null) {
			mapper.writeValue(output, resp);
			output.flush();
		}

		output.close();
	}

	protected void logIt(LambdaContext context, String msg) {
		context.getLogger().log(msg);
	}
}
