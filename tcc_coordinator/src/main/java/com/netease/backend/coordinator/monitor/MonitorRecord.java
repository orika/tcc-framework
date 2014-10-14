package com.netease.backend.coordinator.monitor;

public class MonitorRecord {
	private int serverId;
	private long timestamp;
	private long curTrxNum;
	private long curProcessTrxNum;
	private long registTrxNum;
	private long confirmTrxNum;
	private long cancelTrxNum;
	private long expireTrxNum;
	private long avgRegistTime;
	private long maxRegistTime;
	private long avgConfirmTime;
	private long maxConfirmTime;
	private long avgCancelTime;
	private long maxCancelTime;
	
	public MonitorRecord(int serverId, long timestamp, long curTrxNum,
			long curProcessTrxNum, long registTrxNum, long confirmTrxNum,
			long cancelTrxNum, long expireTrxNum, long avgRegistTime,
			long maxRegistTime, long avgConfirmTime, long maxConfirmTime,
			long avgCancelTime, long maxCancelTime) {
		super();
		this.serverId = serverId;
		this.timestamp = timestamp;
		this.curTrxNum = curTrxNum;
		this.curProcessTrxNum = curProcessTrxNum;
		this.registTrxNum = registTrxNum;
		this.confirmTrxNum = confirmTrxNum;
		this.cancelTrxNum = cancelTrxNum;
		this.expireTrxNum = expireTrxNum;
		this.avgRegistTime = avgRegistTime;
		this.maxRegistTime = maxRegistTime;
		this.avgConfirmTime = avgConfirmTime;
		this.maxConfirmTime = maxConfirmTime;
		this.avgCancelTime = avgCancelTime;
		this.maxCancelTime = maxCancelTime;
	}

	public int getServerId() {
		return serverId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getCurTrxNum() {
		return curTrxNum;
	}

	public long getCurProcessTrxNum() {
		return curProcessTrxNum;
	}

	public long getRegistTrxNum() {
		return registTrxNum;
	}

	public long getConfirmTrxNum() {
		return confirmTrxNum;
	}

	public long getCancelTrxNum() {
		return cancelTrxNum;
	}

	public long getExpireTrxNum() {
		return expireTrxNum;
	}

	public long getAvgConfirmTime() {
		return avgConfirmTime;
	}

	public long getMaxConfirmTime() {
		return maxConfirmTime;
	}

	public long getAvgCancelTime() {
		return avgCancelTime;
	}

	public long getMaxCancelTime() {
		return maxCancelTime;
	}

	public long getAvgRegistTime() {
		return avgRegistTime;
	}

	public long getMaxRegistTime() {
		return maxRegistTime;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setCurTrxNum(long curTrxNum) {
		this.curTrxNum = curTrxNum;
	}

	public void setCurProcessTrxNum(long curProcessTrxNum) {
		this.curProcessTrxNum = curProcessTrxNum;
	}

	public void setRegistTrxNum(long registTrxNum) {
		this.registTrxNum = registTrxNum;
	}

	public void setConfirmTrxNum(long confirmTrxNum) {
		this.confirmTrxNum = confirmTrxNum;
	}

	public void setCancelTrxNum(long cancelTrxNum) {
		this.cancelTrxNum = cancelTrxNum;
	}

	public void setExpireTrxNum(long expireTrxNum) {
		this.expireTrxNum = expireTrxNum;
	}

	public void setAvgConfirmTime(long avgConfirmTime) {
		this.avgConfirmTime = avgConfirmTime;
	}

	public void setMaxConfirmTime(long maxConfirmTime) {
		this.maxConfirmTime = maxConfirmTime;
	}

	public void setAvgCancelTime(long avgCancelTime) {
		this.avgCancelTime = avgCancelTime;
	}

	public void setMaxCancelTime(long maxCancelTime) {
		this.maxCancelTime = maxCancelTime;
	}

	public void setAvgRegistTime(long avgRegistTime) {
		this.avgRegistTime = avgRegistTime;
	}

	public void setMaxRegistTime(long maxRegistTime) {
		this.maxRegistTime = maxRegistTime;
	}
}
