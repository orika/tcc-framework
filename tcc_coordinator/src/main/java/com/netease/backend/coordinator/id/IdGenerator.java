package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdAtLastDown idLastDown = null;
	private IdForCoodinator idOfCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	public int getCoordinatorId() {
		return idOfCoordinator.get();
	}
	
	public long getUUIDOfLastDown() {
		return idLastDown.get();
	}
	
	public long getNextUUID() {
		return uuidGenerator.next();
	}
}
