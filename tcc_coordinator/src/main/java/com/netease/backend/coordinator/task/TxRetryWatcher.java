package com.netease.backend.coordinator.task;

import com.netease.backend.coordinator.transaction.Transaction;

public interface TxRetryWatcher {
	
	void processError(Transaction tx);
	
	void processSuccess(Transaction tx);
}
