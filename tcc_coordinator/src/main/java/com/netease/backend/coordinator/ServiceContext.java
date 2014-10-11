package com.netease.backend.coordinator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.error.ParticipantException;

public class ServiceContext implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;
	private static Map<String, Participant> serviceMap = new HashMap<String, Participant>();
	private static Class<?> rootType = null;
	
	static {
		try {
			rootType = Class.forName("com.netease.backend.tcc.Participant");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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

	public static Participant getService(String name) throws BeansException, ClassNotFoundException, ParticipantException {
		Participant service = serviceMap.get(name);
		if (service != null)
			return service;
		synchronized (serviceMap) {
			service = serviceMap.get(name);
			if (service != null)
				return service;
			for (String beanId : applicationContext.getBeanNamesForType(rootType)) {
				Object bean = applicationContext.getBean(beanId);
				Class<?>[] cls = bean.getClass().getInterfaces();
				for (int i = cls.length - 1; i >= 0; i--) {
					String typeName = cls[i].getName();
					if (rootType.isAssignableFrom(cls[i]) && typeName.equals(name)) {
						Participant pt = (Participant) bean;
						serviceMap.put(name, pt);
						return pt;
					}
				}
			}
			throw new ParticipantException("service " + name + " is not find in coordinator");
		}
	}
	
	public void start() {
//		Participant p = getService("payment");
//		for (Class clzz : p.getClass().getInterfaces())
//			System.out.println(clzz.getName());
	}
}
