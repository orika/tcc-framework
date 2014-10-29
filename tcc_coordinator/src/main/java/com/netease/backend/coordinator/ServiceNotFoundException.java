package com.netease.backend.coordinator;

public class ServiceNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3912323269438791315L;
	private String service;
	private String version;
	
	public ServiceNotFoundException(String service, String version) {
		this.service = service;
		this.version = version;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
