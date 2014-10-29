package com.netease.backend.tcc.error;

import com.netease.backend.tcc.Procedure;

public class TimeoutException extends CoordinatorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6272232747340953859L;
	
	private Procedure proc = null;
	
	public TimeoutException(long timeout, Procedure proc, long uuid) {
		super(new StringBuilder().append("uuid ").append(uuid).append(" call ")
				.append(proc.getService()).append(" timeout:").append(timeout).toString());
		this.proc = proc;
	}
	
	public Procedure getProcedure() {
		return proc;
	}
}
