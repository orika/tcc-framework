package com.netease.backend.coordinator.recover;

import com.netease.backend.tcc.error.CoordinatorException;

public interface RecoverManager {
	
	void init() throws CoordinatorException; 
	
	long getLastMaxUUID();
}
