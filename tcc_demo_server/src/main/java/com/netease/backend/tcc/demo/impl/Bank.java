package com.netease.backend.tcc.demo.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {
	
	private static final Map<String, Customer> bank = new ConcurrentHashMap<String, Customer>();
	
	static {
		bank.put("james", new Customer("james", 1000000));
		bank.put("kevin", new Customer("kevin", 500000));
	}

	public Customer getCustomer(String person) {
		return bank.get(person);
	}

	protected static class Customer {
		
		private String name;
		private int account;
		private int reserve;
		private Map<Long, Integer> reserveTable = new HashMap<Long, Integer>();
		
		public Customer(String name, int account) {
			this.name = name;
			this.account = account;
			this.reserve = 0;
		}

		public int getAccount() {
			return account;
		}

		public void setAccount(int account) {
			this.account = account;
		}

		public int getReserve() {
			return reserve;
		}

		public void setReserve(int reserve) {
			this.reserve = reserve;
		}

		public int getMoney(String persion) {
			return account;
		}

		public synchronized void reserve(long uuid, int money) {
			if (money > account - reserve)
				throw new RuntimeException(name + " has not enough money");
			if (reserveTable.containsKey(uuid))
				throw new RuntimeException("duplicate uuid for transaction!");
			reserve += money;
			reserveTable.put(uuid, money);
			LogUtil.log(name + " reserve " + money);
		}

		public synchronized void pay(long uuid) {
			Integer money = reserveTable.remove(uuid);
			if (money == null) {
				LogUtil.log(name + "try confirm " + uuid + " paying " + money);
				return;
			}
			reserve -= money;
			account -= money;
			LogUtil.log(name + " confirm " + uuid + " paying " + money);
		}
		
		public synchronized void reserveBack(long uuid) {
			Integer money = reserveTable.remove(uuid);
			if (money == null) {
				LogUtil.log(name + "try cancel or expire " + uuid + " paying " + money);
				return;
			}
			reserve -= money;
			account += money;
		}
	}
}