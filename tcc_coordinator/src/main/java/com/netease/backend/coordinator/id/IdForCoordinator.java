package com.netease.backend.coordinator.id;


public interface IdForCoordinator {
	int get();
	boolean isUuidOwn(long uuid);
}
