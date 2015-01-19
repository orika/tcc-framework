package com.netease.backend.tcc;

public abstract class ParticipantGroup{
	
	public final static int[] DEFAULT_SEQ = new int[0]; //sequence like 0,1,2,3...
	
	public abstract Participant[] getParticipants();

	public boolean isCustomed() {
		return true;
	}
}
