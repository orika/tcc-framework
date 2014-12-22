package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;

import com.netease.backend.coordinator.transaction.TxTable;
import com.netease.backend.tcc.common.Action;

public class GlobalMetric implements Metric {
	
	private List<ActionMetric> actions = new ArrayList<ActionMetric>();
	private HeuristicsMetric heuristicsMetric = new HeuristicsMetric();
	private RealTimeMetric realTimeMetric = null;
	private LogMetric logMetric = new LogMetric();
	
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
	
	public void incCompleted(Action action, long time, long logTime) {
		ActionMetric am = actions.get(action.getCode());
		am.addCompleted(time);
		realTimeMetric.decAction(action);
		logMetric.addLogTime(logTime);
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
	
	public void incRunningCount(Action action, long logTime) {
		realTimeMetric.incAction(action);
		logMetric.addLogTime(logTime);
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
		logMetric.reset();
	}
	
	@Override
	public String toString() {
		return "log avg time " + logMetric.getAvgTime();
	}
}
