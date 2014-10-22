package com.netease.backend.coordinator.test.container;

import com.netease.backend.tcc.Participant;

public interface ServiceContainer extends Participant {
	
	void tryDo();
	
	Service getService();

	void setService(Service service);
	
	Object getResult();
}
