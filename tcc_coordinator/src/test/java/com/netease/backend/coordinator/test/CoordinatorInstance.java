package com.netease.backend.coordinator.test;

import java.io.IOException;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.coordinator.TccContainer;
import com.netease.backend.tcc.Coordinator;

public class CoordinatorInstance {
	
	private static Coordinator coordinator = null;
	
	public static Coordinator get() {
		return coordinator;
	}
	
	public static void touch() {
		if (coordinator == null)
			init();
	}

	public static void init() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					TccContainer.main(new String[]{"classpath*:/test/*.xml"});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		while (coordinator == null) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (ServiceContext.getApplicationContext() == null)
				continue;
			coordinator = ServiceContext.getCoordinator();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
