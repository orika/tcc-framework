package com.netease.backend.coordinator.log;

public enum LogType {
	TRX_BEGIN(0),
	TRX_START_CONFIRM(1),
	TRX_START_CANCEL(2),
	TRX_START_EXPIRE(3),
	TRX_END_CONFIRM(4),
	TRX_END_CANCEL(5),
	TRX_END_EXPIRE(6),
	TRX_HEURESTIC(7);
	
	private int code;
	
	private LogType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
