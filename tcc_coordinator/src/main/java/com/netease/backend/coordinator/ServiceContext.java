package com.netease.backend.coordinator;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.tcc.Coordinator;

public class ServiceContext implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;
	private static ConcurrentHashMap<Integer, ParticipantProxy> serviceMap = 
			new ConcurrentHashMap<Integer, ParticipantProxy>();
	
	private CoordinatorConfig config = null;
	
	public ServiceContext(CoordinatorConfig config) {
		this.config = config;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		ServiceContext.applicationContext = applicationContext;
	}
	
	  /** 
     * @return ApplicationContext 
     */  
    public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }  

	public ParticipantProxy getService(String name, String version) throws ServiceUnavailableException  {
		if (name == null)
			throw new IllegalArgumentException("service name can not be null!");
		Integer sig = getSignature(name, version);
		ParticipantProxy service = serviceMap.get(sig);
		if (service == null)
			serviceMap.putIfAbsent(sig, new ParticipantProxy(1000));
		service = serviceMap.get(sig);
		if (!service.isInitialized() && !service.init(name, version, config)) {
			throw new ServiceUnavailableException(name, version);
		}
		return service;
	}
	
	private int getSignature(String service, String version) {
		if (version == null)
			return service.hashCode();
		return service.hashCode() + version.hashCode();
	}
	
	public static Coordinator getCoordinator() {
		Object obj = applicationContext.getBean("coordinator");
		if (obj == null)
			return null;
		return (Coordinator) obj;
	}
	
	public static Object getBean(String beanId) {
		return applicationContext.getBean(beanId);
	}
}
