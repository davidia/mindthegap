package mindthegap.util;

import java.util.Date;
import java.util.Formatter;

public class Trade {
	double open,close;
	DayTime openTime,closeTime;
	Direction buySell;
	String symbol;
	
	public Trade(String symbol,double open, double close, DayTime openTime, DayTime closeTime,Direction buySell) {
		this.open = open;
		this.close = close;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.buySell = buySell;
		this.symbol = symbol;
	}
	
	public boolean isOpen() {
		return openTime != null && closeTime == null;
	}
	
	public boolean isClosed() {
		return closeTime != null;
	}
	
	public Trade(String symbol,Direction buySell){
		this.buySell = buySell;
		this.symbol = symbol;
	}
	
	public void open(DayTime time,double price){
		openTime = time;
		open = price;
	}
	
	public void close(DayTime time,double price){
		closeTime = time;
		close = price;
	}
	
	public double getPnl(){
		return (close - open) * buySell.toInt();
	}
	
	public double getReturn(){
		return (close/open - 1) * buySell.toInt();
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public DayTime getOpenTime() {
		return openTime;
	}

	public DayTime getCloseTime() {
		return closeTime;
	}

	public Direction getBuySell() {
		return buySell;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		sb.append(symbol);
		sb.append(": ");
		sb.append(openTime);
		sb.append(" ");
		sb.append(buySell);
		sb.append(" ");
		sb.append(open);
		sb.append(" ");
		sb.append(close);	
		formatter.format(" (%.2f%%)", 100 * getPnl() / getOpen());
		return sb.toString();
	}
	
	
	
}
