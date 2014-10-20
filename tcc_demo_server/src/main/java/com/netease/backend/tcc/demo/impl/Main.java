/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.netease.backend.tcc.demo.impl;

public class Main {
	
	
	public static void main(String[] args) {
		System.setProperty("dubbo.spring.config", "classpath*:/spring/*.xml");
		com.alibaba.dubbo.container.Main.main(args);
	}
}