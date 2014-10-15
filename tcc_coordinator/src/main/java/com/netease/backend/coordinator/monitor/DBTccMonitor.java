package com.netease.backend.coordinator.monitor;

import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.coordinator.util.MonitorUtil;
import com.netease.om.Monitor;

public class DBTccMonitor extends TccMonitor {
	
	private DbUtil dbUtil = null;
	private MonitorUtil monUtil = null;
	
	public DBTccMonitor() {
		super();
	}

	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	public void setMonitor(MonitorUtil monUtil) {
		this.monUtil = monUtil;
	}
	
	@Override
	public void write(MonitorRecord rec) throws MonitorException {
		monUtil.writeMonitorRec(rec);
		dbUtil.writeMonitorRec(rec);
	}
}
