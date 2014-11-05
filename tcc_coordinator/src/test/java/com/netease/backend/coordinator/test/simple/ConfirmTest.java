package com.netease.backend.coordinator.test.simple;

import org.junit.Test;

import com.netease.backend.coordinator.test.TestBase;
import com.netease.backend.coordinator.test.container.ServiceContainer;
import com.netease.backend.tcc.error.CoordinatorException;

public class ConfirmTest extends TestBase {
	
	private static final String BEAN_ID = "SimpleTest";
	private ServiceContainer service1 = null;
	private ServiceContainer service2 = null;

	@Override
	protected void initServices() {
		service1 = containers.getService(0, new SimpleService());
		service2 = containers.getService(1, new SimpleService());
	}

	@Override
	protected void tryDo() throws Exception {
		service1.tryDo();
		service2.tryDo();
	}

	@Override
	protected void assertResult(Object expected) {
		assertEquals(expected, service1.getResult());
		assertEquals(expected, service2.getResult());
	}
	
//	@Test
	public void testConfirm() {
		try {
			tx = tccManager.beginTransaction(BEAN_ID);
			normalProc();
			assertResult(1);
		} catch (CoordinatorException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testExpire() {
		try {
			tx = tccManager.beginTransaction(BEAN_ID);
			expireProc(5000);
			assertResult(3);
		} catch (CoordinatorException e) {
			e.printStackTrace();
		}
	}
}
