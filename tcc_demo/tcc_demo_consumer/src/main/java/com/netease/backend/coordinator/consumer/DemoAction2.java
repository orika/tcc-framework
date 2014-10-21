package com.netease.backend.coordinator.consumer;

import java.util.Random;

import com.netease.backend.tcc.Participant;
import com.netease.backend.tcc.ParticipantGroup;
import com.netease.backend.tcc.TccManager;
import com.netease.backend.tcc.Transaction;
import com.netease.backend.tcc.demo.IOrder;
import com.netease.backend.tcc.demo.IPayment;
import com.netease.backend.tcc.demo.ISale;

public class DemoAction2 {
    
    private TccManager tccManager;
    private IPayment payment;
    private ISale sale;
    
    private static final String PAY = "payment";
    private static final String SALE = "sale";
    private static final String ORDER = "order";
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
	    	int buyCount = random.nextInt(10) + 1;
	    	int money = price * buyCount;
	    	Transaction tx = tccManager.beginTransaction(new ParticipantGroup() {
				@Override
				public Participant[] getParticipants() {
					return new Participant[]{payment, sale};
				}
			});
	    	long uuid = tx.getUUID();
			try {
				payment.reserve(uuid, PERSON, money);
				sale.reserve(uuid, buyCount);
				if (random.nextInt(10) < 2) 
					throw new RuntimeException("unknown Exception");
			} catch (Throwable t) {
				System.out.println("try     : " + " buy " + buyCount + " items failed cause " + t.getMessage());
				tx.getProxy(IPayment.class).cancel(uuid, PERSON);
	    		tx.getProxy(ISale.class).cancel(uuid);
				tx.cancel();
				continue;
			}
			try {
	    		tx.getProxy(IPayment.class).pay(uuid, PERSON);
	    		tx.setSequence(0);
	    		tx.getProxy(ISale.class).confirm(uuid);
	    		tx.setSequence(0);
	    		tx.getProxy(IOrder.class).available(PERSON, buyCount);
	    		tx.setSequence(1);
	    		System.out.println("confirm : " + " buy " + buyCount + " items");
				tx.confirm();
			} catch (Throwable t) {
				t.printStackTrace();
				System.out.println("confirm : " + " buy " + buyCount + " items failed cause " + t.getMessage());
			}
	    	Thread.sleep(2000);
	    }
	}
}


