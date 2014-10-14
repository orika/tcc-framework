package com.netease.backend.coordinator.log.db;

import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.tcc.error.HeuristicsException;

public class LogManagerImp implements LogManager {

	private DbUtil dbUtil = null;
	
	public LogManagerImp() {
		this.dbUtil = new DbUtil();
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
		case EXPIRED:
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
		case EXPIRED:
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
	public boolean checkExpired(long uuid) throws LogException {
		// TODO Auto-generated method stub
		boolean res = this.dbUtil.checkExpire(uuid);
		return res;
	}

	@Override
	public void logHeuristics(Transaction tx, Action action,
			HeuristicsException e) throws LogException {
		// TODO Auto-generated method stub
		
	}

}
