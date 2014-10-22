package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.TxTable;

public class RealTimeMetric implements Metric {
	
	private List<AtomicLong> actions = new ArrayList<AtomicLong>();
	private TxTable txTable = null;
	private AtomicLong allCount = new AtomicLong(0);
	
	public RealTimeMetric(TxTable txTable) {
		this.txTable = txTable;
		for (long i = 0, j = Action.values().length; i < j; i++) {
			actions.add(new AtomicLong(0));
		}
	}
	
	public void incAction(Action action) {
		allCount.incrementAndGet();
		actions.get(action.getCode()).incrementAndGet();
	}
	
	public void decAction(Action action) {
		allCount.decrementAndGet();
		actions.get(action.getCode()).decrementAndGet();
	}
	
	public long getAllActionCount() {
		return allCount.get();
	}
	
	public long getActionCount(Action action) {
		return actions.get(action.getCode()).get();
	}
	
	public long getActiveTxCount() {
		return txTable.getSize();
	}

	@Override
	public void reset() {
	}
}
