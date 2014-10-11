package com.netease.backend.coordinator.recover;

import com.netease.backend.tcc.error.CoordinatorException;

public interface RecoverManager {
	
	public void init() throws CoordinatorException; 
}
