package com.netease.backend.coordinator.test.container;

import com.netease.backend.tcc.error.ParticipantException;

public class DefaultContainer implements ServiceContainer {
	
	private Service service;
	
	public void setService(Service service) {
		this.service = service;
	}

	@Override
	public void cancel(long uuid) throws ParticipantException {
		service.cancel(uuid);
	}

	@Override
	public void confirm(long uuid) throws ParticipantException {
		service.confirm(uuid);
	}

	@Override
	public void expired(long uuid) throws ParticipantException {
		service.expired(uuid);
	}

	@Override
	public void invoke(String methodName, Object[] args)
			throws ParticipantException {
		service.invoke(methodName, args);
	}

	@Override
	public void tryDo() {
		service.tryDo();
	}

	@Override
	public Service getService() {
		return service;
	}

	@Override
	public Object getResult() {
		return service.getResult();
	}
}
