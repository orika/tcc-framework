package com.netease.backend.coordinator.log;

public interface LogScanner {
	void beginScan(long startpoint) throws LogException;
	boolean hasNext() throws LogException;
	LogRecord next() throws LogException;
	void endScan() throws LogException;
}
