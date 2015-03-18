package it.isislab.dmason.test.sim.field;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import it.isislab.dmason.sim.field.CellType;

// TODO: Auto-generated Javadoc
/**
 * Test the Class CellType.
 * 
 * @author Mario Capuozzo
 */
public class CellTypeTester{

	/** The cell type. */
	CellType ct;
	
	/** The number of loop of the iteration of tests. */
	int testLimit; // number of for's cycle

	/**
	 * Set the enviroment.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		ct = new CellType(0, 0);
		testLimit = 110;
	}

	// getInitialValue
	/**
	 * Test get initial value.
	 */
	@Test
	public void testGetInitialValue() {
		assertEquals(0, ct.getInitialValue());
	}

	/**
	 * Test get initial value0_1.
	 */
	@Test
	public void testGetInitialValue0_1() {
		ct.pos_j = 1;
		assertEquals(1, ct.getInitialValue());
	}

	/**
	 * Test get initial value1_0.
	 */
	@Test
	public void testGetInitialValue1_0() {
		ct.pos_i = 1;
		assertEquals(10, ct.getInitialValue());
	}

	/**
	 * Test get initial value0_ x.
	 */
	@Test
	public void testGetInitialValue0_X() {
		for (int i = 0; i < testLimit; i++) {
			ct.pos_j = i;
			assertEquals(i, ct.getInitialValue());
		}
	}

	/**
	 * Test get initial value x_0.
	 */
	@Test
	public void testGetInitialValueX_0() {
		for (int i = 0; i < testLimit; i++) {
			ct.pos_i = i;
			assertEquals(i * 10, ct.getInitialValue());
		}
	}

	/**
	 * Test get initial value x_ y.
	 */
	@Test
	public void testGetInitialValueX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				String app = "" + y;
				int count = 1;
				for (int i = 0; i < app.length(); i++)
					count *= 10;
				assertEquals("x=" + x + " y=" + y, (x * count) + y,
						ct.getInitialValue());
			}
		}
	}

	// getId
	/**
	 * Test get id0.
	 */
	@Test
	public void testGetId0() {
		assertEquals(0, ct.getId(0));
	}

	/**
	 * Test get id0 for0_1.
	 */
	@Test
	public void testGetId0For0_1() {
		ct.pos_j = 1;
		assertEquals(1, ct.getId(0));
	}

	/**
	 * Test get id0 for1_0.
	 */
	@Test
	public void testGetId0For1_0() {
		ct.pos_i = 1;
		assertEquals(10, ct.getId(0));
	}

	/**
	 * Test get id1 for0_1.
	 */
	@Test
	public void testGetId1For0_1() {
		ct.pos_j = 1;
		assertEquals(2, ct.getId(1));
	}

	/**
	 * Test get id1 for1_0.
	 */
	@Test
	public void testGetId1For1_0() {
		ct.pos_i = 1;
		assertEquals(11, ct.getId(1));
	}

	/**
	 * Test get id x_ y_ z.
	 */
	@Test
	public void testGetIdX_Y_Z() {
		for (int z = 0; z < testLimit; z++) {
			for (int x = 0; x < testLimit; x++) {
				ct.pos_i = x;
				for (int y = 0; y < testLimit; y++) {
					ct.pos_j = y;
					String app = "" + y;
					int count = 1;
					for (int i = 0; i < app.length(); i++)
						count *= 10;
					assertEquals("x=" + x + " y=" + y, (x * count) + y + z,
							ct.getId(z));
				}
			}
		}
	}

	// getNeighbourLeft

	/**
	 * Test get neighbour left x_ y.
	 */
	@Test
	public void testGetNeighbourLeftX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals(x + "-" + (y - 1), ct.getNeighbourLeft());
			}
		}

	}

	// getNeighbourDiagLeftUp
	/**
	 * Test get neighbour diag left up x_ y.
	 */
	@Test
	public void testGetNeighbourDiagLeftUpX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x - 1) + "-" + (y - 1),
						ct.getNeighbourDiagLeftUp());
			}
		}

	}

	// getNeighbourUp

	/**
	 * Test get neighbour up x_ y.
	 */
	@Test
	public void testGetNeighbourUpX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x - 1) + "-" + (y), ct.getNeighbourUp());
			}
		}

	}

	// getNeighbourDiagRightUp
	/**
	 * Test get neighbour diag right up x_ y.
	 */
	@Test
	public void testGetNeighbourDiagRightUpX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x - 1) + "-" + (y + 1),
						ct.getNeighbourDiagRightUp());
			}
		}

	}

	// getNeighbourRight
	/**
	 * Test get neighbour right x_ y.
	 */
	@Test
	public void testGetNeighbourRightX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x) + "-" + (y + 1), ct.getNeighbourRight());
			}
		}

	}

	// getNeighbourDiagRightDown
	/**
	 * Test get neighbour diag right down x_ y.
	 */
	@Test
	public void testGetNeighbourDiagRightDownX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x + 1) + "-" + (y + 1),
						ct.getNeighbourDiagRightDown());
			}
		}

	}

	// getNeighbourDown
	/**
	 * Test get neighbour down x_ y.
	 */
	@Test
	public void testGetNeighbourDownX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x + 1) + "-" + (y), ct.getNeighbourDown());
			}
		}

	}

	// getNeighbourDiagLeftDown
	/**
	 * Test get neighbour diag left down x_ y.
	 */
	@Test
	public void testGetNeighbourDiagLeftDownX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x + 1) + "-" + (y - 1),
						ct.getNeighbourDiagLeftDown());
			}
		}

	}

	// toString
	/**
	 * Test to string x_ y.
	 */
	@Test
	public void testToStringX_Y() {
		for (int x = 0; x < testLimit; x++) {
			ct.pos_i = x;
			for (int y = 0; y < testLimit; y++) {
				ct.pos_j = y;
				assertEquals((x) + "-" + (y), ct.toString());
			}
		}

	}
}
