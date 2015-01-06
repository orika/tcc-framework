package com.netease.backend.tcc.common;

import java.io.Serializable;

public class HeuristicsInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long uuid;
	private Action action;
	
	public HeuristicsInfo(long uuid, Action action) {
		this.uuid = uuid;
		this.action = action;
	}
	public long getUuid() {
		return uuid;
	}
	public void setUuid(long uuid) {
		this.uuid = uuid;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	
}
