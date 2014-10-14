package com.netease.backend.coordinator.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.error.HeuristicsException;

public class TxResult {
	
	private CountDownLatch countDown;
	private HeuristicsException exception;
	private Worker[] workers;
	private long uuid;
	private volatile TxResultWatcher watcher = null;
	
	public TxResult(long uuid, int count, TxResultWatcher watcher) {
		this.uuid = uuid;
		this.countDown = new CountDownLatch(count);
		this.workers = new Worker[count];
		this.watcher = watcher;
	}
	
	private void releaseOne() {
		countDown.countDown();
		if (watcher != null && countDown.getCount() == 0) {
			watcher.notifyResult(this);
		}
	}
	
	private void interrupt() {
		for (Worker worker : workers) {
			worker.interrupt();
		}
	}
	
	public long getUUID() {
		return uuid;
	}
	
	public void success(int index) throws InterruptedException {
		workers[index].done();
		releaseOne();
	}
	
	public void setWorker(int index, Thread thread) {
		workers[index] = new Worker(thread);
	}
	
	public void failed(int index, HeuristicsException exception) {
		if (this.exception != null) {
			this.exception = exception;
			interrupt();
		}
		releaseOne();
	}
	
	public boolean isFailed() {
		return exception != null;
	}
	
	public HeuristicsException getException() {
		if (exception != null)
			return exception;
		return new HeuristicsException();
	}
	
	public void await() throws InterruptedException {
		countDown.await();
	}
	
	public boolean await(long timeout) throws InterruptedException {
		if(!countDown.await(timeout, TimeUnit.MILLISECONDS)) {
			interrupt();
			return false;
		}
		return true;
	}
	
	public void setWatcher(TxResultWatcher watcher) {
		this.watcher = watcher;
		if (countDown.getCount() == 0)
			watcher.notifyResult(this);
	}
	
	private class Worker {
		private Thread thread;
		private AtomicReference<Status> status;
		
		Worker(Thread thread) {
			this.thread = thread;
		}
		
		boolean interrupt() {
			if (status.compareAndSet(Status.WORK, Status.INTERRUPT)) {
				thread.interrupt();
				return true;
			}
			return false;
		}
		
		void done() throws InterruptedException {
			if (!status.compareAndSet(Status.WORK, Status.DONE))
				throw new InterruptedException();
		}
	}
	
	private enum Status {
		WORK,
		DONE,
		INTERRUPT;
	}
}
