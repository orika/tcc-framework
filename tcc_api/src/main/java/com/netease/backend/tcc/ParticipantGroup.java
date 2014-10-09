package com.netease.backend.tcc;

public abstract class ParticipantGroup implements Expirable {
	
	public final static int[] DEFAULT_SEQ = new int[0]; //sequence like 0,1,2,3...
	
	public abstract Participant[] getParticipants();

	public void expired(Transaction tx) {
		//do nothing, auto generate uncustomed cancel
	}

	public boolean isCustomed() {
		return true;
	}
}
