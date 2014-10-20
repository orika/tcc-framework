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
	private ServiceContext context = null;
	
	public TccContainer(ServiceContext context, TccMonitor monitor, RecoverManager recoverManager, 
			TxManager txManager, UUIDGenerator uuidGenerator) {
		this.context = context;
		this.monitor = monitor;
		this.recoverManager = recoverManager;
		this.txManager = txManager;
		this.uuidGenerator = uuidGenerator;
	}
	
	public void start() {
		context.init();
		txManager.enableRetry();
		recoverManager.init();
		uuidGenerator.init(recoverManager.getLastMaxUUID());
		monitor.start();
		txManager.beginExpire();
	}
	
	public static void main(String[] args) throws IOException {
		System.setProperty("dubbo.spring.config", "classpath*:/spring/*.xml");
		com.alibaba.dubbo.container.Main.main(args);
		logger.info("Tcc Service initializing...");
	}
}
