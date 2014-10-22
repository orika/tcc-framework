package com.netease.backend.coordinator.test.container;

import com.netease.backend.tcc.Participant;

public interface Service extends Participant {
	void tryDo();
	
	Object getResult();
}
