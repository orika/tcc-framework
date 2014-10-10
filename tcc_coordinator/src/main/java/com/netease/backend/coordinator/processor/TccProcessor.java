package com.netease.backend.coordinator.processor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.netease.backend.coordinator.processor.ServiceTask.TaskResult;
import com.netease.backend.tcc.HeuristicsException;
import com.netease.backend.tcc.Procedure;

public class TccProcessor {
	
	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public void perform(long uuid, List<Procedure> procedures) 
			throws HeuristicsException {
		Collections.sort(procedures);
		TaskResult result = null;
		while (procedures.size() != 0) {
			Procedure lastOne = null;
			int count = 0;
			for (Iterator<Procedure> it = procedures.iterator(); it.hasNext(); ) {
				Procedure cur = it.next();
				if (cur.getSequence() < 0) {
					it.remove();
					continue;
				}
				count++;
				if (lastOne != null && lastOne.getSequence() != cur.getSequence())
					break;
			}
			result = new TaskResult(new CountDownLatch(count));
			for (Iterator<Procedure> it = procedures.iterator(); it.hasNext() && count > 0; count--) {
				Procedure cur = it.next();
				if (count == 1) {
					new ServiceTask(uuid, cur, result).run();
				} else {
					threadPool.execute(new ServiceTask(uuid, cur, result));
				}
				it.remove();
			}
			try {
				result.await();
			} catch (InterruptedException e) {
				throw new HeuristicsException(e);
			}
			if (result.isFailed()) {
				throw new HeuristicsException(result.getException());
			}
		}
	}
	
	public void perform(final long uuid, final List<Procedure> group, long timeout) 
			throws HeuristicsException {
		final TaskResult barrier = new TaskResult(null);
		synchronized (barrier) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						perform(uuid, group);
					} catch (HeuristicsException e) {
						barrier.setException(e);;
					}
					synchronized (barrier) {
						barrier.notifyAll();
					}
				}
			});
			try {
				barrier.wait(timeout);
			} catch (InterruptedException e) {
				throw new HeuristicsException(e);
			}
		}
		if (barrier.isFailed()) {
			throw (HeuristicsException) barrier.getException();
		}
	}
}
