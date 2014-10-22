package com.netease.backend.coordinator.transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdGenerator;
import com.netease.backend.coordinator.log.Checkpoint;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.processor.ExpireProcessor;

public class TxTable extends TimerTask {
	
	private static final Logger logger = Logger.getLogger("TxTable");
	private Map<Long, Transaction> table = new ConcurrentHashMap<Long, Transaction>();
	private ExpireProcessor expireProcessor = null;
	private LogManager logManager = null;
	private IdGenerator idGenerator = null;
	private long expireTime;
	private long checkInterval;

	public TxTable(CoordinatorConfig config, ExpireProcessor expireProcessor, LogManager logManager,
			IdGenerator idGenerator) {
		this.expireTime = config.getExpireTime();
		this.checkInterval = config.getCkptInterval();
		this.expireProcessor = expireProcessor;
		this.logManager = logManager;
		this.idGenerator = idGenerator;
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
				count++;
			}
		}
		logger.info("expire transaction count:" + count);
		logger.info("total transaction count:" + table.size());
		if (cpTime != Long.MAX_VALUE) {
			try {
				Checkpoint cp = new Checkpoint(cpTime, idGenerator.getNextUUID());
				logManager.setCheckpoint(cp);
				logger.info("set checkpoint:" + cp);
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
	
	public Iterator<Transaction> getTxIterator() {
		return table.values().iterator();
	}
	
	public int getSize() {
		return table.size();
	}
	
	public void beginExpiring() {
		Timer timer = new Timer();
		timer.schedule(this, 0, checkInterval);
	}
}
