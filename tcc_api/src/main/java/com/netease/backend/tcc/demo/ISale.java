package com.netease.backend.tcc.demo;

import com.netease.backend.tcc.Participant;


public interface ISale extends Participant {

	int getPrice();
	
	int getItemCount();
	
	void sell(int count);
	
	void refond(int count);
	
	void reserve(long uuid, int count);
	
	public void confirm(long uuid);

	public void cancel(long uuid);
	
	boolean isConfirmed(long uuid);
}
