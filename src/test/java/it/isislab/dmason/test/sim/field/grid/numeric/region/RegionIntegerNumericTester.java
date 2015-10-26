package it.isislab.dmason.test.sim.field.grid.numeric.region;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumeric;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
/**
* The Class RegionIntegerNumericTester. Tests the RegionIntegerNumeric. 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class RegionIntegerNumericTester {
	/** The rd. */
	RegionIntegerNumeric rd;

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
		rd = new RegionIntegerNumeric(0, 0, 1, 1);
		
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

	// createRegion
	/**
	 * Test create region xx under0.
	 */
	@Test
	public void testCreateRegionXXUnder0() {
		
		assertNull(rd.createRegionNumeric(-1, 0, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region yy under0.
	 */
	@Test
	public void testCreateRegionYYUnder0() {

		assertNull(rd.createRegionNumeric(0, -1, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region xx over0.
	 */
	@Test
	public void testCreateRegionXXOver0() {

		assertNotNull(rd.createRegionNumeric(1, 0, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region yy over0.
	 */
	@Test
	public void testCreateRegionYYOver0() {

		assertNotNull(rd.createRegionNumeric(0, 1, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region xxw over w.
	 */
	@Test
	public void testCreateRegionXxwOverW() {

		assertNull(rd.createRegionNumeric(11, 0, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region xxw equal w.
	 */
	@Test
	public void testCreateRegionXxwEqualW() {

		assertNull(rd.createRegionNumeric(10, 0, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region yyh over h.
	 */
	@Test
	public void testCreateRegionYyhOverH() {

		assertNull(rd.createRegionNumeric(0, 11, 0, 0, 0, 0, 10, 10));

	}

	/**
	 * Test create region yyh equal h.
	 */
	@Test
	public void testCreateRegionYyhEqualH() {

		assertNull(rd.createRegionNumeric(0, 10, 0, 0, 0, 0, 10, 10));

	}
}
