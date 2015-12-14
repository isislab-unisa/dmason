package it.isislab.dmason.test.sim.field.continuous;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DXY;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

// TODO: Auto-generated Javadoc
/**
 * Test the Class DContinuous2DXY for a toroidal distribution mode.
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 * 
 */
public class DContinuousGrid2DXYTester {

	/** The to test. */
	DContinuousGrid2DXY toTest;

	/** The distributed state. */
	StubDistributedState ss;

	/** The remote agent. */
	StubRemotePositionedAgent sa;
	
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
		 * @param params
		 *            the params
		 */
		public StubDistributedState(GeneralParam params) {
			super(params, new DistributedMultiSchedule<Double2D>(), "stub",
					params.getConnectionType());

			this.MODE = params.getMode();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
		 */
		@Override
		public DistributedField<Double2D> getField() {
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
		public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
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
			Random r = new Random();
			
			id = "stub-"+r.nextInt();
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
		public Double2D getPos() {
			// TODO Auto-generated method stub
			return new Double2D(0, 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.engine.RemotePositionedAgent#setPos(java.lang
		 * .Object)
		 */
		@Override
		public void setPos(Double2D pos) {
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

		width = 110;
		height = 110;
		maxDistance = 1;
		rows = 5;
		columns = 5;
		numAgents = numLoop;
		mode = DistributedField2D.SQUARE_DISTRIBUTION_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		sa = new StubRemotePositionedAgent();
		
		ss = new StubDistributedState(genParam);
		//		toTest = new DContinuous2DXY(/* discretization */0.5, width, height, /* simState */
		//		ss, maxDistance, /* i */0, /* j */0, rows, columns, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, width, height, ss, maxDistance, 0, 0, rows, columns, mode,
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
					new StubRemotePositionedAgent(), /* SimState */ss));
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
			Double2D location = toTest.getAvailableRandomLocation();
			
			toTest.setDistributedObjectLocation(location,
					/* RemotePositionedAgent */	new StubRemotePositionedAgent(),
					/* SimState */ss);
		}
		assertEquals(numLoop, toTest.myfield.size());
	}

	/**
	 * Test get all visible agent0.
	 */
	@Ignore
	public void testGetAllVisibleAgent0() {

		assertEquals(0, toTest.getAllVisibleAgent().size());
	}

