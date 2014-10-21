package com.netease.backend.coordinator.log;

public class Checkpoint {
	private long timestamp;
	private long maxUuid;
	
	
	public Checkpoint(long timestamp, long maxUuid) {
		super();
		this.timestamp = timestamp;
		this.maxUuid = maxUuid;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getMaxUuid() {
		return maxUuid;
	}
	
	public void setMaxUuid(long maxUuid) {
		this.maxUuid = maxUuid;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Timestamp:").append(timestamp);
		builder.append("/MaxUUID:").append(maxUuid);
		return builder.toString();
	}
}
