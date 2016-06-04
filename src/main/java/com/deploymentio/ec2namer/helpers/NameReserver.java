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

public class NameReserver implements Validator {

	/**
	 * Reserves the main name based on {@link NamerRequest#getGroup()}. The name
	 * can be reserved in any permanent storage, but in this case we will record
	 * it in SDB.
	 * 
	 * @param req
	 *            the namer request
	 * @param context
	 *            the lambda function execution context
	 * @return the reserved name
	 * @throws IOException
	 *             if a name cannot be reserved
	 */
	public ReservedName reserve(NamerRequest req, LambdaContext context) throws IOException {

		// TODO: figure out what is the next available name
		// TODO: also, reserve the name so others don't take it
		
		// Temporarily, for testing, return the name based on the group name -
		// no actual reservation is being made here
		ReservedName name = new ReservedName();
		name.setHostname(req.getGroup() + "01");
		name.setIndex(1);

		return name;
	}
	
	
	@Override
	public boolean validate(NamerRequest req, LambdaContext context) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
