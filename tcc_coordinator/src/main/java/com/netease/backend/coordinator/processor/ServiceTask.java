package com.netease.backend.coordinator.processor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.Procedure;
import com.netease.backend.tcc.TccCode;
import com.netease.backend.tcc.error.HeuristicsException;
import com.netease.backend.tcc.error.ParticipantException;

public class ServiceTask implements Runnable {
	
	public static final String CONFIRM = "confirm";
	public static final String CANCEL = "cancel";
	public static final String EXPIRED = "expired";
	
	private Procedure proc;
	private TaskResult result;
	private long uuid;
	private int index;
	
	public ServiceTask(long uuid, int index, Procedure proc, TaskResult result) {
		this.uuid = uuid;
		this.index = index;
		this.proc = proc;
		this.result = result;
	}
	
	@Override
	public void run() {
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
		} catch (ClassNotFoundException e) {
			result.failed(new HeuristicsException(TccCode.getServiceDownCode(index), proc));
		} catch (ParticipantException e) {
			result.failed(new HeuristicsException(e.getErrorCode(), proc));
		} 
		result.success();
	}
	
	protected static class TaskResult {
		private CountDownLatch countDown;
		private HeuristicsException exception;
		
		public TaskResult(CountDownLatch countDown) {
			this.countDown = countDown;
		}
		
		public void success() {
			countDown.countDown();
		}
		
		public void failed(HeuristicsException exception) {
			this.exception = exception;
			countDown.countDown();
		}
		
		public boolean isFailed() {
			return exception != null;
		}
		
		public HeuristicsException getException() {
			return exception;
		}
		
		public void setException(HeuristicsException exception) {
			this.exception = exception;
		}
		
		public void await() throws InterruptedException {
			countDown.await();
		}
	}

}

