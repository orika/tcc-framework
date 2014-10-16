package com.netease.backend.coordinator.log;

import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;

public interface LogScanner {
	boolean hasNext() throws LogException;
	LogRecord next() throws LogException;
	void endScan() throws LogException;
}
