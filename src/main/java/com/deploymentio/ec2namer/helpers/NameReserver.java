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

import com.deploymentio.ec2namer.DenamingRequest;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;

public class NameReserver implements Validator {

	protected NamerDB db = new NamerDB();
	
	/**
	 * Reserves the main name based on {@link NamingRequest#getGroup()}. The name
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
	public ReservedName reserve(NamingRequest req, LambdaContext context) throws IOException {

		// names are based on group and an index. If group is 'bar' and index is
		// 12, then the name would be 'bar012'.
		
		/*
		 * TODO: use 'db' to figure out what is the next available index, use the following rules
		 *       - first valid index to return is 1
		 *       - last valid index is 999
		 *       - gaps should be filled first - if 1, 3, and 4 are in use, return 2
		 *       - if all indexes are in use 1-999, don't reserve anything, and return null
		 *       - if any of the existing index in use has the same instance-id as this request, reuse that index (this should be highest priority)
		 *       
		 * TODO: also, use 'db' to reserve the index so others don't take it
		 */
		
		// Temporarily, for testing, return the name based on the group name -
		// no actual reservation is being made here. Obviously, this will return
		// the same index/name everytime it is called.
		return new ReservedName(req.getGroup(), 1);
	}
	

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
		
		// need to first find the reserved name from DB (if it exists) given
		// instance-id
		//
		// if found, need to un-reserve it - again through the db
		
		return null;
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {
		
		// TODO: validate that we have the required fields in the request:
		// group, environment, instance-id, and base-domain. This method will be
		// called before reserve() is called.
		
		return true;
	}
	
}
