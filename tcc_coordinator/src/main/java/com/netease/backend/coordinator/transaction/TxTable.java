package com.netease.backend.coordinator.transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.processor.ExpireProcessor;

public class TxTable extends TimerTask {
	
	private static final Logger logger = Logger.getLogger("TxTable");
	private Map<Long, Transaction> table = new ConcurrentHashMap<Long, Transaction>();
	private ExpireProcessor expireProcessor = null;
	private LogManager logManager = null;
	private long expireTime;
	private long checkInterval;

	public TxTable(CoordinatorConfig config, ExpireProcessor expireProcessor, LogManager logManager) {
		this.expireTime = config.getExpireTime();
		this.checkInterval = config.getCkptInterval();
		this.expireProcessor = expireProcessor;
		this.logManager = logManager;
	}
	
	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	
	public void setLogManager(LogManager logManager) {
		this.logManager = logManager;
	}

	@Override
	public void run() {
		long expireTs = System.currentTimeMillis() - expireTime;
		long cpTime = Long.MAX_VALUE;
		int count = 0;
		for (Iterator<Transaction> it = table.values().iterator(); it.hasNext(); ) {
			Transaction tx = it.next();
			long lastTs = tx.getLastTimeStamp();
			if (lastTs < cpTime)
				cpTime = lastTs;
			if (tx.isExpired(expireTs)) {
				expireProcessor.process(tx);
				it.remove();
				count++;
			}
		}
		logger.info("expire transaction count:" + count);
		logger.info("total transaction count:" + table.size());
		if (cpTime != Long.MAX_VALUE) {
			try {
				// Todo:set timestamp and maxuuid
				logManager.setCheckpoint(null);
				logger.info("set checkpoint:" + cpTime + " / " );
			} catch (LogException e) {
				logger.error("set checkpoint error.", e);
			}
		}
	}
			
	public Transaction get(long uuid) {
		return table.get(uuid);
	}
	
	public void put(Transaction tx) {
		table.put(tx.getUUID(), tx);
	}
	
	public Transaction remove(long uuid) {
		return table.remove(uuid);
	}
	
	public Map<Long, Transaction> getTxMap() {
		return table;
	}
	
	public void beginExpiring() {
		Timer timer = new Timer();
		timer.schedule(this, 0, checkInterval);
	}
}
