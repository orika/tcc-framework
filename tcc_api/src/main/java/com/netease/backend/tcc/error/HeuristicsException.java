package com.netease.backend.tcc.error;

import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.TccCode;
import com.netease.backend.tcc.TccUtils;

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
		this.code = TccUtils.UNDEFINED;
	}
	
	public HeuristicsException(Procedure proc) {
		super();
		this.code = TccUtils.UNDEFINED;
		this.proc = proc;
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
	
	public static HeuristicsException getException(HeuristicsType type, Procedure proc) {
		switch (type) {
			case SERVICE_NOT_FOUND: 
				return new HeuristicsException(TccCode.getServiceNotFound(proc.getIndex()), proc);
			case TIMEOUT: 
				return new HeuristicsException(TccCode.getTimeout(proc.getIndex()), proc);
			case UNDEFINED:
				return new HeuristicsException(proc);
			default:
				throw new UnsupportedOperationException("normal HeuristicsException must has a code");
		} 
	}
	
	public static HeuristicsException getException(short code, Procedure proc) {
			return new HeuristicsException(code, proc);
	}
}