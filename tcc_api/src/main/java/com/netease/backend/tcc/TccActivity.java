package com.netease.backend.tcc;

public class TccActivity extends ParticipantGroup {

	protected Participant[] participants;
	
	public TccActivity(TccManager tccManager) {
		tccManager.register(this);
	}
	
	public TccActivity(TccManager tccManager, Participant... participants) {
		this.participants = participants;
		tccManager.register(this);
	}
	
	public int[] getConfirmSeq() {
		return DEFAULT_SEQ;
	}
	
	public int[] getCancelSeq() {
		return DEFAULT_SEQ;
	}
	
	public int[] getExpireSeq() {
		return  getCancelSeq();
	}
	
	@Override
	public Participant[] getParticipants() {
		return participants;
	}

	@Override
	public boolean isCustomed() {
		return false;
	}

}
