package com.netease.backend.coordinator.transaction;

import java.util.List;

import com.netease.backend.tcc.Procedure;

public class Transaction {

	private long uuid = -1;
	private long createTime = -1;
	private long beginTime = -1;
	private long endTime = -1;
	private Action status = Action.REGISTERED;
	protected List<Procedure> expireList = null;
	
	public Transaction(long uuid, List<Procedure> expireList) {
		this.uuid = uuid;
		this.expireList = expireList;
		this.createTime = System.currentTimeMillis();
	}
	
	public long getUUID() {
		return uuid;
	}
	
	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<Procedure> getExpireList() {
		return expireList;
	}

	public void setExpireList(List<Procedure> expireList) {
		this.expireList = expireList;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public Action getStatus() {
		return status;
	}
	
	public void setStatus(Action status) {
		this.status = status;
	}
}
