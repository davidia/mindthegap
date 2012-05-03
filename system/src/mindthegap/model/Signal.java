package mindthegap.model;

import mindthegap.util.Direction;

public class Signal {
	String symbol;
	double strength;
	Direction direction;
	
	public Signal(String symbol, double strength, Direction direction) {	
		this.symbol = symbol;
		this.strength = strength;
		this.direction = direction;
	}
	
	public Direction getDirection(){
		return direction;
	}
	
	public double getStrength(){
		return strength;
	}
	
	public String getSymbol(){
		return symbol;
	}
	
}
