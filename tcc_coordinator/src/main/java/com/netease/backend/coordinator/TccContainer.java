package com.netease.backend.coordinator;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.monitor.TccMonitor;
import com.netease.backend.coordinator.recover.RecoverManager;

public class TccContainer {
	
	private static final Logger logger = Logger.getLogger("TccContainer");

	public TccContainer(TccMonitor monitor, RecoverManager recoverManager) {
		logger.info("Tcc Service initializing...");
		recoverManager.init();
		monitor.start();
		com.alibaba.dubbo.container.Main.main(new String[0]);
	}
}
