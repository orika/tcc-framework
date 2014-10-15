package com.netease.backend.coordinator.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netease.backend.coordinator.config.CoordinatorConfig;

public class BgExecutor {

	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService threadPool = null;
	
	public BgExecutor(int bgMaxThreadNum) {
		threadPool = new ThreadPoolExecutor(1, bgMaxThreadNum,
	            60L, TimeUnit.SECONDS, queue); 
	}
	
	public void execute(Runnable task) {
		threadPool.execute(task);
	}
}
