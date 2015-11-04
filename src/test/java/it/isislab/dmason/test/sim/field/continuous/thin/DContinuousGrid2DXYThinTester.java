package it.isislab.dmason.test.sim.field.continuous.thin;

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
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DXY;
import it.isislab.dmason.sim.field.continuous.thin.DContinuousGrid2DXYThin;
import it.isislab.dmason.test.sim.field.continuous.DContinuousGrid2DXYTester.StubRemotePositionedAgent;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.RemoteParam;
import it.isislab.dmason.util.connection.ConnectionType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;
/**
 * The Class DContinuous2DXYThinTester. Tests the DContinuous2DXYThin for the roroidal distribution.
 * 
 * * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *@author Mario Capuozzo
 * @author Mario Capuozzo
 */
public class DContinuousGrid2DXYThinTester {
	/** The to test. */
	DContinuousGrid2DXYThin toTest;

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
	public class StubDistributedState extends DistributedState<Double2D> {

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
		 * @param params the params
		 */
		public StubDistributedState(GeneralParam params) {
			super(params, new DistributedMultiSchedule<Double2D>(), "stub",
					params.getConnectionType());

			this.MODE = params.getMode();

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
		 */
		@Override
		public DistributedField<Double2D> getField() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#addToField(it.isislab.dmason.sim.engine.RemotePositionedAgent, java.lang.Object)
		 */
		@Override
		public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#getState()
		 */
		@Override
		public SimState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#setPortrayalForObject(java.lang.Object)
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
	RemotePositionedAgent<Double2D> {

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

		/* (non-Javadoc)
		 * @see sim.engine.Steppable#step(sim.engine.SimState)
		 */
		@Override
		public void step(SimState arg0) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#getPos()
		 */
		@Override
		public Double2D getPos() {
			// TODO Auto-generated method stub
			return new Double2D(0, 0);
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#setPos(java.lang.Object)
		 */
		@Override
		public void setPos(Double2D pos) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#getId()
		 */
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return id;
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.RemotePositionedAgent#setId(java.lang.String)
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
		sa= new RemoteParam<StubRemotePositionedAgent>(stubAgent);
		ss = new StubDistributedState(genParam);
		//		toTest = new DContinuous2DXY(/* discretization */0.5, width, height, /* simState */
		//		ss, maxDistance, /* i */0, /* j */0, rows, columns, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, width, height, ss, maxDistance, 2, 2, rows, columns, mode,
				"test", "", true);

	}

	/**
	 * Test set distributed object location.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocation() throws DMasonException {

		for (int i = 0; i < numLoop; i++) {
			Double2D location = toTest.getAvailableRandomLocation();
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
			Double2D location = toTest.getAvailableRandomLocation();
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
			Double2D location = toTest.getAvailableRandomLocation();
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
			Double2D location = toTest.getAvailableRandomLocation();
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

		double i = toTest.rmap.corner_mine_up_left.upl_xx;
		double j = toTest.rmap.corner_mine_up_left.upl_yy;

		double iEnd = toTest.rmap.corner_mine_up_left.down_xx;
		double jEnd = toTest.rmap.corner_mine_up_left.down_yy;

		double stepI = (toTest.rmap.corner_mine_up_left.down_xx - toTest.rmap.corner_mine_up_left.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.corner_mine_up_left.down_yy - toTest.rmap.corner_mine_up_left.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_left.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value corner mine up left.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineUpLeft() throws DMasonException {

		double i = toTest.rmap.corner_mine_up_left.upl_xx;
		double j = toTest.rmap.corner_mine_up_left.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						sa, /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.corner_mine_up_left.size());

	}

	/**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpRight() throws DMasonException {

		double i = toTest.rmap.corner_mine_up_right.upl_xx;
		double j = toTest.rmap.corner_mine_up_right.upl_yy;

		double iEnd = toTest.rmap.corner_mine_up_right.down_xx;
		double jEnd = toTest.rmap.corner_mine_up_right.down_yy;

		double stepI = (toTest.rmap.corner_mine_up_right.down_xx - toTest.rmap.corner_mine_up_right.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.corner_mine_up_right.down_yy - toTest.rmap.corner_mine_up_right.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_right.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value corner mine up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineUpRight() throws DMasonException {

		double i = toTest.rmap.corner_mine_up_right.upl_xx;
		double j = toTest.rmap.corner_mine_up_right.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.corner_mine_up_right.size());

	}

	/**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownLeft() throws DMasonException {

		double i = toTest.rmap.corner_mine_down_left.upl_xx;
		double j = toTest.rmap.corner_mine_down_left.upl_yy;

		double iEnd = toTest.rmap.corner_mine_down_left.down_xx;
		double jEnd = toTest.rmap.corner_mine_down_left.down_yy;

		double stepI = (toTest.rmap.corner_mine_down_left.down_xx - toTest.rmap.corner_mine_down_left.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.corner_mine_down_left.down_yy - toTest.rmap.corner_mine_down_left.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_left.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value corner mine down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineDownLeft() throws DMasonException {

		double i = toTest.rmap.corner_mine_down_left.upl_xx;
		double j = toTest.rmap.corner_mine_down_left.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.corner_mine_down_left.size());

	}

	/**
	 * Test corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownRight() throws DMasonException {

		double i = toTest.rmap.corner_mine_down_right.upl_xx;
		double j = toTest.rmap.corner_mine_down_right.upl_yy;

		double iEnd = toTest.rmap.corner_mine_down_right.down_xx;
		double jEnd = toTest.rmap.corner_mine_down_right.down_yy;

		double stepI = (toTest.rmap.corner_mine_down_right.down_xx - toTest.rmap.corner_mine_down_right.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.corner_mine_down_right.down_yy - toTest.rmap.corner_mine_down_right.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_right.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineDownRight() throws DMasonException {

		double i = toTest.rmap.corner_mine_down_right.upl_xx;
		double j = toTest.rmap.corner_mine_down_right.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.corner_mine_down_right.size());

	}

	/**
	 * Test down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMine() throws DMasonException {

		double i = toTest.rmap.down_mine.upl_xx;
		double j = toTest.rmap.down_mine.upl_yy;

		double iEnd = toTest.rmap.down_mine.down_xx;
		double jEnd = toTest.rmap.down_mine.down_yy;

		double stepI = (iEnd - i) / numLoop;
		double stepJ = (jEnd - j) / numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.down_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueDownMine() throws DMasonException {

		double i = toTest.rmap.down_mine.upl_xx;
		double j = toTest.rmap.down_mine.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.down_mine.size());

	}

	/**
	 * Test left mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMine() throws DMasonException {

		double i = toTest.rmap.left_mine.upl_xx;
		double j = toTest.rmap.left_mine.upl_yy;

		double iEnd = toTest.rmap.left_mine.down_xx;
		double jEnd = toTest.rmap.left_mine.down_yy;

		double stepI = (toTest.rmap.left_mine.down_xx - toTest.rmap.left_mine.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.left_mine.down_yy - toTest.rmap.left_mine.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.left_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value left mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueLeftMine() throws DMasonException {

		double i = toTest.rmap.left_mine.upl_xx;
		double j = toTest.rmap.left_mine.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.left_mine.size());

	}

	/**
	 * Test right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMine() throws DMasonException {

		double i = toTest.rmap.right_mine.upl_xx;
		double j = toTest.rmap.right_mine.upl_yy;

		double iEnd = toTest.rmap.right_mine.down_xx;
		double jEnd = toTest.rmap.right_mine.down_yy;

		double stepI = (toTest.rmap.right_mine.down_xx - toTest.rmap.right_mine.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.right_mine.down_yy - toTest.rmap.right_mine.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.right_mine.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueRightMine() throws DMasonException {

		double i = toTest.rmap.right_mine.upl_xx;
		double j = toTest.rmap.right_mine.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.right_mine.size());

	}

	/**
	 * Test up mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMine() throws DMasonException {

		double i = toTest.rmap.up_mine.upl_xx;
		double j = toTest.rmap.up_mine.upl_yy;

		double iEnd = toTest.rmap.up_mine.down_xx;
		double jEnd = toTest.rmap.up_mine.down_yy;

		double stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = stepJ + toTest.rmap.up_mine.upl_yy;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
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
	 * Test boundary value up mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueUpMine() throws DMasonException {

		double i = toTest.rmap.up_mine.upl_xx;
		double j = toTest.rmap.up_mine.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.up_mine.size());

	}

	/**
	 * Test set distributed object location congruence size.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocationCongruenceSize() throws DMasonException {
		double i = toTest.rmap.up_mine.upl_xx;
		double j = toTest.rmap.up_mine.upl_yy;

		double stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 3;
		double stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 3;

		Double2D location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		assertEquals("duplication of agents", 1, toTest.rmap.up_mine.size());

	}

	/**
	 * Test double set distributed object location god agent.
	 * @throws DMasonException 
	 */
	@Test
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		double i = toTest.rmap.up_mine.upl_xx;
		double j = toTest.rmap.up_mine.upl_yy;

		double stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 4;
		double stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 4;

		Double2D location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss);

