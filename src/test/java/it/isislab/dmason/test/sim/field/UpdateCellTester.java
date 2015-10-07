package it.isislab.dmason.test.sim.field;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.UpdateCell;
import it.isislab.dmason.sim.field.grid.region.RegionIntegerLB;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellIntegerField;
import junit.framework.TestCase;

// TODO: Auto-generated Javadoc
/**
 * Test the Class UpdateCell.
 * 
 * @author Mario Capuozzo
 */
public class UpdateCellTester {

	/** The update cell. */
	UpdateCell<Integer, Integer> uc;

	/** The region map. */
	RegionMap<Integer, Integer> myRMap;

	/** The field. */
	RegionIntegerLB myField;

	/** The parent type. */
	CellType parentType;

	/** The num of loop for the tests. */
	int numLoop = 1000;

	/**
	 * Set the enviroment.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		uc = new UpdateCell<Integer, Integer>();
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
		MyCellIntegerField app = new MyCellIntegerField(myRMap, myField,
				"mario", 0, 0, 0, 0, 0, parentType, 0);
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(0, 1).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}
		assertEquals("[" + app + "]", ts);
	}

	// put
	/**
	 * Test put 2 elements.
	 */
	@Test
	public void test2Put() {
		MyCellIntegerField app = new MyCellIntegerField(myRMap, myField,
				"mario", 0, 0, 0, 0, 0, parentType, 0);
		MyCellIntegerField app2 = new MyCellIntegerField(myRMap, myField,
				"maria", 0, 0, 0, 0, 0, parentType, 0);
		uc.put(0, app);
		uc.put(0, app2);
		String ts = null;

		try {
			ts = uc.getUpdates(0, 2).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

		String tr[] = ts.split(", ");

		assertEquals("[" + app, tr[0]);
		assertEquals(app2 + "]", tr[1]);
	}

	/**
	 * Test put n elements.
	 */
	@Test
	public void testNPut() {
		ArrayList<MyCellIntegerField> app = new ArrayList<MyCellIntegerField>();
		for (int i = 0; i < numLoop; i++) {
			app.add(i, new MyCellIntegerField(myRMap, myField, "mario" + i, 0,
					0, 0, 0, 0, parentType, 0));
			uc.put(0, app.get(i));
		}

		String ts = null;

		try {
			ts = uc.getUpdates(0, numLoop).toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} catch (DMasonException e) {
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
		MyCellIntegerField app = null;

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
	
	// (timeout=100)
	@Ignore
	public void testGetUpdatesIncongruenceStep() {
		MyCellIntegerField app = new MyCellIntegerField(myRMap, myField,
				"mario", 0, 0, 0, 0, 0, parentType, 0);
		uc.put(0, app);
		String ts = null;

		try {
			ts = uc.getUpdates(10, 0).toString();
			fail("must be launch an exception");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}catch (DMasonException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

	}

	/**
	 * Test get updates over queue length.
	 */
	@Test(timeout = 500)
	public void testGetUpdatesOverQueueLength() {
		MyCellIntegerField app = new MyCellIntegerField(myRMap, myField,
				"mario", 0, 0, 0, 0, 0, parentType, 0);
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
		MyCellIntegerField app = new MyCellIntegerField(myRMap, myField,
				"mario", 0, 0, 0, 0, 0, parentType, 0);
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
