package com.netease.backend.tcc;

public interface Expirable {

	void expired(Transaction tx);
	
	boolean isCustomed();
}
