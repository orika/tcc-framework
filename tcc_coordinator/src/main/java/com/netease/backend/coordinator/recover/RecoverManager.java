package com.netease.backend.coordinator.recover;


public interface RecoverManager {
	
	void init(); 
	
	long getLastMaxUUID();
}
