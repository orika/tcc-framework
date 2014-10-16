package com.netease.backend.coordinator.id.db;



import com.netease.backend.coordinator.id.IdForCoordinator;
import com.netease.backend.coordinator.util.DbUtil;
import com.netease.backend.tcc.error.CoordinatorException;

public class ServerIdDistributor implements IdForCoordinator {

	private int serverId;
	
	public ServerIdDistributor(DbUtil dbUtil) throws CoordinatorException {
		this.serverId = dbUtil.getServerId();;
	}
	
	@Override
	public int get() {
		return this.serverId;
	}

	@Override
	public boolean isUuidOwn(long uuid) {
		// check high 12 bit to determine
		int serverId = (int) (uuid >> 48);
		return this.serverId == serverId;
	}

}
