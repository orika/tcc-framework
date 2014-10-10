package com.netease.backend.coordinator;

import java.util.List;

import com.netease.backend.coordinator.processor.ServiceTask;
import com.netease.backend.coordinator.recover.RecoverManager;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.Coordinator;
import com.netease.backend.tcc.CoordinatorException;
import com.netease.backend.tcc.Procedure;

public class DefaultCoordinator implements Coordinator {
	
	private RecoverManager recoverManager = null;
	private TxManager txManager = null;

	public long begin(int sequenceId, List<Procedure> expireGroups) throws CoordinatorException {
		Transaction tx = null;
		try {
			tx = txManager.createTx(expireGroups);
		} catch (Exception e) {
			throw new CoordinatorException(e);
		}
		return tx.getUUID();
	}
	
	public void confirm(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures);
		} catch (Exception e) {
			throw new CoordinatorException(e);
		}
	}
	
	public void confirm(int sequenceId, final long uuid, long timeout, final List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures, timeout);
		} catch (Exception e) {
			throw new CoordinatorException(e);
		}
	}

	@Override
	public void cancel(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures);
		} catch (Exception e) {
			throw new CoordinatorException(e);
		}
	}

	@Override
	public void cancel(int sequenceId, long uuid, long timeout, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures, timeout);
		} catch (Exception e) {
			throw new CoordinatorException(e);
		}
	}
	
	public void start() throws CoordinatorException {
		recoverManager.init();
		if (!recoverManager.isAvailable())
			throw new CoordinatorException("this coordinator is not available yet");
		com.alibaba.dubbo.container.Main.main(new String[0]);
	}
}