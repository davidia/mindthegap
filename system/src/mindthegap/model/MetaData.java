package mindthegap.model;

import java.util.*;

import mindthegap.util.Day;


public class MetaData {
	String symbol;
	Day date;	
	
	public MetaData(String symbol, Day date) {
		this.symbol = symbol;
		this.date = date;		
	}

	public String getSymbol() {
		return symbol;
	}

	public Day getDate() {
		return date;
	}
}
