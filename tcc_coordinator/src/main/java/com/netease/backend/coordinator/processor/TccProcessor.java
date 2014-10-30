package com.netease.backend.coordinator.processor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.task.TxResult;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsException;

public class TccProcessor {
	
	private ExecutorService foreExecutor = null;
	private ExecutorService bgExecutor = null;
	private ServiceContext context = null;
	
	public TccProcessor(ExecutorService foreExecutor, ExecutorService bgExecutor, 
			ServiceContext context) {
		this.foreExecutor = foreExecutor;
		this.bgExecutor = bgExecutor;
		this.context = context;
	}
	
	public void perform(long uuid, List<Procedure> procedures, boolean isBg) 
			throws HeuristicsException {
		preCheck(procedures);
		for (int start = 0, size = procedures.size(), count = 0; start < size; start += count) {
			count = getNextRangeCount(start, procedures);
			int lastIndex = count - 1;
			TxResult result = new TxResult(uuid, count);
			for (int i = 0; i < lastIndex; i++) {
				Procedure proc = procedures.get(i + start);
				if (isBg)
					bgExecutor.execute(new ServiceTask(uuid, i, proc, result, context));
				else
					foreExecutor.execute(new ServiceTask(uuid, i, proc, result, context));
			}
			new ServiceTask(uuid, lastIndex, procedures.get(lastIndex + start), result, context).run();
			try {
				result.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new HeuristicsException();
			}
			if (result.isFailed()) {
				throw result.getException();
			}
		}
	}
	
	public void perform(long uuid, List<Procedure> procedures, long timeout, boolean isBg) 
			throws HeuristicsException {
		preCheck(procedures);
		for (int start = 0, size = procedures.size(), count = 0; start < size; start += count) {
			count = getNextRangeCount(start, procedures);
			TxResult result = new TxResult(uuid, count);
			for (int i = 0; i < count; i++) {
				Procedure proc = procedures.get(i + start);
				if (isBg)
					bgExecutor.execute(new ServiceTask(uuid, i, proc, result, context));
				else
					foreExecutor.execute(new ServiceTask(uuid, i, proc, result, context));
			}
			long now = System.currentTimeMillis();
			try {
				result.interruptAfter(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new HeuristicsException();
			}
			if (result.isFailed()) {
				throw result.getException();
			}
			timeout -= System.currentTimeMillis() - now;
		}
	}
	
	private void preCheck(List<Procedure> procList) {
		Collections.sort(procList);
		Iterator<Procedure> itr = procList.iterator();
		while (itr.hasNext()) {
			Procedure cur = itr.next();
			if (cur.getSequence() < 0) {
				itr.remove();
				continue;
			} else
				break;
		}
	}
	
	private int getNextRangeCount(int start, List<Procedure> procList) {
		Procedure lastOne = null;
		int count = 0;
		for (int i = start, s = procList.size(); i < s; i++) {
			Procedure cur = procList.get(i);
			if (lastOne == null || lastOne.getSequence() == cur.getSequence()) {
				count++;
				lastOne = cur;
			} else  {
				break;
			}
		}
		return count;
	}
}
