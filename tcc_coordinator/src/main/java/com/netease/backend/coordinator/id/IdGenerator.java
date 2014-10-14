package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdForCoordinator idOfCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	public int getCoordinatorId()  {
		return idOfCoordinator.get();
	}
	
	public long getNextUUID() {
		return uuidGenerator.next();
	}
	
}
