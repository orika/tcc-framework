package com.netease.backend.tcc.error;

public class ParticipantException extends CoordinatorException {

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
	
	public ParticipantException(String message, short code) {
		super(new StringBuilder().append('#').append(code).append(':').append(message).toString());
		this.code = code;
	}
	
	public ParticipantException(String message, Throwable cause) {
		super(message, cause);
	}
}
