package com.netease.backend.coordinator.config;

import com.netease.backend.coordinator.util.SystemUtil;


public class CoordinatorConfig {
	private String serverIp;
	private String rdsIp;
	private int bgMaxThreadNum = 100;
	private int retryParallelism = 20;
	private int expireTime = 10800000;
	private int ckptInterval = 30000;
	private int monitorInterval = 60000;
	private String productName = "TCC";
	
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

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public int getCkptInterval() {
		return ckptInterval;
	}

	public void setCkptInterval(int ckptInterval) {
		this.ckptInterval = ckptInterval;
	}

	public int getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
}
