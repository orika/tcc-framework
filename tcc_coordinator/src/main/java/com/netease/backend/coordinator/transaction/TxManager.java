package com.netease.backend.coordinator.transaction;

import java.util.List;

import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.tcc.Procedure;

public class TxManager {
	
	private IdGenerator idGenerator = null;
	private TxTable txTable = null;
	private LogManager logManager = null;

	public Transaction createTx(List<Procedure> procList) throws LogException {
		Transaction tx = new Transaction(idGenerator.getNextUUID(), procList);
		tx.setCreateTime(System.currentTimeMillis());
		logManager.logRegister(tx);
		txTable.put(tx);
		return tx;
	}
	
	public void begin(long uuid, Action action) throws LogException {
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
	
	public void finish(long uuid, Action action) throws LogException {
		Transaction tx = txTable.remove(uuid);
		if (tx == null)
			throw new RuntimeException("finish a null transaction!!");
		tx.setEndTime(System.currentTimeMillis());
		logManager.logFinish(tx, action);
	}
}
