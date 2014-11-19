package com.netease.backend.tcc.perftest.server;

import com.netease.backend.tcc.error.ParticipantException;
import com.netease.backend.tcc.perftest.face.CommonInterface;

public class BaseService implements CommonInterface {
	
	public static int sleepTime = 10;

	@Override
	public void tryDo() {
		
	}

	@Override
	public void cancel(Long uuid) throws ParticipantException {
		perform();
	}

	@Override
	public void confirm(Long uuid) throws ParticipantException {
		perform();
	}

	@Override
	public void expired(Long uuid) throws ParticipantException {
		perform();
	}
	
	private void perform() {
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
