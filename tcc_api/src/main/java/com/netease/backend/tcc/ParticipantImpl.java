package com.netease.backend.tcc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netease.backend.tcc.error.ParticipantException;


public abstract class ParticipantImpl implements Participant {
	
	private MethodPool mpool = new MethodPool();
	private static final Map<Class<?>, Class<?>> TYPE_CONVERT = new HashMap<Class<?>, Class<?>>();
	static {
		TYPE_CONVERT.put(long.class, Long.class);
		TYPE_CONVERT.put(int.class, Integer.class);
		TYPE_CONVERT.put(float.class, Float.class);
		TYPE_CONVERT.put(double.class, Double.class);
		TYPE_CONVERT.put(short.class, Short.class);
		TYPE_CONVERT.put(boolean.class, Boolean.class);
		TYPE_CONVERT.put(char.class, Character.class);
		TYPE_CONVERT.put(byte.class, Byte.class);
	}
	
	public void invoke(String methodName, Object[] args) throws ParticipantException {
		Method method = mpool.getMethod(methodName, args);
		try {
			method.invoke(this, args);
		} catch (Exception e) {
			throw new ParticipantException(e);
		}
	}
	
	//init mark for spring
	public void initMethods(String typeName) throws ClassNotFoundException {
		Class<?> clz = null;
		for (Class<?> clazz : this.getClass().getInterfaces()) {
			if (clazz.getName().equals(typeName))
				clz = clazz;
		}
		if (clz == null)
			throw new ClassNotFoundException(typeName + " is not found");
		for (Method method : clz.getMethods()) {
			mpool.register(method);;
		}
	}
	
	private class MethodPool {
		
		private Map<String, Entry> methodMap = new HashMap<String, Entry>();
		
		public void register(Method method) {
			String name = method.getName();
			Entry entry = methodMap.get(name);
			if (entry == null) {
				entry = new Entry(method);
				methodMap.put(name, entry);
			} else
				entry.addMethod(method);
		}
		
		public Method getMethod(String name, Object[] params) throws ParticipantException {
			Entry entry = methodMap.get(name);
			if (entry != null) {
				Method method = entry.getMethod(params);
				if (method != null)
					return method;
			}
			throw new ParticipantException("Method:" + name + " is not found");
		}
	}
	
	private class Entry {
		
		private Method first;
		private List<Method> others;
		
		public Entry(Method method) {
			this.first = method;
		}
		
		public void addMethod(Method method) {
			if (others == null)
				others = new ArrayList<Method>();
			others.add(method);
		}
;		
		public Method getMethod(Object[] params) {
			if (others == null)
				return first;
			if (match(first, params))
				return first;
			for (Method method : others) {
				if (match(method, params))
					return method;
			}
			return null;
		}
		
		private boolean match(Method method, Object[] params) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if (params.length != paramTypes.length)
				return false;
			boolean isMatched = true;
			for (int i = 0; i < params.length; i++) {
				Class<?> clz = params[i].getClass();
				if (!clz.isAssignableFrom(paramTypes[i])) {
					if (paramTypes[i].isPrimitive() && 
							clz.isAssignableFrom(TYPE_CONVERT.get(paramTypes[i]))) {
						continue;
					}
					isMatched = false;
					break;
				}
			}
			return isMatched;
		}
	}
}