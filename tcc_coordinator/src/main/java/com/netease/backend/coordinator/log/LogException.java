package com.netease.backend.coordinator.log;

import com.netease.backend.tcc.CoordinatorException;

public class LogException extends CoordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8019056627152712074L;
	
	public LogException(String message) {
		super(message);
	}
}
