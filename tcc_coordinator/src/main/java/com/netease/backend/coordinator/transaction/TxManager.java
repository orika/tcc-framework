package com.netease.backend.coordinator.transaction;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.processor.TccProcessor;
import com.netease.backend.coordinator.task.TxResultWatcher;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class TxManager {
	
	private final Logger logger = Logger.getLogger("TxManager");
	
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
			throws HeuristicsException, CoordinatorException {
		Transaction tx = begin(uuid, action, procList);
		try {
			tccProcessor.perform(uuid, procList);
			finish(uuid, action);
		} catch (HeuristicsException e) {
			heuristic(tx, action, e);
		}
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList, long timeout) 
			throws HeuristicsException, CoordinatorException {
		Transaction tx = begin(uuid, action, procList);
		try {
			tccProcessor.perform(uuid, procList, timeout);
			finish(uuid, action);
		} catch (HeuristicsException e) {
			heuristic(tx, action, e);
		}
	}
	
	private Transaction begin(long uuid, Action action, List<Procedure> procList) throws CoordinatorException {
		Transaction tx = txTable.get(uuid);
		if (tx == null) {
			tx = new Transaction(uuid, null);
			txTable.put(tx);
		}
		switch (action) {
			case CONFIRM:
				tx.confirm(procList);
				break;
			case CANCEL:
				tx.cancel(procList);
				break;
			default:
				throw new IllegalActionException(uuid, tx.getAction(), action);
		}
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, action);
		return tx;
	}
	
	private void finish(long uuid, Action action) throws LogException {
		Transaction tx = txTable.remove(uuid);
		if (tx == null)
			throw new RuntimeException("finish a null transaction!!");
		tx.setEndTime(System.currentTimeMillis());
		try {
			logManager.logFinish(tx, action);
		} catch (LogException e) {
			logger.warn("log finish failed:" + tx.getUUID());
		}
	}
	
	public void heuristic(Transaction tx, Action action, HeuristicsException e) throws LogException {
		tx.setEndTime(System.currentTimeMillis());
		logManager.logHeuristics(tx, action, e);
	}
	
	
	public void retryAsync(Transaction tx, TxResultWatcher watcher) throws HeuristicsException, LogException {
		Action action = tx.getAction();
		if (action == Action.EXPIRED && !logManager.checkExpire(tx))
			return;
		performAsync(tx, action, watcher);
	}
	
	public void expire(Transaction tx) throws HeuristicsException, CoordinatorException {
		if (!logManager.checkExpire(tx))
			return;
		tx.expire();
		perform(tx, Action.EXPIRED);
	}
	
	private void perform(Transaction tx, Action action) throws LogException, HeuristicsException {
		long uuid = tx.getUUID();
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, action);
		tccProcessor.perform(uuid, tx.getProcList(action));
		txTable.remove(uuid);
		tx.setEndTime(System.currentTimeMillis());
		try {
			logManager.logFinish(tx, action);
		} catch (LogException e) {
			logger.warn("log finish failed:" + tx.getUUID());
		}
	}
	
	private void performAsync(Transaction tx, Action action, TxResultWatcher watcher) throws LogException {
		long uuid = tx.getUUID();
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, action);
		tccProcessor.performAsync(uuid, tx.getProcList(action), watcher, true);
		txTable.remove(uuid);
		tx.setEndTime(System.currentTimeMillis());
		try {
			logManager.logFinish(tx, action);
		} catch (LogException e) {
			logger.warn("log finish failed:" + tx.getUUID());
		}
	}
	 
	public void recover() {
		
	}
}
