package com.netease.backend.tcc.error;

import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.TccCode;

public class HeuristicsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1620721172886005574L;
	
	private short code;
	private Procedure proc;

	public HeuristicsException(short code, Procedure proc) {
		super();
		this.code = code;
		this.proc = proc;
	}
	
	public HeuristicsException() {
		super();
		this.code = TccCode.UNDEFINED;
	}
	
	public short getCode() {
		return code;
	}
	
	public String getServiceName() {
		return proc.getService();
	}
	
	public String getMethodName() {
		return proc.getMethod();
	}
}
