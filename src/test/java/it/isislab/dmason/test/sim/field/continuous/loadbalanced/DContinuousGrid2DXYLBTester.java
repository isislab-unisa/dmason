package it.isislab.dmason.test.sim.field.continuous.loadbalanced;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.sim.field.continuous.loadbalanced.DContinuousGrid2DXYLB;
import it.isislab.dmason.test.sim.field.continuous.DContinuousGrid2DXYTester.StubRemotePositionedAgent;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;

/**
 * The Class DContinuous2DXYLBLBTester. Tests the DContinuous2DXYLB.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class DContinuousGrid2DXYLBTester {
	/** The to test. */
	DContinuousGrid2DXYLB toTest;

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

		maxDistance = 1;
		rows = 10;
		columns = 10;
		width = 60 * columns;
		height = 60 * rows;
		numAgents = numLoop;
		mode = DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		
		sa= new StubRemotePositionedAgent();
		ss = new StubDistributedState(genParam);
		// toTest = new DContinuous2DXYLB(/* discretization */0.5, width,
		// height, /* simState */
		// ss, maxDistance, /* i */0, /* j */0, rows, columns, /* name */
		// "test", /* prefix */"");
		toTest = (DContinuousGrid2DXYLB) DContinuousGrid2DFactory.createDContinuous2D(
				0.5, width, height, ss, maxDistance, 0, 0, rows, columns, mode,
				"test", "", true);

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
					new StubRemotePositionedAgent(), /* SimState */ss);
		}
		assertEquals(numLoop, toTest.getNumAgents());
	}

}
