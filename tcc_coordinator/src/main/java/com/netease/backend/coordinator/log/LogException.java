/**
 * @Description:			Exception for log operation
 * Copy Right:				Netease
 * Project:					TCC
 * JDK Version				jdk-1.6
 * @version					1.0
 * @author					huwei				
 */

package com.netease.backend.coordinator.log;

import com.netease.backend.tcc.error.CoordinatorException;

public class LogException extends CoordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8019056627152712074L;
	
	public LogException(String message, Throwable t) {
		super(message, t);
	}
	
	public LogException(String message) {
		super(message);
	}
}
