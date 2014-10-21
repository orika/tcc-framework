package com.netease.backend.coordinator.recover;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.log.Checkpoint;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.log.LogRecord;
import com.netease.backend.coordinator.log.LogScanner;
import com.netease.backend.coordinator.log.LogType;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.coordinator.transaction.TxTable;
import com.netease.backend.coordinator.util.LogUtil;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.CoordinatorException;

public class DBRecoverManager implements RecoverManager {
	
	private static final Logger logger = Logger.getLogger(DBRecoverManager.class);
	private TxTable txTable = null;
	private TxManager txManager = null;
	private LogManager logMgr = null;
	private IdForCoordinator idForCoordinator = null;
	private long lastMaxUUID = 0;




	public void setIdForCoordinator(IdForCoordinator idForCoordinator) {
		this.idForCoordinator = idForCoordinator;
	}

	public void setTxManager(TxManager txManager) {
		this.txManager = txManager;
		this.txTable = txManager.getTxTable();
	}

	public void setLogMgr(LogManager logMgr) {
		this.logMgr = logMgr;
	}
	

	@Override
	public void init() {
		logger.info("begin recovering transaction table");
		LogScanner logScanner = null;
		try {
			// recover from log
			Checkpoint checkpoint = logMgr.getCheckpoint();
			long cpTime = checkpoint.getTimestamp();
			lastMaxUUID = checkpoint.getMaxUuid();
			logScanner = logMgr.beginScan(cpTime);
			while (logScanner.hasNext()){
				LogRecord logRec = logScanner.next();
				long uuid = logRec.getTrxId();
				LogType logType = logRec.getLogType();
				long timestamp = logRec.getTimestamp();
				byte[] procs = logRec.getProcs();
				Transaction trx = this.txTable.get(uuid);
				if (trx == null) {
					// if trx not exist in txTable, than put a new one
					List<Procedure> expireProcs = null;
					if (logType == LogType.TRX_BEGIN)
						expireProcs = LogUtil.deserialize(procs);
					trx = new Transaction(uuid, expireProcs);
					this.txTable.put(trx);
				}
				
				// set tx attributes
				switch(logType) {
				case TRX_BEGIN:
					trx.setCreateTime(timestamp);
					break;
				case TRX_START_CONFIRM:
					trx.confirm(LogUtil.deserialize(procs));
					trx.setBeginTime(timestamp);
					break;
				case TRX_START_CANCEL:
					trx.cancel(LogUtil.deserialize(procs));
					trx.setBeginTime(timestamp);
					break;
				case TRX_START_EXPIRE:
					trx.expire();
					break;
				case TRX_END_CONFIRM:
				case TRX_END_CANCEL:
				case TRX_END_EXPIRE:
				case TRX_HEURESTIC:
					// if trx is finish or heuristic, remove it from txtable
					this.txTable.remove(uuid);
					break;
				}
				// update max Uuid
				if (this.idForCoordinator.isUuidOwn(trx.getUUID()) &&
						uuid > lastMaxUUID){
					lastMaxUUID = uuid;
				}
			}
			logScanner.endScan();
			logger.info("Transaction table has been recovered");
			logger.info("Last Max UUID " + lastMaxUUID);
			txManager.recover();
		} catch (CoordinatorException e) {
			logger.error("DBRecoverManager init failed.", e);
			System.exit(1);
		}
	}

	@Override
	public long getLastMaxUUID() {
		return lastMaxUUID;
	}
}
