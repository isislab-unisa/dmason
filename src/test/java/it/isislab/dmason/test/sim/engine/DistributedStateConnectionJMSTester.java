package it.isislab.dmason.test.sim.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;
import org.junit.Test;
import sim.engine.SimState;
import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import it.isislab.dmason.util.connection.testconnection.VirtualConnection;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;

// TODO: Auto-generated Javadoc
/**
 * This is the DistributedStateConnectionJMS's tester.
 *
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */

public class DistributedStateConnectionJMSTester {

	/**
	 * The Class StubDistributedState.
	 */
	class StubDistributedState extends DistributedState<Double2D> {

		/**
		 * Instantiates a new stub distributed state.
		 *
		 * @param field
		 *            the field
		 */
		public StubDistributedState(DistributedField2D<Double2D> field) {

			super();
			((DistributedMultiSchedule<Double2D>) schedule).addField(field);

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
	}

	/**
	 * The Class StubConnectionJMS.
	 */
	class StubConnectionJMS extends VirtualConnection implements ConnectionJMS {

		/**
		 * Instantiates a new stub connection jms.
		 */
		public StubConnectionJMS() {
			// TODO Auto-generated constructor stub
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.util.connection.jms.ConnectionJMS#asynchronousReceive
		 * (java.lang.String,
		 * it.isislab.dmason.util.connection.jms.activemq.MyMessageListener)
		 */
		@Override
		public boolean asynchronousReceive(String arg0, MyMessageListener arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.util.connection.jms.ConnectionJMS#setTable(java
		 * .util.HashMap)
		 */
		@Override
		public void setTable(HashMap table) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.util.connection.jms.ConnectionJMS#close()
		 */
		@Override
		public void close() throws JMSException {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * The Class StubDistributedField2D.
	 */
	public class StubDistributedField2D implements DistributedField2D<Double2D> {

		/** The toroidal. */
		boolean toroidal = false;

		/**
		 * Instantiates a new stub distributed field2 d.
		 */
		public StubDistributedField2D() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField#synchro()
		 */
		@Override
		public boolean synchro() {
			// TODO Auto-generated method stub
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField#getState()
		 */
		@Override
		public DistributedState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.field.DistributedField#setTable(java.util.HashMap
		 * )
		 */
		@Override
		public void setTable(HashMap table) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField#getID()
		 */
		@Override
		public String getDistributedFieldID() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField#getUpdates()
		 */
		@Override
		public UpdateMap getUpdates() {
			// TODO Auto-generated method stub
			return null;
		}

		


		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField#getGlobals()
		 */
		@Override
		public VisualizationUpdateMap getGlobals() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField2D#isToroidal()
		 */
		@Override
		public boolean isToroidal() {

			// TODO Auto-generated method stub
			return toroidal;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.field.DistributedField2D#setToroidal(boolean)
		 */
		@Override
		public void setToroidal(boolean isToroidal) {
			// TODO Auto-generated method stub
			toroidal = isToroidal;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.isislab.dmason.sim.field.DistributedField2D#
		 * setDistributedObjectLocation(java.lang.Object,
		 * it.isislab.dmason.sim.engine.RemotePositionedAgent,
		 * sim.engine.SimState)
		 */
		@Override
		public boolean setDistributedObjectLocation(Double2D location,
				/*RemotePositionedAgent<Double2D> rm*/Object RemoteObject, SimState sm) throws DMasonException{
			// TODO Auto-generated method stub
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.isislab.dmason.sim.field.DistributedField2D#getAvailableRandomLocation
		 * ()
		 */
		@Override
		public Double2D getAvailableRandomLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean verifyPosition(Double2D pos) {
			// TODO Auto-generated method stub
			return false;
		}


	}

	/** The variable to test. */
	DistributedStateConnectionJMS<Double2D> toTest;

	/** The connection. */
	StubConnectionJMS stConnection;

	/** The num of loop of tests. */
	int numLoop = 8;

	/**
	 * Set the dimension of field, if the mode is square or horizontal and if
	 * the space is toroidal.
	 * 
	 * @param rows
	 *            the number of rows
	 * @param colums
	 *            the number of colums
	 * @param mode
	 *            the modality that we use for the space: horizontal or square
	 * @param posi
	 *            the rows's index of cell under test
	 * @param posj
	 *            the colum's index of cell under test
	 * @param isToroidal
	 *            set if the space is toroidal or not
	 * */
	public void setEnviroment(int rows, int colums, int mode, int posi,
			int posj, boolean isToroidal) {
		stConnection = new StubConnectionJMS();

		DistributedField2D<Double2D> field = new StubDistributedField2D();

		field.setToroidal(isToroidal);
		StubDistributedState dm = new StubDistributedState(field);
		dm.rows = rows;
		dm.columns = colums;
		dm.NUMPEERS = 1; // if numpeers == 0 is throwed an exeption for a
							// division by 0
		dm.MODE = mode;
		dm.TYPE = new CellType(posi, posj);

		toTest = new DistributedStateConnectionJMS<Double2D>(dm, "127.0.0.1",
				"61616", stConnection);
	}

	// @Before
	// public void setUp() {
	//
	// stConnection = new StubConnectionJMS();
	//
	// DistributedField2D<Double2D> field = new StubDistributedField2D();
	//
	// field.setToroidal(false);
	// StubDistributedState dm = new StubDistributedState(field);
	//
	// dm.rows = 5;
	// dm.columns = 5;
	//
	// dm.MODE = 1;
	// dm.TYPE = new CellType(1, 1);
	//
	// toTestNoToroidal = new DistributedStateConnectionJMS<Double2D>(dm,
	// "127.0.0.1",
	// "61616", stConnection);
	//
	// field.setToroidal(true);
	// dm = new StubDistributedState(field);
	//
	// dm.rows = 5;
	// dm.columns = 5;
	//
	// dm.MODE = 1;
	// dm.TYPE = new CellType(1, 1);
	// toTestToroidal = new DistributedStateConnectionJMS<Double2D>(dm,
	// "127.0.0.1",
	// "61616", stConnection);
	//
	// }

	/**
	 * GENERIC TESTS.
	 */

	/**
	 * Verify if the topics are instantiated for a simply, static configuration
	 * */
	@Test
	public void testInstantiatedTopicsNoToroidalSquareMode() { // mode=1
		// 5 rows 5 columns mode 1 not toroidal posi=1 posj=1
		setEnviroment(5, 5, 1, 1, 1, false);

		toTest.init_connection();
		int i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1L"))
				i = 1;
		assertFalse("topic L not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1R"))
				i = 1;
		assertFalse("topic R not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1U"))
				i = 1;
		assertFalse("topic U not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1D"))
				i = 1;
		assertFalse("topic D not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CDDR"))
				i = 1;
		assertFalse("topic CDDR not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CUDR"))
				i = 1;
		assertFalse("topic CUDR not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CDDL"))
				i = 1;
		assertFalse("topic CDDL not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CUDL"))
				i = 1;
		assertFalse("topic CUDL not created", i == 0);

	}

