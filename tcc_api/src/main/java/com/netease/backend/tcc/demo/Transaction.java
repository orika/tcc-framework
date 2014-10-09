package com.netease.backend.tcc.demo;

import com.netease.backend.tcc.CoordinatorException;

public interface Transaction {
	
	long getUUID();
	
	public int getSequenceId();
	
	public <T> T getProxy(String serviceName, Class<T> serviceType);
	
	void confirm() throws CoordinatorException;
	
	public void confirm(long timeout) throws CoordinatorException;
	
	public void cancel() throws CoordinatorException;
	
	public void cancel(long timeout) throws CoordinatorException;
}