		assertNotSame("the agent is in two places at once",
				toTest.rmap.up_mine.get(0).r, toTest.rmap.up_mine.get(1).r);

	}

	/**
	 * Test set distributed object location change position.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocationChangePosition() throws DMasonException {
		double i = toTest.rmap.up_mine.upl_xx;
		double j = toTest.rmap.up_mine.upl_yy;

		double stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 4;
		double stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 4;

		Double2D location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				new RemoteParam<StubRemotePositionedAgent>(new StubRemotePositionedAgent()), /* SimState */ss);

		assertNotSame("the method has not changed the position",
				toTest.rmap.up_mine.get(0).l, toTest.rmap.up_mine.get(1).l);
	}

	/**
	 * test for the field partitioning.
	 * @throws DMasonException 
	 */

	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		double w = 10.0;
		double h = 10.0;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.myfield.down_xx;
		Double x1 = toTest.myfield.upl_xx;
		Double y2 = toTest.myfield.down_yy;
		Double y1 = toTest.myfield.upl_yy;

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w, 2.0) + Math.pow(h, 2.0));

		assertEquals(diag, diagwh);
	}

	/**
	 * Test my field partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		double w = 10.0;
		double h = 10.0;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);


		Double x2 = toTest.myfield.down_xx;
		Double x1 = toTest.myfield.upl_xx;
		Double y2 = toTest.myfield.down_yy;
		Double y1 = toTest.myfield.upl_yy;

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w - 2 * maxD, 2.0)
				+ Math.pow(h - 2 * maxD, 2.0));

		assertEquals(diag, diagwh);
	}

	/**
	 * Test my field partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testMyFieldPartitioningWHX() throws DMasonException {

		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; (j < i) && (j < k); j++) {
					// i need that w and h is equal for using the Pitagora's
					// theorem
					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.myfield.down_xx;
					Double x1 = toTest.myfield.upl_xx;
					Double y2 = toTest.myfield.down_yy;
					Double y1 = toTest.myfield.upl_yy;

					// find diagonal with the theorem of distance between 2
					// points
					Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
							+ Math.pow(y2 - y1, 2.0));

					// find diagonal with the theorem of Pitagora
					Double diagwh = Math.sqrt(Math.pow(w - 2 * maxD, 2.0)
							+ Math.pow(h - 2 * maxD, 2.0));

					assertEquals(diag, diagwh);
				}
			}
		}
	}

	/**
	 * Test up mine partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMinePartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.up_mine.down_xx;
		Double x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test down mine partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMinePartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.up_mine.down_xx;
		Double x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test down mine partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMinePartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.up_mine.down_xx;
					Double x1 = toTest.rmap.up_mine.upl_xx;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.width, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test down mine partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMinePartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.down_mine.down_xx;
		Double x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test up mine partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMinePartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.down_mine.down_xx;
		Double x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test up mine partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMinePartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.down_mine.down_xx;
					Double x1 = toTest.rmap.down_mine.upl_xx;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.width, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test left mine partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMinePartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.left_mine.down_yy;
		Double x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test left mine partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMinePartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.left_mine.down_yy;
		Double x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test left mine partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMinePartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.left_mine.down_yy;
					Double x1 = toTest.rmap.left_mine.upl_yy;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.height, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test right mine partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMinePartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.right_mine.down_yy;
		Double x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test right mine partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMinePartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.right_mine.down_yy;
		Double x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test right mine partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMinePartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.right_mine.down_yy;
					Double x1 = toTest.rmap.right_mine.upl_yy;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.height, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test up out partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpOutPartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.up_out.down_xx;
		Double x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test down out partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownOutPartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.up_out.down_xx;
		Double x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test down out partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownOutPartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.up_out.down_xx;
					Double x1 = toTest.rmap.up_out.upl_xx;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.width, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test down out partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownOutPartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.down_out.down_xx;
		Double x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test up out partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpOutPartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.down_out.down_xx;
		Double x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.width, side.doubleValue(), 0);
	}

	/**
	 * Test up out partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpOutPartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.down_out.down_xx;
					Double x1 = toTest.rmap.down_out.upl_xx;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.width, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test left out partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftOutPartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.left_out.down_yy;
		Double x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test left out partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftOutPartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.left_out.down_yy;
		Double x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test left out partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftOutPartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.left_out.down_yy;
					Double x1 = toTest.rmap.left_out.upl_yy;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.height, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test right out partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightOutPartitioning() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 0;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.right_out.down_yy;
		Double x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test right out partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightOutPartitioningMaxDistance1() throws DMasonException {

		double w = 10;
		double h = w;
		int maxD = 1;

		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.right_out.down_yy;
		Double x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Double side = x2 - x1;

		assertEquals(toTest.myfield.height, side.doubleValue(), 0);
	}

	/**
	 * Test right out partitioning whx.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightOutPartitioningWHX() throws DMasonException {
		for (double i = 1; i < numLoop; i++) {
			for (double k = 1; k < numLoop; k++) {
				for (int j = 0; j < i; j++) {

					double w = i;
					double h = k;
					int maxD = j;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.right_out.down_yy;
					Double x1 = toTest.rmap.right_out.upl_yy;

					// find distance between 2 points
					Double side = x2 - x1;

					assertEquals(toTest.myfield.height, side.doubleValue(), 0);
				}
			}
		}
	}

	/**
	 * Test corner mine down left partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownLeftPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.corner_mine_down_left.down_xx;
			Double y1 = toTest.rmap.corner_mine_down_left.down_yy;
			Double x2 = toTest.rmap.corner_mine_down_left.upl_xx;
			Double y2 = toTest.rmap.corner_mine_down_left.upl_yy;

			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner mine down right partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownRightPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.corner_mine_down_right.down_xx;
			Double y1 = toTest.rmap.corner_mine_down_right.down_yy;
			Double x2 = toTest.rmap.corner_mine_down_right.upl_xx;
			Double y2 = toTest.rmap.corner_mine_down_right.upl_yy;

			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner mine up left partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpLeftPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.corner_mine_up_left.down_xx;
			Double y1 = toTest.rmap.corner_mine_up_left.down_yy;
			Double x2 = toTest.rmap.corner_mine_up_left.upl_xx;
			Double y2 = toTest.rmap.corner_mine_up_left.upl_yy;

			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner mine up right partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpRightPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.corner_mine_up_right.down_xx;
			Double y1 = toTest.rmap.corner_mine_up_right.down_yy;
			Double x2 = toTest.rmap.corner_mine_up_right.upl_xx;
			Double y2 = toTest.rmap.corner_mine_up_right.upl_yy;

			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down left diag center partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerOutDownLeftDiagCenterPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.corner_out_down_left_diag_center.down_xx;
			Double y1 = toTest.rmap.corner_out_down_left_diag_center.down_yy;
			Double x2 = toTest.rmap.corner_out_down_left_diag_center.upl_xx;
			Double y2 = toTest.rmap.corner_out_down_left_diag_center.upl_yy;

			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down left diag down partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutDownLeftDiagDownPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_down_left_diag_down.upl_xx;
				y2 = toTest.rmap.corner_out_down_left_diag_down.upl_yy;
				x1 = toTest.rmap.corner_out_down_left_diag_down.down_xx;
				y1 = toTest.rmap.corner_out_down_left_diag_down.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down left diag left partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutDownLeftDiagLeftPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_down_left_diag_left.upl_xx;
				y2 = toTest.rmap.corner_out_down_left_diag_left.upl_yy;
				x1 = toTest.rmap.corner_out_down_left_diag_left.down_xx;
				y1 = toTest.rmap.corner_out_down_left_diag_left.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down rigth diag center partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerOutDownRigthDiagCenterPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_down_right_diag_center.upl_xx;
				y2 = toTest.rmap.corner_out_down_right_diag_center.upl_yy;
				x1 = toTest.rmap.corner_out_down_right_diag_center.down_xx;
				y1 = toTest.rmap.corner_out_down_right_diag_center.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down rigth diag down partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutDownRigthDiagDownPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_down_right_diag_down.upl_xx;
				y2 = toTest.rmap.corner_out_down_right_diag_down.upl_yy;
				x1 = toTest.rmap.corner_out_down_right_diag_down.down_xx;
				y1 = toTest.rmap.corner_out_down_right_diag_down.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out down rigth diag right partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutDownRigthDiagRightPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_down_right_diag_right.upl_xx;
				y2 = toTest.rmap.corner_out_down_right_diag_right.upl_yy;
				x1 = toTest.rmap.corner_out_down_right_diag_right.down_xx;
				y1 = toTest.rmap.corner_out_down_right_diag_right.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out up left diag center partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerOutUpLeftDiagCenterPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_left_diag_center.upl_xx;
				y2 = toTest.rmap.corner_out_up_left_diag_center.upl_yy;
				x1 = toTest.rmap.corner_out_up_left_diag_center.down_xx;
				y1 = toTest.rmap.corner_out_up_left_diag_center.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}
	}

	/**
	 * Test corner out up left diag left partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutUpLeftDiagLeftPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			//			toTest.setToroidal(true);
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_left_diag_left.upl_xx;
				y2 = toTest.rmap.corner_out_up_left_diag_left.upl_yy;
				x1 = toTest.rmap.corner_out_up_left_diag_left.down_xx;
				y1 = toTest.rmap.corner_out_up_left_diag_left.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out up left diag up partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutUpLeftDiagUpPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_left_diag_up.upl_xx;
				y2 = toTest.rmap.corner_out_up_left_diag_up.upl_yy;
				x1 = toTest.rmap.corner_out_up_left_diag_up.down_xx;
				y1 = toTest.rmap.corner_out_up_left_diag_up.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out up right diag center partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerOutUpRightDiagCenterPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_right_diag_center.upl_xx;
				y2 = toTest.rmap.corner_out_up_right_diag_center.upl_yy;
				x1 = toTest.rmap.corner_out_up_right_diag_center.down_xx;
				y1 = toTest.rmap.corner_out_up_right_diag_center.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out up right diag right partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutUpRightDiagRightPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_right_diag_right.upl_xx;
				y2 = toTest.rmap.corner_out_up_right_diag_right.upl_yy;
				x1 = toTest.rmap.corner_out_up_right_diag_right.down_xx;
				y1 = toTest.rmap.corner_out_up_right_diag_right.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner out up right diag up partitioning.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCornerOutUpRightDiagUpPartitioning() throws DMasonException {
		for (int i = 1; i < numLoop; i++) {
			//			toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
			//			"test", /* prefix */"");
			toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.corner_out_up_right_diag_up.upl_xx;
				y2 = toTest.rmap.corner_out_up_right_diag_up.upl_yy;
				x1 = toTest.rmap.corner_out_up_right_diag_up.down_xx;
				y1 = toTest.rmap.corner_out_up_right_diag_up.down_yy;
			} catch (NullPointerException e) {
				fail("corner has no size");
			}
			// find diagonal with the theorem of distance between 2
			// points
			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
					+ Math.pow(y2 - y1, 2.0));

			// find diagonal with the theorem of Pitagora
			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));

			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
		}

	}

	/**
	 * Test corner congruence up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerCongruenceUpRight() throws DMasonException {
		//		toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.corner_mine_up_right.upl_xx,
				toTest.rmap.corner_out_up_right_diag_center.down_xx - 2, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_right.upl_yy,
				toTest.rmap.corner_out_up_right_diag_center.down_yy, 0);
	}

	/**
	 * Test corner congruence up left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerCongruenceUpLeft() throws DMasonException {
		//		toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.corner_mine_up_left.upl_xx,
				toTest.rmap.corner_out_up_left_diag_center.down_xx, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_left.upl_yy,
				toTest.rmap.corner_out_up_left_diag_center.down_yy, 0);
	}

	/**
	 * Test corner congruence down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerCongruenceDownLeft() throws DMasonException {
		//		toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.corner_mine_down_left.upl_xx,
				toTest.rmap.corner_out_down_left_diag_center.down_xx, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_left.upl_yy,
				toTest.rmap.corner_out_down_left_diag_center.down_yy - 2, 0);
	}

	/**
	 * Test corner congruence down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerCongruenceDownRight() throws DMasonException {
		//		toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.corner_mine_down_right.down_xx,
				toTest.rmap.corner_out_down_right_diag_center.upl_xx, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_right.down_yy,
				toTest.rmap.corner_out_down_right_diag_center.upl_yy, 0);
	}

	/**
	 * Test my field congruence.
	 * @throws DMasonException 
	 */
	@Test
	public void testMyFieldCongruence() throws DMasonException {
		//		toTest = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

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

	/**
	 * Test congruence for all xyrc not boundary.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testCongruenceForAllXYRCNotBoundary() throws DMasonException {

		int r, c;
		int nLoop = numLoop;
		r = nLoop;
		c = nLoop;

		for (int ir = 3; ir < r; ir++) {
			for (int ic = 3; ic < c; ic++) {
				for (int ii = 1; ii < ir - 1; ii++) {
					for (int ij = 1; ij < ic - 1; ij++) {

						toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(/* discretization */0.5,
								10, 10, /* simState */
								ss, 1, /* i */ii, /* j */ij, /* rows */ir, /* Colums */
								ic,mode, /* name */
								"test", /* prefix */"",true);

						// cornerUpRight
						assertEquals(
								"cornerUpRight x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_up_right.upl_xx,
								toTest.rmap.corner_out_up_right_diag_center.down_xx - 2,
								0);
						assertEquals(
								"cornerUpRight y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_up_right.upl_yy,
								toTest.rmap.corner_out_up_right_diag_center.down_yy,
								0);

						// cornerUpLeft
						assertEquals(
								"cornerUpLeft x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_up_left.upl_xx,
								toTest.rmap.corner_out_up_left_diag_center.down_xx,
								0);
						assertEquals(
								"cornerUpLeft y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_up_left.upl_yy,
								toTest.rmap.corner_out_up_left_diag_center.down_yy,
								0);

						// cornerDownLeft
						assertEquals(
								"cornerDownLeft x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_down_left.upl_xx,
								toTest.rmap.corner_out_down_left_diag_center.down_xx,
								0);
						assertEquals(
								"cornerDownLeft y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_down_left.upl_yy,
								toTest.rmap.corner_out_down_left_diag_center.down_yy - 2,
								0);

						// cornerDownRight
						assertEquals(
								"cornerDownRight x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_down_right.down_xx,
								toTest.rmap.corner_out_down_right_diag_center.upl_xx,
								0);
						assertEquals(
								"cornerDownRight y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_mine_down_right.down_yy,
								toTest.rmap.corner_out_down_right_diag_center.upl_yy,
								0);

						// myField upLeft
						assertEquals("myField X Up Left i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.myfield.upl_xx,
								toTest.rmap.up_mine.upl_xx + 1, 0);
						assertEquals("myField Y Up Left i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.myfield.upl_yy,
								toTest.rmap.up_mine.upl_yy + 1, 0);
						// myField downRight
						assertEquals("myField X Down Right i=" + ii + " j="
								+ ij + " rows=" + ir + " colums=" + ic,
								toTest.myfield.down_xx,
								toTest.rmap.down_mine.down_xx - 1, 0);
						assertEquals("myField Y Down Right i=" + ii + " j="
								+ ij + " rows=" + ir + " colums=" + ic,
								toTest.myfield.down_yy,
								toTest.rmap.down_mine.down_yy - 1, 0);
					}
				}
			}
		}
	}

	/**
	 * Test for all partitioning.
	 * @throws DMasonException 
	 */
	@Test
	public void testForAllPartitioning() throws DMasonException {
		int r, c;
		r = numLoop;
		c = numLoop;
		width = numLoop - 1;
		height = numLoop;

		DContinuousGrid2DXY nord = null;
		DContinuousGrid2DXY sud = null;
		DContinuousGrid2DXY est = null;
		DContinuousGrid2DXY ovest = null;
		DContinuousGrid2DXY nordEst = null;
		DContinuousGrid2DXY sudEst = null;
		DContinuousGrid2DXY sudOvest = null;
		DContinuousGrid2DXY nordOvest = null;

		for (int ir = 2; ir < r; ir++) {
			for (int ic = 2; ic < c; ic++) {
				int contH = 0;
				for (int ii = 0; ii < ir; ii++) {
					int contW = 0;
					for (int ij = 0; ij < ic; ij++) {

						toTest = (DContinuousGrid2DXYThin) DContinuousGrid2DFactory.createDContinuous2DThin(/* discretization */0.5,
								width, height, /* simState */
								ss, 1, /* i */ii, /* j */ij, /* rows */ir, /* Colums */
								ic,mode, /* name */
								"test", /* prefix */"",true);

						// neighbors
						// up
						if (ii == 0 && ij > 0 && ij < ic - 1) {

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ii == ir - 1 && ij > 0 && ij < ic - 1) {// down

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */0, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ij == 0 && ii > 0 && ii < ir - 1) { // left

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ij == ic - 1 && ii > 0 && ii < ir - 1) { // right

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ii > 0 && ij > 0 && ii < ir - 1
								&& ij < ic - 1) { // center

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ii == 0 && ij == 0) { // up-left

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ii == ir - 1 && ij == 0) { // down-left

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */0, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */ij + 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");

						} else if (ii == ir - 1 && ij == ic - 1) { // down-right

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */0, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii - 1, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */0, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
						} else if (ii == 0 && ij == ic - 1) { // upRight

							nord = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sud = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ic - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							est = new DContinuousGrid2DXY(/* discretization */0.5,
									width, height, /* simState */
									ss, 1, /* i */0, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							ovest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							nordEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ir - 1, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudOvest = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */ij - 1, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
							sudEst = new DContinuousGrid2DXY(
									/* discretization */0.5, width, height, /* simState */
									ss, 1, /* i */ii + 1, /* j */0, /* rows */
									ir, /* Colums */
									ic, /* name */
									"test", /* prefix */"");
						}

						/* verify congruence */

						// height
						assertEquals("incongruence height with ovest i=" + ii
								+ " j=" + ij + " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_mine.down_yy
								- toTest.rmap.up_mine.upl_yy,
								ovest.rmap.down_mine.down_yy
								- ovest.rmap.up_mine.upl_yy, 0);
						assertEquals("incongruence height with est i=" + ii
								+ " j=" + ij + " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_mine.down_yy
								- toTest.rmap.up_mine.upl_yy,
								est.rmap.down_mine.down_yy
								- est.rmap.up_mine.upl_yy, 0);

						// width
						assertEquals("incongruence width with nord i=" + ii
								+ " j=" + ij + " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_mine.down_xx
								- toTest.rmap.down_mine.upl_xx,
								nord.rmap.down_mine.down_xx
								- nord.rmap.down_mine.upl_xx, 0);
						assertEquals("incongruence width with sud i=" + ii
								+ " j=" + ij + " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_mine.down_xx
								- toTest.rmap.down_mine.upl_xx,
								sud.rmap.down_mine.down_xx
								- sud.rmap.down_mine.upl_xx, 0);

						// upOut
						assertEquals("upOut downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.up_out.down_xx,
								nord.rmap.down_mine.down_xx, 0);
						assertEquals("upOut downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.up_out.down_yy,
								nord.rmap.down_mine.down_yy, 0);

						assertEquals("upOut uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.up_out.upl_xx,
								nord.rmap.down_mine.upl_xx, 0);
						assertEquals("upOut uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.up_out.upl_yy,
								nord.rmap.down_mine.upl_yy, 0);

						// downOut
						assertEquals("downOut downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_out.down_xx,
								sud.rmap.up_mine.down_xx, 0);
						assertEquals("downOut downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_out.down_yy,
								sud.rmap.up_mine.down_yy, 0);

						assertEquals("downOut uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_out.upl_xx,
								sud.rmap.up_mine.upl_xx, 0);
						assertEquals("downOut uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.down_out.upl_yy,
								sud.rmap.up_mine.upl_yy, 0);

						// leftOut

						assertEquals("leftOut downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.left_out.down_xx,
								ovest.rmap.right_mine.down_xx, 0);
						assertEquals("leftOut downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.left_out.down_yy,
								ovest.rmap.right_mine.down_yy, 0);

						assertEquals("leftOut uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.left_out.upl_xx,
								ovest.rmap.right_mine.upl_xx, 0);
						assertEquals("leftOut uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.left_out.upl_yy,
								ovest.rmap.right_mine.upl_yy, 0);

						// rightOut

						assertEquals("rightOut downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.right_out.down_xx,
								est.rmap.left_mine.down_xx, 0);
						assertEquals("rightOut downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.right_out.down_yy,
								est.rmap.left_mine.down_yy, 0);

						assertEquals("rightOut uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.right_out.upl_xx,
								est.rmap.left_mine.upl_xx, 0);
						assertEquals("rightOut uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.right_out.upl_yy,
								est.rmap.left_mine.upl_yy, 0);

						// cornerOutUpLeft

						assertEquals(
								"cornerOutUpLeft downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_left_diag_center.down_xx,
								nordOvest.rmap.corner_mine_down_right.down_xx,
								0);
						assertEquals(
								"cornerOutUpLeft downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_left_diag_center.down_yy,
								nordOvest.rmap.corner_mine_down_right.down_yy,
								0);
						assertEquals(
								"cornerOutUpLeft uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_left_diag_center.upl_xx,
								nordOvest.rmap.corner_mine_down_right.upl_xx, 0);
						assertEquals(
								"cornerOutUpLeft uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_left_diag_center.upl_yy,
								nordOvest.rmap.corner_mine_down_right.upl_yy, 0);

						// cornerOutUpRight

						assertEquals(
								"cornerOutUpRight downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_right_diag_center.down_xx,
								nordEst.rmap.corner_mine_down_left.down_xx, 0);
						assertEquals(
								"cornerOutUpRight downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_right_diag_center.down_yy,
								nordEst.rmap.corner_mine_down_left.down_yy, 0);
						assertEquals(
								"cornerOutUpRight uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_right_diag_center.upl_xx,
								nordEst.rmap.corner_mine_down_left.upl_xx, 0);
						assertEquals(
								"cornerOutUpRight uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_up_right_diag_center.upl_yy,
								nordEst.rmap.corner_mine_down_left.upl_yy, 0);

						// cornerOutDownRight
						assertEquals(
								"cornerOutDownRight downXX i=" + ii + " j="
										+ ij + " rows=" + ir + " colums=" + ic,
										toTest.rmap.corner_out_down_right_diag_center.down_xx,
										sudEst.rmap.corner_mine_up_left.down_xx, 0);
						assertEquals(
								"cornerOutDownRight downYY i=" + ii + " j="
										+ ij + " rows=" + ir + " colums=" + ic,
										toTest.rmap.corner_out_down_right_diag_center.down_yy,
										sudEst.rmap.corner_mine_up_left.down_yy, 0);
						assertEquals(
								"cornerOutDownRight uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_right_diag_center.upl_xx,
								sudEst.rmap.corner_mine_up_left.upl_xx, 0);
						assertEquals(
								"cornerOutDownRight uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_right_diag_center.upl_yy,
								sudEst.rmap.corner_mine_up_left.upl_yy, 0);

						// cornerOutDownLeft
						assertEquals(
								"cornerOutDownLeft downXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_left_diag_center.down_xx,
								sudOvest.rmap.corner_mine_up_right.down_xx, 0);
						assertEquals(
								"cornerOutDownLeft downYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_left_diag_center.down_yy,
								sudOvest.rmap.corner_mine_up_right.down_yy, 0);
						assertEquals(
								"cornerOutDownLeft uplXX i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_left_diag_center.upl_xx,
								sudOvest.rmap.corner_mine_up_right.upl_xx, 0);
						assertEquals(
								"cornerOutDownLeft uplYY i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_left_diag_center.upl_yy,
								sudOvest.rmap.corner_mine_up_right.upl_yy, 0);

						// cornerMineUpLeft/nordOvest congruence
						assertEquals(
								"cornerMineUpLeft/nordOvest congruence xx i="
										+ ii + " j=" + ij + " rows=" + ir
										+ " colums=" + ic,
										toTest.rmap.corner_mine_up_left.upl_xx,
										nordOvest.rmap.corner_mine_down_right.down_xx
										% width, 0);

						assertEquals("cornerMineUp/nordOvest congruence yy i="
								+ ii + " j=" + ij + " rows=" + ir + " colums="
								+ ic, toTest.rmap.corner_mine_up_left.upl_yy,
								nordOvest.rmap.corner_mine_down_right.down_yy
								% height, 0);

						// cornerMineUpLeft/ovest congruence
						assertEquals(
								"cornerMineUpLeft/ovest congruence xx i=" + ii
								+ " j=" + ij + " rows=" + ir
								+ " colums=" + ic,
								toTest.rmap.corner_mine_up_left.upl_xx,
								ovest.rmap.corner_mine_up_right.down_xx % width,
								0);

						assertEquals(
								"cornerMineUpLeft/ovest congruence yy i=" + ii
								+ " j=" + ij + " rows=" + ir
								+ " colums=" + ic,
								toTest.rmap.corner_mine_up_left.upl_yy,
								ovest.rmap.corner_mine_up_right.upl_yy % height,
								0);

						// cornerMineUpLeft/nord congruence
						assertEquals("cornerMineUpLeft/nord congruence xx i="
								+ ii + " j=" + ij + " rows=" + ir + " colums="
								+ ic, toTest.rmap.corner_mine_up_left.upl_xx,
								nord.rmap.corner_mine_down_left.upl_xx % width,
								0);

						assertEquals("cornerMineUpLeft/nord congruence yy i="
								+ ii + " j=" + ij + " rows=" + ir + " colums="
								+ ic, toTest.rmap.corner_mine_up_left.upl_yy,
								nord.rmap.corner_mine_down_left.down_yy
								% height, 0);

						// diagonal congruence
						Double x1 = toTest.rmap.corner_mine_up_left.upl_xx;
						Double y1 = toTest.rmap.corner_mine_up_left.upl_yy;

						Double x2 = toTest.rmap.corner_mine_down_right.down_xx;
						Double y2 = toTest.rmap.corner_mine_down_right.down_yy;

						Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
								+ Math.pow(y2 - y1, 2.0));

						// find diagonal with the theorem of Pitagora
						Double realDiag = Math.sqrt(Math.pow(toTest.my_height,
								2.0) + Math.pow(toTest.my_width, 2.0));

						assertEquals("i=" + ii + " j=" + ij + " rows=" + ir
								+ " colums=" + ic, diag, realDiag, 0);

						// width-height congruence
						contW += toTest.rmap.corner_mine_down_right.down_xx
								- toTest.rmap.corner_mine_up_left.upl_xx;
					}
					assertEquals("width error rows=" + ir + " colums=" + ic,
							width, contW);

					contH += toTest.rmap.corner_mine_down_right.down_yy
							- toTest.rmap.corner_mine_up_left.upl_yy;

				}
				assertEquals("height error  rows=" + ir + " colums=" + ic,
						height, contH);

			}
		}

	}
}
