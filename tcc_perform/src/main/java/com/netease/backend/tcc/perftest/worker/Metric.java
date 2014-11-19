package com.netease.backend.tcc.perftest.worker;

public class Metric {
	
	private long count = 0;
	private long[] resTime = null;;
	
	public Metric(int threadNum) {
		this.resTime = new long[threadNum];
	}
	
	public long getCount() {
		return count;
	}
	
	public int getResTime() {
		int result = 0;
		for (long time : resTime) {
			result += time;
		}
		return result / resTime.length;
	}
	
	public void addResTime(int index, long time) {
		resTime[index] = time;
	}
	
	public void addCount(long count) {
		this.count += count;
	}
}
