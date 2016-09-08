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

		/*
		 * names are based on group and an index. If group is 'bar' and index is
		 * 12, then the name would be 'bar012'.
		 * 
		 * use 'db' to figure out what is the next available index, use the following rules
		 *       - first valid index to return is 1
		 *       - last valid index is 999
		 *       - gaps should be filled first - if 1, 3, and 4 are in use, return 2
		 *       - if all indexes are in use 1-999, don't reserve anything, and return null
		 *       - if any of the existing index in use has the same instance-id as this request, reuse that index (this should be highest priority)
		 *       
		 * also, use 'db' to reserve the index so others don't take it
		 */

		boolean gapFound = false;
		int lastIdxInUse = 0;
		
		for (IndexInUse indexInUse : db.getGroupIndxesInUse(req)) {

			// this instance is already assigned a name, so return that
			if (indexInUse.getInstanceId().equals(req.getInstanceId())) {
				return reserve(req, context, indexInUse.getIndex());
			}
			
			// if we have not found a gap, keep looking
			if (!gapFound) {
				if (indexInUse.getIndex() - lastIdxInUse > 1) {
					gapFound = true;
				} else {
					lastIdxInUse = indexInUse.getIndex();
				}
			}
		}
		
		// last valid index is 999
		if (lastIdxInUse == 999) {
			return null;
		}
		
		// reserve and then return the info
		return reserve(req, context, lastIdxInUse+1);
	}

	private ReservedName reserve(NamingRequest req, LambdaContext context, int idx) throws IOException {
		db.reserveGroupIndex(req, context, idx);
		return new ReservedName(req.getGroup(), idx);
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {
		
		if (StringUtils.isEmpty(req.getGroup())) {
			context.log("Group is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getEnvironment())) {
			context.log("Environment is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getInstanceId())) {
			context.log("Instance-id is missing");
			return false;
		}
		
		if (StringUtils.isEmpty(req.getBaseDomain())) {
			context.log("Base-domain is missing");
			return false;
		}
		
		return true;
	}
}
