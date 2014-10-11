package com.netease.backend.coordinator.log;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;

public interface LogManager {
	
	void logBegin(Transaction tx, Action action) throws LogException;
	
	void logFinish(Transaction tx, Action action) throws LogException;
	
	void logRegister(Transaction tx) throws LogException;
	
	boolean checkExpired(long uuid) throws LogException;
}
