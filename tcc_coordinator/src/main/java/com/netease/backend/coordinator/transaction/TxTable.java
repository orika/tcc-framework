package com.netease.backend.coordinator.transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.netease.backend.coordinator.processor.ExpireProcessor;

public class TxTable extends TimerTask {
	
	private Map<Long, Transaction> table = new ConcurrentHashMap<Long, Transaction>();
	private ExpireProcessor expireProcessor = null;

	public TxTable() {
		Timer timer = new Timer();
		timer.schedule(this, 30000, 30000);
	}
	
	@Override
	public void run() {
		long now = System.currentTimeMillis() - 10800000;
		for (Iterator<Transaction> it = table.values().iterator(); it.hasNext(); ) {
			Transaction tx = it.next();
			if (tx.getCreateTime() < now) {
				expireProcessor.process(tx);
				it.remove();
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
}
