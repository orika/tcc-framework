package com.netease.backend.coordinator.metric;

import java.util.concurrent.atomic.AtomicInteger;

public class HeuristicsMetric implements Metric {
	
	private AtomicInteger count;
	
	public int getCount() {
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
