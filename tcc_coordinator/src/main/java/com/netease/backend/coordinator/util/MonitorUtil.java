package com.netease.backend.coordinator.util;

import java.util.HashMap;
import java.util.Map;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.monitor.AlarmMsg;
import com.netease.backend.coordinator.monitor.MonitorRecord;
//import com.netease.om.Monitor;

public class MonitorUtil {
	
	public static final String RUNNING_COUNT_OF = "Active tx count gonna overflow";
	public static final String TX_COUNT_OF = "Tx Table size gonna bloom";
	public static final String HEURISTIC = "heuristic encountered";
	
	private Map<String, Long> alarmMap = new HashMap<String, Long>();
	private long alarmInterval = 0;
	
	public MonitorUtil(CoordinatorConfig config) {
//		Monitor monitor = Monitor.getInstance(config.getProductName());
//		monitor.init();
//		alarmMap.put(RUNNING_COUNT_OF, 0L);
//		alarmMap.put(TX_COUNT_OF, 0L);
//		alarmMap.put(HEURISTIC, 0L);
//		this.alarmInterval = config.getAlarmInterval() * 1000;
	}
	
	public void writeMonitorRec(MonitorRecord rec) {
//		Monitor.setAttributeCount("CUR_TRX_NUM", rec.getCurTrxNum());
//		Monitor.setAttributeCount("CUR_PROCESS_TRX_NUM", rec.getCurProcessTrxNum());
//		Monitor.setAttributeCount("REGISTED_TRX_NUM", rec.getRegistTrxNum());
//		Monitor.setAttributeCount("CONFIRM_TRX_NUM", rec.getConfirmTrxNum());
//		Monitor.setAttributeCount("CANCEL_TRX_NUM", rec.getCancelTrxNum());
//		Monitor.setAttributeCount("EXPIRED_TRX_NUM", rec.getExpireTrxNum());
//		Monitor.setAttributeCount("AVG_REGIST_TIME", rec.getAvgRegistTime());
//		Monitor.setAttributeCount("MAX_REGIST_TIME", rec.getMaxRegistTime());
//		Monitor.setAttributeCount("AVG_CONFIRM_TIME", rec.getAvgConfirmTime());
//		Monitor.setAttributeCount("MAX_CONFIRM_TIME", rec.getMaxConfirmTime());
//		Monitor.setAttributeCount("AVG_CANCEL_TIME", rec.getAvgCancelTime());
//		Monitor.setAttributeCount("MAX_CANCEL_TIME", rec.getMaxCancelTime());
	}
	
	public void alertAll(String title, AlarmMsg msg) {
//		Long lastTs = alarmMap.get(title);
//		long now = System.currentTimeMillis();
//		if (lastTs == null)
//			Monitor.alert(title, msg.getContent());
//		if (lastTs + alarmInterval < now) {
//			synchronized (alarmMap) {
//				Long ts = alarmMap.get(title);
//				if (ts == null || ts + alarmInterval < now) {
//					alarmMap.put(title, now);
//					Monitor.alert(title, msg.getContent());
//				}
//			}
//		}
	}
}
