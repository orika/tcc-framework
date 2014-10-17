package com.netease.backend.coordinator.task;

import com.netease.backend.coordinator.transaction.Transaction;

public interface TxLogWatcher {
	void processError(Transaction tx);
}
