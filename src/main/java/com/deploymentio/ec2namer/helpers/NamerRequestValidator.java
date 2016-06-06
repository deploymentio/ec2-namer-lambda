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

import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamerRequest;

public class NamerRequestValidator implements Validator {

	private Validator[] internalValidators;
	
	public NamerRequestValidator(Validator...validators) {
		this.internalValidators = validators;
	}
	
	@Override
	public boolean validate(NamerRequest req, LambdaContext context) {
		
		// TODO: we want to check with each internal-validators and ask them to validate
		// the request. If they all validate, then we return true. At the first internal
		// validation failure, we should bug out - returning false
		
		// for now returning true
		return true;
	}
}