	/**
	 * Test get all visible agent x.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testGetAllVisibleAgentX() throws DMasonException {
		for (int i = 0; i < numLoop; i++) {

			Double2D location = toTest.getAvailableRandomLocation();
			
			toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					new StubRemotePositionedAgent(), /* SimState */ss);
		}

		assertEquals(numLoop, toTest.getAllVisibleAgent().size());

	}

	/**
	 * Test reset add all true.
	 * @throws DMasonException 
	 */
	@Ignore
	public void testResetAddAllTrue() throws DMasonException {
		for (int i = 0; i < numLoop; i++) {

			Double2D location = toTest.getAvailableRandomLocation();
			
			toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
					new StubRemotePositionedAgent(), /* SimState */ss);
		}
		ArrayList<RemotePositionedAgent<Double2D>> ag = new ArrayList<RemotePositionedAgent<Double2D>>();
		for (int i = 0; i < 10; i++) {
			ag.add(sa);
		}

		assertTrue(toTest.resetAddAll(ag));
	}

	// AGENTS IS MEMORIZED IN THE rmap

	/**
	 * Test corner mine up left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpLeft() throws DMasonException {

		double i = toTest.rmap.NORTH_WEST_MINE.upl_xx;
		double j = toTest.rmap.NORTH_WEST_MINE.upl_yy;

		double iEnd = toTest.rmap.NORTH_WEST_MINE.down_xx;
		double jEnd = toTest.rmap.NORTH_WEST_MINE.down_yy;

		double stepI = (toTest.rmap.NORTH_WEST_MINE.down_xx - toTest.rmap.NORTH_WEST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.NORTH_WEST_MINE.down_yy - toTest.rmap.NORTH_WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;
	
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.NORTH_WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
	
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.NORTH_WEST_MINE.size());
	}

	/**
	 * Test boundary value corner mine up left.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineUpLeft() throws DMasonException {

		double i = toTest.rmap.NORTH_WEST_MINE.upl_xx;
		double j = toTest.rmap.NORTH_WEST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						sa, /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.NORTH_WEST_MINE.size());

	}

	/**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineUpRight() throws DMasonException {

		double i = toTest.rmap.NORTH_EAST_MINE.upl_xx;
		double j = toTest.rmap.NORTH_EAST_MINE.upl_yy;

		double iEnd = toTest.rmap.NORTH_EAST_MINE.down_xx;
		double jEnd = toTest.rmap.NORTH_EAST_MINE.down_yy;

		double stepI = (toTest.rmap.NORTH_EAST_MINE.down_xx - toTest.rmap.NORTH_EAST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.NORTH_EAST_MINE.down_yy - toTest.rmap.NORTH_EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.NORTH_EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
		
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.NORTH_EAST_MINE.size());
	}

	/**
	 * Test boundary value corner mine up right.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineUpRight() throws DMasonException {

		double i = toTest.rmap.NORTH_EAST_MINE.upl_xx;
		double j = toTest.rmap.NORTH_EAST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);
		
		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.NORTH_EAST_MINE.size());

	}

	/**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownLeft() throws DMasonException {

		double i = toTest.rmap.SOUTH_WEST_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_WEST_MINE.upl_yy;

		double iEnd = toTest.rmap.SOUTH_WEST_MINE.down_xx;
		double jEnd = toTest.rmap.SOUTH_WEST_MINE.down_yy;

		double stepI = (toTest.rmap.SOUTH_WEST_MINE.down_xx - toTest.rmap.SOUTH_WEST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.SOUTH_WEST_MINE.down_yy - toTest.rmap.SOUTH_WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
			
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_WEST_MINE.size());
	}

	/**
	 * Test boundary value corner mine down left.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineDownLeft() throws DMasonException {

		double i = toTest.rmap.SOUTH_WEST_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_WEST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);
		
		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.SOUTH_WEST_MINE.size());

	}

	/**
	 * Test corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testCornerMineDownRight() throws DMasonException {

		double i = toTest.rmap.SOUTH_EAST_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_EAST_MINE.upl_yy;

		double iEnd = toTest.rmap.SOUTH_EAST_MINE.down_xx;
		double jEnd = toTest.rmap.SOUTH_EAST_MINE.down_yy;

		double stepI = (toTest.rmap.SOUTH_EAST_MINE.down_xx - toTest.rmap.SOUTH_EAST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.SOUTH_EAST_MINE.down_yy - toTest.rmap.SOUTH_EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);

				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_EAST_MINE.size());
	}

	/**
	 * Test boundary value corner mine down right.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueCornerMineDownRight() throws DMasonException {

		double i = toTest.rmap.SOUTH_EAST_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_EAST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);
		
		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1,
				toTest.rmap.SOUTH_EAST_MINE.size());

	}

	/**
	 * Test down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testDownMine() throws DMasonException {

		double i = toTest.rmap.SOUTH_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_MINE.upl_yy;

		double iEnd = toTest.rmap.SOUTH_MINE.down_xx;
		double jEnd = toTest.rmap.SOUTH_MINE.down_yy;

		double stepI = (iEnd - i) / numLoop;
		double stepJ = (jEnd - j) / numLoop;

		i += stepI;
		
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.SOUTH_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
		
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;

			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.SOUTH_MINE.size());
	}

	/**
	 * Test boundary value down mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueDownMine() throws DMasonException {

		double i = toTest.rmap.SOUTH_MINE.upl_xx;
		double j = toTest.rmap.SOUTH_MINE.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.SOUTH_MINE.size());

	}

	/**
	 * Test left mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testLeftMine() throws DMasonException {

		double i = toTest.rmap.WEST_MINE.upl_xx;
		double j = toTest.rmap.WEST_MINE.upl_yy;

		double iEnd = toTest.rmap.WEST_MINE.down_xx;
		double jEnd = toTest.rmap.WEST_MINE.down_yy;

		double stepI = (toTest.rmap.WEST_MINE.down_xx - toTest.rmap.WEST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.WEST_MINE.down_yy - toTest.rmap.WEST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.WEST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.WEST_MINE.size());
	}

	/**
	 * Test boundary value left mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueLeftMine() throws DMasonException {

		double i = toTest.rmap.WEST_MINE.upl_xx;
		double j = toTest.rmap.WEST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.WEST_MINE.size());

	}

	/**
	 * Test right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testRightMine() throws DMasonException {

		double i = toTest.rmap.EAST_MINE.upl_xx;
		double j = toTest.rmap.EAST_MINE.upl_yy;

		double iEnd = toTest.rmap.EAST_MINE.down_xx;
		double jEnd = toTest.rmap.EAST_MINE.down_yy;

		double stepI = (toTest.rmap.EAST_MINE.down_xx - toTest.rmap.EAST_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.EAST_MINE.down_yy - toTest.rmap.EAST_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.EAST_MINE.upl_yy + stepJ;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.EAST_MINE.size());
	}

	/**
	 * Test boundary value right mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueRightMine() throws DMasonException {

		double i = toTest.rmap.EAST_MINE.upl_xx;
		double j = toTest.rmap.EAST_MINE.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.EAST_MINE.size());

	}

	/**
	 * Test up mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testUpMine() throws DMasonException {

		double i = toTest.rmap.NORTH_MINE.upl_xx;
		double j = toTest.rmap.NORTH_MINE.upl_yy;

		double iEnd = toTest.rmap.NORTH_MINE.down_xx;
		double jEnd = toTest.rmap.NORTH_MINE.down_yy;

		double stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx)
				/ numLoop;
		double stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy)
				/ numLoop;

		i += stepI;

		int count = 0;
		while (i < iEnd) {
			j = stepJ + toTest.rmap.NORTH_MINE.upl_yy;
			while (j < jEnd) {
				Double2D location = new Double2D(i, j);
				if (toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}
		assertEquals(count, toTest.rmap.NORTH_MINE.size());

	}

	/**
	 * Test boundary value up mine.
	 * @throws DMasonException 
	 */
	@Test
	public void testBoundaryValueUpMine() throws DMasonException {

		double i = toTest.rmap.NORTH_MINE.upl_xx;
		double j = toTest.rmap.NORTH_MINE.upl_yy;

		Double2D location = new Double2D(i, j);

		assertTrue("i=upl_xx j=upl_yy",
				toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
						new StubRemotePositionedAgent(), /* SimState */ss));

		assertEquals("agent is not created", 1, toTest.rmap.NORTH_MINE.size());

	}

	/**
	 * Test set distributed object location congruence size.
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocationCongruenceSize() throws DMasonException {
		double i = toTest.rmap.NORTH_MINE.upl_xx;
		double j = toTest.rmap.NORTH_MINE.upl_yy;

		double stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx) / 3;
		double stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy) / 3;

		Double2D location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		assertEquals("duplication of agents", 1, toTest.rmap.NORTH_MINE.size());

	}

	/**
	 * Test double set distributed object location god agent.
	 * We use an HashMap and not an ArrayList, so this test is useless
	 * @throws DMasonException 
	 */
	@Deprecated
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		double i = toTest.rmap.NORTH_MINE.upl_xx;
		double j = toTest.rmap.NORTH_MINE.upl_yy;

		double stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx) / 4;
		double stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy) / 4;

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
				new StubRemotePositionedAgent(), /* SimState */ss);

		assertNotSame("the agent is in two places at once",
				toTest.rmap.NORTH_MINE.get(0), toTest.rmap.NORTH_MINE.get(1));

	}

	/**
	 * Test set distributed object location change position.
	 * @throws DMasonException 
	 */
	@Deprecated
	public void testSetDistributedObjectLocationChangePosition() throws DMasonException {
		double i = toTest.rmap.NORTH_MINE.upl_xx;
		double j = toTest.rmap.NORTH_MINE.upl_yy;

		double stepI = (toTest.rmap.NORTH_MINE.down_xx - toTest.rmap.NORTH_MINE.upl_xx) / 4;
		double stepJ = (toTest.rmap.NORTH_MINE.down_yy - toTest.rmap.NORTH_MINE.upl_yy) / 4;

		Double2D location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		i += stepI;
		j += stepJ;

		location = new Double2D(i, j);

		toTest.setDistributedObjectLocation(location, /* RemotePositionedAgent */
				sa, /* SimState */ss);

		assertNotSame("the method has not changed the position",
				toTest.rmap.NORTH_MINE.get(sa.id).l, toTest.rmap.NORTH_MINE.get(sa.id).l);
	}

	/**
	 * test for the field partitioning.
	 * @throws DMasonException 
	 */

	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		double w = 120;
		double h = 120;
		int maxD = 1; 
        int rows=3;
        int columns=rows; // for Pitagora's theorem rows and colums must be equal
        
		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
        
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, rows, columns, mode,
				"test", "", true);


		Double x1 = toTest.rmap.NORTH_WEST_MINE.getUpl_xx();//toTest.myfield.getUpl_xx();
		Double y1 = toTest.rmap.NORTH_WEST_MINE.getUpl_yy();

		Double x2 = toTest.rmap.SOUTH_EAST_MINE.getDown_xx();
		Double y2 = toTest.rmap.SOUTH_EAST_MINE.getDown_yy();
		
		
		
		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w, 2.0) + Math.pow(h, 2.0));
		diagwh=diagwh/rows; // calculate only cell's diagonal
		assertEquals(diag, diagwh);
	}

	/**
	 * Test my field partitioning max distance1.
	 * @throws DMasonException 
	 */
	@Test
	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		double w = 100.0;
		double h = 100.0;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 1, 1, 2, 2, mode,
				"test", "", true);


		
		
		Double x1 = toTest.myfield.upl_xx;
		Double y1 = toTest.myfield.upl_yy;
		Double x2 = toTest.myfield.down_xx;
		Double y2 = toTest.myfield.down_yy;
		

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w/2 - 2 * maxD, 2.0)+ Math.pow(h/2 - 2 * maxD, 2.0));

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
					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.NORTH_MINE.down_xx;
		Double x1 = toTest.rmap.NORTH_MINE.upl_xx;

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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.NORTH_MINE.down_xx;
		Double x1 = toTest.rmap.NORTH_MINE.upl_xx;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.NORTH_MINE.down_xx;
					Double x1 = toTest.rmap.NORTH_MINE.upl_xx;

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
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.SOUTH_MINE.down_xx;
		Double x1 = toTest.rmap.SOUTH_MINE.upl_xx;

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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.SOUTH_MINE.down_xx;
		Double x1 = toTest.rmap.SOUTH_MINE.upl_xx;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;
					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.SOUTH_MINE.down_xx;
					Double x1 = toTest.rmap.SOUTH_MINE.upl_xx;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.WEST_MINE.down_yy;
		Double x1 = toTest.rmap.WEST_MINE.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.WEST_MINE.down_yy;
		Double x1 = toTest.rmap.WEST_MINE.upl_yy;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;
					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.WEST_MINE.down_yy;
					Double x1 = toTest.rmap.WEST_MINE.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.EAST_MINE.down_yy;
		Double x1 = toTest.rmap.EAST_MINE.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.EAST_MINE.down_yy;
		Double x1 = toTest.rmap.EAST_MINE.upl_yy;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.EAST_MINE.down_yy;
					Double x1 = toTest.rmap.EAST_MINE.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.NORTH_OUT.down_xx;
		Double x1 = toTest.rmap.NORTH_OUT.upl_xx;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.NORTH_OUT.down_xx;
		Double x1 = toTest.rmap.NORTH_OUT.upl_xx;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
                    if(maxD<1) maxD=1;
					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.NORTH_OUT.down_xx;
					Double x1 = toTest.rmap.NORTH_OUT.upl_xx;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.SOUTH_OUT.down_xx;
		Double x1 = toTest.rmap.SOUTH_OUT.upl_xx;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.SOUTH_OUT.down_xx;
		Double x1 = toTest.rmap.SOUTH_OUT.upl_xx;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.SOUTH_OUT.down_xx;
					Double x1 = toTest.rmap.SOUTH_OUT.upl_xx;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.WEST_OUT.down_yy;
		Double x1 = toTest.rmap.WEST_OUT.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.WEST_OUT.down_yy;
		Double x1 = toTest.rmap.WEST_OUT.upl_yy;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;

					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.WEST_OUT.down_yy;
					Double x1 = toTest.rmap.WEST_OUT.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		//		toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
		//		"test", /* prefix */"");
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.EAST_OUT.down_yy;
		Double x1 = toTest.rmap.EAST_OUT.upl_yy;

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

		double w = 100;
		double h = w;
		int maxD = 1;

		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
				"test", "", true);

		Double x2 = toTest.rmap.EAST_OUT.down_yy;
		Double x1 = toTest.rmap.EAST_OUT.upl_yy;

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

					double w = i*100;
					double h = k*100;
					int maxD = j;
					if(maxD<1) maxD=1;
					//					toTest = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
					//					"test", /* prefix */"");
					toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
							0.5, w, h, ss, maxD, 0, 0, 1, 1, mode,
							"test", "", true);

					Double x2 = toTest.rmap.EAST_OUT.down_yy;
					Double x1 = toTest.rmap.EAST_OUT.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.SOUTH_WEST_MINE.down_xx;
			Double y1 = toTest.rmap.SOUTH_WEST_MINE.down_yy;
			Double x2 = toTest.rmap.SOUTH_WEST_MINE.upl_xx;
			Double y2 = toTest.rmap.SOUTH_WEST_MINE.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.SOUTH_EAST_MINE.down_xx;
			Double y1 = toTest.rmap.SOUTH_EAST_MINE.down_yy;
			Double x2 = toTest.rmap.SOUTH_EAST_MINE.upl_xx;
			Double y2 = toTest.rmap.SOUTH_EAST_MINE.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.NORTH_WEST_MINE.down_xx;
			Double y1 = toTest.rmap.NORTH_WEST_MINE.down_yy;
			Double x2 = toTest.rmap.NORTH_WEST_MINE.upl_xx;
			Double y2 = toTest.rmap.NORTH_WEST_MINE.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.NORTH_EAST_MINE.down_xx;
			Double y1 = toTest.rmap.NORTH_EAST_MINE.down_yy;
			Double x2 = toTest.rmap.NORTH_EAST_MINE.upl_xx;
			Double y2 = toTest.rmap.NORTH_EAST_MINE.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = toTest.rmap.SOUTH_WEST_OUT.down_xx;
			Double y1 = toTest.rmap.SOUTH_WEST_OUT.down_yy;
			Double x2 = toTest.rmap.SOUTH_WEST_OUT.upl_xx;
			Double y2 = toTest.rmap.SOUTH_WEST_OUT.upl_yy;

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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.SOUTH_EAST_OUT.upl_xx;
				y2 = toTest.rmap.SOUTH_EAST_OUT.upl_yy;
				x1 = toTest.rmap.SOUTH_EAST_OUT.down_xx;
				y1 = toTest.rmap.SOUTH_EAST_OUT.down_yy;
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.NORTH_WEST_OUT.upl_xx;
				y2 = toTest.rmap.NORTH_WEST_OUT.upl_yy;
				x1 = toTest.rmap.NORTH_WEST_OUT.down_xx;
				y1 = toTest.rmap.NORTH_WEST_OUT.down_yy;
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
					0.5, 10, 10, ss, i, 0, 0, 1, 1, mode,
					"test", "", true);

			Double x1 = null, x2 = null, y1 = null, y2 = null;
			try {
				x2 = toTest.rmap.NORTH_EAST_OUT.upl_xx;
				y2 = toTest.rmap.NORTH_EAST_OUT.upl_yy;
				x1 = toTest.rmap.NORTH_EAST_OUT.down_xx;
				y1 = toTest.rmap.NORTH_EAST_OUT.down_yy;
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
			toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, 1000, 1000, ss, /*maxD*/1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.NORTH_EAST_MINE.upl_xx,
				toTest.rmap.NORTH_EAST_OUT.down_xx - 2, 0);
		assertEquals("y", toTest.rmap.NORTH_EAST_MINE.upl_yy,
				toTest.rmap.NORTH_EAST_OUT.down_yy, 0);
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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.NORTH_WEST_MINE.upl_xx,
				toTest.rmap.NORTH_WEST_OUT.down_xx, 0);
		assertEquals("y", toTest.rmap.NORTH_WEST_MINE.upl_yy,
				toTest.rmap.NORTH_WEST_OUT.down_yy, 0);
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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.SOUTH_WEST_MINE.upl_xx,
				toTest.rmap.SOUTH_WEST_OUT.down_xx, 0);
		assertEquals("y", toTest.rmap.SOUTH_WEST_MINE.upl_yy,
				toTest.rmap.SOUTH_WEST_OUT.down_yy - 2, 0);
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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

		assertEquals("x", toTest.rmap.SOUTH_EAST_MINE.down_xx,
				toTest.rmap.SOUTH_EAST_OUT.upl_xx, 0);
		assertEquals("y", toTest.rmap.SOUTH_EAST_MINE.down_yy,
				toTest.rmap.SOUTH_EAST_OUT.upl_yy, 0);
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
		toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, 10, 10, ss, 1, 1, 1, 3, 3, mode,
				"test", "", true);

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

						toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(/* discretization */0.5,
								10, 10, /* simState */
								ss, 1, /* i */ii, /* j */ij, /* rows */ir, /* Colums */
								ic,mode, /* name */
								"test", /* prefix */"",true);

						// cornerUpRight
						assertEquals(
								"cornerUpRight x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.NORTH_EAST_MINE.upl_xx,
								toTest.rmap.NORTH_EAST_OUT.down_xx - 2,
								0);
						assertEquals(
								"cornerUpRight y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.NORTH_EAST_MINE.upl_yy,
								toTest.rmap.NORTH_EAST_OUT.down_yy,
								0);

						// cornerUpLeft
						assertEquals(
								"cornerUpLeft x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.NORTH_WEST_MINE.upl_xx,
								toTest.rmap.NORTH_WEST_OUT.down_xx,
								0);
						assertEquals(
								"cornerUpLeft y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.NORTH_WEST_MINE.upl_yy,
								toTest.rmap.NORTH_WEST_OUT.down_yy,
								0);

						// cornerDownLeft
						assertEquals(
								"cornerDownLeft x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.SOUTH_WEST_MINE.upl_xx,
								toTest.rmap.SOUTH_WEST_OUT.down_xx,
								0);
						assertEquals(
								"cornerDownLeft y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.SOUTH_WEST_MINE.upl_yy,
								toTest.rmap.SOUTH_WEST_OUT.down_yy - 2,
								0);

						// cornerDownRight
						assertEquals(
								"cornerDownRight x i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.SOUTH_EAST_MINE.down_xx,
								toTest.rmap.SOUTH_EAST_OUT.upl_xx,
								0);
						assertEquals(
								"cornerDownRight y i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.rmap.SOUTH_EAST_MINE.down_yy,
								toTest.rmap.SOUTH_EAST_OUT.upl_yy,
								0);

						// myField upLeft
						assertEquals("myField X Up Left i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.myfield.upl_xx,
								toTest.rmap.NORTH_MINE.upl_xx + 1, 0);
						assertEquals("myField Y Up Left i=" + ii + " j=" + ij
								+ " rows=" + ir + " colums=" + ic,
								toTest.myfield.upl_yy,
								toTest.rmap.NORTH_MINE.upl_yy + 1, 0);
						// myField downRight
						assertEquals("myField X Down Right i=" + ii + " j="
								+ ij + " rows=" + ir + " colums=" + ic,
								toTest.myfield.down_xx,
								toTest.rmap.SOUTH_MINE.down_xx - 1, 0);
						assertEquals("myField Y Down Right i=" + ii + " j="
								+ ij + " rows=" + ir + " colums=" + ic,
								toTest.myfield.down_yy,
								toTest.rmap.SOUTH_MINE.down_yy - 1, 0);
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
		width = 563;
		height = 491;
		double discretization = 0.5;
		int max_distance = 1;
		String name = "test", prefix = "";

		/*
		DContinuous2DXY nord = null;
		DContinuous2DXY nordEst = null;
		DContinuous2DXY est = null;
		DContinuous2DXY sudEst = null;
		DContinuous2DXY sud = null;
		DContinuous2DXY sudOvest = null;
		DContinuous2DXY ovest = null;
		DContinuous2DXY nordOvest = null;*/
		
		DContinuousGrid2DXY effective = null;
		// iterate for all r*c cells each one with celltype ij
		for (int i = 0; i < r; i++)

			for (int j = 0; j < c; j++) {

				toTest = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(/* discretization */0.5,
						width, height, /* simState */ ss, max_distance, /* i */i, /* j */j, /* rows */r, /* Colums */c,mode, /* name */"test", /* prefix */"",true);

				effective = new DContinuousGrid2DXY(discretization, width, height, ss, max_distance, i, j, r, c, name, prefix);
				
				assertEquals("Problems with myfield.down_xx",effective.myfield.down_xx,toTest.myfield.down_xx);
				assertEquals("Problems with myfield.down_yy",effective.myfield.down_yy,toTest.myfield.down_yy);
				assertEquals("Problems with myfield.upl_xx",effective.myfield.upl_xx,toTest.myfield.upl_xx);
				assertEquals("Problems with myfield.upl_yy",effective.myfield.upl_yy,toTest.myfield.upl_yy);
				
				assertEquals("Problems with rmap.left_out",effective.rmap.WEST_OUT,toTest.rmap.WEST_OUT);
				assertEquals("Problems with rmap.left_mine",effective.rmap.WEST_MINE,toTest.rmap.WEST_MINE);
				assertEquals("Problems with rmap.right_mine",effective.rmap.EAST_MINE,toTest.rmap.EAST_MINE);
				assertEquals("Problems with rmap.right_out",effective.rmap.EAST_OUT,toTest.rmap.EAST_OUT);
				assertEquals("Problems with rmap.down_mine",effective.rmap.SOUTH_MINE,toTest.rmap.SOUTH_MINE);
				assertEquals("Problems with rmap.down_out",effective.rmap.SOUTH_OUT,toTest.rmap.SOUTH_OUT);
				assertEquals("Problems with rmap.up_mine",effective.rmap.NORTH_MINE,toTest.rmap.NORTH_MINE);
				assertEquals("Problems with rmap.up_out",effective.rmap.NORTH_OUT,toTest.rmap.NORTH_OUT);
				assertEquals("Problems with rmap.corner_mine_up_left",effective.rmap.NORTH_WEST_MINE,toTest.rmap.NORTH_WEST_MINE);
				assertEquals("Problems with rmap.corner_out_up_left_diag_center",effective.rmap.NORTH_WEST_OUT,toTest.rmap.NORTH_WEST_OUT);      
				assertEquals("Problems with rmap.corner_out_up_left_diag_up",effective.rmap.corner_out_up_left_diag_up,toTest.rmap.corner_out_up_left_diag_up);                          
				assertEquals("Problems with rmap.corner_out_up_left_diag_left",effective.rmap.corner_out_up_left_diag_left,toTest.rmap.corner_out_up_left_diag_left);
				assertEquals("Problems with rmap.corner_mine_up_right",effective.rmap.NORTH_EAST_MINE,toTest.rmap.NORTH_EAST_MINE);
				assertEquals("Problems with rmap.corner_out_up_right_diag_center",effective.rmap.NORTH_EAST_OUT,toTest.rmap.NORTH_EAST_OUT);
				assertEquals("Problems with rmap.corner_out_up_right_diag_up",effective.rmap.corner_out_up_right_diag_up,toTest.rmap.corner_out_up_right_diag_up);                          
				assertEquals("Problems with rmap.corner_out_up_right_diag_right",effective.rmap.corner_out_up_right_diag_right,toTest.rmap.corner_out_up_right_diag_right);
				assertEquals("Problems with rmap.corner_mine_down_left",effective.rmap.SOUTH_WEST_MINE,toTest.rmap.SOUTH_WEST_MINE);
				assertEquals("Problems with rmap.corner_out_down_left_diag_center",effective.rmap.SOUTH_WEST_OUT,toTest.rmap.SOUTH_WEST_OUT); 
				assertEquals("Problems with rmap.corner_out_down_left_diag_left",effective.rmap.corner_out_down_left_diag_left,toTest.rmap.corner_out_down_left_diag_left);                    
				assertEquals("Problems with rmap.corner_out_down_left_diag_down",effective.rmap.corner_out_down_left_diag_down,toTest.rmap.corner_out_down_left_diag_down);
				assertEquals("Problems with rmap.corner_mine_down_right",effective.rmap.SOUTH_EAST_MINE,toTest.rmap.SOUTH_EAST_MINE);
				assertEquals("Problems with rmap.corner_out_down_right_diag_center",effective.rmap.SOUTH_EAST_OUT,toTest.rmap.SOUTH_EAST_OUT);
				assertEquals("Problems with rmap.corner_out_down_right_diag_right",effective.rmap.corner_out_down_right_diag_right,toTest.rmap.corner_out_down_right_diag_right);                
				assertEquals("Problems with rmap.corner_out_down_right_diag_down",effective.rmap.corner_out_down_right_diag_down,toTest.rmap.corner_out_down_right_diag_down);
				
				
				
/*				// neighbors
				// up
				if (i == 0 && j > 0 && j < c - 1) {

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (i == ir - 1 && j > 0 && j < ic - 1) {// down

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i 0,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (j == 0 && i > 0 && i < ir - 1) { // left

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (j == ic - 1 && i > 0 && i < ir - 1) { // right

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (i > 0 && j > 0 && i < ir - 1
						&& j < ic - 1) { // center

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (i == 0 && j == 0) { // up-left

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (i == ir - 1 && j == 0) { // down-left

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i 0,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j j + 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");

				} else if (i == ir - 1 && j == ic - 1) { // down-right

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i 0,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i - 1,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i 0,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
				} else if (i == 0 && j == ic - 1) { // upRight

					nord = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sud = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i i + 1,  j ic - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					est = new DContinuous2DXY( discretization 0.5,
							width, height,  simState 
							ss, 1,  i 0,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					ovest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					nordEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i ir - 1,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudOvest = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j j - 1,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
					sudEst = new DContinuous2DXY(
							 discretization 0.5, width, height,  simState 
							ss, 1,  i i + 1,  j 0,  rows 
							ir,  Colums 
							ic,  name 
							"test",  prefix "");
				}

				 verify congruence 

				// height
				assertEquals("incongruence height with ovest i=" + i
						+ " j=" + j + " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_mine.down_yy
						- toTest.rmap.up_mine.upl_yy,
						ovest.rmap.down_mine.down_yy
						- ovest.rmap.up_mine.upl_yy, 0);
				assertEquals("incongruence height with est i=" + i
						+ " j=" + j + " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_mine.down_yy
						- toTest.rmap.up_mine.upl_yy,
						est.rmap.down_mine.down_yy
						- est.rmap.up_mine.upl_yy, 0);

				// width
				assertEquals("incongruence width with nord i=" + i
						+ " j=" + j + " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_mine.down_xx
						- toTest.rmap.down_mine.upl_xx,
						nord.rmap.down_mine.down_xx
						- nord.rmap.down_mine.upl_xx, 0);
				assertEquals("incongruence width with sud i=" + i
						+ " j=" + j + " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_mine.down_xx
						- toTest.rmap.down_mine.upl_xx,
						sud.rmap.down_mine.down_xx
						- sud.rmap.down_mine.upl_xx, 0);

				// upOut
				assertEquals("upOut downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.up_out.down_xx,
						nord.rmap.down_mine.down_xx, 0);
				assertEquals("upOut downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.up_out.down_yy,
						nord.rmap.down_mine.down_yy, 0);

				assertEquals("upOut uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.up_out.upl_xx,
						nord.rmap.down_mine.upl_xx, 0);
				assertEquals("upOut uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.up_out.upl_yy,
						nord.rmap.down_mine.upl_yy, 0);

				// downOut
				assertEquals("downOut downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_out.down_xx,
						sud.rmap.up_mine.down_xx, 0);
				assertEquals("downOut downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_out.down_yy,
						sud.rmap.up_mine.down_yy, 0);

				assertEquals("downOut uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_out.upl_xx,
						sud.rmap.up_mine.upl_xx, 0);
				assertEquals("downOut uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.down_out.upl_yy,
						sud.rmap.up_mine.upl_yy, 0);

				// leftOut

				assertEquals("leftOut downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.left_out.down_xx,
						ovest.rmap.right_mine.down_xx, 0);
				assertEquals("leftOut downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.left_out.down_yy,
						ovest.rmap.right_mine.down_yy, 0);

				assertEquals("leftOut uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.left_out.upl_xx,
						ovest.rmap.right_mine.upl_xx, 0);
				assertEquals("leftOut uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.left_out.upl_yy,
						ovest.rmap.right_mine.upl_yy, 0);

				// rightOut

				assertEquals("rightOut downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.right_out.down_xx,
						est.rmap.left_mine.down_xx, 0);
				assertEquals("rightOut downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.right_out.down_yy,
						est.rmap.left_mine.down_yy, 0);

				assertEquals("rightOut uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.right_out.upl_xx,
						est.rmap.left_mine.upl_xx, 0);
				assertEquals("rightOut uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.right_out.upl_yy,
						est.rmap.left_mine.upl_yy, 0);

				// cornerOutUpLeft

				assertEquals(
						"cornerOutUpLeft downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_left_diag_center.down_xx,
						nordOvest.rmap.corner_mine_down_right.down_xx,
						0);
				assertEquals(
						"cornerOutUpLeft downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_left_diag_center.down_yy,
						nordOvest.rmap.corner_mine_down_right.down_yy,
						0);
				assertEquals(
						"cornerOutUpLeft uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_left_diag_center.upl_xx,
						nordOvest.rmap.corner_mine_down_right.upl_xx, 0);
				assertEquals(
						"cornerOutUpLeft uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_left_diag_center.upl_yy,
						nordOvest.rmap.corner_mine_down_right.upl_yy, 0);

				// cornerOutUpRight

				assertEquals(
						"cornerOutUpRight downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_right_diag_center.down_xx,
						nordEst.rmap.corner_mine_down_left.down_xx, 0);
				assertEquals(
						"cornerOutUpRight downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_right_diag_center.down_yy,
						nordEst.rmap.corner_mine_down_left.down_yy, 0);
				assertEquals(
						"cornerOutUpRight uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_right_diag_center.upl_xx,
						nordEst.rmap.corner_mine_down_left.upl_xx, 0);
				assertEquals(
						"cornerOutUpRight uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_up_right_diag_center.upl_yy,
						nordEst.rmap.corner_mine_down_left.upl_yy, 0);

				// cornerOutDownRight
				assertEquals(
						"cornerOutDownRight downXX i=" + i + " j="
								+ j + " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_right_diag_center.down_xx,
								sudEst.rmap.corner_mine_up_left.down_xx, 0);
				assertEquals(
						"cornerOutDownRight downYY i=" + i + " j="
								+ j + " rows=" + ir + " colums=" + ic,
								toTest.rmap.corner_out_down_right_diag_center.down_yy,
								sudEst.rmap.corner_mine_up_left.down_yy, 0);
				assertEquals(
						"cornerOutDownRight uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_right_diag_center.upl_xx,
						sudEst.rmap.corner_mine_up_left.upl_xx, 0);
				assertEquals(
						"cornerOutDownRight uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_right_diag_center.upl_yy,
						sudEst.rmap.corner_mine_up_left.upl_yy, 0);

				// cornerOutDownLeft
				assertEquals(
						"cornerOutDownLeft downXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_left_diag_center.down_xx,
						sudOvest.rmap.corner_mine_up_right.down_xx, 0);
				assertEquals(
						"cornerOutDownLeft downYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_left_diag_center.down_yy,
						sudOvest.rmap.corner_mine_up_right.down_yy, 0);
				assertEquals(
						"cornerOutDownLeft uplXX i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_left_diag_center.upl_xx,
						sudOvest.rmap.corner_mine_up_right.upl_xx, 0);
				assertEquals(
						"cornerOutDownLeft uplYY i=" + i + " j=" + j
						+ " rows=" + ir + " colums=" + ic,
						toTest.rmap.corner_out_down_left_diag_center.upl_yy,
						sudOvest.rmap.corner_mine_up_right.upl_yy, 0);

				// cornerMineUpLeft/nordOvest congruence
				assertEquals(
						"cornerMineUpLeft/nordOvest congruence xx i="
								+ i + " j=" + j + " rows=" + ir
								+ " colums=" + ic,
								toTest.rmap.corner_mine_up_left.upl_xx,
								nordOvest.rmap.corner_mine_down_right.down_xx
								% width, 0);

				assertEquals("cornerMineUp/nordOvest congruence yy i="
						+ i + " j=" + j + " rows=" + ir + " colums="
						+ ic, toTest.rmap.corner_mine_up_left.upl_yy,
						nordOvest.rmap.corner_mine_down_right.down_yy
						% height, 0);

				// cornerMineUpLeft/ovest congruence
				assertEquals(
						"cornerMineUpLeft/ovest congruence xx i=" + i
						+ " j=" + j + " rows=" + ir
						+ " colums=" + ic,
						toTest.rmap.corner_mine_up_left.upl_xx,
						ovest.rmap.corner_mine_up_right.down_xx % width,
						0);

				assertEquals(
						"cornerMineUpLeft/ovest congruence yy i=" + i
						+ " j=" + j + " rows=" + ir
						+ " colums=" + ic,
						toTest.rmap.corner_mine_up_left.upl_yy,
						ovest.rmap.corner_mine_up_right.upl_yy % height,
						0);

				// cornerMineUpLeft/nord congruence
				assertEquals("cornerMineUpLeft/nord congruence xx i="
						+ i + " j=" + j + " rows=" + ir + " colums="
						+ ic, toTest.rmap.corner_mine_up_left.upl_xx,
						nord.rmap.corner_mine_down_left.upl_xx % width,
						0);

				assertEquals("cornerMineUpLeft/nord congruence yy i="
						+ i + " j=" + j + " rows=" + ir + " colums="
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

				assertEquals("i=" + i + " j=" + j + " rows=" + ir
						+ " colums=" + ic, diag, realDiag, 0);

				// width-height congruence
				contW += toTest.rmap.corner_mine_down_right.down_xx
						- toTest.rmap.corner_mine_up_left.upl_xx;
			}
		assertEquals("width error rows=" + ir + " colums=" + ic,
				width, contW); */
		}

	}
}
