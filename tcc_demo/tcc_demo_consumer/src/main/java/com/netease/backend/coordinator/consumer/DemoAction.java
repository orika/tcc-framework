package com.netease.backend.coordinator.consumer;

import java.util.Random;

import com.netease.backend.tcc.TccManager;
import com.netease.backend.tcc.Transaction;
import com.netease.backend.tcc.demo.IPayment;
import com.netease.backend.tcc.demo.ISale;

public class DemoAction {
    
    private TccManager tccManager;
    private IPayment payment;
    private ISale sale;
//    private static final String VERSION = "1.0.0";
    
    private static final String PERSON = "kevin";
    
    public void setTccManager(TccManager tccManager) {
        this.tccManager = tccManager;
    }
    
    public void setPayment(IPayment payment) {
		this.payment = payment;
	}

	public void setSale(ISale sale) {
		this.sale = sale;
	}

	public void start() throws Exception {
		Random random = new Random();
		int price = sale.getPrice();
		int onSale = sale.getItemCount();
	    for (int i = 0; i < Integer.MAX_VALUE && onSale > 0; i++, onSale = sale.getItemCount()) {
	    	Thread.sleep(2000);
	    	int buyCount = random.nextInt(10) + 1;
	    	int money = price * buyCount;
	    	Transaction tx = tccManager.beginTransaction("shopping");
	    	long uuid = tx.getUUID();
			try {
				payment.reserve(uuid, PERSON, money);
				sale.reserve(uuid, buyCount);
//				if (random.nextInt(10) < 2) 
//					throw new RuntimeException("unknown Exception");
//				if (random.nextInt(10) < 5) {
//					System.out.println("canceling " + uuid);
//					tx.cancel();
//					continue;
//				}
			} catch (Throwable t) {
				System.out.println("waiting expiring " + uuid);
				continue;
			}
			try {
				System.out.println("confirming " + uuid);
				tx.confirm();
			} catch (Throwable t) {
				t.printStackTrace();
			}
	    }
	}
}


