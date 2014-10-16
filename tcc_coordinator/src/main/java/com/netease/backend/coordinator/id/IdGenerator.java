package com.netease.backend.coordinator.id;

public class IdGenerator {
	
	private IdForCoordinator idForCoordinator = null;
	private UUIDGenerator uuidGenerator = null;

	public IdGenerator() {
	}
	
	/**
	 * 
	 * @param idForCoordinator
	 */
	public void setIdForCoordinator(IdForCoordinator idForCoordinator) {
		this.idForCoordinator = idForCoordinator;
	}

	/**
	 * 
	 * @param uuidGenerator
	 */
	public void setUuidGenerator(UUIDGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;
	}

	/**
	 * Description: get id of current coordinator node
	 * @return serverid
	 */
	public int getCoordinatorId()  {
		return idForCoordinator.get();
	}
	
	/**
	 * Description: get next uuid
	 * @return next uuid
	 */
	public long getNextUUID() {
		return uuidGenerator.next();
	}
	
}
