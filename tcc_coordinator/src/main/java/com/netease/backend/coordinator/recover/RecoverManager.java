package com.netease.backend.coordinator.recover;

import com.netease.backend.tcc.error.CoordinatorException;

public abstract class RecoverManager {
	
	protected volatile boolean isAvailable = false;
	
	public boolean isAvailable() {
		return isAvailable;
	}
	
	public abstract void init() throws CoordinatorException; 
}
