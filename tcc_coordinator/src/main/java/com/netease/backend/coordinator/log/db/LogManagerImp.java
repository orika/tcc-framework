/**
 * @Description:			LogManager implemention
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */

package com.netease.backend.coordinator.log.db;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.log.Checkpoint;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.log.LogScanner;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.tcc.error.HeuristicsException;

public class LogManagerImp implements LogManager {

	private DbUtil dbUtil = null;
	private static final Logger logger = Logger.getLogger(LogManagerImp.class);
	
	public LogManagerImp() {
	}
	
	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	@Override
	public void logBegin(Transaction tx, Action action) throws LogException {	
		// Get log Type
		LogType logType = null;
		switch(action) {
		case CONFIRM:
			logType = LogType.TRX_START_CONFIRM;
			break;
		case CANCEL:
			logType = LogType.TRX_START_CANCEL;
			break;
		case EXPIRE:
			logType = LogType.TRX_START_EXPIRE;
			break;
		default:
			throw new LogException("Action Type Error in logBegin");
		}
		
		this.dbUtil.writeLog(tx, logType);
	}

	@Override
	public void logFinish(Transaction tx, Action action) throws LogException {
		// Get log Type
		LogType logType = null;
		switch(action) {
		case CONFIRM:
			logType = LogType.TRX_END_CONFIRM;
			break;
		case CANCEL:
			logType = LogType.TRX_END_CANCEL;
			break;
		case EXPIRE:
			logType = LogType.TRX_END_EXPIRE;
			break;
		default:
			throw new LogException("Action Type Error in logFinish");
		}
		this.dbUtil.writeLog(tx, logType);
	}

	@Override
	public void logRegister(Transaction tx) throws LogException {
		LogType logType = LogType.TRX_BEGIN;
		
		this.dbUtil.writeLog(tx, logType);
	}

	@Override
	public boolean checkExpire(long uuid) throws LogException {
		boolean res = this.dbUtil.checkExpire(uuid);
		return res;
	}

	@Override
	public void logHeuristics(Transaction tx, Action action,
			HeuristicsException e) throws LogException {
		try {
			this.dbUtil.writeHeuristicRec(tx, action, e, false);
		} catch (LogException e1) {
			logger.error("Write system heuristic record error", e1);
			this.dbUtil.writeHeuristicRec(tx, action, e, true);
		}
		LogType logType = LogType.TRX_HEURESTIC;
		
		try {
			this.dbUtil.writeLog(tx, logType);
		} catch (LogException e2) {
			logger.error("Write heuristic log error", e2);
		}
	}

	@Override
	public void setCheckpoint(Checkpoint checkpoint) throws LogException {
		this.dbUtil.setCheckpoint(checkpoint);
	}

	@Override
	public Checkpoint getCheckpoint() throws LogException {
		return this.dbUtil.getCheckpoint();
	}

	@Override
	public boolean checkRetryAction(long uuid) throws LogException {
		boolean res = this.dbUtil.checkRetryAction(uuid);
		return res;
	}

	@Override
	public boolean checkLocalLogMgrAlive() {
		boolean res = this.dbUtil.checkLocaLogMgrAlive();
		return res;
	}

	@Override
	public LogScanner beginScan(long startpoint) throws LogException {
		return dbUtil.beginScan(startpoint);
	}
}


