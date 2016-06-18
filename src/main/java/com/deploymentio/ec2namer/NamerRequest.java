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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the naming request
 */

public class NamerRequest implements InstanceNamingRequest {

	private String environment;
	private String baseDomain;
	private String group;
	private String instanceId;
	private String osConfiguration;
	private boolean alwaysUsePublicName;
	
	private List<RequestedName> requestedNames = Collections.emptyList();
	private Map<String, String> requestedTags = Collections.emptyMap();
	
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
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getOsConfiguration() {
		return osConfiguration;
	}
	public void setOsConfiguration(String osConfiguration) {
		this.osConfiguration = osConfiguration;
	}
	public List<RequestedName> getRequestedNames() {
		return requestedNames;
	}
	public void setRequestedNames(List<RequestedName> names) {
		this.requestedNames = names;
	}
	public Map<String, String> getRequestedTags() {
		return requestedTags;
	}
	public void setRequestedTags(Map<String, String> requestedTags) {
		this.requestedTags = requestedTags;
	}
	public boolean isAlwaysUsePublicName() {
		return alwaysUsePublicName;
	}
	public void setAlwaysUsePublicName(boolean usePublicName) {
		this.alwaysUsePublicName = usePublicName;
	}
	public String createFqdn(String name) {
		return name + "." + getEnvironment() + "." + getBaseDomain();
	}
}
