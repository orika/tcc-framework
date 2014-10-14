package com.netease.backend.coordinator.monitor;

public interface TccMonitor {
	void Write(MonitorRecord rec) throws MonitorException;
}
