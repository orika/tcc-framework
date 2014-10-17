package com.netease.backend.coordinator.metric;

import java.util.concurrent.atomic.AtomicLong;

public class HeuristicsMetric implements Metric {
	
	private AtomicLong count = new AtomicLong(0);
	
	public long getCount() {
		return count.get();
	}
	
	public void incrCount() {
		count.incrementAndGet();
	}

	@Override
	public void reset() {
		count.set(0);
	}
}
