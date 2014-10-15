package com.netease.backend.coordinator.processor;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.Procedure;

public class ExpireProcessor {
	
	private static final Logger logger = Logger.getLogger("ExpireProcessor");
	private RetryProcessor processor = null;
	
	public ExpireProcessor(RetryProcessor processor) {
		this.processor = processor;
	}

	public void process(Transaction tx) {
		List<Procedure> procList = tx.getExpireList();
		if (procList == null)
			return;
		for (Procedure proc : procList) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.EXPIRED);
		}
		logger.info("expire " + tx);
		processor.process(tx, 1);
	}
}
