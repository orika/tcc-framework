package com.netease.backend.tcc;

import java.io.Serializable;
import java.util.List;

public class Procedure implements Comparable<Procedure>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String service;
	
	private String version;
	
	private String method;
	
	private int sequence;
	
	private List<Object> parameters;
	
	public Procedure() {
	}
	
	public Procedure(String service) {
		this.service = service;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public List<Object> getParameters() {
		return parameters;
	}

	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int compareTo(Procedure o) {
		return this.sequence - o.sequence;
	}
}
