package com.netease.backend.tcc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.netease.backend.tcc.error.CoordinatorException;

public abstract class Transaction {

	protected long uuid = -1;
	protected int id;
	protected Coordinator coordinator;
	protected List<Procedure> expireList = null;
	
	protected static AtomicInteger idGenerator = new AtomicInteger(0);
	
	protected Transaction(Coordinator coordinator) {
		this.coordinator = coordinator;
		this.id = idGenerator.incrementAndGet();
	}
	
	public long getUUID() {
		return uuid;
	}
	
	public int getId() {
		return id;
	}
	
	protected void checkBegin() {
		if (uuid == -1)
			throw new UnsupportedOperationException("this tcc transaction has not begun yet");
	}
	
	protected void begin() throws CoordinatorException {
		if (uuid != -1)
			throw new CoordinatorException("duplicate begin for this tcc transaction");
		uuid = coordinator.begin(id, expireList);
	}
	
	public abstract void setSequence(int seq);
	
	public abstract <T> T getProxy(Class<T> serviceType);
	
	public abstract void confirm() throws CoordinatorException;
	
	public abstract void confirm(long timeout) throws CoordinatorException;
	
	public abstract void cancel() throws CoordinatorException;
	
	public abstract void cancel(long timeout) throws CoordinatorException; 
}
