package it.isislab.dmason.test.sim.field.grid.numeric.thin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.field.grid.numeric.thin.DIntGrid2DXYThin;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.util.connection.ConnectionType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 * The Class DIntGrid2DXYThinTester. Tests the DIntGrid2DXYThin for toroidal
 * distribution.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class DIntGrid2DXYThinTester {
	/** The to test. */
	DIntGrid2DXYThin toTest;

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

	}

	
	/**
	 * Sets the enviroment.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {

		width = 1000;
		height = 1000;
		maxDistance = 1;
		rows = 10;
		columns = 10;
		numAgents = numLoop;
		mode = DistributedField2D.UNIFORM_PARTITIONING_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		ss = new StubDistributedState(genParam);

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(
				width, height,/* simState */
				ss, maxDistance, /* i */2, /* j */2, rows, columns, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

	}
	
	/**
	 * Test for getAvailableRandomLocation. If getAvailableRandomLocation returns an good position, for toTest region, setDistributedObjectLocation returns true otherwise it return false.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocation() throws DMasonException {

		
			Int2D location = new Int2D(toTest.myfield.down_xx, toTest.myfield.down_yy);
			assertTrue("The Int2D location should belong at myfield",toTest.setDistributedObjectLocation(location, /* grid value */
					5, /* SimState */ss));
	}
	

	/**
	 * Test for getAvailableRandomLocation. If getAvailableRandomLocation returns an good position, for toTest region, setDistributedObjectLocation returns true otherwise it return false.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetAvailableRandomLocation() throws DMasonException {

		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			assertTrue(toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss));
		}
	}

	
	/**
	 * Test for setDistributedObjectLocation using double value.
	 * 
	 * @throws DMasonException if a double value 
	 */
	@Test
	public void testSetDistributedObjectLocationWithMistake() throws DMasonException {

		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			try{
			toTest.setDistributedObjectLocation(location, /* grid value */
					4.3, /* SimState */ss);
				fail("it should throw a DMasonException");
			}catch(DMasonException e) {/* good */}
		}
	}
	
	
	
	
	/**
	 * Test get state.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetState() throws DMasonException {
		toTest.get(0, 0);
		// i'm moving an agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss);
		}

		assertSame(ss, toTest.getState());
	}

	/**
	 * Test get num agent for same agent.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetNumAgentForSameAgent() throws DMasonException {
		// i'm moving an agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss);
		}
		assertEquals(1, toTest.myfield.size());
	}

	/**
	 * Test get num agent different agent.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetNumAgentDifferentAgent() throws DMasonException {
		// i'm positioning more agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location, /* grid value */
					i, /* SimState */ss);
		}
		assertEquals(numLoop, toTest.myfield.size());
	}

	// AGENTS IS MEMORIZED IN THE rmap

	/**
	 * Test corner mine up left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpLeft() throws DMasonException {

		
			
		int i = toTest.rmap.NORTH_WEST_MINE.upl_xx;
		int j = toTest.rmap.NORTH_WEST_MINE.upl_yy;

		int iEnd = toTest.rmap.NORTH_WEST_MINE.down_xx;
		int jEnd = toTest.rmap.NORTH_WEST_MINE.down_yy;

		int stepI = (toTest.rmap.NORTH_WEST_MINE.down_xx - toTest.rmap.NORTH_WEST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.NORTH_WEST_MINE.down_yy - toTest.rmap.NORTH_WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.NORTH_WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.NORTH_WEST_MINE.size());
	}

	/**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpRight() throws DMasonException {

		int i = toTest.rmap.NORTH_EAST_MINE.upl_xx;
		int j = toTest.rmap.NORTH_EAST_MINE.upl_yy;

		int iEnd = toTest.rmap.NORTH_EAST_MINE.down_xx;
		int jEnd = toTest.rmap.NORTH_EAST_MINE.down_yy;

		int stepI = (toTest.rmap.NORTH_EAST_MINE.down_xx - toTest.rmap.NORTH_EAST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.NORTH_EAST_MINE.down_yy - toTest.rmap.NORTH_EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.NORTH_EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.NORTH_EAST_MINE.size());
	}

	/**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownLeft() throws DMasonException {

		int i = toTest.rmap.SOUTH_WEST_MINE.upl_xx;
		int j = toTest.rmap.SOUTH_WEST_MINE.upl_yy;

		int iEnd = toTest.rmap.SOUTH_WEST_MINE.down_xx;
		int jEnd = toTest.rmap.SOUTH_WEST_MINE.down_yy;

		int stepI = (toTest.rmap.SOUTH_WEST_MINE.down_xx - toTest.rmap.SOUTH_WEST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.SOUTH_WEST_MINE.down_yy - toTest.rmap.SOUTH_WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_WEST_MINE.size());
	}

	/**
	 * Test corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownRight() throws DMasonException {

		int i = toTest.rmap.SOUTH_EAST_MINE.upl_xx;
		int j = toTest.rmap.SOUTH_EAST_MINE.upl_yy;

		int iEnd = toTest.rmap.SOUTH_EAST_MINE.down_xx;
		int jEnd = toTest.rmap.SOUTH_EAST_MINE.down_yy;

		int stepI = (toTest.rmap.SOUTH_EAST_MINE.down_xx - toTest.rmap.SOUTH_EAST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.SOUTH_EAST_MINE.down_yy - toTest.rmap.SOUTH_EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_EAST_MINE.size());
	}

	/**
	 * Test down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMine() throws DMasonException {

		int i = toTest.rmap.SOUTH_MINE.upl_xx;
		int j = toTest.rmap.SOUTH_MINE.upl_yy;

		int iEnd = toTest.rmap.SOUTH_MINE.down_xx;
		int jEnd = toTest.rmap.SOUTH_MINE.down_yy;

		int stepI = (iEnd - i) / numLoop;
		int stepJ = (jEnd - j) / numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;

			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_MINE.size());
	}

	/**
	 * Test left mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMine() throws DMasonException {

		int i = toTest.rmap.WEST_MINE.upl_xx;
		int j = toTest.rmap.WEST_MINE.upl_yy;

		int iEnd = toTest.rmap.WEST_MINE.down_xx;
		int jEnd = toTest.rmap.WEST_MINE.down_yy;

		int stepI = (toTest.rmap.WEST_MINE.down_xx - toTest.rmap.WEST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.WEST_MINE.down_yy - toTest.rmap.WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.WEST_MINE.size());
	}

	/**
	 * Test boundary value left mine.
	 */

	/**
	 * Test right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMine() throws DMasonException {

		int i = toTest.rmap.EAST_MINE.upl_xx;
		int j = toTest.rmap.EAST_MINE.upl_yy;

		int iEnd = toTest.rmap.EAST_MINE.down_xx;
		int jEnd = toTest.rmap.EAST_MINE.down_yy;

		int stepI = (toTest.rmap.EAST_MINE.down_xx - toTest.rmap.EAST_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.EAST_MINE.down_yy - toTest.rmap.EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.EAST_MINE.size());
	}

	/**
	 * Test up mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMine() throws DMasonException {

		int i = toTest.rmap.NORTH_MINE.upl_xx;
		int j = toTest.rmap.NORTH_MINE.upl_yy;

		int iEnd = toTest.rmap.NORTH_MINE.down_xx;
		int jEnd = toTest.rmap.NORTH_MINE.down_yy;

		int stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx)
				/ numLoop;
		int stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = stepJ + toTest.rmap.NORTH_MINE.upl_yy;
			while (j < jEnd) {
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* grid value */
						i, /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}
		assertEquals(count, toTest.rmap.NORTH_MINE.size());

	}

	/**
	 * Test double set distributed object location god agent. This is not a field for agents
	 * @throws DMasonException 
	 */
	@Deprecated
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		int i = toTest.rmap.NORTH_MINE.upl_xx;
		int j = toTest.rmap.NORTH_MINE.upl_yy;

		int stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx) / 4;
		int stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy) / 4;

		Int2D location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* grid value */
				1, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* grid value */
				2, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* grid value */
				2, /* SimState */ss);

		assertNotSame("the agent is in two places at once",
				toTest.rmap.NORTH_MINE.get(0).r, toTest.rmap.NORTH_MINE.get(1).r);

	}

	/**
	 * Test set distributed object location change position.
	 * This is not a field for agents
	 * @throws DMasonException 
	 */
	@Deprecated
	public void testSetDistributedObjectLocationChangePosition() throws DMasonException {
		int i = toTest.rmap.NORTH_MINE.upl_xx;
		int j = toTest.rmap.NORTH_MINE.upl_yy;

		int stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx) / 4;
		int stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy) / 4;

		Int2D location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* grid value */
				2, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* grid value */
				2, /* SimState */ss);

		assertNotSame("the method has not changed the position",
				toTest.rmap.NORTH_MINE.get(0).l, toTest.rmap.NORTH_MINE.get(1).l);
	}

	/**
	 * test for the field partitioning.
	 * 
	 * @throws DMasonException
	 */

	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 100;
		int h = 10;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.myfield.down_xx;
		Integer x1 = toTest.myfield.upl_xx;
		Integer y2 = toTest.myfield.down_yy;
		Integer y1 = toTest.myfield.upl_yy;

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w - 1, 2.0) + Math.pow(h - 1, 2.0));

		assertEquals(diag, diagwh / 10, 1);
	}

	/**
	 * Test my field partitioning max distance1.
	 */
	@Test
	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 10;
		int h = 10;
		int maxD = 1;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
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
	 */
	@Test
	public void testUpMinePartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.NORTH_MINE.down_xx;
		Integer x1 = toTest.rmap.NORTH_MINE.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w, side.intValue() + 1);
	}

	/**
	 * Test down mine partitioning max distance1.
	 */
	@Test
	public void testDownMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.SOUTH_MINE.down_xx;
		Integer x1 = toTest.rmap.SOUTH_MINE.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test down mine partitioning.
	 */
	@Test
	public void testDownMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.SOUTH_MINE.down_xx;
		Integer x1 = toTest.rmap.SOUTH_MINE.upl_xx;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.SOUTH_MINE.down_xx;
		Integer x1 = toTest.rmap.SOUTH_MINE.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test left mine partitioning.
	 */
	@Test
	public void testLeftMinePartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_MINE.down_yy;
		Integer x1 = toTest.rmap.WEST_MINE.upl_yy;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_MINE.down_yy;
		Integer x1 = toTest.rmap.WEST_MINE.upl_yy;

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
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.EAST_MINE.down_yy;
		Integer x1 = toTest.rmap.EAST_MINE.upl_yy;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.EAST_MINE.down_yy;
		Integer x1 = toTest.rmap.EAST_MINE.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test up out partitioning.
	 */
	@Test
	public void testUpOutPartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.NORTH_OUT.down_xx;
		Integer x1 = toTest.rmap.NORTH_OUT.upl_xx;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.NORTH_OUT.down_xx;
		Integer x1 = toTest.rmap.NORTH_OUT.upl_xx;

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
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.SOUTH_OUT.down_xx;
		Integer x1 = toTest.rmap.SOUTH_OUT.upl_xx;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.SOUTH_OUT.down_xx;
		Integer x1 = toTest.rmap.SOUTH_OUT.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w / 10, side.intValue() + 1);
	}

	/**
	 * Test left out partitioning.
	 */
	@Test
	public void testLeftOutPartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */0, /* j */0, 1, 1, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_OUT.down_yy;
		Integer x1 = toTest.rmap.WEST_OUT.upl_yy;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_OUT.down_yy;
		Integer x1 = toTest.rmap.WEST_OUT.upl_yy;

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
		int maxD = 0;

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_OUT.down_yy;
		Integer x1 = toTest.rmap.WEST_OUT.upl_yy;

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

		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(w,
				h,/* simState */
				ss, maxD, /* i */1, /* j */1, 10, 10, mode, 0, false,/* name */
				"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.WEST_OUT.down_yy;
		Integer x1 = toTest.rmap.WEST_OUT.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h / 10, side.intValue() + 1);
	}

	/**
	 * Test corner congruence up right.
	 */
	@Test
	public void testCornerCongruenceUpRight() throws DMasonException {
		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(10,
				10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.NORTH_EAST_MINE.upl_xx,
				toTest.rmap.NORTH_EAST_OUT.down_xx - 1, 0);
		assertEquals("y", toTest.rmap.NORTH_EAST_MINE.upl_yy,
				toTest.rmap.NORTH_EAST_OUT.down_yy + 1, 0);
	}

	/**
	 * Test corner congruence up left.
	 */
	@Test
	public void testCornerCongruenceUpLeft() throws DMasonException {
		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(10,
				10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.NORTH_WEST_MINE.upl_xx,
				toTest.rmap.SOUTH_WEST_OUT.down_xx + 1, 0);
		assertEquals("y", toTest.rmap.NORTH_WEST_MINE.upl_yy,
				toTest.rmap.SOUTH_WEST_OUT.down_yy + 1, 0);
	}

	/**
	 * Test corner congruence down left.
	 */
	@Test
	public void testCornerCongruenceDownLeft() throws DMasonException {
		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(10,
				10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.SOUTH_WEST_MINE.upl_xx,
				toTest.rmap.SOUTH_WEST_MINE.down_xx + 1, 0);
		assertEquals("y", toTest.rmap.SOUTH_WEST_MINE.upl_yy,
				toTest.rmap.SOUTH_WEST_MINE.down_yy - 1, 0);
	}

	/**
	 * Test corner congruence down right.
	 */
	@Test
	public void testCornerCongruenceDownRight() throws DMasonException {
		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(10,
				10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		assertEquals("x", toTest.rmap.SOUTH_EAST_MINE.down_xx,
				toTest.rmap.SOUTH_EAST_OUT.upl_xx - 1, 0);
		assertEquals("y", toTest.rmap.SOUTH_EAST_MINE.down_yy,
				toTest.rmap.SOUTH_EAST_OUT.upl_yy - 1, 0);
	}

	/**
	 * Test my field congruence.
	 */
	@Test
	public void testMyFieldCongruence() throws DMasonException {
		toTest = (DIntGrid2DXYThin) DIntGrid2DFactory.createDIntGrid2DThin(10,
				10,/* simState */
				ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, mode, 0,
				false,/* name */
				"test", /* prefix */"", true);

		// upLeft
		assertEquals("X Up Left", toTest.myfield.upl_xx,
				toTest.rmap.NORTH_MINE.upl_xx + 1, 0);
		assertEquals("Y Up Left", toTest.myfield.upl_yy,
				toTest.rmap.NORTH_MINE.upl_yy + 1, 0);
		// downRight
		assertEquals("X Down Right", toTest.myfield.down_xx,
				toTest.rmap.SOUTH_MINE.down_xx - 1, 0);
		assertEquals("Y Down Right", toTest.myfield.down_yy,
				toTest.rmap.SOUTH_MINE.down_yy - 1, 0);

	}

}
