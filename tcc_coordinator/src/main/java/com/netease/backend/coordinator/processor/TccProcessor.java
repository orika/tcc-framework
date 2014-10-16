package com.netease.backend.coordinator.processor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.netease.backend.coordinator.config.CoordinatorConfig;
import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.task.TxResult;
import com.netease.backend.coordinator.task.TxResultWatcher;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsException;

public class TccProcessor {
	
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private BgExecutor bgExecutor = null;
	
	public TccProcessor(CoordinatorConfig config) {
		this.bgExecutor = new BgExecutor(config.getBgMaxThreadNum());
	}

	public void perform(long uuid, List<Procedure> procedures) 
			throws HeuristicsException {
		TxResult result = performAsync(uuid, procedures, null, false);
		try {
			result.await();
		} catch (InterruptedException e) {
			throw new HeuristicsException();
		}
		if (result.isFailed()) {
			throw result.getException();
		}
	}
	
	public void perform(final long uuid, final List<Procedure> procedures, long timeout) 
			throws HeuristicsException {
		TxResult result = performAsync(uuid, procedures, null, false);
		boolean isOk = false;
		try {
			isOk = result.await(timeout);
		} catch (InterruptedException e) {
			throw new HeuristicsException();
		}
		if (!isOk || result.isFailed()) {
			throw result.getException();
		}
	}
	
	public TxResult performAsync(long uuid, List<Procedure> procedures, TxResultWatcher watcher, boolean isBackground) 
			throws HeuristicsException {
		Collections.sort(procedures);
		TxResult result = null;
		while (procedures.size() != 0) {
			Procedure lastOne = null;
			int count = 0;
			int index = 0;
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
			result = new TxResult(uuid, count, watcher);
			for (Iterator<Procedure> it = procedures.iterator(); it.hasNext() && count > 0; count--) {
				Procedure cur = it.next();
				if (count == 1) {
					new ServiceTask(uuid, index++, cur, result).run();
				} else {
					if (isBackground)
						bgExecutor.execute(new ServiceTask(uuid, index++, cur, result));
					else
						threadPool.execute(new ServiceTask(uuid, index++, cur, result));
				}
				it.remove();
			}
			try {
				result.await();
			} catch (InterruptedException e) {
				throw new HeuristicsException();
			}
			if (result.isFailed()) {
				throw result.getException();
			}
		}
		return result;
	}
}
