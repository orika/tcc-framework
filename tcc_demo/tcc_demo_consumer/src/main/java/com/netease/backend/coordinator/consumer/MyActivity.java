package com.netease.backend.coordinator.consumer;

import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.TccActivity;
import com.netease.backend.tcc.TccManager;

public class MyActivity extends TccActivity {

	public MyActivity(TccManager tccManager, Participant[] participants) {
		super(tccManager, participants);
	}

	@Override
	public int[] getConfirmSeq() {
		return new int[]{0,0,1};
	}

	@Override
	public int[] getCancelSeq() {
		return new int[]{0,0,-1};
	}
}
