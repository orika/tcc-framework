package com.netease.backend.coordinator.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsException;
import com.netease.backend.tcc.error.HeuristicsType;

public class TxResult {
	
	private CountDownLatch countDown;
	private AtomicReference<HeuristicsException> exception = new AtomicReference<HeuristicsException>();
	private Worker[] workers;
	private long uuid;
	private volatile boolean isInterrupted = false;
	
	private static final HeuristicsException INTERRUPTED = new HeuristicsException();
	
	public TxResult(long uuid, int count) {
		this.uuid = uuid;
		this.countDown = new CountDownLatch(count);
		this.workers = new Worker[count];
	}
	
	private void releaseOne() {
		countDown.countDown();
	}
	
	private void interruptBy(Worker interrupter) {
		if (isInterrupted)
			return;
		for (Worker worker : workers) {
			if (worker == null || worker == interrupter)
				continue;
			worker.interrupt();
		}
		isInterrupted = true;
	}
	
	private void interruptAll() {
		if (isInterrupted)
			return;
		for (Worker worker : workers) {
			if (worker == null)
				continue;
			worker.interrupt();
		}
		isInterrupted = true;
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
		if (exception.get() != null) {
			workers[index].interrupt();
		}
	}
	
	public void failed(int index, short errorCode, Procedure proc, String msg) {
		Worker interrupter = workers[index];
		if (exception.get() == null &&
				exception.compareAndSet(null, HeuristicsException.getException(errorCode, proc, msg))) {
			interruptBy(interrupter);
		} else
			interrupter.fence();
		releaseOne();
	}
	
	public void failed(int index, HeuristicsType type, Procedure proc, String msg) {
		Worker interrupter = workers[index];
		if (exception.get() == null &&
				exception.compareAndSet(null, HeuristicsException.getException(type, proc, msg))) {
			interruptBy(interrupter);
		} else
			interrupter.fence();
		releaseOne();
	}
	
	public void interrupted(int index, Procedure proc, String msg) {
		Worker interrupter = workers[index];
		if (exception.compareAndSet(null, HeuristicsException.getException(HeuristicsType.TIMEOUT, proc, msg))) {
			interruptBy(interrupter);
		}
		releaseOne();
	}
	
	public boolean isFailed() {
		return exception.get() != null;
	}
	
	public HeuristicsException getException() {
		return exception.get();
	}
	
	public void await() throws InterruptedException {
		countDown.await();
	}
	
	public boolean interruptAfter(long timeout) throws InterruptedException {
		if(!countDown.await(timeout, TimeUnit.MILLISECONDS)) {
			if (countDown.getCount() != 0 &&
					exception.compareAndSet(null, INTERRUPTED))
				interruptAll();
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
		
		void fence() {
			while (!isInterrupted)
				LockSupport.parkNanos(1000);
			Thread.interrupted();
		}
		
		/*
		 * when a interrupt happened, must eat this interrupt!
		 */
		void done() {
			if (!status.compareAndSet(Status.WORK, Status.DONE)) {
				while (!Thread.interrupted())
					LockSupport.parkNanos(1000);
			}
		}
	}
	
	private enum Status {
		WORK,
		DONE,
		INTERRUPT;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread() {
			public void run() {
				while (!Thread.interrupted()) {
					System.out.println("thread is parked");
					while (!Thread.interrupted())
						LockSupport.parkNanos(1000000000000L);
					System.out.println("thread is interrupted");
				}
			}
		};
		t.setDaemon(false);
		t.start();
		System.out.println("wait 2s...");
		t.interrupt();
	}
}
