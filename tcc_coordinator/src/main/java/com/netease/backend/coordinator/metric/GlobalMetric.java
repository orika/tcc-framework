package com.netease.backend.coordinator.metric;

import java.util.ArrayList;
import java.util.List;

import com.netease.backend.coordinator.transaction.Action;

public class GlobalMetric implements Metric {
	
	private List<ActionMetric> actions = new ArrayList<ActionMetric>();
	
	public GlobalMetric() {
		for (int i = 0, j = Action.values().length; i < j; i++) {
			actions.add(new ActionMetric());
		}
	}
	
	public void incCompleted(Action action, int time) {
		ActionMetric am = actions.get(action.getCode());
		am.addCompleted(time);
	}
	
	public int getCompletedCount(Action action) {
		return actions.get(action.getCode()).getCount();
	}

	@Override
	public void reset() {
		for (ActionMetric am : actions) {
			am.reset();
		}
	}
}
