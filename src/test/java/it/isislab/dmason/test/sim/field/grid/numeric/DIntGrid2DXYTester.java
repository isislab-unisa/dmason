package it.isislab.dmason.test.sim.field.grid.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DXY;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import it.isislab.dmason.test.sim.field.continuous.DContinuousGrid2DXYTester.StubRemotePositionedAgent;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * The Class DIntGrid2DXYTester. Tests the DIntGrid2DXY for the roroidal
 * distribution.
 * 
 *     
 *   @author Michele Carillo
 *   @author Flavio Serrapica
 *   @author Carmine Spagnuolo 
 */
public class DIntGrid2DXYTester {
	/** The to test. */
	DIntGrid2DXY toTest;

	/** The distributed state. */
	StubDistributedState ss;

	/** The num of loop of the tests. */
	int numLoop = 8; // the max value for numLoop is 8 because for numLoop>8
	// the java's approssimation is wrong
	/** The width. */
	int width;

	/** The height. */
	int height;

	/** The max distance. */
	int maxDistance;

	/** The rows. */
	int rows;

	/** The columns. */
	int columns;

	/** The num agents. */
	int numAgents;

	/** The mode. */
	int mode;

	/** The connection type. */
	int connectionType;

	/**
	 * The Class StubDistributedState.
	 */
	public class StubDistributedState extends DistributedState<Int2D> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new stub distributed state.
		 */
		public StubDistributedState() {
			super();
		}

		/**
		 * Instantiates a new stub distributed state.
		 *
		 * @param params
		 *            the params
		 */
		public StubDistributedState(GeneralParam params) {
			super(params, new DistributedMultiSchedule<Int2D>(), "stub", params
					.getConnectionType());

			this.MODE = params.getMode();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
		 */
		@Override
		public DistributedField<Int2D> getField() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.engine.DistributedState#addToField(it.isislab
		 * .dmason.sim.engine.RemotePositionedAgent, java.lang.Object)
		 */
		@Override
		public void addToField(RemotePositionedAgent<Int2D> rm, Int2D loc) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.engine.DistributedState#getState()
		 */
		@Override
		public SimState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.engine.DistributedState#setPortrayalForObject
		 * (java.lang.Object)
		 */
		@Override
		public boolean setPortrayalForObject(Object o) {
			// TODO Auto-generated method stub
			return false;
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

		width = 100;
		height = 100;
		maxDistance = 1;
		rows = 10;
		columns = 10;
		numAgents = numLoop;
		mode = DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		ss = new StubDistributedState(genParam);

		new DIntGrid2DFactory();
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(width,
				height, ss, maxDistance, /* i */0, /* j */0, rows, columns, mode,
				0, false,/* name */
				"test", /* prefix */"", true);

	}

	/**
	 * Test set distributed object location.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocation() throws DMasonException {
		
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			assertTrue(toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss));
		}
	}

	/**
	 * Test get state.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetState() throws DMasonException {

		// i'm moving an agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss);
		}

		assertSame(ss, toTest.getState());
	}

	// AGENTS IS MEMORIZED IN THE rmap

	/**
	 * Test corner mine up left.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testCornerMineUpLeft() throws DMasonException {

		int i = toTest.rmap.corner_mine_up_left.upl_xx;
		int j = toTest.rmap.corner_mine_up_left.upl_yy;

		int iEnd = toTest.rmap.corner_mine_up_left.down_xx;
		int jEnd = toTest.rmap.corner_mine_up_left.down_yy;

		int stepI = (toTest.rmap.corner_mine_up_left.down_xx - toTest.rmap.corner_mine_up_left.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.corner_mine_up_left.down_yy - toTest.rmap.corner_mine_up_left.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_left.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  grid value 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_left.size());
	}*/

