package com.netease.backend.coordinator.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public abstract class TccMonitor extends TimerTask {
	
	private static final Logger logger = Logger.getLogger("TccMonitor");
	
	public void start() {
		Timer timer = new Timer();
		timer.schedule(this, 60000);
		logger.info("TccMonitor started, metrics reset at interval:60000ms");
	}
	
	@Override
	public void run() {
	}
	
	public abstract void write(MonitorRecord rec) throws MonitorException;
}
