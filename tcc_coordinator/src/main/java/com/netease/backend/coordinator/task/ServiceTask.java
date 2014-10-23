package com.netease.backend.coordinator.task;

import java.util.List;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsType;
import com.netease.backend.tcc.error.ParticipantException;

public class ServiceTask implements Runnable {
	
	public static final String CONFIRM = "confirm";
	public static final String CANCEL = "cancel";
	public static final String EXPIRED = "expired";
	
	private Procedure proc;
	private TxResult result;
	private long uuid;
	private int index;
	
	public ServiceTask(long uuid, int index, Procedure proc, TxResult result) {
		this.uuid = uuid;
		this.index = index;
		this.proc = proc;
		this.result = result;
	}
	
	@Override
	public void run() {
		result.setThread(index, Thread.currentThread());
		try {
			Participant participant = ServiceContext.getService(proc.getService());
			List<Object> params = proc.getParameters();
			String method = proc.getMethod();
			if (method == CONFIRM)
				participant.confirm(uuid);
			else if (method == CANCEL)
				participant.cancel(uuid);
			else if (method == EXPIRED)
				participant.expired(uuid);
			else
				participant.invoke(method, params.toArray());
			result.success(index);
		} catch (ClassNotFoundException e) {
			result.failed(index, HeuristicsType.SERVICE_NOT_FOUND, proc);
		} catch (ParticipantException e) {
			result.failed(index, e.getErrorCode(), proc);
		} catch (InterruptedException e) {
			result.failed(index, HeuristicsType.TIMEOUT, proc);
		} catch (RuntimeException e) {
			result.failed(index, HeuristicsType.UNDEFINED, proc);
		}
	}
}

