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
import com.deploymentio.ec2namer.NamingRequest;

public class NameReserverSimple extends NameReserver {

	@Override
	public ReservedName reserve(NamingRequest req, LambdaContext context) throws IOException {
		
		int idx = 0;
		for (IndexInUse indexInUse : db.getGroupIndxesInUse(req)) {
			idx = indexInUse.getIndex();
		}

		return db.reserveGroupIndex(req, idx+1);
	}
	

	@Override
	public DenamingRequest unreserve(String instanceId, LambdaContext context) throws IOException {
		DenamingRequest request = db.findReservedNameForDenaming(instanceId);
		db.unreserve(request);
		return request;
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {

		if (StringUtils.isEmpty(req.getGroup()) || StringUtils.isEmpty(req.getEnvironment()) || StringUtils.isEmpty(req.getBaseDomain()) || StringUtils.isEmpty(req.getInstanceId())) {
			return false;
		}
	
		return true;
	}
	
}
