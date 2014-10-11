package com.netease.backend.tcc.error;

public class ParticipantException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected short code;

	public ParticipantException(Throwable cause) {
		super(cause);
	}
	
	public ParticipantException(String message) {
		super(message);
	}
	
	public ParticipantException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public short getErrorCode() {
		return code;
	}
	
	public void setErrorCode(short code) {
		this.code = code;
	}
}
