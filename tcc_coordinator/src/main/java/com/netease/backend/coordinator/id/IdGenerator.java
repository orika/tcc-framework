package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdAtLastDown idLastDown = null;
	private IdForCoordinator idOfCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	public int getCoordinatorId()  {
		return idOfCoordinator.get();
	}
	
	public long getUUIDOfLastDown() {
		return idLastDown.get();
	}
	
	public long getNextUUID() {
		return uuidGenerator.next();
	}
	
	public boolean isUuidOwn(long uuid) {
		return idOfCoordinator.isUuidOwn(uuid);
	}

	public void setUUIDofLastDown(long uuid) {
		// TODO Auto-generated method stub
		
	}
}
