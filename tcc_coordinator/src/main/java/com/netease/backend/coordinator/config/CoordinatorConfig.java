package com.netease.backend.coordinator.config;

import com.netease.backend.coordinator.util.SystemUtil;


public class CoordinatorConfig {
	private String serverIp;
	private String rdsIp;
	private int bgMaxThreadNum = 100;
	private int retryParallelism = 20;
	
	public String getServerIp() {
		if (serverIp == null)
			return SystemUtil.getIpAddress();
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

	public int getBgMaxThreadNum() {
		return bgMaxThreadNum;
	}

	public void setBgMaxThreadNum(int bgMaxThreadNum) {
		this.bgMaxThreadNum = bgMaxThreadNum;
	}

	public int getRetryParallelism() {
		return retryParallelism;
	}

	public void setRetryParallelism(int retryParallelism) {
		this.retryParallelism = retryParallelism;
	}
}
