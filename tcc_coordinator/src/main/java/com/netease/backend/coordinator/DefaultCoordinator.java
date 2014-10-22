package com.netease.backend.coordinator;

import java.util.List;

import org.apache.log4j.Logger;

import com.netease.backend.coordinator.log.LogException;
import com.netease.backend.coordinator.task.ServiceTask;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.IllegalActionException;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.coordinator.transaction.TxManager;
import com.netease.backend.tcc.Coordinator;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.TccCode;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.error.HeuristicsException;

public class DefaultCoordinator implements Coordinator {
	
	private static final Logger logger = Logger.getLogger("Coordinator");
	private TxManager txManager = null;
	
	public DefaultCoordinator(TccContainer container) {
		container.start();
		this.txManager = container.getTxManager();
	}

	public long begin(int sequenceId, List<Procedure> expireGroups) throws CoordinatorException {
		try {
			Transaction tx = null;
			tx = txManager.createTx(expireGroups);
			return tx.getUUID();
		} catch (Exception e) {
			logger.error("transaction register error", e);
			throw new CoordinatorException(e.getMessage(), e);
		}
	}
	
	public short confirm(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		logger.debug("process:" + procedures);
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures);
			return 0;
		} catch (HeuristicsException e) {
			return e.getCode();
		} catch (IllegalActionException e) {
			logger.error("transaction " + uuid + " confirm error.", e);
			throw new CoordinatorException(e);
		} catch (LogException e) {
			logger.error("transaction " + uuid + " confirm error.", e);
			throw new CoordinatorException(e);
		}
	} 
	
	public short confirm(int sequenceId, final long uuid, long timeout, final List<Procedure> procedures) 
			throws CoordinatorException {
		logger.debug("process:" + procedures);
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CONFIRM);
		}
		try {
			txManager.perform(uuid, Action.CONFIRM, procedures, timeout);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		}  catch (IllegalActionException e) {
			logger.error("transaction " + uuid + " confirm error.", e);
			throw new CoordinatorException(e);
		} catch (LogException e) {
			logger.error("transaction " + uuid + " confirm error.", e);
			throw new CoordinatorException(e);
		}
	}

	@Override
	public short cancel(int sequenceId, long uuid, List<Procedure> procedures) 
			throws CoordinatorException {
		logger.debug("process:" + procedures);
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		} catch (IllegalActionException e) {
			logger.error("transaction " + uuid + " cancel error.", e);
			throw new CoordinatorException(e);
		} catch (LogException e) {
			logger.error("transaction " + uuid + " cancel error.", e);
			throw new CoordinatorException(e);
		}
	}

	@Override
	public short cancel(int sequenceId, long uuid, long timeout, List<Procedure> procedures) 
			throws CoordinatorException {
		logger.debug("process:" + procedures);
		for (Procedure proc : procedures) {
			if (proc.getMethod() == null)
				proc.setMethod(ServiceTask.CANCEL);
		}
		try {
			txManager.perform(uuid, Action.CANCEL, procedures, timeout);
			return TccCode.OK;
		} catch (HeuristicsException e) {
			return e.getCode();
		} catch (IllegalActionException e) {
			logger.error("transaction " + uuid + " cancel error.", e);
			throw new CoordinatorException(e);
		} catch (LogException e) {
			logger.error("transaction " + uuid + " cancel error.", e);
			throw new CoordinatorException(e);
		}
	}

	@Override
	public int getTxTableSize() {
		return txManager.getTxTable().getSize();
	}
}