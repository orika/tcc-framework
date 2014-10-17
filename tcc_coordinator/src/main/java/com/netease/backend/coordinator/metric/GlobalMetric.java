package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.TxTable;

public class GlobalMetric implements Metric {
	
	private List<ActionMetric> actions = new ArrayList<ActionMetric>();
	private HeuristicsMetric heuristicsMetric = new HeuristicsMetric();
	private RealTimeMetric realTimeMetric = null;
	
	public GlobalMetric(TxTable txTable) {
		this.realTimeMetric = new RealTimeMetric(txTable);
		for (long i = 0, j = Action.values().length; i < j; i++) {
			actions.add(new ActionMetric());
		}
	}
	
	public void incCompleted(Action action, long time) {
		ActionMetric am = actions.get(action.getCode());
		am.addCompleted(time);
		realTimeMetric.decAction(action);
	}
	
	public long getCompletedCount(Action action) {
		return actions.get(action.getCode()).getCount();
	}
	
	public long getAllCompletedCount() {
		long count = 0;
		for (ActionMetric am : actions)
			count += am.getCount();
		return count;
	}
	
	public long getAvgTime(Action action) {
		return actions.get(action.getCode()).getAvgTime();
	}
	
	public long getMaxTime(Action action) {
		return actions.get(action.getCode()).getMaxTime();
	}
	
	public void incRunningCount(Action action) {
		realTimeMetric.incAction(action);
	}
	
	public long getRunningCount(Action action) {
		return realTimeMetric.getActionCount(action);
	}
	
	public long getActiveTxCount() {
		return realTimeMetric.getActiveTxCount();
	}
	
	public long getAllRunningCount() {
		return realTimeMetric.getAllActionCount();
	}
	
	public void incHeuristics() {
		heuristicsMetric.incrCount();
	}
	
	public long getHeuristicsCount() {
		return heuristicsMetric.getCount();
	}

	@Override
	public void reset() {
		for (ActionMetric am : actions) {
			am.reset();
		}
		heuristicsMetric.reset();
		realTimeMetric.reset();
		heuristicsMetric.reset();
	}
}
