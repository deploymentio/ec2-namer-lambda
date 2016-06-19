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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamingRequest;

public class InstanceTagger implements Validator {

	protected AmazonEC2 ec2 = new AmazonEC2Client();
	
	/**
	 * Tags the EC2 instance we are naming. The tag's name/values are provided
	 * in the request. Additionally, if no <code>Name</code> tag is provided,
	 * this method will add one in the format of
	 * <code>{environment}:{reserved-name}</code>.
	 * 
	 * @param req
	 *            the namer request
	 * @param context
	 *            the lambda function execution context
	 * @param name
	 *            the reserved name for this instance
	 * @throws IOException
	 *             if the instance cannot be tagged
	 */
	public void tag(NamingRequest req, LambdaContext context, ReservedName name) throws IOException {

		HashMap<String, String> map = new HashMap<>(req.getRequestedTags());
		if (!map.containsKey("Name")) {
			map.put("Name", req.getEnvironment() + ":" + name.getHostname());
		}

		ArrayList<Tag> tags = new ArrayList<>();
		for(String key : map.keySet()) {
			String val = map.get(key);
			tags.add(new Tag().withKey(key).withValue(val));
		}
		
		ec2.createTags(new CreateTagsRequest()
			.withResources(req.getInstanceId())
			.withTags(tags));
	}
	
	@Override
	public boolean validate(NamingRequest req, LambdaContext context) {
		
		if (StringUtils.isEmpty(req.getInstanceId())) {
			context.log("InstanceId is missing");
			return false;
		}

		if (StringUtils.isEmpty(req.getEnvironment())) {
			context.log("Environment is missing");
			return false;
		}
		
		return true;
	}
}
