package com.netease.backend.tcc.demo.impl;

import java.util.HashMap;
import java.util.Map;

import com.netease.backend.tcc.DefaultParticipant;
import com.netease.backend.tcc.demo.IPayment;
import com.netease.backend.tcc.demo.impl.Bank.Customer;
import com.netease.backend.tcc.error.ParticipantException;

public class Payment extends DefaultParticipant implements IPayment {

	private Bank bank = new Bank();
	
	private Map<Long, String> cache = new HashMap<Long, String>();
	
	public Payment() {
		try {
			initMethods("com.netease.backend.tcc.demo.IPayment");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void pay(Long uuid, String person) {
		Customer customer = bank.getCustomer(person);
		if (customer == null)
			throw new RuntimeException(person + " is not in the bank");
		customer.pay(uuid);
	}
	
	public void reserve(Long uuid, String person, int money) {
		Customer customer = bank.getCustomer(person);
		if (customer == null)
			throw new RuntimeException(person + " is not in the bank");
		customer.reserve(uuid, money);
		cache.put(uuid, person);
	}

	public void cancel(Long uuid, String person) {
		if (person == null)
			return;
		Customer customer = bank.getCustomer(person);
		if (customer == null)
			throw new RuntimeException(person + " is not in the bank");
		LogUtil.log(person + " cancel " + uuid + " paying");
		customer.reserveBack(uuid);
	}
	
	public void expire(Long uuid, String person) {
		if (person == null)
			return;
		Customer customer = bank.getCustomer(person);
		if (customer == null)
			throw new RuntimeException(person + " is not in the bank");
		LogUtil.log(person + " expire " + uuid + " paying");
		customer.reserveBack(uuid);
	}

	@Override
	public void cancel(Long uuid) throws ParticipantException {
		cancel(uuid, cache.get(uuid));
		cache.remove(uuid);
	}

	@Override
	public void confirm(Long uuid) throws ParticipantException {
		pay(uuid, cache.get(uuid));
		cache.remove(uuid);
	}

	@Override
	public void expired(Long uuid) throws ParticipantException {
		expire(uuid, cache.get(uuid));
		cache.remove(uuid);
	}

	@Override
	public boolean isConfirmed(Long uuid) {
		return !cache.containsKey(uuid);
	}
}
