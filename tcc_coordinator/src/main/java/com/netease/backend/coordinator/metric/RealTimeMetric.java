package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.TxTable;

public class RealTimeMetric implements Metric {
	
	private List<AtomicInteger> actions = new ArrayList<AtomicInteger>();
	private TxTable txTable = null;
	
	public RealTimeMetric() {
		for (int i = 0, j = Action.values().length; i < j; i++) {
			actions.add(new AtomicInteger(0));
		}
	}
	
	public void incAction(Action action) {
		actions.get(action.getCode()).incrementAndGet();
	}
	
	public void decAction(Action action) {
		actions.get(action.getCode()).decrementAndGet();
	}
	
	public int getAllActionCount() {
		int count = 0;
		for (AtomicInteger ai : actions) {
			count += ai.get();
		}
		return count;
	}
	
	public int getActionCount(Action action) {
		return actions.get(action.getCode()).get();
	}
	
	public int getActiveTxCount() {
		return txTable.getTxMap().size();
	}

	@Override
	public void reset() {
		for (AtomicInteger ai : actions) {
			ai.set(0);
		}
	}
}
