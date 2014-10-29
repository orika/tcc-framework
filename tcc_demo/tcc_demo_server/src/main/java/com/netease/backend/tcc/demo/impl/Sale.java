package com.netease.backend.tcc.demo.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.netease.backend.tcc.DefaultParticipant;
import com.netease.backend.tcc.demo.ISale;
import com.netease.backend.tcc.error.ParticipantException;

public class Sale extends DefaultParticipant implements ISale {
	
	private static final int SUM = 100000;
	private AtomicInteger itemCount = new AtomicInteger(SUM);
	private AtomicLong income = new AtomicLong(0);
	private AtomicInteger reserve = new AtomicInteger(0);
	private int price = 88;
	private Map<Long, Integer> reserveTable = new HashMap<Long, Integer>();
	
	public Sale() {
		try {
			initMethods("com.netease.backend.tcc.demo.ISale");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getPrice() {
		return price;
	}
	
	@Override
	public int getItemCount() {
		return itemCount.get() - reserve.get();
	}

	public void sell(Long uuid, int count) {
		for (;;) {
			int c = itemCount.get();
			if (itemCount.compareAndSet(c, c- count))
				break;
		}
		long  in = price * count;
		for (;;) {
			Long inco = income.get();
			if (income.compareAndSet(inco, inco + in))
				break;
		}
		LogUtil.log("store confirm " + uuid + " selling " + count + " items");
	}

	public void refond(Long uuid, int count) {
		synchronized (reserve) {
			if (reserve.get() + count > SUM)
				throw new RuntimeException("can not refond " + count + " items, now reverve " + reserve.get());
			reserve.set(reserve.get() + count);
		}
	}

	public void reserve(Long uuid, int count) {
		synchronized (reserve) {
			int left = itemCount.get() - reserve.get();
			if (count > left)
				throw new RuntimeException("can not reserve " + count + " items, left is " + left);
			reserve.set(reserve.get() - count);
		}
		reserveTable.put(uuid, count);
		LogUtil.log("store reserves " + count + " items");
	}

	public void confirm(Long uuid) {
		Integer count = reserveTable.remove(uuid);
		if (count == null) {
			LogUtil.log("store try confirm " + uuid + " selling " + count + " items");
			return;
		}
		sell(uuid, count);
		reserveTable.remove(uuid);
	}

	public void cancel(Long uuid) {
		Integer count = reserveTable.remove(uuid);
		if (count == null) {
			LogUtil.log("store try cancel " + uuid + " selling " + count + " items");
			return;
		}
		LogUtil.log("store cancel " + uuid + " selling " + count + " items");
		refond(uuid, count);
		reserveTable.remove(uuid);
	}


	@Override
	public void expired(Long uuid) throws ParticipantException {
		Integer count = reserveTable.remove(uuid);
		if (count == null) {
			LogUtil.log("store try expire " + uuid + " selling " + count + " items");
			return;
		}
		LogUtil.log("store expire " + uuid + " selling " + count + " items");
		refond(uuid, count);
		reserveTable.remove(uuid);
	}
	
	@Override
	public boolean isConfirmed(Long uuid) {
		return !reserveTable.containsKey(uuid);
	}
}
