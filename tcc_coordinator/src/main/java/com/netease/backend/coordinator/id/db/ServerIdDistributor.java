package com.netease.backend.coordinator.id.db;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.id.IdForCoordinatorException;
import com.netease.backend.coordinator.util.DbUtil;

public class ServerIdDistributor implements IdForCoordinator {

	private String ip;
	private int serverId;
	private DbUtil dbUtil;
	
	public ServerIdDistributor() {
	
	}
	
	
	public void init() throws UnknownHostException {
		InetAddress addr = InetAddress.getLocalHost();
		this.ip = addr.getHostAddress().toString();
		this.dbUtil = new DbUtil();
		this.serverId = -1;
	}
	
	@Override
	public int get() throws IdForCoordinatorException {
		// 1. read local db to fetch serverid
		if (this.serverId == -1) {
			this.serverId = this.dbUtil.getServerId(CoordinatorConfig.getInstance());
		}
		return this.serverId;
	}

}
