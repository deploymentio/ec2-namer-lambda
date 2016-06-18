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

package com.deploymentio.ec2namer;

import java.util.List;

import com.deploymentio.ec2namer.helpers.ReservedName;

public class DenameRequest implements InstanceNamingRequest {

	private String instanceId;
	private String environment;
	private String baseDomain;
	private ReservedName reservedName;
	private List<String> requestedNames;
	private boolean alwaysUsePublicName;
	
	public boolean isAlwaysUsePublicName() {
		return alwaysUsePublicName;
	}
	public void setAlwaysUsePublicName(boolean alwaysUsePublicName) {
		this.alwaysUsePublicName = alwaysUsePublicName;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getBaseDomain() {
		return baseDomain;
	}
	public void setBaseDomain(String baseDomain) {
		this.baseDomain = baseDomain;
	}
	public ReservedName getReservedName() {
		return reservedName;
	}
	public void setReservedName(ReservedName reservedName) {
		this.reservedName = reservedName;
	}
	public List<String> getRequestedNames() {
		return requestedNames;
	}
	public void setRequestedNames(List<String> names) {
		this.requestedNames = names;
	}
	public String createFqdn(String name) {
		return name + "." + getEnvironment() + "." + getBaseDomain();
	}
}
