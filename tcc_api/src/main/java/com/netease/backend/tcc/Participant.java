package com.netease.backend.tcc;

import com.netease.backend.tcc.error.ParticipantException;


public interface Participant {
	
	void cancel(Long uuid) throws ParticipantException;
	
	void confirm(Long uuid) throws ParticipantException;
	
	void expired(Long uuid) throws ParticipantException;
}
