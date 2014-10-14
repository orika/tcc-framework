package com.netease.backend.coordinator.metric;

import java.util.concurrent.atomic.AtomicInteger;

public class ActionMetric implements Metric{
	
	private AtomicInteger count = new AtomicInteger(0);
	private AtomicInteger time = new AtomicInteger(0);
	private AtomicInteger maxTime = new AtomicInteger(0);

	int getCount() {
		return count.get();
	}
	
	int getAvgTime() {
		if (count.get() == 0)
			return 0;
		return time.get() / count.get();
	}
	
	int getMaxTime() {
		return maxTime.get();
	}
	
	public void addCompleted(int time) {
		count.incrementAndGet();
		this.time.addAndGet(time);
		while (true) {
			int t = maxTime.get();
			if (time > t) {
				if (maxTime.compareAndSet(t, time))
					break;
			} else
				break;
		}
	}

	@Override
	public void reset() {
		count.set(0);
		time.set(0);
		maxTime.set(0);;
	}
}
