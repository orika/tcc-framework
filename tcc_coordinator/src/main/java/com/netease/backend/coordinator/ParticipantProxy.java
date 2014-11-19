package com.netease.backend.coordinator;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.error.ParticipantException;

public class ParticipantProxy implements Participant {

	private static final String PCLZ = "com.netease.backend.tcc.error.ParticipantException";
	private static final String CONFIRM = "confirm";
	private static final String CANCEL = "cancel";
	private static final String EXPIRED = "expired";
	
	private static final String[] LONG_PARAM = new String[] {"java.lang.Long"};
	
	private GenericService participant = null;
	private long lastFailedTs = 0;
	private long retryInterval;
	
	public ParticipantProxy(long retryInterval) {
		this.retryInterval = retryInterval;
	}
	
	private ParticipantException getParticipantExp(GenericException e) {
		String clz = e.getExceptionClass();
		if (clz.equals(PCLZ)) {
			String msg = e.getExceptionMessage();
			if (msg.startsWith("#")) {
				int index = msg.indexOf(':');
				short code = Short.valueOf(msg.substring(1, index));
				return new ParticipantException(msg.substring(index), code);
			}
			return new ParticipantException(msg);
		}
		return null;
	}
	
	@Override
	public void cancel(Long uuid) throws ParticipantException {
		try {
			participant.$invoke(CANCEL, LONG_PARAM, new Object[] {uuid});
		} catch (GenericException e) {
			ParticipantException exp = getParticipantExp(e);
			if (exp != null)
				throw exp;
			throw e;
		}
	}

	@Override
	public void confirm(Long uuid) throws ParticipantException {
		try {
			participant.$invoke(CONFIRM, LONG_PARAM, new Object[] {uuid});
		} catch (GenericException e) {
			ParticipantException exp = getParticipantExp(e);
			if (exp != null)
				throw exp;
			throw e;
		}
	}

	@Override
	public void expired(Long uuid) throws ParticipantException {
		try {
			participant.$invoke(EXPIRED, LONG_PARAM, new Object[] {uuid});
		} catch (GenericException e) {
			ParticipantException exp = getParticipantExp(e);
			if (exp != null)
				throw exp;
			throw e;
		}
	}
	
	public void invoke(String methodName, Object[] params) throws ParticipantException {
		String[] paramTypes = new String[params.length];
		for (int i = 0, j = params.length; i < j; i++)
			paramTypes[i] = params[i].getClass().getName();
		try {
			participant.$invoke(methodName, paramTypes, params);
		} catch (GenericException e) {
			ParticipantException exp = getParticipantExp(e);
			if (exp != null)
				throw exp;
			throw e;
		}
	}
	
	public synchronized boolean init(String service, String version, CoordinatorConfig config) {
		if (participant != null) {
			return true;
		}
		if (lastFailedTs != 0 && !shouldRetry()) {
			return false;
		}
		ApplicationConfig application = new ApplicationConfig();
		RegistryConfig registry = new RegistryConfig();
		ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();

		String appName = config.getAppName();
		String zookeeperUrl = config.getZkAddress();
		String group = config.getAppGroup();
		int timeout = config.getRpcTimeout();

		application.setName(appName);
		registry.setAddress(zookeeperUrl);
		if (group != null)
			registry.setGroup(group);

		reference.setApplication(application);
		reference.setRegistry(registry);
		reference.setInterface(service); 
		reference.setGeneric(true); 
		if (version != null)
			reference.setVersion(version);
		if (timeout > 0)
			reference.setTimeout(timeout);

		try {
			participant = reference.get();
			lastFailedTs = 0;
		} catch (RuntimeException e) {
			lastFailedTs = System.currentTimeMillis();
			return false;
		}
		return true;
	}
	
	public boolean isInitialized() {
		return participant != null;
	}
	
	private boolean shouldRetry() {
		return lastFailedTs + retryInterval < System.currentTimeMillis();
	}
}
