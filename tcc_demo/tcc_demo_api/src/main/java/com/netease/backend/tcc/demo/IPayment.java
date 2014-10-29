package com.netease.backend.tcc.demo;

import com.netease.backend.tcc.Participant;


public interface IPayment extends Participant {
	
	void reserve(Long uuid, String persion, int money);
	
	void cancel(Long uuid, String person);

	void pay(Long uuid, String persion);
	
	boolean isConfirmed(Long uuid);
}