	/**
	 * Verify if the topics are instantiated for a simply toroidal square mode.
	 */
	@Test
	public void testInstantiatedTopicsToroidalSquareMode() {// mode=1
		// 5 rows 5 columns mode 1 toroidal posi=1 posj=1
		setEnviroment(5, 5, 1, 1, 1, true);

		toTest.init_connection();
		int i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1L"))
				i = 1;
		assertFalse("topic L not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1R"))
				i = 1;
		assertFalse("topic R not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1U"))
				i = 1;
		assertFalse("topic U not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1D"))
				i = 1;
		assertFalse("topic D not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CDDR"))
				i = 1;
		assertFalse("topic CDDR not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CUDR"))
				i = 1;
		assertFalse("topic CUDR not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CDDL"))
				i = 1;
		assertFalse("topic CDDL not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("1-1CUDL"))
				i = 1;
		assertFalse("topic CUDL not created", i == 0);

	}

	/**
	 * Test for instantiated topics in a no toroidal horizontal mode.
	 */
	@Test
	public void testInstantiatedTopicsNoToroidalHorizontalMode() { // mode=0||mode=3
		// 1 rows 5 columns mode 0 not toroidal posi=0 posj=1
		setEnviroment(1, 5, 0, 0, 1, false);
		toTest.init_connection();
		int i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("0-1L"))
				i = 1;
		assertFalse("topic L not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("0-1R"))
				i = 1;
		assertFalse("topic R not created", i == 0);

	}

	/**
	 * Test for instantiated topics in a toroidal horizontal mode.
	 */
	@Test
	public void testInstantiatedTopicsToroidalHorizontalMode() { // mode=0||mode=3
		// 1 rows 5 columns mode 0 not toroidal posi=0 posj=1
		setEnviroment(1, 5, 0, 0, 1, true);
		toTest.init_connection();
		int i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("0-1L"))
				i = 1;
		assertFalse("topic L not created", i == 0);

		i = 0;
		for (String top : stConnection.topics)
			if (top.contentEquals("0-1R"))
				i = 1;
		assertFalse("topic R not created", i == 0);

	}

	/* TEST FOR ALL CREATED TOPICS */

	/**
	 * Test created topics in a no toroidal horizontal mode.
	 */
	@Test
	public void testCreatedTopicsNoToroidalHorizontalMode() {

		for (int i = 0; i < numLoop; i++) {
			setEnviroment(1, numLoop, 0, 0, i, false);
			toTest.init_connection();

			if ((i > 0 && i < numLoop - 1)) {
				int flag = 0;
				for (String top : stConnection.topics)
					if (top.contentEquals("0-" + i + "L"))
						flag = 1;
				assertFalse("topic L not created", flag == 0);

				flag = 0;
				for (String top : stConnection.topics)
					if (top.contentEquals("0-" + i + "R"))
						flag = 1;
				assertFalse("topic R not created", flag == 0);

			} else if (i == 0) {
				int flag = 0;
				for (String top : stConnection.topics)
					if (top.contentEquals("0-" + i + "L"))
						flag = 1;
				assertTrue("topic L created with jPos=0", flag == 0);
			} else if (i == numLoop - 1) {
				int flag = 0;
				for (String top : stConnection.topics)
					if (top.contentEquals("0-" + i + "R"))
						flag = 1;
				assertTrue("topic R created with jPos=lastCell", flag == 0);
			}

		}

	}

	/**
	 * Test created topics in a toroidal horizontal mode.
	 */
	@Test
	public void testCreatedTopicsToroidalHorizontalMode() {

		for (int i = 0; i < numLoop; i++) {

			setEnviroment(1, numLoop, 0, 0, i, true);
			toTest.init_connection();

			int flag = 0;
			for (String top : stConnection.topics)
				if (top.contentEquals("0-" + i + "L"))
					flag = 1;
			assertFalse("topic L not created with posj=" + i, flag == 0);

			flag = 0;
			for (String top : stConnection.topics)
				if (top.contentEquals("0-" + i + "R"))
					flag = 1;
			assertFalse("topic R not created with posj=" + i, flag == 0);

		}
	}

	/**
	 * Test created topics in a no toroidal square mode.
	 */
	@Test
	public void testCreatedTopicsNoToroidalSquareMode() { // square not balanced

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {
						setEnviroment(rows, colums, 1, i, j, false);
						toTest.init_connection();

						if ((i > 0 && i < rows - 1)
								&& (j > 0 && j < colums - 1)) { // interno della
																// cornice
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L not created whith colums="
									+ colums + " rows=" + rows + " posi=" + i
									+ " posj=" + j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertFalse("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertFalse("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertFalse("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertFalse("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						} else if (i == 0 && j > 0 && j < colums - 1) { // lato
																		// in
							// alto
							// senza estremi

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertFalse("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertTrue("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertTrue("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertTrue("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						} else if (i == rows - 1 && j > 0 && j < colums - 1) { // lato
																				// in
																				// basso
																				// senza
																				// estremi

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertFalse("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertFalse("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertTrue("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertTrue("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertTrue("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						}

					}
				}
			}
		}
	}

	/**
	 * Test created topics in a no toroidal square mode right.
	 */
	@Test
	public void testCreatedTopicsNoToroidalSquareModeRight() { // square
																// not
																// balanced
																// verifica
																// dei
																// topic
																// creati
																// al
																// lato destro
																// esclusi
																// gli
																// estremi

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {
						setEnviroment(rows, colums, 1, i, j, false);
						toTest.init_connection();

						if (i > 0 && i < rows - 1 && j == colums - 1) { // lato
																		// a
																		// destra
																		// senza
																		// estremi

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertFalse("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertFalse("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertTrue("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertTrue("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertTrue("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						}

					}
				}
			}
		}
	}

	/**
	 * Test created topics in a no toroidal square mode left.
	 */
	@Test
	public void testCreatedTopicsNoToroidalSquareModeLeft() { // square
																// not
																// balanced
																// verifica
																// dei
																// topic
																// creati
																// al
																// lato sinistro
																// esclusi
																// gli
																// estremi

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {
						setEnviroment(rows, colums, 1, i, j, false);
						toTest.init_connection();

						if (i > 0 && i < rows - 1 && j == 0) {
							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertFalse("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertFalse("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertTrue("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertTrue("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertTrue("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						}

					}
				}
			}
		}
	}

	/**
	 * Test created topics in a no toroidal square mode corners up.
	 */
	@Test
	public void testCreatedTopicsNoToroidalSquareModeCornersUp() {

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {
						setEnviroment(rows, colums, 1, i, j, false);
						toTest.init_connection();

						if (i == 0 && j == 0) {

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertFalse("topic CDDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertTrue("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertTrue("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertTrue("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						} else if (i == 0 && j == colums - 1) {

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertFalse("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertFalse("topic CDDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertTrue("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertTrue("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertTrue("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						}

					}
				}
			}
		}
	}

	/**
	 * Test created topics in a no toroidal square mode corners down.
	 */
	@Test
	public void testCreatedTopicsNoToroidalSquareModeCornersDown() {

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {
						setEnviroment(rows, colums, 1, i, j, false);
						toTest.init_connection();

						if (i == rows && j == 0) {

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertFalse("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDR"))
									flag = 1;
							assertFalse("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertTrue("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertTrue("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDL"))
									flag = 1;
							assertTrue("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						} else if (i == rows - 1 && j == colums - 1) {

							// topic creati
							int flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "U"))
									flag = 1;
							assertFalse("topic U whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "L"))
									flag = 1;
							assertFalse("topic L whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CUDL"))
									flag = 1;
							assertFalse("topic CUDL whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							// topic non creati

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "R"))
									flag = 1;
							assertTrue("topic R whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "D"))
									flag = 1;
							assertTrue("topic D whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

							flag = 0;
							for (String top : stConnection.topics)
								if (top.contentEquals(i + "-" + j + "CDDR"))
									flag = 1;
							assertTrue("topic CUDR whith colums=" + colums
									+ " rows=" + rows + " posi=" + i + " posj="
									+ j, flag == 0);

						}

					}
				}
			}
		}

	}

	/**
	 * Test created topics in a toroidal square mode.
	 */
	@Test
	public void testCreatedTopicsToroidalSquareMode() {

		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {

						setEnviroment(rows, colums, 1, i, j, true);
						toTest.init_connection();

						// topic creati (tutti)

						int flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "U"))
								flag = 1;
						assertFalse("topic U whith colums=" + colums + " rows="
								+ rows + " posi=" + i + " posj=" + j, flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "D"))
								flag = 1;
						assertFalse("topic D whith colums=" + colums + " rows="
								+ rows + " posi=" + i + " posj=" + j, flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "R"))
								flag = 1;
						assertFalse("topic R whith colums=" + colums + " rows="
								+ rows + " posi=" + i + " posj=" + j, flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "L"))
								flag = 1;
						assertFalse("topic L whith colums=" + colums + " rows="
								+ rows + " posi=" + i + " posj=" + j, flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "CUDL"))
								flag = 1;
						assertFalse(
								"topic CUDL whith colums=" + colums + " rows="
										+ rows + " posi=" + i + " posj=" + j,
								flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "CDDL"))
								flag = 1;
						assertFalse(
								"topic CDDL whith colums=" + colums + " rows="
										+ rows + " posi=" + i + " posj=" + j,
								flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "CUDR"))
								flag = 1;
						assertFalse(
								"topic CUDR whith colums=" + colums + " rows="
										+ rows + " posi=" + i + " posj=" + j,
								flag == 0);

						flag = 0;
						for (String top : stConnection.topics)
							if (top.contentEquals(i + "-" + j + "CDDR"))
								flag = 1;
						assertFalse(
								"topic CDDR whith colums=" + colums + " rows="
										+ rows + " posi=" + i + " posj=" + j,
								flag == 0);
					}
				}
			}

		}

	}

	/**
	 * Test subscribed topics in a no toroidal horizontal mode.
	 */
	@Test
	public void testsubscribedTopicsNoToroidalHorizontalMode() {

		for (int colums = 2; colums < numLoop; colums++) {
			for (int j = 0; j < colums; j++) {

				setEnviroment(1, colums, 0, 0, j, false);
				toTest.init_connection();

				if (j == 0) {
					// topic iscritti
					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
							flag = 1;
					assertFalse("topic L whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

					// non iscritti
					// trigger graphics global_reduced e altri 2 sono
					// presenti... quindi
					// per verificare che il numero dei topic iscritti sia
					// corretto basta vedere se la dim - 5 è uguale al numero
					// dei topic creati altrimenti significa che la cella si è
					// iscritta a qualche topic di troppo

					assertEquals("too topic subscribed", 1,
							stConnection.topicsSubscribed.size() - 5);

				} else if (j > 0 && j < colums - 1) {
					// topic iscritti
					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
							flag = 1;
					assertFalse("topic L whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

					flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
							flag = 1;
					assertFalse("topic R whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);
					// topic non iscritti

					// trigger graphics global_reduced e altri 2 sono
					// presenti... quindi
					// per verificare che il numero dei topic iscritti sia
					// corretto basta vedere se la dim - 5 è uguale al numero
					// dei topic creati altrimenti significa che la cella si è
					// iscritta a qualche topic di troppo

					assertEquals("too topic subscribed", 2,
							stConnection.topicsSubscribed.size() - 5);
				} else if (j == colums - 1) {
					// topic iscritti
					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
							flag = 1;
					assertFalse("topic R whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

					// topic non iscritti

					// trigger graphics global_reduced e altri 2 sono
					// presenti... quindi
					// per verificare che il numero dei topic iscritti sia
					// corretto basta vedere se la dim - 5 è uguale al numero
					// dei topic creati altrimenti significa che la cella si è
					// iscritta a qualche topic di troppo

					assertEquals("too topic subscribed", 1,
							stConnection.topicsSubscribed.size() - 5);
				}

			}
		}

	}

	/**
	 * Test subscribed topics in a toroidal horizontal mode.
	 */
	@Test
	public void testsubscribedTopicsToroidalHorizontalMode() {

		for (int colums = 2; colums < numLoop; colums++) {
			for (int j = 0; j < colums; j++) {

				setEnviroment(1, colums, 0, 0, j, true);
				toTest.init_connection();

				if (j == 0) {

					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
							flag = 1;
					assertFalse("topic L whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);
					flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (colums - 1) + "R"))
							flag = 1;
					assertFalse("topic R whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

				} else if (j > 0 && j < colums - 1) {

					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
							flag = 1;
					assertFalse("topic L whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

					flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
							flag = 1;
					assertFalse("topic R whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);
				} else if (j == colums - 1) {
					int flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
							flag = 1;
					assertFalse("topic R whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);

					flag = 0;
					for (String top : stConnection.topicsSubscribed)
						if (top.contentEquals(0 + "-" + (0) + "L"))
							flag = 1;
					assertFalse("topic L whith colums=" + colums + " rows=" + 1
							+ " posi=" + 0 + " posj=" + j, flag == 0);
				}

			}
		}

	}

	/**
	 * Test subscribed topics in corners of square mode toroidal.
	 */
	@Test
	public void testsubscribedTopicsCornersSquareModeToroidal() { // avvolte
																	// questo
																	// test
																	// fallisce
																	// per via
																	// di un
																	// topic al
																	// quale la
																	// cella non
																	// sempre si
																	// iscrive
		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {

						setEnviroment(rows, colums, 1, i, j, true);
						toTest.init_connection();

						if (i == 0 && j == 0) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(0 + "-" + (colums - 1)
										+ "R"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-0D"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-"
										+ (colums - 1) + "CDDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-1L"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-0U"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-1CUDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-1CDDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-" + (colums - 1)
										+ "CUDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);

						} else if (i == 0 && j == colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-0L"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-"
										+ (colums - 1) + "D"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-0CDDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-" + (colums - 2) + "R"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-" + (colums - 1) + "U"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-" + (colums - 2)
										+ "CUDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-"
										+ (colums - 2) + "CDDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("1-0CUDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);

						} else if (i == rows - 1 && j == 0) {

							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 2) + "-0D"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-0U"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-"
										+ (colums - 1) + "R"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((rows - 1) + "-1L"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-" + (colums - 1)
										+ "CUDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i - 1) + "-"
										+ (colums - 1) + "CDDR"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i - 1) + "-" + (j + 1)
										+ "CDDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals("0-" + (j + 1) + "CUDL"))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
						} else if (i == rows - 1 && j == colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (colums - 1) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-"
										+ (colums - 1) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-"
										+ (colums - 2) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-0L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-"
										+ (colums - 2) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-0CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (colums - 2) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-0CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);

						}
					}
				}
			}
		}

	}

	/**
	 * Test subscribed topics in sides of square mode toroidal.
	 */
	@Test
	public void testsubscribedTopicsSidesSquareModeToroidal() { // ance in
																// questo
																// test si
																// evince che
																// spesso la
																// cella non si
																// iscrive ad
																// alcuni field
		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {

						setEnviroment(rows, colums, 1, i, j, true);
						toTest.init_connection();

						if (i == 0 && j > 0 && j < colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (j - 1) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (j + 1) + "L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-" + (j) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-"
										+ (j - 1) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-"
										+ (j + 1) + "CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j + 1) + "CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);

						} else if (i == (rows - 1) && j > 0 && j < colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-"
										+ (j - 1) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 1) + "-"
										+ (j + 1) + "L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-" + (j) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (j) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-"
										+ (j - 1) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((rows - 2) + "-"
										+ (j + 1) + "CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (j - 1) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(("0-" + (j + 1) + "CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
						} else if (i > 0 && i < rows - 1 && j == colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i + "-" + (j - 1) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i + "-0L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j - 1) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-0CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-0CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
						} else if (i > 0 && i < rows - 1 && j == 0) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i + "-" + (colums - 1) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals((i + "-1L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-"
										+ (colums - 1) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-"
										+ (colums - 1) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-1CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-1CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
						}
					}
				}
			}
		}

	}

	/**
	 * Test subscribed topics in internal of square mode toroidal.
	 */
	@Test
	public void testsubscribedTopicsInternalSquareModeToroidal() {
		for (int colums = 2; colums < numLoop; colums++) {
			for (int rows = 2; rows < numLoop; rows++) {
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < colums; j++) {

						setEnviroment(rows, colums, 1, i, j, true);
						toTest.init_connection();

						if (i > 0 && i < rows - 1 && j > 0 && j < colums - 1) {
							int flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i) + "-" + (j + 1) + "L")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i) + "-" + (j - 1) + "R")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j - 1) + "CDDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i - 1) + "-" + (j + 1) + "CDDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
							flag = 0;
							for (String top : stConnection.topicsSubscribed)
								if (top.contentEquals(((i + 1) + "-" + (j + 1) + "CUDL")))
									flag = 1;
							assertFalse("topic whith colums=" + colums
									+ " rows=" + rows + " posi=" + 0 + " posj="
									+ j, flag == 0);
						}
					}
				}
			}
		}
	}
}