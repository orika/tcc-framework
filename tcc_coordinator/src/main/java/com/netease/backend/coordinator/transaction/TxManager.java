package com.netease.backend.coordinator.transaction;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.metric.GlobalMetric;
import com.netease.backend.coordinator.processor.ExpireProcessor;
import com.netease.backend.coordinator.processor.RetryProcessor;
import com.netease.backend.coordinator.processor.TccProcessor;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class TxManager {
	
	private final Logger logger = Logger.getLogger("TxManager");
	
	private IdGenerator idGenerator = null;
	private TxTable txTable = null;
	private LogManager logManager = null;
	private TccProcessor tccProcessor = null;
	private RetryProcessor retryProcessor = null;
	private ExpireProcessor expireProcessor = null;
	private GlobalMetric metric = null;
	
	public TxManager(CoordinatorConfig config, LogManager logManager, IdGenerator idGenerator) {
		this.tccProcessor = new TccProcessor(config);
		this.retryProcessor = new RetryProcessor(config, tccProcessor);
		this.expireProcessor = new ExpireProcessor(retryProcessor);
		this.txTable = new TxTable(config, expireProcessor, logManager, idGenerator);
		this.metric = new GlobalMetric(txTable);
		this.idGenerator = idGenerator;
		this.logManager = logManager;
		retryProcessor.setTxManager(this);
	}
	
	public void beginExpire() {
		txTable.beginExpiring();
	}
	
	public TxTable getTxTable() {
		return txTable;
	}
	
	public void recover() throws CoordinatorException {
		retryProcessor.recover(txTable.getTxMap().values().iterator());
	}
	
	public void enableRetry() {
		retryProcessor.start();
	}
	
	public GlobalMetric getGlobalMetric() {
		return metric;
	}

	/*
	 * register is not added to metric right now
	 */
	public Transaction createTx(List<Procedure> procList) throws LogException {
		Transaction tx = new Transaction(idGenerator.getNextUUID(), procList);
		tx.setCreateTime(System.currentTimeMillis());
		metric.incRunningCount(Action.REGISTERED);
		logManager.logRegister(tx);
		txTable.put(tx);
		if (logger.isDebugEnabled()) 
			logger.debug("register: " + tx);
		metric.incCompleted(Action.REGISTERED, System.currentTimeMillis() - tx.getCreateTime());
		return tx;
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList) 
			throws LogException, HeuristicsException, IllegalActionException {
		Transaction tx = begin(uuid, action, procList);
		try {
			if (logger.isDebugEnabled())
				logger.debug("perform:" + tx);
			tccProcessor.perform(uuid, procList, false);
			finish(uuid, action);
		} catch (HeuristicsException e) {
			heuristic(tx, action, e);
			throw e;
		}
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList, long timeout) 
			throws LogException, HeuristicsException, IllegalActionException {
		Transaction tx = begin(uuid, action, procList);
		try {
			if (logger.isDebugEnabled())
				logger.debug("perform timeout " + timeout + ":" + tx);
			tccProcessor.perform(uuid, procList, timeout, false);
			finish(uuid, action);
		} catch (HeuristicsException e) {
			heuristic(tx, action, e);
			throw e;
		}
	}
	
	private Transaction begin(long uuid, Action action, List<Procedure> procList) 
			throws LogException, IllegalActionException {
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
		if (logger.isDebugEnabled())
			logger.debug("begin " + tx);
		tx.setBeginTime(System.currentTimeMillis());
		logManager.logBegin(tx, action);
		metric.incRunningCount(action);
		return tx;
	}
	
	private void finish(long uuid, Action action) throws LogException {
		Transaction tx = txTable.remove(uuid);
		if (tx == null)
			throw new RuntimeException("finish a null transaction!!");
		tx.setEndTime(System.currentTimeMillis());
		metric.incCompleted(action, tx.getElapsed());
		try {
			logManager.logFinish(tx, action);
			if (logger.isDebugEnabled())
				logger.debug("finish " + tx);
		} catch (LogException e) {
			logger.warn("log finish failed:" + tx.getUUID());
		}
	}
	
	public void heuristic(Transaction tx, Action action, HeuristicsException e) throws LogException {
		tx.setEndTime(System.currentTimeMillis());
		logManager.logHeuristics(tx, action, e);
		txTable.remove(tx.getUUID());
		logger.info("tx " + tx.getUUID() + " heuristics code:" + e.getCode());
		metric.incHeuristics();
	}
	
	
	public void retry(Transaction tx) throws LogException {
		Action action = tx.getAction();
		if ((action == Action.EXPIRE || action == Action.REGISTERED) && !logManager.checkExpire(tx.getUUID())) {
			if (logger.isDebugEnabled())
				logger.debug("Transaction " + tx.getUUID() + " check expire false");
			return;
		}
		perform(tx, action);
	}
	
	private void perform(Transaction tx, Action action) throws LogException {
		long uuid = tx.getUUID();
		tx.setBeginTime(System.currentTimeMillis());
		metric.incRunningCount(action);
		try {
			tccProcessor.perform(uuid, tx.getProcList(action), true);
		} catch (HeuristicsException e) {
			heuristic(tx, action, e);
			return;
		}
		if (logger.isDebugEnabled())
			logger.debug("perform background :" + tx);
		txTable.remove(uuid);
		tx.setEndTime(System.currentTimeMillis());
		metric.incCompleted(action, tx.getElapsed());
		try {
			logManager.logFinish(tx, action);
		} catch (LogException e) {
			logger.warn("log finish failed:" + tx.getUUID());
		}
	}
}
