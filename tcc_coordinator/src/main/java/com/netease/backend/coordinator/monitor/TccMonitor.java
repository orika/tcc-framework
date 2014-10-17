package com.netease.backend.coordinator.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.metric.GlobalMetric;

public abstract class TccMonitor extends TimerTask {
	
	private static final Logger logger = Logger.getLogger("TccMonitor");
	
	private int interval = Integer.MAX_VALUE;
	private GlobalMetric metric = null;
	
	public TccMonitor(int interval, GlobalMetric metric) {
		this.interval = interval;
		this.metric = metric;
	}
	
	public void start() {
		Timer timer = new Timer();
		timer.schedule(this, interval);
		logger.info("TccMonitor started, metrics reset at interval:" + interval + "ms");
	}
	
	@Override
	public void run() {
		logger.info("monitor log:" + metric);
		metric.reset();
	}
	
	public abstract void write(MonitorRecord rec) throws MonitorException;
}
