package com.netease.backend.coordinator.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsException;
import com.netease.backend.tcc.error.HeuristicsType;

public class TxResult {
	
	private CountDownLatch countDown;
	private HeuristicsException exception;
	private Worker[] workers;
	private volatile boolean isInterrupted = false;
	private long uuid;
	
	public TxResult(long uuid, int count) {
		this.uuid = uuid;
		this.countDown = new CountDownLatch(count);
		this.workers = new Worker[count];
	}
	
	private void releaseOne() {
		countDown.countDown();
	}
	
	private void interrupt() {
		if (isInterrupted)
			return;
		isInterrupted = true;
		for (Worker worker : workers) {
			if (worker == null)
				continue;
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
	
	public void setThread(int index, Thread thread) {
		workers[index] = new Worker(thread);
		if (isInterrupted) {
			workers[index].interrupt();
		}
	}
	
	public void failed(int index, short errorCode, Procedure proc) {
		if (this.exception == null) {
			this.exception = HeuristicsException.getException(errorCode, proc);
			interrupt();
		}
		releaseOne();
	}
	
	public void failed(int index, HeuristicsType type, Procedure proc) {
		if (this.exception == null) {
			this.exception = HeuristicsException.getException(type, proc);
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
	
	private class Worker {
		private Thread thread;
		private AtomicReference<Status> status = new AtomicReference<Status>();
		
		Worker(Thread thread) {
			this.thread = thread;
			this.status.set(Status.WORK);
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
