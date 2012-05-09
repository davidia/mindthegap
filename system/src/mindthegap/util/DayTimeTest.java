package mindthegap.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DayTimeTest {

	@Test
	public void test() {
		DayTime dt = new DayTime(2013,12,31,23,59);
		DayTime d2 = DayTime.unpack(dt.pack());		
		
		assertEquals(dt.getDay(), d2.getDay());
		assertEquals(dt.getTime(), d2.getTime());
		
		
	}

}
