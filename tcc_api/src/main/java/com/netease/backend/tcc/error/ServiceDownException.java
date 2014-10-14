package com.netease.backend.tcc.error;

import com.netease.backend.tcc.Procedure;

public class ServiceDownException extends CoordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744170308586376825L;
	
	private Procedure proc = null;
	
	public ServiceDownException(Procedure proc) {
		super("service " + proc.getService() + " is not available");
		this.proc = proc;
	}
	
	public Procedure getProcedure() {
		return proc;
	}
}
