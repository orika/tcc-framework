package com.netease.backend.coordinator;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.id.UUIDGenerator;
import com.netease.backend.coordinator.monitor.TccMonitor;
import com.netease.backend.coordinator.recover.RecoverManager;
import com.netease.backend.coordinator.transaction.TxManager;

public class TccContainer {
	
	private static final Logger logger = Logger.getLogger("TccContainer");
	
	private TccMonitor monitor;
	private RecoverManager recoverManager = null;
	private TxManager txManager = null;
	private UUIDGenerator uuidGenerator = null;
	
	public TccContainer(TccMonitor monitor, RecoverManager recoverManager, 
			TxManager txManager, UUIDGenerator uuidGenerator) {
		this.monitor = monitor;
		this.recoverManager = recoverManager;
		this.txManager = txManager;
		this.uuidGenerator = uuidGenerator;
	}
	
	public void start() {
		txManager.enableRetry();
		recoverManager.init();
		uuidGenerator.init(recoverManager.getLastMaxUUID());
		monitor.start();
		txManager.beginExpire();
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 0)
			System.setProperty("dubbo.spring.config", args[0]);
		else
			System.setProperty("dubbo.spring.config", "classpath*:/spring/*.xml");
		com.alibaba.dubbo.container.Main.main(new String[0]);
		logger.info("Tcc Service initializing...");
	}
	
	public TxManager getTxManager() {
		return txManager;
	}
}
