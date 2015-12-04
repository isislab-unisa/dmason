package it.isislab.dmason.test.sim.field.grid.numeric.region;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionDoubleNumeric;



import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
* The Class RegionDoubleNumericTester. Tests the RegionDoubleNumeric.
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class RegionDoubleNumericTester {
	/** The rd. */
	RegionDoubleNumeric rd;

	/** The loop test. */
	int loopTest;

	/**
	 * Sets the up.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		int width=100;
		int height=100;
		rd = new RegionDoubleNumeric(0, 0, 100, 100,width,height);
		
		loopTest = 10000;
	}

	// isMine
	// verify if an entry is located in my region

	/**
	 * Test is mine0_0.
	 */
	@Test
	public void testIsMine0_0() {
		// (x>=0) && (y >= 0) && (x <=1 ) && (y<=1 );
		assertTrue(rd.isMine(0, 0));
	}

	/**
	 * Test is mine0_1.
	 */
	@Test
	public void testIsMine0_1() {
		// (x>=0) && (y >= 0) && (x <=1 ) && (y<=1 );
		assertTrue(rd.isMine(0, 1));
	}

	/**
	 * Test is mine1_0.
	 */
	@Test
	public void testIsMine1_0() {
		// (x>=0) && (y >= 0) && (x <=1 ) && (y<=1 );
		assertTrue(rd.isMine(1, 0));
	}

	/**
	 * Test is mine range0_1_0_1.
	 */
	@Ignore
	public void testIsMineRange0_1_0_1() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		int step = 1 / loopTest;
		int i = 0;
		while (i < 1) {
			int j = 0;
			while (j < 1) {
				assertTrue(rd.isMine(i, j));
				j += step;
			}
			i += step;
		}

	}

	/**
	 * Test is mine negative range1_0_0_1.
	 */
	@Ignore
	public void testIsMineNegativeRange1_0_0_1() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		int step = 1 / loopTest;
		int i = -1;
		while (i < 0) {
			int j = 0;
			while (j < 1) {
				assertFalse(rd.isMine(i, j));
				j += step;
			}
			i += step;
		}

	}
	
	
}
