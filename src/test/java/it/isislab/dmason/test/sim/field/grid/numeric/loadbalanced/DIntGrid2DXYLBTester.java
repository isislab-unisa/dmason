package it.isislab.dmason.test.sim.field.grid.numeric.loadbalanced;

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
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.sim.field.grid.numeric.loadbalanced.DIntGrid2DXYLB;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * The Class DIntGrid2DXYLBTester. Tests the DIntGrid2DXYLB for a toroidal
 * distribution.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class DIntGrid2DXYLBTester {
	/** The to test. */
	DIntGrid2DXYLB toTest;

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
		maxDistance = 1;
		rows = 10;
		columns = 10;
		width = 6 * columns;
		height = 6 * rows;

		numAgents = numLoop;
		mode = DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE;
		connectionType = ConnectionType.pureActiveMQ;

		GeneralParam genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		ss = new StubDistributedState(genParam);

		toTest = (DIntGrid2DXYLB) DIntGrid2DFactory.createDIntGrid2D(width,
				height,/* simState */
				ss, maxDistance, /* i */0, /* j */0, rows, columns, mode, 0,
				false,/* name */
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
			toTest.setDistributedObjectLocation(location,  
					i,  ss);
		}

		assertSame(ss, toTest.getState());
	}
/*
	*//**
	 * Test get num agent for same agent.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testGetNumAgentForSameAgent() throws DMasonException {
		// i'm moving an agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location,  grid value 
					i,  SimState ss);
		}
		assertEquals(1, toTest.getNumAgents());
	}
*/
	/**
	 * Test get num agent different agent.
	 * @throws DMasonException 
	 *//*
	@Test
	public void testGetNumAgentDifferentAgent() throws DMasonException {
		// i'm positioning more agent in the DistributedState
		for (int i = 0; i < numLoop; i++) {
			Int2D location = toTest.getAvailableRandomLocation();
			toTest.setDistributedObjectLocation(location,  grid value 
					i,  SimState ss);
		}
		assertEquals(numLoop, toTest.getNumAgents());
	}*/

}