	/**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testCornerMineUpRight() throws DMasonException {

		int i = toTest.rmap.corner_mine_up_right.upl_xx;
		int j = toTest.rmap.corner_mine_up_right.upl_yy;

		int iEnd = toTest.rmap.corner_mine_up_right.down_xx;
		int jEnd = toTest.rmap.corner_mine_up_right.down_yy;

		int stepI = (toTest.rmap.corner_mine_up_right.down_xx - toTest.rmap.corner_mine_up_right.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.corner_mine_up_right.down_yy - toTest.rmap.corner_mine_up_right.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_right.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  grid value 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_right.size());
	}*/
/*
	*//**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testCornerMineDownLeft() throws DMasonException {

		int i = toTest.rmap.corner_mine_down_left.upl_xx;
		int j = toTest.rmap.corner_mine_down_left.upl_yy;

		int iEnd = toTest.rmap.corner_mine_down_left.down_xx;
		int jEnd = toTest.rmap.corner_mine_down_left.down_yy;

		int stepI = (toTest.rmap.corner_mine_down_left.down_xx - toTest.rmap.corner_mine_down_left.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.corner_mine_down_left.down_yy - toTest.rmap.corner_mine_down_left.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_left.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  grid value 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_down_left.size());
	}
*/
	/**
	 * Test corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownRight() throws DMasonException {

		int i = toTest.rmap.corner_mine_down_right.upl_xx;
		int j = toTest.rmap.corner_mine_down_right.upl_yy;

		int iEnd = toTest.rmap.corner_mine_down_right.down_xx;
		int jEnd = toTest.rmap.corner_mine_down_right.down_yy;

		int stepI = (toTest.rmap.corner_mine_down_right.down_xx - toTest.rmap.corner_mine_down_right.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.corner_mine_down_right.down_yy - toTest.rmap.corner_mine_down_right.upl_yy)
				/ numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_right.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						j, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_down_right.size());
	}

	/**
	 * Test down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMine() throws DMasonException {

		int i = toTest.rmap.down_mine.upl_xx;
		int j = toTest.rmap.down_mine.upl_yy;

		int iEnd = toTest.rmap.down_mine.down_xx;
		int jEnd = toTest.rmap.down_mine.down_yy;

		int stepI = (iEnd - i) / numLoop;
		int stepJ = (jEnd - j) / numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.down_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);

				if (toTest.setDistributedObjectLocation(location, /* grid value */
						j, /* SimState */ss))
					count += 1;
				j += stepJ;

			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.down_mine.size());
	}

