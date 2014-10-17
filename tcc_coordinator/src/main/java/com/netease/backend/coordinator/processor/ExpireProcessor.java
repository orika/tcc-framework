package com.netease.backend.coordinator.processor;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.task.TxLogWatcher;
import com.netease.backend.coordinator.transaction.IllegalActionException;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxTable;
import com.netease.backend.tcc.Procedure;

public class ExpireProcessor {
	
	private static final Logger logger = Logger.getLogger("ExpireProcessor");
	private RetryProcessor processor = null;
	private TxTable txTable = null;
	private LogWatcher logWatcher = new LogWatcher();
	
	public ExpireProcessor(RetryProcessor processor) {
		this.processor = processor;
	}
	
	public void setTxTable(TxTable txTable) {
		this.txTable = txTable;
	}

	public void process(Transaction tx) {
		List<Procedure> procList = tx.getExpireList();
		if (procList == null)
			return;
		for (Procedure proc : procList) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.EXPIRED);
		}
		try {
			tx.expire();
		} catch (IllegalActionException e) {
			return;
		}
		logger.info("expire " + tx);
		processor.process(tx, 1, logWatcher);
	}
	
	private class LogWatcher implements TxLogWatcher {
		
		@Override
		public void processError(Transaction tx) {
			txTable.put(tx);
		}
	}
}
