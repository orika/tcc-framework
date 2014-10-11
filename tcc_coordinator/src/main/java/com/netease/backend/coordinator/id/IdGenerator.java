package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdAtLastDown idLastDown = null;
	private IdForCoordinator idOfCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	public int getCoordinatorId() throws IdForCoordinatorException {
		return idOfCoordinator.get();
	}
	
	public long getUUIDOfLastDown() {
		return idLastDown.get();
	}
	
	public long getNextUUID() throws IdForCoordinatorException {
		return uuidGenerator.next();
	}
}
