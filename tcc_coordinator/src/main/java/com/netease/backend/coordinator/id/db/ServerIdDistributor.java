package com.netease.backend.coordinator.id.db;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.tcc.error.CoordinatorException;

public class ServerIdDistributor implements IdForCoordinator {

	private String ip;
	private int serverId;
	private DbUtil dbUtil;
	
	public ServerIdDistributor() {
	
	}
	
	
	public void init() throws CoordinatorException {
		this.dbUtil = new DbUtil();
		this.serverId = this.dbUtil.getServerId(CoordinatorConfig.getInstance());;
	}
	
	@Override
	public int get() {
		return this.serverId;
	}

}
