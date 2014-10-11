package com.netease.backend.coordinator.recover.db;

import com.netease.backend.coordinator.recover.RecoverManager;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.error.CoordinatorException;

public class RecoverManagerImp extends RecoverManager {

	@Override
	public void init() throws CoordinatorException {
		// TODO Auto-generated method stub
		TxManager txMgr = new TxManager();
	}

}
