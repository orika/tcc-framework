package com.netease.backend.tcc;

public class TccCode {
	
	public static final short OK = 0;
	
	static boolean isTimeout(short code) {
		return (code & TccUtils.TIMEOUT_MASK) == TccUtils.TIMEOUT_MASK;
	}
	
	static boolean isServiceNotFound(short code) {
		return (code & TccUtils.UNVAILABLE_MASK) == TccUtils.UNVAILABLE_MASK;
	}
	
	public static short getServiceNotFound(int index) {
		return (short) (index | TccUtils.UNVAILABLE_MASK) ;
	}
	
	public static short getTimeout(int index) {
		return (short) (index | TccUtils.TIMEOUT_MASK) ;
	}
}
