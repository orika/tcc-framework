/**
 * @Description:			Log Type enum
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */

package com.netease.backend.coordinator.recover;


public interface RecoverManager {
	
	/**
	 * Description: read log and recover the active transaction table
	 */
	void init(); 
	
	/**
	 * Description: get max uuid after recovery
	 * @return read max uuid
	 */
	long getLastMaxUUID();
}
