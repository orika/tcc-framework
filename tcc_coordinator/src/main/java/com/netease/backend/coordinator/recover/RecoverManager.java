package com.netease.backend.coordinator.recover;

import com.netease.backend.tcc.error.CoordinatorException;

public interface RecoverManager {
	public abstract void init() throws CoordinatorException; 
}
