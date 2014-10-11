package com.netease.backend.coordinator.processor;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class ExpireProcessor implements Runnable {
	
	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService threadPool = new ThreadPoolExecutor(0, 100,
            60L, TimeUnit.SECONDS, queue); 
	private DelayQueue<Task> taskQueue = new DelayQueue<Task>();
	private TxManager txManager = null;
	
	@Override
	public void run() {
		Task task = null;
		try {
			task = taskQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		threadPool.execute(task);
	}
	
	public void process(Transaction tx) {
		threadPool.execute(new Task(tx));
	}
	
	private class Task implements Runnable, Delayed {
		
		private Transaction tx;
		private long ts;
		
		Task(Transaction tx) {
			this.tx = tx;
		}

		@Override
		public void run() {
			List<Procedure> procList = tx.getExpireList();
			try {
				if (procList == null)
					return;
				for (Procedure proc : procList) {
					if (proc.getMethod() == null)
						proc.setMethod(ServiceTask.EXPIRED);
				}
				txManager.expire(tx);
			} catch (HeuristicsException e) {
				try {
					txManager.heuristic(tx, Action.EXPIRED, e);
				} catch (LogException e1) {
					e1.printStackTrace();
				}
			} catch (LogException e) {
				
			} catch (CoordinatorException e) {
				
			}
		}

		@Override
		public int compareTo(Delayed o) {
			Task task = (Task) o;
			return (int) (ts - task.ts);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(ts, TimeUnit.MILLISECONDS);
		}
	}
}
