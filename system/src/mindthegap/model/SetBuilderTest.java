package mindthegap.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SetBuilderTest {
	
	SetBuilder.Engine engine;

	@Before
	public void setUp() throws Exception {
		engine = (new SetBuilder(null,null,15, 1, 10, 1)).engine;
	}

	@Test
	public void test() {
		SetBuilder.Range a = engine.getTrainSet(0);
		SetBuilder.Range b = engine.getTestSet(0);
		
		assertEquals(5, a.getStart());
		assertEquals(14, a.getEnd());
		assertEquals(15, b.getStart());
		assertEquals(15, b.getEnd());
		
	}

}
