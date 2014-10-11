package com.netease.backend.coordinator.config;

public class CoordinatorConfig {
	private static CoordinatorConfig config;
	private String serverIp;
	private String rdsIp;
	
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getRdsIp() {
		return rdsIp;
	}
	public void setRdsIp(String rdsIp) {
		this.rdsIp = rdsIp;
	}
	public static CoordinatorConfig getInstance() {
		return config;
	}
}
