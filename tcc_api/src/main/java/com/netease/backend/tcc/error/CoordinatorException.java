package com.netease.backend.tcc.error;

public class CoordinatorException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2471063501124816722L;
	protected short code;
	
	public CoordinatorException(Throwable cause) {
		super(cause);
	}
	
	public CoordinatorException(String message) {
		super(message);
	}
	
	public CoordinatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public short getErrorCode() {
		return code;
	}
}
