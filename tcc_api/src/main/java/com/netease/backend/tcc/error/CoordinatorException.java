package com.netease.backend.tcc.error;

public class CoordinatorException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CoordinatorException(Throwable cause) {
		super(cause);
	}
	
	public CoordinatorException(String message) {
		super(message);
	}
	
	public CoordinatorException(String message, Throwable cause) {
		super(message, cause);
	}
}
