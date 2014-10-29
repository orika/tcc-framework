package com.netease.backend.tcc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netease.backend.tcc.error.CoordinatorException;

public class TccManager implements ApplicationContextAware {
	
	private Coordinator coordinator;
	private Map<Participant, String> p2Service = new HashMap<Participant, String>();
	private Map<String, Participant> s2Participant = new HashMap<String, Participant>();
	private Map<Class<?>, String> typeToService = new HashMap<Class<?>, String>();
	private Map<TccActivity, CertainTx> txPool = new ConcurrentHashMap<TccActivity, CertainTx>(); 
	private static ApplicationContext applicationContext = null;
	
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		TccManager.applicationContext = applicationContext;
	}
	
	private void init() {
		Class<?> pClass = null;
		try {
			pClass = Class.forName("com.netease.backend.tcc.Participant");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		for (String name : applicationContext.getBeanNamesForType(pClass)) {
			Object bean = applicationContext.getBean(name);
			Class<?>[] cls = bean.getClass().getInterfaces();
			for (int i = cls.length - 1; i >= 0; i--) {
				if (pClass.isAssignableFrom(cls[i])) {
					String serviceName = cls[i].getName();
					p2Service.put((Participant) bean, serviceName);
					s2Participant.put(serviceName, (Participant) bean);
					typeToService.put(cls[i], serviceName);
//					System.out.println("register service:" + serviceName);
					break;
				}
			}
		}
	}
	
	public void register(TccActivity activity) {
		txPool.put(activity, new CertainTx(activity, coordinator));
	}
	
	public void setCoordinator(Coordinator coordinator) {
		this.coordinator = coordinator;
	}
	
	public Transaction beginTransaction(String beanId) throws BeansException, CoordinatorException {
		ParticipantGroup pg = (ParticipantGroup) applicationContext.getBean(beanId);
		return beginTransaction(pg);
	}
	
	public Transaction beginTransaction(ParticipantGroup group) throws CoordinatorException {
		Transaction tx = null;
		if (group.isCustomed()) {
			tx = new CustomedTx(group, coordinator);
		} else {
			CertainTx cache = txPool.get(group);
			if (cache == null)
				throw new CoordinatorException("use TccActivity after register it in TccManager!");
			try {
				tx = cache.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		tx.begin();
		return tx;
	}
	
	public String getServiceName(Participant pt) {
		String name = p2Service.get(pt);
		if (name == null) {
			if (p2Service.size() == 0) {
				synchronized (this) {
					init();
				}
			} else
				throw new IllegalArgumentException("participant not found in TccManager, check if TccManager is in the top context!");
			return getServiceName(pt);
		} else
			return name;
	}
	
	public Participant getParticipant(String serviceName) {
		Participant pt = s2Participant.get(serviceName);
		if (pt == null) {
			if (p2Service.size() == 0) {
				synchronized (this) {
					if (p2Service.size() == 0) 
						init();
				}
			} else
				throw new IllegalArgumentException("serviceName not found in TccManager, check if TccManager is in the top context!");
			return getParticipant(serviceName);
		} else
			return pt;
	}
	
	public class CertainTx extends Transaction implements Cloneable {
		
		private List<Procedure> confirmList = new ArrayList<Procedure>();
		private List<Procedure> cancelList = new ArrayList<Procedure>();
		
		private CertainTx(TccActivity activity, Coordinator coordinator) {
			super(coordinator);
			this.expireList = new ArrayList<Procedure>();
			int[] confirmSeq = activity.getConfirmSeq();
			int[] cancelSeq = activity.getCancelSeq();
			int[] expireSeq = activity.getExpireSeq();
			Participant[] participants = activity.getParticipants();
			for (int i = 0; i < participants.length; i++) {
				Procedure proc = new Procedure();
				if (confirmSeq == ParticipantGroup.DEFAULT_SEQ)
					proc.setSequence(i);
				else
					proc.setSequence(confirmSeq[i]);
				proc.setService(getServiceName((participants[i])));
				confirmList.add(proc);
			}
			
			for (int i = 0; i < participants.length; i++) {
				Procedure proc = new Procedure();
				if (cancelSeq == ParticipantGroup.DEFAULT_SEQ)
					proc.setSequence(i);
				else
					proc.setSequence(cancelSeq[i]);
				proc.setService(getServiceName(participants[i]));
				cancelList.add(proc);
			}
			
			for (int i = 0; i < participants.length; i++) {
				Procedure proc = new Procedure();
				if (expireSeq == ParticipantGroup.DEFAULT_SEQ)
					proc.setSequence(i);
				else
					proc.setSequence(expireSeq[i]);
				proc.setService(getServiceName(participants[i]));
				expireList.add(proc);
			}
		}

		@Override
		public void setSequence(int level) {
			throw new UnsupportedOperationException("TccActivity's levels is certained, can not change");
		}

		@Override
		public <T> T getProxy(Class<T> serviceType) {
			throw new UnsupportedOperationException("TccActivity's procedure is certained, can not change");
		}

		@Override
		public void confirm() throws CoordinatorException {
			checkBegin();
			short code = coordinator.confirm(id, uuid, confirmList);
			checkResult(code, confirmList);
		}

		@Override
		public void cancel() throws CoordinatorException {
			checkBegin();
			short code = coordinator.cancel(id, uuid, cancelList);
			checkResult(code, cancelList);
		}

		@Override
		public void confirm(long timeout) throws CoordinatorException {
			checkBegin();
			short code = coordinator.confirm(id, uuid, timeout, confirmList);
			checkResult(code, confirmList, timeout);
		}

		@Override
		public void cancel(long timeout) throws CoordinatorException {
			checkBegin();
			short code = coordinator.cancel(id, uuid, timeout, cancelList);
			checkResult(code, cancelList, timeout);
		}
		
		@Override
		public CertainTx clone() throws CloneNotSupportedException {
			CertainTx tx = (CertainTx) super.clone();
			tx.id = idGenerator.incrementAndGet();
			tx.uuid = -1;
			return tx;
		}
	}
	
	public class CustomedTx extends Transaction {
		
		private List<Procedure> procList = new ArrayList<Procedure>();
		private Map<Class<?>, Object> proxyCache = new HashMap<Class<?>, Object>();

		protected CustomedTx(ParticipantGroup activity, Coordinator coordinator) {
			super(coordinator);
			activity.expired(this);
			this.expireList = procList;
			this.procList = new ArrayList<Procedure>();
		}

		@Override
		public void setSequence(int seq) {
			if (procList.isEmpty())
				throw new UnsupportedOperationException("the transaction has not any activity yet");
			procList.get(procList.size() - 1).setSequence(seq);
		}

		@Override
		public <T> T getProxy(Class<T> serviceType) {
			checkBegin();
			Object proxy = proxyCache.get(serviceType);
			if (proxy != null)
				return serviceType.cast(proxy);
			else {
				SericeProxy service = new SericeProxy(typeToService.get(serviceType));
				proxy = Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[]{serviceType}, service);
				proxyCache.put(serviceType, proxy);
				return serviceType.cast(proxy);
			}
		}

		@Override
		public void confirm() throws CoordinatorException {
			checkBegin();
			if (procList.isEmpty())
				return;
			try {
				short code = coordinator.confirm(id, uuid, procList);
				checkResult(code, procList);
			} finally {
				procList.clear();
			}
		}

		@Override
		public void confirm(long timeout) throws CoordinatorException {
			checkBegin();
			if (procList.isEmpty())
				return;
			try {
				short code = coordinator.confirm(id, uuid, timeout, procList);
				checkResult(code, procList, timeout);
			} finally {
				procList.clear();
			}
		}

		@Override
		public void cancel() throws CoordinatorException {
			checkBegin();
			if (procList.isEmpty())
				return;
			try {
				short code = coordinator.cancel(id, uuid, procList);
				checkResult(code, procList);
			} finally {
				procList.clear();
			}
		}

		@Override
		public void cancel(long timeout) throws CoordinatorException {
			checkBegin();
			if (procList.isEmpty())
				return;
			try {
				short code = coordinator.cancel(id, uuid, timeout, procList);
				checkResult(code, procList, timeout);
			} finally {
				procList.clear();
			}
		}
		
		private class SericeProxy implements InvocationHandler {
			
			private String name;
		      
		    public SericeProxy(String name) {
		    	this.name = name;
		    }  
			
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				Procedure proc = new Procedure(name);
				proc.setMethod(method.getName());
				proc.setParameters(Arrays.asList(args));
				procList.add(proc);
				return null;
			}
		}
	}
}
