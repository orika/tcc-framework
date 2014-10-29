package com.netease.backend.coordinator.task;

import java.util.List;

import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.rpc.RpcException;
import com.netease.backend.coordinator.ParticipantProxy;
import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.coordinator.ServiceNotFoundException;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.error.HeuristicsType;
import com.netease.backend.tcc.error.ParticipantException;

public class ServiceTask implements Runnable {
	
	private static final String CONFIRM = "1";
	private static final String CANCEL = "2";
	private static final String EXPIRED = "3";
	
	public static void setConfirmSig(Procedure proc) {
		proc.setMethod(CONFIRM);
	}
	
	public static void setCancelSig(Procedure proc) {
		proc.setMethod(CANCEL);
	}
	
	public static void setExpiredSig(Procedure proc) {
		proc.setMethod(EXPIRED);
	}
	
	private Procedure proc;
	private TxResult result;
	private long uuid;
	private int seq;
	private ServiceContext serviceContext;
	
	public ServiceTask(long uuid, int seq, Procedure proc, 
			TxResult result, ServiceContext serviceContext) {
		this.uuid = uuid;
		this.seq = seq;
		this.proc = proc;
		this.result = result;
		this.serviceContext = serviceContext;
	}
	
	@Override
	public void run() {
		result.setThread(seq, Thread.currentThread());
		try {
			ParticipantProxy participant = serviceContext.getService(proc.getService(), proc.getVersion());
			List<Object> params = proc.getParameters();
			String method = proc.getMethod();
			if (method.equals(CONFIRM))
				participant.confirm(uuid);
			else if (method.equals(CANCEL))
				participant.cancel(uuid);
			else if (method.equals(EXPIRED))
				participant.expired(uuid);
			else
				participant.invoke(method, params.toArray());
			result.success(seq);
		} catch (ServiceNotFoundException e) {
			result.failed(seq, HeuristicsType.SERVICE_NOT_FOUND, proc, null);
		} catch (ParticipantException e) {
			result.failed(seq, e.getErrorCode(), proc, e.getMessage());
		} catch (InterruptedException e) {
			result.failed(seq, HeuristicsType.TIMEOUT, proc, "Interrupted");
		} catch (RpcException e) {
			if (e.getCause() instanceof TimeoutException)
				result.failed(seq, HeuristicsType.TIMEOUT, proc, "Interrupted");
			result.failed(seq, HeuristicsType.UNDEFINED, proc, e.getMessage());
		} catch (RuntimeException e) {
			result.failed(seq, HeuristicsType.UNDEFINED, proc, e.getMessage());
		}
	}
}

