package com.netease.backend.tcc;

public class TccActivity extends ParticipantGroup {

	protected Participant[] participants;
	
	public TccActivity(TccManager tccManager) {
		tccManager.register(this);
	}
	
	public TccActivity(TccManager tccManager, Participant... participants) {
		this.participants = participants;
		checkParticipants();
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
	public final boolean isCustomed() {
		return false;
	}

	private void checkParticipants() {
		if (getConfirmSeq() != DEFAULT_SEQ && getConfirmSeq().length != participants.length)
			throw new IllegalArgumentException("illegal participant count for confirming:" 
					+ getConfirmSeq().length);
		if (getCancelSeq() != DEFAULT_SEQ && getCancelSeq().length != participants.length)
			throw new IllegalArgumentException("illegal participant count for canceling:" 
					+ getConfirmSeq().length);
		if (getExpireSeq() != DEFAULT_SEQ && getExpireSeq().length != participants.length)
			throw new IllegalArgumentException("illegal participant count for expiring:" 
					+ getConfirmSeq().length);
	}
}
