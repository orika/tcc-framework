package com.netease.backend.coordinator;

import java.util.List;

import com.netease.backend.coordinator.processor.ServiceTask;
import com.netease.backend.coordinator.recover.RecoverManager;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.Coordinator;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.TccCode;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class DefaultCoordinator implements Coordinator {
	
	private RecoverManager recoverManager = null;
	private TxManager txManager = null;

	public long begin(int sequenceId, List<Procedure> expireGroups) throws CoordinatorException {
		Transaction tx = null;
		tx = txManager.createTx(expireGroups);
		return tx.getUUID();
	}
	
	public short confirm(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures);
			return 0;
		} catch (HeuristicsException e) {
			return e.getCode();
		}
	}
	
	public short confirm(int sequenceId, final long uuid, long timeout, final List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures, timeout);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		}
	}

	@Override
	public short cancel(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		}
	}

	@Override
	public short cancel(int sequenceId, long uuid, long timeout, List<Procedure> procedures) 
			throws CoordinatorException {
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures, timeout);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		}
	}
	
	public void start() throws CoordinatorException {
		recoverManager.init();
		com.alibaba.dubbo.container.Main.main(new String[0]);
	}
}