package mindthegap.activetick;

import static org.junit.Assert.*;

import java.util.List;

import mindthegap.data.Bar;
import mindthegap.data.Intraday;

import mindthegap.util.DayTime;

import org.junit.Before;
import org.junit.Test;

public class ActiveTickTest implements BarHistoryEventListener {
	
	ActiveTick at = new ActiveTick();
	
	@Before
	public void setup(){
		at.connect();
	}

	@Test
	public void test() {
		try {
		at.registerBarHistoryEventListener(this);
		Thread.sleep(2000);
		at.barRequest("AAPL",new DayTime(2012,1,3,9,30), new DayTime(2012,1,3,9,59));
		System.out.println("called");
	
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void handleBarHistoryEvent(Intraday data) {
		for(Bar ohlc : data){
			System.out.println(ohlc);
		}
		
	}

}
