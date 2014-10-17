package com.netease.backend.coordinator.metric;

import java.util.concurrent.atomic.AtomicLong;

public class ActionMetric implements Metric{
	
	private AtomicLong count = new AtomicLong(0);
	private AtomicLong time = new AtomicLong(0);
	private AtomicLong maxTime = new AtomicLong(0);

	long getCount() {
		return count.get();
	}
	
	long getAvgTime() {
		if (count.get() == 0)
			return 0;
		return time.get() / count.get();
	}
	
	long getMaxTime() {
		return maxTime.get();
	}
	
	public void addCompleted(long time) {
		count.incrementAndGet();
		this.time.addAndGet(time);
		while (true) {
			long t = maxTime.get();
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
