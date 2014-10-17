/**
 * @Description:			Log record 
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */


package com.netease.backend.coordinator.log;

public class LogRecord {
	private long trxId;
	private LogType logType;
	private long timestamp;
	private byte[] procs;
	
	public LogRecord(long trxId, LogType logType, long timestamp, byte[] procs) {
		this.trxId = trxId;
		this.logType = logType;
		this.timestamp = timestamp;
		this.procs = procs;
	}
	
	public long getTrxId() {
		return trxId;
	}
	public LogType getLogType() {
		return logType;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public byte[] getProcs() {
		return procs;
	}
	public void setTrxId(long trxId) {
		this.trxId = trxId;
	}
	public void setLogType(LogType logType) {
		this.logType = logType;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public void setProcs(byte[] procs) {
		this.procs = procs;
	}
	
}
