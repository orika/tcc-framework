package com.netease.backend.coordinator.transaction;


public class IllegalActionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 130940037250816868L;
	
	private long uuid;
	private Action first;
	private Action second;
	
	public IllegalActionException(long uuid, Action first, Action second) {
		super(new StringBuilder().append("tx ").append(uuid).append(" illegal action switch:")
				.append(first).append(" to ").append(second).toString());
		this.uuid = uuid;
		this.first = first;
		this.second = second;
	}
	
	public Action getFirstAction() {
		return first;
	}

	public Action getSecondAction() {
		return second;
	}
	
	public long getUUID() {
		return uuid;
	}
}
