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

import org.apache.commons.lang3.StringUtils;

import com.deploymentio.ec2namer.DenamingRequest;
import com.deploymentio.ec2namer.LambdaContext;

public class NameUnreserver {

	protected NamerDB db = new NamerDB();
	
	/**
	 * Unreserves the name for the given instance-id
	 * 
	 * @param instanceId
	 *            the instance ID for which the name needs to be unreserved
	 * @param context
	 *            the lambda function execution context
	 * @return the details of unreserving of the name
	 * @throws IOException
	 *             if the name cannot be unreserved
	 */
	public DenamingRequest unreserve(String instanceId, LambdaContext context) throws IOException {
		
		DenamingRequest request = db.findReservedNameForDenaming(instanceId);
		if (request != null) {
			context.log("Found name to unreserve: Name=" + request.createFqdn(request.getReservedName().getHostname()) + " RequestedNames=" + StringUtils.join(request.getRequestedNames()));
			db.unreserve(request);
		}
		return request;
	}
}
