package com.netease.backend.coordinator.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BgExecutor {

	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService threadPool = null;
	
	public BgExecutor(int bgThreadNum) {
		threadPool = new ThreadPoolExecutor(bgThreadNum, bgThreadNum, 
				600L, TimeUnit.SECONDS, queue);
	}
	
	public void execute(Runnable task) {
		threadPool.execute(task);
	}
}
