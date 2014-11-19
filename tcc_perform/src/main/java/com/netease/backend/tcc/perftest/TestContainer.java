package com.netease.backend.tcc.perftest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class TestContainer implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		TestContainer.applicationContext = applicationContext;
	}
	
	  /** 
     * @return ApplicationContext 
     */  
    public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }  
	
	public Object getBean(String id) {
		return applicationContext.getBean(id);
	}
}
