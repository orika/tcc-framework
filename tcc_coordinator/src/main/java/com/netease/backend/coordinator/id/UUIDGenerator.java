package com.netease.backend.coordinator.id;

public interface UUIDGenerator {
	void init(long lastUUID);
	long next();
}
