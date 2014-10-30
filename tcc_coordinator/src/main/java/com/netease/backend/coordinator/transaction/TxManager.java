package com.netease.backend.coordinator.transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.metric.GlobalMetric;
import com.netease.backend.coordinator.monitor.AlarmMsg;
import com.netease.backend.coordinator.processor.ExpireProcessor;
import com.netease.backend.coordinator.processor.RetryProcessor;
import com.netease.backend.coordinator.processor.TccProcessor;
import com.netease.backend.coordinator.util.MonitorUtil;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.common.Action;
import com.netease.backend.tcc.common.IllegalActionException;
import com.netease.backend.tcc.common.LogException;
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
	private MonitorUtil monitorUtil = null;
	private long maxRunningTx = 0;
	private long alarmRunningTx = 0;
	private int maxTxCount = 0;
	private int alarmTxCount = 0;
	private String rdsIp = null;
	
	public TxManager(CoordinatorConfig config, LogManager logManager, IdGenerator idGenerator, 
			MonitorUtil monitorUtil, ServiceContext context) {
		ExecutorService bgExecutor = Executors.newFixedThreadPool(config.getBgThreadNum());
		ExecutorService foreExecutor = Executors.newCachedThreadPool();
		this.tccProcessor = new TccProcessor(foreExecutor, bgExecutor, context);
		this.retryProcessor = new RetryProcessor(config, this, foreExecutor);
		this.expireProcessor = new ExpireProcessor(retryProcessor);
		this.txTable = new TxTable(config, expireProcessor, logManager, idGenerator);
		this.metric = new GlobalMetric(txTable);
		this.idGenerator = idGenerator;
		this.logManager = logManager;
		this.monitorUtil = monitorUtil;
		this.maxRunningTx = config.getMaxRunningTx();
		this.alarmRunningTx = config.getAlarmRunningTx();
		this.maxTxCount = config.getMaxTxCount();
		this.alarmTxCount = config.getAlarmTxCount();
		this.rdsIp = config.getRdsIp();
	}
	
	public void beginExpire() {
		txTable.beginExpiring();
	}
	
	public TxTable getTxTable() {
		return txTable;
	}
	
	public void recover() throws CoordinatorException {
		// print txTable
		txTable.print();
		retryProcessor.recover(txTable.getTxIterator());
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
	public Transaction createTx(List<Procedure> procList) throws LogException, CoordinatorException {
		if (checkOverFlow())
			throw new CoordinatorException("reject,running tx count is overflow at " + maxRunningTx);
		if (checkTxCount())
			throw new CoordinatorException("reject,txTable size blooms at " + maxTxCount);
		Transaction tx = new Transaction(idGenerator.getNextUUID(), procList);
		tx.setCreateTime(System.currentTimeMillis());
		metric.incRunningCount(Action.REGISTERED);
		logManager.logRegister(tx);
		txTable.put(tx);
		if (logger.isDebugEnabled()) 
			logger.debug("register: " + tx);
		metric.incCompleted(Action.REGISTERED, System.currentTimeMillis() - tx.getCreateTime());
//		return tx;
		throw new LogException("fuck", new SQLException("this is a SQLException"));
	}
	
	public void perform(long uuid, Action action, List<Procedure> procList) 
			throws LogException, HeuristicsException, IllegalActionException, CoordinatorException {
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
			throws LogException, HeuristicsException, IllegalActionException, CoordinatorException {
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
			throws LogException, IllegalActionException, CoordinatorException {
		Transaction tx = txTable.get(uuid);
		boolean isLocalTx = tx != null;
		if (!isLocalTx) {
			if (checkOverFlow())
				throw new CoordinatorException("reject,running tx count is overflow at " + maxRunningTx);
			if (checkTxCount())
				throw new CoordinatorException("reject,txTable size blooms at " + maxTxCount);
			logManager.checkRetryAction(uuid, action);
			tx = new Transaction(uuid, null);
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
		if (isLocalTx) {
			txTable.put(tx);
		}
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
			// if checkExpire return false , it means we can not execute expire any more
			// remove the tx out of txTable
			txTable.remove(tx.getUUID());
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
	
	private boolean checkOverFlow() {
		final long count = metric.getAllRunningCount();
		if (count > alarmRunningTx) {
			monitorUtil.alertAll(MonitorUtil.RUNNING_COUNT_OF, new AlarmMsg() {
				@Override
				public String getContent() {
					StringBuilder builder = new StringBuilder();
					builder.append("id:").append(idGenerator.getCoordinatorId()).append(" \n");
					builder.append("rds ip:").append(rdsIp).append(" \n");
					builder.append("current running tx count:").append(count);
					return builder.toString();
				}
			});
		}
		return count > maxRunningTx;
	}
	
	private boolean checkTxCount() {
		final int size = txTable.getSize();
		if (size > alarmTxCount) {
			monitorUtil.alertAll(MonitorUtil.TX_COUNT_OF, new AlarmMsg() {
				@Override
				public String getContent() {
					StringBuilder builder = new StringBuilder();
					builder.append("id:").append(idGenerator.getCoordinatorId()).append(" \n");
					builder.append("rds ip:").append(rdsIp).append(" \n");
					builder.append("current txTable size:").append(size);
					return builder.toString();
				}
			});
		}
		return size > maxTxCount;
	}
}
