package com.netease.backend.coordinator.test;

import junit.framework.TestCase;

import com.netease.backend.coordinator.ServiceContext;
import com.netease.backend.coordinator.test.container.Containers;
import com.netease.backend.tcc.TccManager;
import com.netease.backend.tcc.Transaction;
import com.netease.backend.tcc.error.CoordinatorException;

public abstract class TestBase extends TestCase {
	
	protected Containers containers = null;
	protected Transaction tx = null;
	protected TccManager tccManager = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CoordinatorInstance.touch();
		this.tccManager = (TccManager) ServiceContext.getBean("tccManager");
		this.containers = (Containers) ServiceContext.getBean("containers");
		initServices();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		containers.clear();
	}
	
	protected abstract void initServices();
	
	protected abstract void tryDo() throws Exception;
	
	protected abstract void assertResult(Object expected);
	
	public void normalProc() throws CoordinatorException {
		try {
			tryDo();
		} catch (Exception e) {
			tx.cancel();
			return;
		}
		tx.confirm();
	}
	
	public void expireProc(long timeout) throws CoordinatorException {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
