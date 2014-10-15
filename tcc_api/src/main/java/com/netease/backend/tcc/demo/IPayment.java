package com.netease.backend.tcc.demo;

import com.netease.backend.tcc.Participant;


public interface IPayment extends Participant {
	
	void reserve(long uuid, String persion, int money);
	
	void cancel(long uuid, String person);

	void pay(long uuid, String persion);
}
