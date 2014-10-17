package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdForCoordinator idForCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	public void setIdForCoordinator(IdForCoordinator idForCoordinator) {
		this.idForCoordinator = idForCoordinator;
	}

	public void setUuidGenerator(UUIDGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;
	}

	public int getCoordinatorId()  {
		return idForCoordinator.get();
	}
	
	public long getNextUUID() {
		return uuidGenerator.next();
	}
}
