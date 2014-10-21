package com.netease.backend.coordinator.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.metric.GlobalMetric;

public abstract class TccMonitor extends TimerTask {
	
	private static final Logger logger = Logger.getLogger("TccMonitor");
	
	private int interval = Integer.MAX_VALUE;
	private GlobalMetric metric = null;
	private IdForCoordinator idForCoordinator = null;
	
	
	public TccMonitor(int interval, GlobalMetric metric, IdForCoordinator idForCoordinator) {
		this.interval = interval;
		this.metric = metric;
		this.idForCoordinator = idForCoordinator;
	}
	
	public void start() {
		Timer timer = new Timer();
		timer.schedule(this, 2000, interval);
		logger.info("TccMonitor started, metrics reset at interval:" + interval + "ms");
	}
	
	@Override
	public void run() {
		logger.info("monitor log:" + metric);
		MonitorRecord rec = new MonitorRecord(idForCoordinator.get(), metric);
		try {
			write(rec);
		} catch (MonitorException e) {
			logger.error("write log exception.", e);
		}
		metric.reset();
	}
	
	public abstract void write(MonitorRecord rec) throws MonitorException;
}
