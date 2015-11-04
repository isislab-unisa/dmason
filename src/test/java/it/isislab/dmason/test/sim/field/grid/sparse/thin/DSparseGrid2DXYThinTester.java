package it.isislab.dmason.test.sim.field.grid.sparse.thin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.sparse.thin.DSparseGrid2DXYThin;
import it.isislab.dmason.test.sim.field.grid.sparse.DSparseGrid2DXYTester.StubRemotePositionedAgent;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.RemoteParam;
import it.isislab.dmason.util.connection.ConnectionType;

import org.junit.Before;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 * The Class DSparseGrid2DXYThinTester. Tests the DSparseGrid2DXYThin for
 * toroidal distribution.
 * 
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *@author Mario Capuozzo
 *
 */
 
public class DSparseGrid2DXYThinTester {
	/** The to test. */
	DSparseGrid2DXYThin toTest;

	/** The distributed state. */
	StubDistributedState ss;

	/** The remote agent. */
	StubRemotePositionedAgent stubAgent;
	RemoteParam<StubRemotePositionedAgent> sa;

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
	 * The Class StubRemotePositionedAgent.
	 */
	public class StubRemotePositionedAgent implements
			RemotePositionedAgent<Int2D> {

		/** The id. */
		String id;

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new stub remote positioned agent.
		 */
		public StubRemotePositionedAgent() {
			super();
			id = "stub";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see sim.engine.Steppable#step(sim.engine.SimState)
		 */
		@Override
		public void step(SimState arg0) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#getPos()
		 */
		@Override
		public Int2D getPos() {
			// TODO Auto-generated method stub
			return new Int2D(0, 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.engine.RemotePositionedAgent#setPos(java.lang
		 * .Object)
		 */
		@Override
		public void setPos(Int2D pos) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#getId()
		 */
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.engine.RemotePositionedAgent#setId(java.lang
		 * .String)
		 */
		@Override
		public void setId(String id) {
			// TODO Auto-generated method stub
			this.id = id;
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
		rows = 5;
		columns = 5;
		numAgents = numLoop;
		mode = DistributedField2D.SQUARE_DISTRIBUTION_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		stubAgent = new StubRemotePositionedAgent();
		sa = new RemoteParam<DSparseGrid2DXYThinTester.StubRemotePositionedAgent>(stubAgent);
		ss = new StubDistributedState(genParam);

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(width, height,/* simState */
						ss, maxDistance, /* i */2, /* j */2, rows, columns, mode,/* name */
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
			assertTrue(toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));
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
			toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					sa, /* SimState */ss);
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
			toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					sa, /* SimState */ss);
		}
		assertEquals(1, toTest.getNumAgents());
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
			toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss);
		}
		assertEquals(numLoop, toTest.getNumAgents());
	}

	// AGENTS IS MEMORIZED IN THE rmap

	/**
	 * Test corner mine up left.
	 * @throws DMasonException 
	 */
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_left.size());
	}

	/**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 */
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_right.size());
	}

	/**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 */
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_down_left.size());
	}

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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;

			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.down_mine.size());
	}

	/**
	 * Test left mine.
	 * @throws DMasonException 
	 */
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.left_mine.size());
	}

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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
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
	 */
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
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}
		assertEquals(count, toTest.rmap.up_mine.size());

	}

	/**
	 * Test double set distributed object location god agent.
	 * @throws DMasonException 
	 */
	@Test
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		int i = toTest.rmap.up_mine.upl_xx;
		int j = toTest.rmap.up_mine.upl_yy;

		int stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 4;
		int stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 4;

		Int2D location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss);

		assertNotSame("the agent is in two places at once",
				toTest.rmap.up_mine.get(0).r, toTest.rmap.up_mine.get(1).r);

	}

	/**
	 * test for the field partitioning.
	 * 
	 * @throws DMasonException
	 */

	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 10;
		int h = 10;
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
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

		assertEquals(diag, diagwh);
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
						"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
						"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
						"test", /* prefix */"", true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
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
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
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
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 1, 1, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */1, /* j */1, 10, 10, mode,/* name */
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
		int maxD = 0;

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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

		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(w, h,/* simState */
						ss, maxD, /* i */0, /* j */0, 10, 10, mode,/* name */
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
		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(10, 10,/* simState */
						ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3,
						mode,/* name */
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
		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(10, 10,/* simState */
						ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3,
						mode,/* name */
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
		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(10, 10,/* simState */
						ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3,
						mode,/* name */
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
		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(10, 10,/* simState */
						ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3,
						mode,/* name */
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
		toTest = (DSparseGrid2DXYThin) DSparseGrid2DFactory
				.createDSparseGrid2DThin(10, 10,/* simState */
						ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3,
						mode,/* name */
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
