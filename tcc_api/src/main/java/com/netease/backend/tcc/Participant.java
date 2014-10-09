package com.netease.backend.tcc;


public interface Participant {
	
	void cancel(long uuid) throws ParticipantException;
	
	void confirm(long uuid) throws ParticipantException;
	
	void expired(long uuid) throws ParticipantException;
	
	void invoke(String methodName, Object[] args) throws ParticipantException;
}
