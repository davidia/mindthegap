package mindthegap.activetick;

import java.util.EventObject;

public class TradeEvent extends EventObject {
	
	private double price;
	private int qty;
	private String symbol;
	private Time time;

	public TradeEvent(Object source,Time time,String symbol,double price,int qty) {
		super(source);		
		this.symbol = symbol;
		this.price = price;
		this.qty = qty;
		this.time = time;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public int getQty() {
		return 	qty;
	}
	
	public double getPrice() {
		return price;
	}

	public Time getTime() {
		return time;
	}


}

