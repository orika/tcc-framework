package com.netease.backend.tcc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.ParticipantException;
import com.netease.backend.tcc.error.ServiceDownException;
import com.netease.backend.tcc.error.TimeoutException;

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
	
	protected void checkResult(short code, List<Procedure> procList, long timeout) 
			throws CoordinatorException {
		if (code == TccCode.OK) {
			return;
		} else if (code == TccUtils.UNDEFINED) {
			throw new CoordinatorException("undefined error code heuristics exception");
		} else if (TccCode.isTimeout(code)) {
			Procedure proc = procList.get(code ^ TccUtils.TIMEOUT_MASK);
			throw new TimeoutException(timeout, proc, uuid);
		} else if (TccCode.isServiceNotFound(code)) {
			Procedure proc = procList.get(code ^ TccUtils.UNVAILABLE_MASK);
			throw new ServiceDownException(proc, uuid);
		} else
			throw new ParticipantException("Participant error with uuid " + uuid, code); 
	}
	
	protected void checkResult(short code, List<Procedure> procList) 
			throws CoordinatorException {
		if (code == TccCode.OK) {
			return;
		} else if (code == TccUtils.UNDEFINED) {
			throw new CoordinatorException("undefined error code heuristics exception");
		} else if (TccCode.isTimeout(code)) {
			Procedure proc = procList.get(code ^ TccUtils.TIMEOUT_MASK);
			throw new TimeoutException(proc, uuid);
		} else if (TccCode.isServiceNotFound(code)) {
			Procedure proc = procList.get(code ^ TccUtils.UNVAILABLE_MASK);
			throw new ServiceDownException(proc, uuid);
		} else
			throw new ParticipantException("Participant error with uuid " + uuid, code);
	}
	
	public static void main(String[] args) {
		short a = (short) 16384;
		System.out.println(TccCode.isTimeout(a));
		System.out.println(a ^ TccUtils.TIMEOUT_MASK);
	}
}
