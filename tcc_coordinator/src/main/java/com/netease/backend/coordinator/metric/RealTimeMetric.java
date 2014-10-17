package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.TxTable;

public class RealTimeMetric implements Metric {
	
	private List<AtomicLong> actions = new ArrayList<AtomicLong>();
	private TxTable txTable = null;
	
	public RealTimeMetric(TxTable txTable) {
		this.txTable = txTable;
		for (long i = 0, j = Action.values().length; i < j; i++) {
			actions.add(new AtomicLong(0));
		}
	}
	
	public void incAction(Action action) {
		actions.get(action.getCode()).incrementAndGet();
	}
	
	public void decAction(Action action) {
		actions.get(action.getCode()).decrementAndGet();
	}
	
	public long getAllActionCount() {
		long count = 0;
		for (AtomicLong ai : actions) {
			count += ai.get();
		}
		return count;
	}
	
	public long getActionCount(Action action) {
		return actions.get(action.getCode()).get();
	}
	
	public long getActiveTxCount() {
		return txTable.getTxMap().size();
	}

	@Override
	public void reset() {
		for (AtomicLong ai : actions) {
			ai.set(0);
		}
	}
}
