package com.netease.backend.coordinator.test.simple;

import com.netease.backend.coordinator.test.container.Service;
import com.netease.backend.tcc.error.ParticipantException;

public class SimpleService implements Service {
		
	private int status = -2;
	
	@Override
	public void cancel(Long uuid) throws ParticipantException {
		if (status == 0)
			status = 2;
		else
			status = -1;
	}

	@Override
	public void confirm(Long uuid) throws ParticipantException {
		if (status == 0)
			status = 1;
		else
			status = -1;
	}

	@Override
	public void expired(Long uuid) throws ParticipantException {
		if (status == 0)
			status = 3;
		else
			status = -1;
	}

	@Override
	public void tryDo() {
		status = 0;
	}

	@Override
	public Object getResult() {
		return status;
	}
}
