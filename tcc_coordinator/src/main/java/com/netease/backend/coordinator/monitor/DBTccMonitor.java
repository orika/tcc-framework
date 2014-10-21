/**
 * @Description:			Monitor of Tcc coordinator
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */
package com.netease.backend.coordinator.monitor;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.coordinator.util.MonitorUtil;

public class DBTccMonitor extends TccMonitor {
	
	private DbUtil dbUtil = null;
	private MonitorUtil monUtil = null;
	
	public DBTccMonitor(CoordinatorConfig config, TxManager manager, IdForCoordinator idForCoordinator) {
		super(config.getMonitorInterval(), manager.getGlobalMetric(), idForCoordinator);
	}

	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	
	public void setMonitorUtil(MonitorUtil monUtil) {
		this.monUtil = monUtil;
	}

	@Override
	public void write(MonitorRecord rec) throws MonitorException {
		// record in monitor platform
		monUtil.writeMonitorRec(rec);
		
		// record in system database
		dbUtil.writeMonitorRec(rec);
	}
}
