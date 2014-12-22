package com.netease.backend.coordinator.metric;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LogMetric implements Metric {

	private AtomicLong time = new AtomicLong();
	private AtomicInteger count = new AtomicInteger();
	
	@Override
	public void reset() {
		time.set(0);
		count.set(0);
	}
	
	public void addLogTime(long t) {
		time.addAndGet(t);
		count.incrementAndGet();
	}
	
	public long getAvgTime() {
		if (count.get() == 0)
			return 0;
		return time.get() / count.get();
	}
}
