package com.netease.backend.tcc.error;

import com.netease.backend.tcc.Procedure;

public class ServiceDownException extends CoordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744170308586376825L;
	
	private Procedure proc = null;
	
	public ServiceDownException(Procedure proc, long uuid) {
		super(new StringBuilder().append("service ")
				.append(proc.getService()).append(" is not available,uuid ").append(uuid).toString());
		this.proc = proc;
	}
	
	public Procedure getProcedure() {
		return proc;
	}
}
