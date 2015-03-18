package it.isislab.dmason.test.sim.field.support.field2D;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.grid.region.RegionIntegerLB;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;

// TODO: Auto-generated Javadoc
/**
 * Test the Class UpdateMap.
 * 
 * @author Mario Capuozzo
 */
public class UpdateMapTester {

	/** The update map. */
	UpdateMap<Integer, Integer> uc;

	/** The region map. */
	RegionMap<Integer, Integer> myRMap;

	/** The  field. */
	RegionIntegerLB myField;

	/** The parent type. */
	CellType parentType;

	/** The num of loop for the test. */
	int numLoop = 1000;

	/**
	 * The Class StubDistributedRegionInterface.
	 */
	class StubDistributedRegionInterface implements DistributedRegionInterface {

		/**
		 * Instantiates a new stub distributed region interface.
		 */
		public StubDistributedRegionInterface() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface
		 * #getStep()
		 */
		@Override
		public long getStep() {
			// TODO Auto-generated method stub
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface
		 * #getPosition()
		 */
		@Override
		public int getPosition() {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	/**
	 * Sets the enviroment.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		uc = new UpdateMap<Integer, Integer>();
		Region<Integer, Integer> r = (Region) new RegionIntegerLB(0, 0, 0, 0,
				0, 0);
		myRMap = new RegionMap<Integer, Integer>(r, r, r, r, r, r, r, r, r, r,
				r, r, r, r, r, r, r, r, r, r, r, r, r, r);
		myField = new RegionIntegerLB(0, 0, 0, 0, 0, 0);
		parentType = new CellType(0, 0);

	}

	// put-getUpdates
	/**
	 * Test put.
	 */
	@Test
	public void testPut() {
		StubDistributedRegionInterface app = new StubDistributedRegionInterface();
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(0, 1).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
		assertEquals("[" + app + "]", ts);
	}

	// put
	/**
	 * Test 2 put.
	 */
	@Test
	public void test2Put() {
		StubDistributedRegionInterface app = new StubDistributedRegionInterface();
		StubDistributedRegionInterface app2 = new StubDistributedRegionInterface();
		uc.put(0, app);
		uc.put(0, app2);
		String ts = null;

		try {
			ts = uc.getUpdates(0, 2).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

		String tr[] = ts.split(", ");

		assertEquals("[" + app, tr[0]);
		assertEquals(app2 + "]", tr[1]);
	}

	/**
	 * Test n put.
	 */
	@Test
	public void testNPut() {
		ArrayList<StubDistributedRegionInterface> app = new ArrayList<StubDistributedRegionInterface>();
		for (int i = 0; i < numLoop; i++) {
			app.add(i, new StubDistributedRegionInterface());
			uc.put(0, app.get(i));
		}

		String ts = null;

		try {
			ts = uc.getUpdates(0, numLoop).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

		String tr[] = ts.split(", ");

		// the first and last entry have the charapter [ and ], so they need to
		// be asserts separately
		assertEquals("[" + app.get(0), tr[0]);
		assertEquals(app.get(numLoop - 1) + "]", tr[numLoop - 1]);

		for (int i = 2; i < numLoop - 2; i++)
			assertEquals(app.get(i).toString(), tr[i].toString());
	}

	/**
	 * Test put null entry.
	 */
	@Test
	public void testPutNullEntry() {
		StubDistributedRegionInterface app = null;

		try {
			uc.put(0, app);
			fail("put of an empty MyCellIntegerField");
		} catch (NullPointerException e) {
			// DOH!

		}

	}

	/**
	 * Test get updates incongruence step.
	 */
	@Ignore
	// @Test (timeout=100)
	public void testGetUpdatesIncongruenceStep() {
		StubDistributedRegionInterface app = new StubDistributedRegionInterface();
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(10, 0).toString();
			fail("must be launch an exception");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

	}

	/**
	 * Test get updates over queue length.
	 */
	@Test(timeout = 500)
	public void testGetUpdatesOverQueueLength() {
		StubDistributedRegionInterface app = new StubDistributedRegionInterface();
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(0, 100).toString();
			fail("must be launch a DMasonException");
			throw new DMasonException(); // se non lo scrivo non mi fa fare il
			// catch DMasonException che servità
			// quando il bug sarà corretto
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (DMasonException e) {
			// good
		}

	}

	/**
	 * Test get updates negative num updates.
	 */
	@Test(timeout = 500)
	public void testGetUpdatesNegativeNumUpdates() {
		StubDistributedRegionInterface app = new StubDistributedRegionInterface();
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(0, -100).toString();
			fail("must be launch a DMasonException");
			throw new DMasonException(); // se non lo scrivo non mi fa fare il
											// catch DMasonException che servità
											// quando il bug sarà corretto
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (DMasonException e) {
			// good
		}

	}

}
