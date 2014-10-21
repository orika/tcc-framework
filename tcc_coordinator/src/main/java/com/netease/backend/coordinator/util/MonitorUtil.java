package com.netease.backend.coordinator.util;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.monitor.MonitorRecord;
import com.netease.om.Monitor;

public class MonitorUtil {
	
	public MonitorUtil(CoordinatorConfig config) {
		Monitor monitor = Monitor.getInstance(config.getProductName());
		monitor.init();
	}
	
	public void writeMonitorRec(MonitorRecord rec) {
		Monitor.setAttributeCount("CUR_TRX_NUM", rec.getCurTrxNum());
		Monitor.setAttributeCount("CUR_PROCESS_TRX_NUM", rec.getCurProcessTrxNum());
		Monitor.setAttributeCount("REGISTED_TRX_NUM", rec.getRegistTrxNum());
		Monitor.setAttributeCount("CONFIRM_TRX_NUM", rec.getConfirmTrxNum());
		Monitor.setAttributeCount("CANCEL_TRX_NUM", rec.getCancelTrxNum());
		Monitor.setAttributeCount("EXPIRED_TRX_NUM", rec.getExpireTrxNum());
		Monitor.setAttributeCount("AVG_REGIST_TIME", rec.getAvgRegistTime());
		Monitor.setAttributeCount("MAX_REGIST_TIME", rec.getMaxRegistTime());
		Monitor.setAttributeCount("AVG_CONFIRM_TIME", rec.getAvgConfirmTime());
		Monitor.setAttributeCount("MAX_CONFIRM_TIME", rec.getMaxConfirmTime());
		Monitor.setAttributeCount("AVG_CANCEL_TIME", rec.getAvgCancelTime());
		Monitor.setAttributeCount("MAX_CANCEL_TIME", rec.getMaxCancelTime());
	}
	
	public void alertAll(String title, String content) {
		Monitor.alert(title, content);
	}
}
