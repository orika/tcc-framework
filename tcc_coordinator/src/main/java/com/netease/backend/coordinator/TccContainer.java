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
	private DefaultCoordinator coordinator = null;
	
	public TccContainer(TccMonitor monitor, RecoverManager recoverManager, 
			TxManager txManager, UUIDGenerator uuidGenerator, 
			DefaultCoordinator coordinator) {
		this.monitor = monitor;
		this.recoverManager = recoverManager;
		this.txManager = txManager;
		this.uuidGenerator = uuidGenerator;
		this.coordinator = coordinator;
	}
	
	public void start() {
		txManager.enableRetry();
		recoverManager.init();
		uuidGenerator.init(recoverManager.getLastMaxUUID());
		coordinator.start();
		monitor.start();
		txManager.beginExpire();
	}
	
	public void stop() {
		coordinator.stop();
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 0)
			System.setProperty("dubbo.spring.config", args[0]);
		else
			System.setProperty("dubbo.spring.config", "classpath*:/spring/*.xml");
		com.alibaba.dubbo.container.Main.main(new String[] {"spring", "log4j"});
		logger.info("Tcc Service initializing...");
	}
	
	public TxManager getTxManager() {
		return txManager;
	}
}
