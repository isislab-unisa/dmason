package it.isislab.dmason.test.sim.field.grid.region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.grid.region.RegionInteger;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.test.sim.app.DParticles.DParticle;
import org.junit.Before;
import org.junit.Test;
import sim.util.Int2D;
/**
 * The Class RegionIntegerTester. Tests the RegionInteger.
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class RegionIntegerTester {

	/** The rd. */
	RegionInteger rd;
    int width,height;
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
		width=100;
		height=100;
		rd = new RegionInteger(0, 0, 100, 100,width,height);
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



	// addAgents

	/**
	 * Test add agents null.
	 */
	@Test
	public void testAddAgentsNull() {
		/*
		 * BUG FIND mi fa inserire un entry null
		 */
		EntryAgent<Int2D> e = null;
		assertFalse(rd.addAgents(e));
	}

	/**
	 * Test add agents.
	 */
	@Test
	public void testAddAgents() {
		RemotePositionedAgent<Int2D> c = null;
		Int2D f = null;
		EntryAgent<Int2D> e = new EntryAgent<Int2D>(c, f);
		assertFalse(rd.addAgents(e));
	}

	/**
	 * Test add agents verify.
	 */
	@Test
	public void testAddAgentsVerify() {
		RemotePositionedAgent<Int2D> c = new DParticle();
		Int2D f = new Int2D(2, 4);
		EntryAgent<Int2D> e = new EntryAgent<Int2D>(c, f);
		rd.addAgents(e);
		assertEquals(e, rd.get(e.r.getId()));
	}

	// clone
	/**
	 * Test clone.
	 */
	@Test
	public void testClone() {

		RegionInteger clone = (RegionInteger) rd.clone();

		assertEquals(rd, clone);
	}

	/**
	 * Test clone with entry.
	 */
	@Test
	public void testCloneWithEntry() {
		rd.clear();
		RemotePositionedAgent<Int2D> c =  new DParticle();
		Int2D f = new Int2D();
		EntryAgent<Int2D> e = new EntryAgent<Int2D>(c, f);
		rd.addAgents(e);
		RegionInteger clone = null;

		try {
			clone = (RegionInteger) rd.clone();// per poter funzionare bisogna implementare il metodo equals 
			// alla classe Region ed Entry -> implica che ogni RemotePositionedAgent deve implementare il metodo equals

		} catch (NullPointerException err) {
			fail("clone fail");
		}
		assertEquals("incorrect copy of entry", rd, clone);

	}
}
