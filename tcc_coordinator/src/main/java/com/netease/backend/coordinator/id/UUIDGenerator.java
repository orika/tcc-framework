package com.netease.backend.coordinator.id;

public interface UUIDGenerator {
	long next() throws IdForCoordinatorException;
}
