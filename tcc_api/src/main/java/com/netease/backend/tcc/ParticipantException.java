package com.netease.backend.tcc;

public class ParticipantException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParticipantException(Throwable cause) {
		super(cause);
	}
	
	public ParticipantException(String message) {
		super(message);
	}
	
	public ParticipantException(String message, Throwable cause) {
		super(message, cause);
	}
}
