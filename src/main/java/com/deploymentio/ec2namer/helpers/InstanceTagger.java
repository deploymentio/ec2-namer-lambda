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

import com.amazonaws.services.lambda.runtime.Context;
import com.deploymentio.ec2namer.NamerRequest;

public class InstanceTagger {

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
	public void tag(NamerRequest req, Context context, ReservedName name) throws IOException {
		
		// TODO: tag the ec2 instance
		
	}
}
