package mindthegap.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DayTest {

	@Test
	public void testPacked() {
		Day day = new Day(2012,12,31);
		Day day2 = new Day(day.getPacked());
		assertEquals(day.getDay(), day2.getDay());
		assertEquals(day.getMonth(), day2.getMonth());
		assertEquals(day.getYear(), day2.getYear());
	}

}
