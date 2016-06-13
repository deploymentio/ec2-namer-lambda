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

import com.amazonaws.services.route53.model.HealthCheckType;

/**
 * A DNS name registration request in addition to the group based name given to
 * an instance. These names will be weighted CNAME entries in Route53 with
 * optional health-checks.
 */

public class RequestedName {

	public enum HealthCheckProtocol {
		HTTP,
		TCP
	}
	
	private String name;
	private int weight = 100; 

	private boolean healthChecked;
	private HealthCheckType healthCheckType;
	private int healthCheckPort;
	private String healthCheckUri;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isHealthChecked() {
		return healthChecked;
	}
	public void setHealthChecked(boolean healthChecked) {
		this.healthChecked = healthChecked;
	}
	public HealthCheckType getHealthCheckType() {
		return healthCheckType;
	}
	public void setHealthCheckType(HealthCheckType healthCheckType) {
		this.healthCheckType = healthCheckType;
	}
	public int getHealthCheckPort() {
		return healthCheckPort;
	}
	public void setHealthCheckPort(int healthCheckPort) {
		this.healthCheckPort = healthCheckPort;
	}
	public String getHealthCheckUri() {
		return healthCheckUri;
	}
	public void setHealthCheckUri(String healthCheckUri) {
		this.healthCheckUri = healthCheckUri;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
}
