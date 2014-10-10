package com.netease.backend.coordinator.transaction;

import java.util.List;

import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.processor.TccProcessor;
import com.netease.backend.tcc.CoordinatorException;
import com.netease.backend.tcc.Procedure;

public class TxManager {
	
	private IdGenerator idGenerator = null;
	private TxTable txTable = null;
	private LogManager logManager = null;
	private TccProcessor tccProcessor = null;

	public Transaction createTx(List<Procedure> procList) throws LogException {
		Transaction tx = new Transaction(idGenerator.getNextUUID(), procList);
		tx.setCreateTime(System.currentTimeMillis());
		logManager.logRegister(tx);
		txTable.put(tx);
		return tx;
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList) 
			throws CoordinatorException {
		begin(uuid, action);
		try {
			tccProcessor.perform(uuid, procList);
		} finally {
			finish(uuid, action);
		}
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList, long timeout) 
			throws CoordinatorException {
		begin(uuid, action);
		try {
			tccProcessor.perform(uuid, procList, timeout);
		} finally {
			finish(uuid, action);
		}
	}
	
	private void begin(long uuid, Action action) throws LogException {
		Transaction tx = txTable.get(uuid);
		boolean registerLocally = tx != null;
		if (!registerLocally) {
			tx = new Transaction(uuid, null);
			txTable.put(tx);
		}
		tx.setStatus(action);
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, action, registerLocally);
	}
	
	private void finish(long uuid, Action action) throws LogException {
		Transaction tx = txTable.remove(uuid);
		if (tx == null)
			throw new RuntimeException("finish a null transaction!!");
		tx.setEndTime(System.currentTimeMillis());
		logManager.logFinish(tx, action);
	}
	
	public void expire(Transaction tx) throws CoordinatorException {
		long uuid = tx.getUUID();
		if (!logManager.checkExpired(uuid))
			return;
		tx.setStatus(Action.EXPIRED);
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, Action.EXPIRED, true);
		tccProcessor.perform(uuid, tx.getExpireList());
		txTable.remove(tx.getUUID());
		tx.setEndTime(System.currentTimeMillis());
		logManager.logFinish(tx, Action.EXPIRED);
	}
}
