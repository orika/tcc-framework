package com.netease.backend.coordinator.monitor;

import com.netease.backend.tcc.error.CoordinatorException;

public class MonitorException extends CoordinatorException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8943650879405912407L;

	public MonitorException(String message, Throwable t) {
		super(message, t);
	}
}
