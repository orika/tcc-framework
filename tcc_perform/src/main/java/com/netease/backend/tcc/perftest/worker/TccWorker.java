package com.netease.backend.tcc.perftest.worker;

import java.util.Random;

import org.springframework.beans.BeansException;

import com.netease.backend.tcc.Transaction;
import com.netease.backend.tcc.error.CoordinatorException;
import com.netease.backend.tcc.perftest.TestContainer;

public class TccWorker extends Worker {
	
	public TccWorker(int index, TestContainer container) {
		super(index, container);
	}

	@Override
	public void run() {
		Random random = new Random();
	    while (!stop && !Thread.interrupted()) {
	    	Transaction tx = null;
			try {
				tx = tccManager.beginTransaction("shopping");
			} catch (BeansException e) {
				e.printStackTrace();
			} catch (CoordinatorException e) {
				e.printStackTrace();
			}
			try {
				payment.tryDo();
				sale.tryDo();
				if (random.nextInt(10) < 5) {
					throw new RuntimeException();
				}
			} catch (Throwable t) {
				try {
					tx.cancel();
				} catch (CoordinatorException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				tx.confirm();
			} catch (Throwable t) {
				t.printStackTrace();
			}
			count++;
	    }
	}
}
