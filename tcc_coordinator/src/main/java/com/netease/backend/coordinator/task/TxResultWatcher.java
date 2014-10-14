package com.netease.backend.coordinator.task;

public interface TxResultWatcher {
	void notifyResult(TxResult result);
}
