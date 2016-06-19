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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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
			if (args.length > 1) {
				ByteArrayInputStream inp = new ByteArrayInputStream(args[1].getBytes());
				func.handleRequest(inp, System.out, context);
			} else {
				func.handleRequest(System.in, System.out, context);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
