package com.netease.backend.tcc.demo.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	
	public static void log(String content) {
		System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]" + content);
	}
}
