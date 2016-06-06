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

import java.io.IOException;

import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamerRequest;

public class OsScriptGenerator implements Validator {

	/**
	 * Generates a script that the requester can run to set their hostname to
	 * the reserved name.
	 * 
	 * @param req
	 *            the namer request
	 * @param context
	 *            the lambda function execution context
	 * @param name
	 *            the reserved name for this instance
	 * @throws IOException
	 *             if the script cannot be generated
	 */
	public String generate(NamerRequest req, LambdaContext context, ReservedName name) throws IOException {
		
		// TODO: generate the OS script to set the hostname
		return null;
	}
	
	@Override
	public boolean validate(NamerRequest req, LambdaContext context) {
		// TODO: validate that we have the os-configuration provided in the
		// request and that we can generate a script for it
		return true;
	}
}
