package com.netease.backend.tcc.perftest;

import com.netease.backend.tcc.perftest.server.BaseService;

public class TccServer {
	
	public static void main(String[] args) {
		if (args.length != 0) {
			String sleepTime = args[0];
			BaseService.sleepTime = Integer.valueOf(sleepTime);
		}
		System.setProperty("dubbo.spring.config", "classpath*:/server.xml");
		com.alibaba.dubbo.container.Main.main(new String[0]);
	}
}
