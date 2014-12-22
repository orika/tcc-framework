package com.netease.backend.coordinator.transaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.common.Action;
import com.netease.backend.tcc.common.IllegalActionException;

public class Transaction {

	private long uuid = -1;
	private long createTime = -1;
	private long beginTime = -1;
	private long endTime = -1;
	private AtomicReference<Action> status = new AtomicReference<Action>();
	protected List<Procedure> expireList = null;
	protected List<Procedure> procList = null;
	
	public Transaction(long uuid, List<Procedure> expireList) {
		this.uuid = uuid;
		this.expireList = expireList;
		this.createTime = System.currentTimeMillis();
		status.set(Action.REGISTERED);
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
	
	public long getElapsed() {
		return endTime - beginTime;
	}

	public List<Procedure> getExpireList() {
		return expireList;
	}

	public List<Procedure> getConfirmList() {
		return procList;
	}
	
	public List<Procedure> getCancelList() {
		return procList;
	}
	
	public List<Procedure> getProcList(Action action) {
		if (action == Action.EXPIRE)
			return expireList;
		else
			return procList;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public Action getAction() {
		return status.get();
	}
	
	public void confirm(List<Procedure> procList) throws IllegalActionException {
		if (status.compareAndSet(Action.REGISTERED, Action.CONFIRM))
			this.procList = procList;
		else {
			throw new IllegalActionException(uuid, status.get(), Action.CONFIRM);
		}
	}
	
	public void cancel(List<Procedure> procList) throws IllegalActionException {
		if (status.compareAndSet(Action.REGISTERED, Action.CANCEL))
			this.procList = procList;
		else 
			throw new IllegalActionException(uuid, status.get(), Action.CANCEL);
	}
	
	public void expire() throws IllegalActionException {
		if (status.get() == Action.EXPIRE)
			return;
		if (!status.compareAndSet(Action.REGISTERED, Action.EXPIRE))
			throw new IllegalActionException(uuid, status.get(), Action.EXPIRE);
	}
	
	public void rollback(){
		Action action = status.get();
		if (action == Action.CONFIRM || action == Action.CANCEL){
			status.set(Action.REGISTERED);
		}
	}
	
	public long getLastTimeStamp() {
		return endTime == -1 ? createTime : endTime;
	}
	
	public boolean isExpired(long timestamp) {
		if (status.get() == Action.EXPIRE)
			return true;
		if (status.get() == Action.REGISTERED)
			return createTime < timestamp;
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Transaction ").append(uuid);
		if (status.get() != Action.REGISTERED && status.get() != Action.EXPIRE) {
			builder.append(" action:").append(status.get().name());
			builder.append(" service").append(procList);
		}
		else 
			builder.append(" service").append(expireList);
		return builder.toString();
	}
}
