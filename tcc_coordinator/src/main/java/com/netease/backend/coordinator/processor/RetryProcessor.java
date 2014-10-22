package com.netease.backend.coordinator.processor;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.task.TxRetryWatcher;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.error.CoordinatorException;

public class RetryProcessor implements Runnable {
	
	private Logger logger = Logger.getLogger("RetryProcessor");
	
	private AtomicInteger parallelism;
	private TxManager txManager = null;
	private DelayQueue<Task> retryQueue = new DelayQueue<Task>();
	private Lock lock = new ReentrantLock();
	private Condition isSpotFree = lock.newCondition();
	private Thread thread = null;
	private ExecutorService executor = null;
	private RetryWatcher watcher = new RetryWatcher();
	private volatile boolean stop = false;
	//optimize notify count
	private volatile boolean isBlocked = false;
	
	public RetryProcessor(CoordinatorConfig config, TxManager txManager, 
			ExecutorService executor) {
		this.parallelism = new AtomicInteger(config.getRetryParallelism());
		this.txManager = txManager;
		this.executor = executor;
	}
	
	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		this.stop = true;
		thread.interrupt();
	}

	@Override
	public void run() {
		while (!stop && !Thread.interrupted()) {
			try {
				for (int i = 0, j = parallelism.get(); i < j; i++) {
					Task task = retryQueue.take();
					executor.execute(task);
				}
			} catch (InterruptedException e) {
				continue;
			}
			lock.lock();
			if (parallelism.get() > 0) {
				lock.unlock();
				continue;
			}
			isBlocked = true;
			isSpotFree.awaitUninterruptibly();
			lock.unlock();
		}
	}
	
	public void recover(Iterator<Transaction> it) throws CoordinatorException {
		int confirmCount = 0;
		int cancelCount = 0;
		int expireCount = 0;
		while (it.hasNext()) {
			Transaction tx = it.next();
			switch (tx.getAction()) {
				case CANCEL:
					cancelCount++;
					process(tx, 1, watcher);
					break;
				case CONFIRM:
					confirmCount++;
					process(tx, 1, watcher);
					break;
				case EXPIRE:
					expireCount++;
					process(tx, 1, watcher);
					break;
				default:
					break;
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Initialize retrying tasks,confirm:").append(confirmCount);
		builder.append(",cancel:" + cancelCount);
		builder.append(",expire:" + expireCount);
		logger.info(builder);
		int count = 0;
		while (retryQueue.size() != 0) {
			try {
				Thread.sleep(1000);
				count++;
			} catch (InterruptedException e) {
				throw new CoordinatorException(e);
			}
			if (count % 5 == 0)
				logger.info("retry queue left Task count:" + retryQueue.size());
		}
	}
	
	/*
	 * failed or success, just drop it
	 */
	private void processResult(Transaction tx) {
		lock.lock();
		parallelism.decrementAndGet();
		if (isBlocked) {
			isBlocked = false;
			isSpotFree.signalAll();
		}
		lock.unlock();
	}
	
	public void process(Transaction tx, int times, TxRetryWatcher watcher) {
		retryQueue.offer(new Task(tx, times, watcher));
	}
	
	private class RetryWatcher implements TxRetryWatcher {

		@Override
		public void processError(Transaction tx) {
			Task task = new Task(tx, 2, this);
			task.delay(10000);
			retryQueue.offer(task);
		}

		@Override
		public void processSuccess(Transaction tx) {
		}
	}
	
	private class Task implements Delayed, Runnable {
		
		private Transaction tx;
		private long ts;
		private int times = 1;
		private TxRetryWatcher watcher = null;
		
		Task(Transaction tx, int times, TxRetryWatcher watcher) {
			this.tx = tx;
			this.ts = 0;
			this.times = times;
			this.watcher = watcher;
		}
		
		@Override
		public void run() {
			times--;
			try {
				txManager.retry(tx);
				if (watcher != null)
					watcher.processSuccess(tx);
			} catch (LogException e) {
				if (watcher != null)
					watcher.processError(tx);
			} finally {
				processResult(tx);
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
		
		public boolean delay(long ts) {
			if (times <= 0)
				return false;
			times--;
			this.ts = ts;
			return true;
		}
		
		/*public String getFailedDescrip() {
			StringBuilder builder = new StringBuilder();
			builder.append("Retry ").append(tx.getAction().name())
				.append(" failed, uuid:").append(tx.getUUID());
			return builder.toString();
		}*/
	}
}
