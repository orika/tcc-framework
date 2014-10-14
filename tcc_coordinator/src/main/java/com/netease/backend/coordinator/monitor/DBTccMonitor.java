package com.netease.backend.coordinator.monitor;

import com.netease.backend.coordinator.util.DbUtil;

public class DBTccMonitor extends TccMonitor {
	
	private DbUtil dbUtil = null;
	
	public DBTccMonitor() {
		super();
	}

	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	@Override
	public void write(MonitorRecord rec) throws MonitorException {
		dbUtil.writeMonitorRec(rec);
	}
}
