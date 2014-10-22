package com.netease.backend.coordinator.test.container;

import java.util.ArrayList;
import java.util.List;

public class Containers {

	private List<ServiceContainer> containerList = new ArrayList<ServiceContainer>();
	private List<ServiceContainer> serviceList = new ArrayList<ServiceContainer>();
	
	private Containers(ServiceContainer container1, ServiceContainer container2, ServiceContainer container3,
			ServiceContainer container4, ServiceContainer container5, ServiceContainer service1, 
			ServiceContainer service2, ServiceContainer service3, ServiceContainer service4, 
			ServiceContainer service5) {
		containerList.add(container1);
		containerList.add(container2);
		containerList.add(container3);
		containerList.add(container4);
		containerList.add(container5);
		serviceList.add(service1);
		serviceList.add(service2);
		serviceList.add(service3);
		serviceList.add(service4);
		serviceList.add(service5);
	}
	
	public ServiceContainer getService(int index, Service service) {
		if (index >= 5 || index < 0)
			throw new IllegalArgumentException("index must < 5 and >= 0");
		containerList.get(index).setService(service);
		return serviceList.get(index);
	}
	
	public void clear() {
		for (ServiceContainer container : containerList)
			container.setService(null);
	}
}
