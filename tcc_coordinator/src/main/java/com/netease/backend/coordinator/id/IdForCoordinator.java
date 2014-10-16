/**
 * @Description:			Manager Coordinator Id (serverId)
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */

package com.netease.backend.coordinator.id;

public interface IdForCoordinator {
	/**
	 * Description: get server id
	 * @return serverid
	 */
	int get();
	
	/**
	 * Description: determine whether a uuid is alloc by itself
	 * @param uuid
	 * @return true is the uuid is alloc by itself
	 */
	boolean isUuidOwn(long uuid);
}
