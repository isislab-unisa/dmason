package it.isislab.dmason.test.sim.field.continuous.region;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import sim.util.Double2D;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.continuous.region.RegionDoubleLB;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.test.sim.app.DFlockers.DFlocker;

// TODO: Auto-generated Javadoc
/**
 * Test the Class RegionDoubleLB.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class RegionDoubleLBTester {
	
	/** The rd. */
	RegionDoubleLB rd;
	
	/** The loop test. */
	int loopTest;

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		rd = new RegionDoubleLB(0.0, 0.0, 0.0, 0.0, 1.0, 1.0);
		loopTest = 10000;
	}

	// isMine
	// verify if an entry is located in my region

	/**
	 * Test is mine0_0.
	 */
	@Test
	public void testIsMine0_0() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		assertTrue(rd.isMine(0.0, 0.0));
	}

	/**
	 * Test is mine0_1.
	 */
	@Test
	public void testIsMine0_1() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		assertFalse(rd.isMine(0.0, 1.0));
	}

	/**
	 * Test is mine1_0.
	 */
	@Test
	public void testIsMine1_0() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		assertFalse(rd.isMine(1.0, 0.0));
	}

	/**
	 * Test is mine range0_1_0_1.
	 */
	@Test
	public void testIsMineRange0_1_0_1() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		double step = 1.0 / loopTest;
		double i = 0.0;
		while (i < 1) {
			double j = 0.0;
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
	@Test
	public void testIsMineNegativeRange1_0_0_1() {
		// (x>=0) && (y >= 0) && (x <1 ) && (y<1 );
		double step = 1.0 / loopTest;
		double i = -1.0;
		while (i < 0) {
			double j = 0.0;
			while (j < 1) {
				assertFalse(rd.isMine(i, j));
				j += step;
			}
			i += step;
		}

	}

//	// createRegion
//	/**
//	 * Test create region xx under0.
//	 */
//	@Test
//	public void testCreateRegionXXUnder0() {
//
//		assertNull(rd.createRegion(-1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region yy under0.
//	 */
//	@Test
//	public void testCreateRegionYYUnder0() {
//
//		assertNull(rd.createRegion(0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region xx over0.
//	 */
//	@Test
//	public void testCreateRegionXXOver0() {
//
//		assertNotNull(rd.createRegion(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region yy over0.
//	 */
//	@Test
//	public void testCreateRegionYYOver0() {
//
//		assertNotNull(rd.createRegion(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region xxw over w.
//	 */
//	@Test
//	public void testCreateRegionXxwOverW() {
//
//		assertNull(rd.createRegion(11.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region xxw equal w.
//	 */
//	@Test
//	public void testCreateRegionXxwEqualW() {
//
//		assertNull(rd.createRegion(10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region yyh over h.
//	 */
//	@Test
//	public void testCreateRegionYyhOverH() {
//
//		assertNull(rd.createRegion(0.0, 11.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region yyh equal h.
//	 */
//	@Test
//	public void testCreateRegionYyhEqualH() {
//
//		assertNull(rd.createRegion(0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 10.0, 10.0));
//
//	}
//
//	/**
//	 * Test create region whidth6.
//	 */
//	@Test
//	public void testCreateRegionWhidth6() {
//		
//		rd.createRegion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 10.0);
//		assertEquals(6,rd.width,0); //because height is static
//
//	}
//
//	/**
//	 * Test create region heigth6.
//	 */
//	@Test
//	public void testCreateRegionHeigth6() {
//
//		rd.createRegion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 6.0);
//		assertEquals(6,rd.height,0); //because height is static
//	}

	
	//addAgents
	
	/**
	 * Test add agents null.
	 */
	@Test
	public void testAddAgentsNull() {
		/*BUG FIND
		 * mi fa inserire un entry null
		 * */
		EntryAgent<Double2D> e = null;
		assertFalse(rd.addAgents(e));
	}
	
	/**
	 * Test add agents.
	 */
	@Test
	public void testAddAgents() {
		RemotePositionedAgent<Double2D> c = null;
		Double2D f = null;
		/*BUG FIND
		 * mi fa inserire un entry con valori null
		 * */
		EntryAgent<Double2D> e = new EntryAgent<Double2D>(c, f);
		assertFalse(rd.addAgents(e));
	}
	
	/**
	 * Test add agents verify.
	 */
	@Test
	public void testAddAgentsVerify() {
		RemotePositionedAgent<Double2D> c = new DFlocker();
		Double2D f = new Double2D();
		EntryAgent<Double2D> e = new EntryAgent<Double2D>(c, f);
		rd.addAgents(e);
		assertEquals(e,rd.get(e.r.getId()));
	}
	
	//clone
	/**
	 * Test clone.
	 */
	@Test
	public void testClone() {
		
		RegionDoubleLB clone=(RegionDoubleLB)rd.clone();
		
		assertEquals(rd,clone);
	}
	
	/**
	 * Test clone with entry.
	 */
	@Test
	public void testCloneWithEntry() {
		RemotePositionedAgent<Double2D> c = new DFlocker();
		Double2D f = new Double2D();
		EntryAgent<Double2D> e = new EntryAgent<Double2D>(c, f);
		rd.addAgents(e);
		RegionDoubleLB clone=null;
		try{
			clone=(RegionDoubleLB)rd.clone();
			
		}catch(Exception err){
			fail("clone fail");
		}
		//assertEquals("incorrect copy of entry",e, clone);
		assertEquals("incorrect copy of entry",rd, clone);
	}
}
