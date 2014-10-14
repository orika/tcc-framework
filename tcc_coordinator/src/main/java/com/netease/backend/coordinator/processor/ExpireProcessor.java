package com.netease.backend.coordinator.processor;

import java.util.List;

import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.Procedure;

public class ExpireProcessor {
	
	private RetryProcessor processor = null;
	
	public void process(Transaction tx) {
		List<Procedure> procList = tx.getExpireList();
		if (procList == null)
			return;
		for (Procedure proc : procList) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.EXPIRED);
		}
		processor.process(tx, 1);
	}
}
