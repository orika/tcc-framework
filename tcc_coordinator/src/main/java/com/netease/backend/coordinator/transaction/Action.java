package com.netease.backend.coordinator.transaction;

public enum Action {
	
	REGISTERED(0),
	CONFIRM(1),
	CANCEL(2),
	EXPIRED(3);
	
	private int code;
	
	private Action(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}