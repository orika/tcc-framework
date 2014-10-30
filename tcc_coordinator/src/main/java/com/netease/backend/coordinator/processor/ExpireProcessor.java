package com.netease.backend.coordinator.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.task.TxRetryWatcher;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.common.IllegalActionException;

public class ExpireProcessor {
	
	private static final Logger logger = Logger.getLogger("ExpireProcessor");
	private RetryProcessor processor = null;
	private RetryWatcher watcher = new RetryWatcher();
	private Set<Transaction> filterSet = new HashSet<Transaction>();
	
	public ExpireProcessor(RetryProcessor processor) {
		this.processor = processor;
	}
	
	public void process(Transaction tx) {
		if (filterSet.contains(tx))
			return;
		List<Procedure> procList = tx.getExpireList();
		if (procList == null)
			return;
		for (Procedure proc : procList) {
			if (proc.getMethod() == null)
				ServiceTask.setExpiredSig(proc);
		}
		try {
			tx.expire();
		} catch (IllegalActionException e) {
			return;
		}
		logger.info("expire " + tx);
		filterSet.add(tx);
		processor.process(tx, 1, watcher);
	}
	
	private class RetryWatcher implements TxRetryWatcher {
		
		@Override
		public void processError(Transaction tx) {
			filterSet.remove(tx);
		}

		@Override
		public void processSuccess(Transaction tx) {
			filterSet.remove(tx);
		}
	}
	
	public Set<Transaction> getExpiringTxSet() {
		return Collections.unmodifiableSet(filterSet);
	}
}
