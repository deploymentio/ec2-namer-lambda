package com.deploymentio.ec2namer;

public class RequestedName {

	public enum HealthCheckProtocol {
		HTTP,
		TCP
	}
	
	private String name;
	private int weight = 100; 

	private boolean healthChecked;
	private HealthCheckProtocol healthCheckProtocol;
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
	public HealthCheckProtocol getHealthCheckProtocol() {
		return healthCheckProtocol;
	}
	public void setHealthCheckProtocol(HealthCheckProtocol healthCheckProtocol) {
		this.healthCheckProtocol = healthCheckProtocol;
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
