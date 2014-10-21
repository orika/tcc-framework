package com.netease.backend.tcc.demo;

import com.netease.backend.tcc.Participant;


public interface IOrder extends Participant {

	void available(String person, int count);
}
