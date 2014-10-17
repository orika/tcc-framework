/**
 * @Description:			Used for scan log in recovery
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */
package com.netease.backend.coordinator.id;

public interface UUIDGenerator {
	void init(long lastUUID);
	long next();
}
