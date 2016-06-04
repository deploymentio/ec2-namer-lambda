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

package com.deploymentio.ec2namer.helpers;

import com.amazonaws.services.lambda.runtime.Context;
import com.deploymentio.ec2namer.NamerRequest;

public class NamerRequestValidator {

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
	public boolean validate(NamerRequest req, Context context) {
		
		// TODO: implement this later
		
		/*
		 *  1. We want to check for required fields - like group, environment, instance-id, and base-domain
		 *  2. Also, need to check everything needs for additional names is provided. For example, if
		 *  name needs to be health-checked, is the protocol and port provided. If it is an HTTP
		 *  health-check, is the uri provided.
		 *  3. If there is a problem we want to log it using the context.getLogger() object and return
		 *  false. To keep it simple, we can bug out on the first sign of a problem.
		 */
		
		return true;
	}
}
