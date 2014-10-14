package com.netease.backend.coordinator.monitor.db;

import com.netease.backend.coordinator.monitor.MonitorException;
import com.netease.backend.coordinator.monitor.MonitorRecord;
import com.netease.backend.coordinator.monitor.TccMonitor;
import com.netease.backend.coordinator.util.DbUtil;

public class TccMonitorImp implements TccMonitor {
	private DbUtil dbUtil = null;
	
	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	@Override
	public void Write(MonitorRecord rec) throws MonitorException {
		// TODO Auto-generated method stub
		dbUtil.writeMonitorRec(rec);
	}

	

}