/*	*//**
	 * Test left mine.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testLeftMine() throws DMasonException {

		int i = toTest.rmap.left_mine.upl_xx;
		int j = toTest.rmap.left_mine.upl_yy;

		int iEnd = toTest.rmap.left_mine.down_xx;
		int jEnd = toTest.rmap.left_mine.down_yy;

		int stepI = (toTest.rmap.left_mine.down_xx - toTest.rmap.left_mine.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.left_mine.down_yy - toTest.rmap.left_mine.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.left_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  grid value 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.left_mine.size());
	}*/

	/**
	 * Test right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMine() throws DMasonException {

		int i = toTest.rmap.right_mine.upl_xx;
		int j = toTest.rmap.right_mine.upl_yy;

		int iEnd = toTest.rmap.right_mine.down_xx;
		int jEnd = toTest.rmap.right_mine.down_yy;

		int stepI = (toTest.rmap.right_mine.down_xx - toTest.rmap.right_mine.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.right_mine.down_yy - toTest.rmap.right_mine.upl_yy)
				/ numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.right_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						j, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.right_mine.size());
	}

	/**
	 * Test up mine.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testUpMine() throws DMasonException {

		int i = toTest.rmap.up_mine.upl_xx;
		int j = toTest.rmap.up_mine.upl_yy;

		int iEnd = toTest.rmap.up_mine.down_xx;
		int jEnd = toTest.rmap.up_mine.down_yy;

		int stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = stepJ + toTest.rmap.up_mine.upl_yy;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  grid value 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}
		assertEquals(count, toTest.rmap.up_mine.size());

	}
*/
	/**
	 * Test double set distributed object location god agent.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		int i = toTest.rmap.up_mine.upl_xx;
		int j = toTest.rmap.up_mine.upl_yy;

		Int2D location = new Int2D(i, j);
		System.out.println(location);
		assertTrue(toTest.setDistributedObjectLocation(location,  grid value 
				Integer.parseInt("6"),  SimState ss));
		
		assertEquals(6,toTest.field[i+1][j+1]);

	}

	*//**
	 * test for the field partitioning.
	 * 
	 * @throws DMasonException
	 *//*
*/
	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 100;
		int h = 100;
		int maxD = 1;
        int rows=3;
        int columns=rows; 
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, rows, columns, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		
		Integer x1 = toTest.rmap.corner_mine_up_left.getUpl_xx();
		Integer y1 = toTest.rmap.corner_mine_up_left.getUpl_yy();
		Integer x2 = toTest.rmap.corner_out_down_right_diag_down.getDown_xx();
		Integer y2 = toTest.rmap.corner_out_down_right_diag_down.getDown_yy();
		

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		//find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w - 1, 2.0) + Math.pow(h - 1, 2.0));
        diagwh=diagwh/rows;
		assertEquals(diag, diagwh);
	}

	/**
	 * Test my field partitioning max distance1.
	 */
	@Test
	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 100;
		int h = 100;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.myfield.down_xx;
		Integer x1 = toTest.myfield.upl_xx;
		Integer y2 = toTest.myfield.down_yy;
		Integer y1 = toTest.myfield.upl_yy;

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w - 2 * maxD, 2.0)
				+ Math.pow(h - 2 * maxD, 2.0));

		assertEquals(diag, diagwh);
	}

	/**
	 * Test up mine partitioning.
	 *//*
	@Test
	public void testUpMinePartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h, simState 
				ss, maxD,  i 0,  j 0, 1, 1, mode, 0, false, name 
				"test",  prefix "", true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w, side.intValue() + 1);
	}
*/
	/**
	 * Test down mine partitioning max distance1.
	 *//*
	@Test
	public void testDownMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h, simState 
				ss, maxD,  i 0,  j 0, 10, 10, mode, 0, false, name 
				"test",  prefix "", true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}
*/
	/**
	 * Test down mine partitioning.
	 */
	@Test
	public void testDownMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.down_mine.down_xx;
		Integer x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test up mine partitioning max distance1.
	 */
	@Test
	public void testUpMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.down_mine.down_xx;
		Integer x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test left mine partitioning.
	 */
	@Test
	public void testLeftMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.left_mine.down_yy;
		Integer x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h, side.intValue() + 1);
	}

	/**
	 * Test left mine partitioning max distance1.
	 */
	@Test
	public void testLeftMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */9, /* j */9, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.left_mine.down_yy;
		Integer x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test right mine partitioning.
	 */
	@Test
	public void testRightMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.right_mine.down_yy;
		Integer x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test right mine partitioning max distance1.
	 */
	@Test
	public void testRightMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.right_mine.down_yy;
		Integer x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test up out partitioning.
	 */
	@Test
	public void testUpOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.up_out.down_xx;
		Integer x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w, side.intValue() + 1);
	}

	/**
	 * Test down out partitioning max distance1.
	 */
	@Test
	public void testDownOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.up_out.down_xx;
		Integer x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test down out partitioning.
	 */
	@Test
	public void testDownOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.down_out.down_xx;
		Integer x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test up out partitioning max distance1.
	 */
	@Test
	public void testUpOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.down_out.down_xx;
		Integer x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test left out partitioning.
	 */
	@Test
	public void testLeftOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.left_out.down_yy;
		Integer x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h, side.intValue() + 1);
	}

	/**
	 * Test left out partitioning max distance1.
	 */
	@Test
	public void testLeftOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.left_out.down_yy;
		Integer x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test right out partitioning.
	 */
	@Test
	public void testRightOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.right_out.down_yy;
		Integer x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test right out partitioning max distance1.
	 */
	@Test
	public void testRightOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(w, h,/* simState */
				ss, maxD, /* i */0, /* j */0, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.right_out.down_yy;
		Integer x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test corner congruence up right.
	 */
	@Test
	public void testCornerCongruenceUpRight() throws DMasonException {
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(10, 10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.corner_mine_up_right.upl_xx,
				toTest.rmap.corner_out_up_right_diag_center.down_xx - 1, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_right.upl_yy,
				toTest.rmap.corner_out_up_right_diag_center.down_yy + 1, 0);
	}

	/**
	 * Test corner congruence up left.
	 */
	@Test
	public void testCornerCongruenceUpLeft() throws DMasonException {
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(10, 10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.corner_mine_up_left.upl_xx,
				toTest.rmap.corner_out_up_left_diag_center.down_xx + 1, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_left.upl_yy,
				toTest.rmap.corner_out_up_left_diag_center.down_yy + 1, 0);
	}

	/**
	 * Test corner congruence down left.
	 */
	@Test
	public void testCornerCongruenceDownLeft() throws DMasonException {
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(10, 10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.corner_mine_down_left.upl_xx,
				toTest.rmap.corner_out_down_left_diag_center.down_xx + 1, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_left.upl_yy,
				toTest.rmap.corner_out_down_left_diag_center.down_yy - 1, 0);
	}

	/**
	 * Test corner congruence down right.
	 */
	@Test
	public void testCornerCongruenceDownRight() throws DMasonException {
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(10, 10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.corner_mine_down_right.down_xx,
				toTest.rmap.corner_out_down_right_diag_center.upl_xx - 1, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_right.down_yy,
				toTest.rmap.corner_out_down_right_diag_center.upl_yy - 1, 0);
	}

	/**
	 * Test my field congruence.
	 */
	@Test
	public void testMyFieldCongruence() throws DMasonException {
		toTest = (DIntGrid2DXY) DIntGrid2DFactory.createDIntGrid2D(10, 10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		// upLeft
		assertEquals("X Up Left", toTest.myfield.upl_xx,
				toTest.rmap.up_mine.upl_xx + 1, 0);
		assertEquals("Y Up Left", toTest.myfield.upl_yy,
				toTest.rmap.up_mine.upl_yy + 1, 0);
		// downRight
		assertEquals("X Down Right", toTest.myfield.down_xx,
				toTest.rmap.down_mine.down_xx - 1, 0);
		assertEquals("Y Down Right", toTest.myfield.down_yy,
				toTest.rmap.down_mine.down_yy - 1, 0);

	}

}
