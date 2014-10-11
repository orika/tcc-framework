package com.netease.backend.tcc;

public class TccCode {
	
	public static final short OK = 0;
	
	public static final short UNDEFINED = (short) 0xFFFF;
	
	static boolean isTimeout(short code) {
		return (code & TccUtils.TIMEOUT_PREFIX) > 0;
	}
	
	static boolean isServiceDown(short code) {
		return code < 0;
	}
	
	public static short getServiceDownCode(int index) {
		return (short) (index * -1) ;
	}
	
	public static short getTimeoutCode(int index) {
		return (short) (index | TccUtils.TIMEOUT_PREFIX) ;
	}
}
