package com.netease.backend.coordinator.monitor;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.coordinator.util.DbUtil;

public class DBTccMonitor extends TccMonitor {
	
	private DbUtil dbUtil = null;
	
	public DBTccMonitor(CoordinatorConfig config, TxManager manager) {
		super(config.getMonitorInterval(), manager.getGlobalMetric());
	}

	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	@Override
	public void write(MonitorRecord rec) throws MonitorException {
		dbUtil.writeMonitorRec(rec);
	}
}
