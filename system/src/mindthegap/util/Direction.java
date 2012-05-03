package mindthegap.util;

public class Direction {
	
	public final static Direction Buy = new Direction(1);
	public final static Direction Sell = new Direction(-1);
	
	private int direction;
	
	private Direction(int d){
		direction = d;
	}
	
	public int toInt(){
		return direction;
	}
	
	public String toString(){
		return direction > 0  ? "Buy" : "Sell";
	}
	
}
