package com.netease.backend.coordinator.consumer;

public class Main {
	
	
	public static void main(String[] args) {
		System.setProperty("dubbo.spring.config", "classpath*:/spring/*.xml");
		com.alibaba.dubbo.container.Main.main(args);
	}
}