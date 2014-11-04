package com.netease.backend.coordinator.config;

import com.netease.backend.coordinator.util.SystemUtil;


public class CoordinatorConfig {
	private String serverIp;
	private String rdsIp;
	private int bgThreadNum = 100;
	private int retryParallelism = 20;
	private int expireTime = 10800000;
	private int ckptInterval = 30000;
	private int monitorInterval = 60000;
	private String productName = "TCC";
	private long maxRunningTx = 2000;
	private long alarmRunningTx = 1500;
	private int maxTxCount = 10000000;
	private int alarmTxCount = 6000000;
	private long alarmInterval = 10;
	private int rpcTimeout = 10000;
	private String appName;
	private String zkAddress;
	
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

	public int getBgThreadNum() {
		return bgThreadNum;
	}

	public void setBgThreadNum(int bgThreadNum) {
		this.bgThreadNum = bgThreadNum;
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

	public long getMaxRunningTx() {
		return maxRunningTx;
	}

	public void setMaxRunningTx(long maxRunningTx) {
		this.maxRunningTx = maxRunningTx;
	}

	public long getAlarmRunningTx() {
		return alarmRunningTx;
	}

	public void setAlarmRunningTx(long alarmRunningTx) {
		this.alarmRunningTx = alarmRunningTx;
	}

	public int getMaxTxCount() {
		return maxTxCount;
	}

	public void setMaxTxCount(int maxTxCount) {
		this.maxTxCount = maxTxCount;
	}

	public int getAlarmTxCount() {
		return alarmTxCount;
	}

	public void setAlarmTxCount(int alarmTxCount) {
		this.alarmTxCount = alarmTxCount;
	}

	public long getAlarmInterval() {
		return alarmInterval;
	}

	public void setAlarmInterval(long alarmInterval) {
		this.alarmInterval = alarmInterval;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	public int getRpcTimeout() {
		return rpcTimeout;
	}

	public void setRpcTimeout(int rpcTimeout) {
		this.rpcTimeout = rpcTimeout;
	}
}
