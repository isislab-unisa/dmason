package it.isislab.dmason.test.sim.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import sim.util.Double2D;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.test.sim.engine.util.DistributedStateConnectionJMSFake;
import it.isislab.dmason.test.sim.engine.util.StubDistributedState;
import it.isislab.dmason.test.sim.field.utils.StubDistributedField2D;
import it.isislab.dmason.test.util.connection.StubConnectionJMS;
import it.isislab.dmason.test.util.connection.VirtualConnection;
import it.isislab.dmason.test.util.connection.VirtualConnectionNFieldsWithVirtualJMS;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

/**
 * This is the DistributedStateConnectionJMS's tester.
 *
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */

public class DistributedStateConnectionJMSTester {


	/** The variable to test. */
	DistributedStateConnectionJMS<Double2D> toTest;

	/** The connection. */
	StubConnectionJMS stConnection;
	StubDistributedState<Double2D> dm;

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
	 * @param posiqqq
	 *            the rows's index of cell under test
	 * @param posj
	 *            the colum's index of cell under test
	 * @param isToroidal
	 *            set if the space is toroidal or not
	 * */
	public void setEnviroment(int rows, int colums, int mode, int posi,
			int posj, boolean isToroidal) {
		stConnection = new StubConnectionJMS();

		DistributedField2D<Double2D> field = new StubDistributedField2D<Double2D>();

		field.setToroidal(isToroidal);
		dm = new StubDistributedState<Double2D>(field);
		dm.rows = rows;
		dm.columns = colums;
		dm.NUMPEERS = 1; // if numpeers == 0 is throwed an exeption for a
							// division by 0
		dm.MODE = mode;
		dm.TYPE = new CellType(posi, posj);

		toTest = new DistributedStateConnectionJMSFake<Double2D>(dm, "127.0.0.1",
				"61616", stConnection);
		toTest.init_connection();
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

	private ArrayList<String> getTopicMappingForTopicCreation(int i, int j){
		ArrayList<String> result = new ArrayList<>();
		CellType c = new CellType(i, j);
		switch(c.toString()){
		case "0-0":	return new ArrayList<String>(){{add("0-0E");add("0-0S");add("0-0SE");}};
		case "0-1": return new ArrayList<String>(){{add("0-1W");add("0-1E");add("0-1S");add("0-1SW");add("0-1SE");}};
		case "0-2": return new ArrayList<String>(){{add("0-2W");add("0-2SW");add("0-2S");}};
		case "1-0": return new ArrayList<String>(){{add("1-0N");add("1-0NE");add("1-0E");add("1-0SE");add("1-0S");}};
		case "1-1": return new ArrayList<String>(){{add("1-1NW");add("1-1N");add("1-1NE");add("1-1E");add("1-1W");add("1-1SW");add("1-1N");add("1-1SE");}};
		case "1-2": return new ArrayList<String>(){{add("1-2NW");add("1-2N");add("1-2S");add("1-2SW");add("1-2W");}};
		case "2-0": return new ArrayList<String>(){{add("2-0N");add("2-0NE");add("2-0E");}};
		case "2-1": return new ArrayList<String>(){{add("2-1W");add("2-1NW");add("2-1N");add("2-1NE");add("2-1E");}};
		case "2-2": return new ArrayList<String>(){{add("2-2W");add("2-2NW");add("2-2N");}};
		default: return null;
		}
	}

	/**
	 * Verify if the topics are instantiated for a simply, static configuration
	 * */
	@Test
	public void testInstantiatedTopicsNoToroidalSquareMode() { // mode=1
		
		
		int rows=3,columns=3;
		boolean isToroidal= false;
		for(int i=0; i < 3; i++)
			for(int j=0; j < 3; j++){
				setEnviroment(rows, columns, DistributedField2D.UNIFORM_PARTITIONING_MODE, i, j, isToroidal);
				//System.out.println(i+"-"+j+" "+getTopicMap(i,j));
				//System.out.println("real "+stConnection.topics);
				assertNotNull(getTopicMappingForTopicCreation(i,j));
				//System.out.println(new String("0-0").equals(new String("0-0")));
				//System.out.println(stConnection.topics.contains(new String[]{"0-0E","0-0S"}));
				assertTrue(stConnection.topics.containsAll(getTopicMappingForTopicCreation(i, j)));
			}
		
		/*int i = 0;
		
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1L"))
				i = 1;
		assertFalse("topic L not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1R"))
				i = 1;
		assertFalse("topic R not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1U"))
				i = 1;
		assertFalse("topic U not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1D"))
				i = 1;
		assertFalse("topic D not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1CDDR"))
				i = 1;
		assertFalse("topic CDDR not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1CUDR"))
				i = 1;
		assertFalse("topic CUDR not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1CDDL"))
				i = 1;
		assertFalse("topic CDDL not created", i == 0);

		i = 0;
		for (String top : scJMS.topics)
			if (top.contentEquals("1-1CUDL"))
				i = 1;
		assertFalse("topic CUDL not created", i == 0);
*/
	}

//	/**
//	 * Verify if the topics are instantiated for a simply toroidal square mode.
//	 */
//	@Test
//	public void testInstantiatedTopicsToroidalSquareMode() {// mode=1
//		// 5 rows 5 columns mode 1 toroidal posi=1 posj=1
//		setEnviroment(5, 5, 1, 1, 1, true);
//
//		toTest.init_connection();
//		int i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1L"))
//				i = 1;
//		assertFalse("topic L not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1R"))
//				i = 1;
//		assertFalse("topic R not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1U"))
//				i = 1;
//		assertFalse("topic U not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1D"))
//				i = 1;
//		assertFalse("topic D not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1CDDR"))
//				i = 1;
//		assertFalse("topic CDDR not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1CUDR"))
//				i = 1;
//		assertFalse("topic CUDR not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1CDDL"))
//				i = 1;
//		assertFalse("topic CDDL not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("1-1CUDL"))
//				i = 1;
//		assertFalse("topic CUDL not created", i == 0);
//
//	}
//
//	/**
//	 * Test for instantiated topics in a no toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testInstantiatedTopicsNoToroidalHorizontalMode() { // mode=0||mode=3
//		// 1 rows 5 columns mode 0 not toroidal posi=0 posj=1
//		setEnviroment(1, 5, 0, 0, 1, false);
//		toTest.init_connection();
//		int i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("0-1L"))
//				i = 1;
//		assertFalse("topic L not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("0-1R"))
//				i = 1;
//		assertFalse("topic R not created", i == 0);
//
//	}
//
//	/**
//	 * Test for instantiated topics in a toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testInstantiatedTopicsToroidalHorizontalMode() { // mode=0||mode=3
//		// 1 rows 5 columns mode 0 not toroidal posi=0 posj=1
//		setEnviroment(1, 5, 0, 0, 1, true);
//		toTest.init_connection();
//		int i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("0-1L"))
//				i = 1;
//		assertFalse("topic L not created", i == 0);
//
//		i = 0;
//		for (String top : stConnection.topics)
//			if (top.contentEquals("0-1R"))
//				i = 1;
//		assertFalse("topic R not created", i == 0);
//
//	}
//
//	/* TEST FOR ALL CREATED TOPICS */
//
//	/**
//	 * Test created topics in a no toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalHorizontalMode() {
//
//		for (int i = 0; i < numLoop; i++) {
//			setEnviroment(1, numLoop, 0, 0, i, false);
//			toTest.init_connection();
//
//			if ((i > 0 && i < numLoop - 1)) {
//				int flag = 0;
//				for (String top : stConnection.topics)
//					if (top.contentEquals("0-" + i + "L"))
//						flag = 1;
//				assertFalse("topic L not created", flag == 0);
//
//				flag = 0;
//				for (String top : stConnection.topics)
//					if (top.contentEquals("0-" + i + "R"))
//						flag = 1;
//				assertFalse("topic R not created", flag == 0);
//
//			} else if (i == 0) {
//				int flag = 0;
//				for (String top : stConnection.topics)
//					if (top.contentEquals("0-" + i + "L"))
//						flag = 1;
//				assertTrue("topic L created with jPos=0", flag == 0);
//			} else if (i == numLoop - 1) {
//				int flag = 0;
//				for (String top : stConnection.topics)
//					if (top.contentEquals("0-" + i + "R"))
//						flag = 1;
//				assertTrue("topic R created with jPos=lastCell", flag == 0);
//			}
//
//		}
//
//	}
//
//	/**
//	 * Test created topics in a toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsToroidalHorizontalMode() {
//
//		for (int i = 0; i < numLoop; i++) {
//
//			setEnviroment(1, numLoop, 0, 0, i, true);
//			toTest.init_connection();
//
//			int flag = 0;
//			for (String top : stConnection.topics)
//				if (top.contentEquals("0-" + i + "L"))
//					flag = 1;
//			assertFalse("topic L not created with posj=" + i, flag == 0);
//
//			flag = 0;
//			for (String top : stConnection.topics)
//				if (top.contentEquals("0-" + i + "R"))
//					flag = 1;
//			assertFalse("topic R not created with posj=" + i, flag == 0);
//
//		}
//	}
//
//	/**
//	 * Test created topics in a no toroidal square mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalSquareMode() { // square not balanced
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//						setEnviroment(rows, colums, 1, i, j, false);
//						toTest.init_connection();
//
//						if ((i > 0 && i < rows - 1)
//								&& (j > 0 && j < colums - 1)) { // interno della
//																// cornice
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L not created whith colums="
//									+ colums + " rows=" + rows + " posi=" + i
//									+ " posj=" + j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertFalse("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertFalse("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertFalse("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertFalse("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						} else if (i == 0 && j > 0 && j < colums - 1) { // lato
//																		// in
//							// alto
//							// senza estremi
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertFalse("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertTrue("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertTrue("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertTrue("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						} else if (i == rows - 1 && j > 0 && j < colums - 1) { // lato
//																				// in
//																				// basso
//																				// senza
//																				// estremi
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertFalse("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertFalse("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertTrue("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertTrue("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertTrue("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						}
//
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test created topics in a no toroidal square mode right.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalSquareModeRight() { // square
//																// not
//																// balanced
//																// verifica
//																// dei
//																// topic
//																// creati
//																// al
//																// lato destro
//																// esclusi
//																// gli
//																// estremi
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//						setEnviroment(rows, colums, 1, i, j, false);
//						toTest.init_connection();
//
//						if (i > 0 && i < rows - 1 && j == colums - 1) { // lato
//																		// a
//																		// destra
//																		// senza
//																		// estremi
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertFalse("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertFalse("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertTrue("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertTrue("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertTrue("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						}
//
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test created topics in a no toroidal square mode left.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalSquareModeLeft() { // square
//																// not
//																// balanced
//																// verifica
//																// dei
//																// topic
//																// creati
//																// al
//																// lato sinistro
//																// esclusi
//																// gli
//																// estremi
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//						setEnviroment(rows, colums, 1, i, j, false);
//						toTest.init_connection();
//
//						if (i > 0 && i < rows - 1 && j == 0) {
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertFalse("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertFalse("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertTrue("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertTrue("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertTrue("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						}
//
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test created topics in a no toroidal square mode corners up.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalSquareModeCornersUp() {
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//						setEnviroment(rows, colums, 1, i, j, false);
//						toTest.init_connection();
//
//						if (i == 0 && j == 0) {
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertFalse("topic CDDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertTrue("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertTrue("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertTrue("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						} else if (i == 0 && j == colums - 1) {
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertFalse("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertFalse("topic CDDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertTrue("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertTrue("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertTrue("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						}
//
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test created topics in a no toroidal square mode corners down.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsNoToroidalSquareModeCornersDown() {
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//						setEnviroment(rows, colums, 1, i, j, false);
//						toTest.init_connection();
//
//						if (i == rows && j == 0) {
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertFalse("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDR"))
//									flag = 1;
//							assertFalse("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertTrue("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertTrue("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDL"))
//									flag = 1;
//							assertTrue("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						} else if (i == rows - 1 && j == colums - 1) {
//
//							// topic creati
//							int flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "U"))
//									flag = 1;
//							assertFalse("topic U whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "L"))
//									flag = 1;
//							assertFalse("topic L whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CUDL"))
//									flag = 1;
//							assertFalse("topic CUDL whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							// topic non creati
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "R"))
//									flag = 1;
//							assertTrue("topic R whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "D"))
//									flag = 1;
//							assertTrue("topic D whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//							flag = 0;
//							for (String top : stConnection.topics)
//								if (top.contentEquals(i + "-" + j + "CDDR"))
//									flag = 1;
//							assertTrue("topic CUDR whith colums=" + colums
//									+ " rows=" + rows + " posi=" + i + " posj="
//									+ j, flag == 0);
//
//						}
//
//					}
//				}
//			}
//		}
//
//	}
//
//	/**
//	 * Test created topics in a toroidal square mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testCreatedTopicsToroidalSquareMode() {
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//
//						setEnviroment(rows, colums, 1, i, j, true);
//						toTest.init_connection();
//
//						// topic creati (tutti)
//
//						int flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "U"))
//								flag = 1;
//						assertFalse("topic U whith colums=" + colums + " rows="
//								+ rows + " posi=" + i + " posj=" + j, flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "D"))
//								flag = 1;
//						assertFalse("topic D whith colums=" + colums + " rows="
//								+ rows + " posi=" + i + " posj=" + j, flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "R"))
//								flag = 1;
//						assertFalse("topic R whith colums=" + colums + " rows="
//								+ rows + " posi=" + i + " posj=" + j, flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "L"))
//								flag = 1;
//						assertFalse("topic L whith colums=" + colums + " rows="
//								+ rows + " posi=" + i + " posj=" + j, flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "CUDL"))
//								flag = 1;
//						assertFalse(
//								"topic CUDL whith colums=" + colums + " rows="
//										+ rows + " posi=" + i + " posj=" + j,
//								flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "CDDL"))
//								flag = 1;
//						assertFalse(
//								"topic CDDL whith colums=" + colums + " rows="
//										+ rows + " posi=" + i + " posj=" + j,
//								flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "CUDR"))
//								flag = 1;
//						assertFalse(
//								"topic CUDR whith colums=" + colums + " rows="
//										+ rows + " posi=" + i + " posj=" + j,
//								flag == 0);
//
//						flag = 0;
//						for (String top : stConnection.topics)
//							if (top.contentEquals(i + "-" + j + "CDDR"))
//								flag = 1;
//						assertFalse(
//								"topic CDDR whith colums=" + colums + " rows="
//										+ rows + " posi=" + i + " posj=" + j,
//								flag == 0);
//					}
//				}
//			}
//
//		}
//
//	}
//
//	/**
//	 * Test subscribed topics in a no toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testsubscribedTopicsNoToroidalHorizontalMode() {
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int j = 0; j < colums; j++) {
//
//				setEnviroment(1, colums, 0, 0, j, false);
//				toTest.init_connection();
//
//				if (j == 0) {
//					// topic iscritti
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
//							flag = 1;
//					assertFalse("topic L whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//					// non iscritti
//					// trigger graphics global_reduced e altri 2 sono
//					// presenti... quindi
//					// per verificare che il numero dei topic iscritti sia
//					// corretto basta vedere se la dim - 5 è uguale al numero
//					// dei topic creati altrimenti significa che la cella si è
//					// iscritta a qualche topic di troppo
//
//					assertEquals("too topic subscribed", 1,
//							stConnection.topicsSubscribed.size() - 5);
//
//				} else if (j > 0 && j < colums - 1) {
//					// topic iscritti
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
//							flag = 1;
//					assertFalse("topic L whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//					flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
//							flag = 1;
//					assertFalse("topic R whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//					// topic non iscritti
//
//					// trigger graphics global_reduced e altri 2 sono
//					// presenti... quindi
//					// per verificare che il numero dei topic iscritti sia
//					// corretto basta vedere se la dim - 5 è uguale al numero
//					// dei topic creati altrimenti significa che la cella si è
//					// iscritta a qualche topic di troppo
//
//					assertEquals("too topic subscribed", 2,
//							stConnection.topicsSubscribed.size() - 5);
//				} else if (j == colums - 1) {
//					// topic iscritti
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
//							flag = 1;
//					assertFalse("topic R whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//					// topic non iscritti
//
//					// trigger graphics global_reduced e altri 2 sono
//					// presenti... quindi
//					// per verificare che il numero dei topic iscritti sia
//					// corretto basta vedere se la dim - 5 è uguale al numero
//					// dei topic creati altrimenti significa che la cella si è
//					// iscritta a qualche topic di troppo
//
//					assertEquals("too topic subscribed", 1,
//							stConnection.topicsSubscribed.size() - 5);
//				}
//
//			}
//		}
//
//	}
//
//	/**
//	 * Test subscribed topics in a toroidal horizontal mode.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testsubscribedTopicsToroidalHorizontalMode() {
//
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int j = 0; j < colums; j++) {
//
//				setEnviroment(1, colums, 0, 0, j, true);
//				toTest.init_connection();
//
//				if (j == 0) {
//
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
//							flag = 1;
//					assertFalse("topic L whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//					flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (colums - 1) + "R"))
//							flag = 1;
//					assertFalse("topic R whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//				} else if (j > 0 && j < colums - 1) {
//
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j + 1) + "L"))
//							flag = 1;
//					assertFalse("topic L whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//					flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
//							flag = 1;
//					assertFalse("topic R whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//				} else if (j == colums - 1) {
//					int flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (j - 1) + "R"))
//							flag = 1;
//					assertFalse("topic R whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//
//					flag = 0;
//					for (String top : stConnection.topicsSubscribed)
//						if (top.contentEquals(0 + "-" + (0) + "L"))
//							flag = 1;
//					assertFalse("topic L whith colums=" + colums + " rows=" + 1
//							+ " posi=" + 0 + " posj=" + j, flag == 0);
//				}
//
//			}
//		}
//
//	}
//
//	/**
//	 * Test subscribed topics in corners of square mode toroidal.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testsubscribedTopicsCornersSquareModeToroidal() { // avvolte
//																	// questo
//																	// test
//																	// fallisce
//																	// per via
//																	// di un
//																	// topic al
//																	// quale la
//																	// cella non
//																	// sempre si
//																	// iscrive
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//
//						setEnviroment(rows, colums, 1, i, j, true);
//						toTest.init_connection();
//
//						if (i == 0 && j == 0) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(0 + "-" + (colums - 1)
//										+ "R"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-0D"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-"
//										+ (colums - 1) + "CDDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-1L"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-0U"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-1CUDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-1CDDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-" + (colums - 1)
//										+ "CUDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//
//						} else if (i == 0 && j == colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-0L"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-"
//										+ (colums - 1) + "D"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-0CDDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-" + (colums - 2) + "R"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-" + (colums - 1) + "U"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-" + (colums - 2)
//										+ "CUDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-"
//										+ (colums - 2) + "CDDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("1-0CUDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//
//						} else if (i == rows - 1 && j == 0) {
//
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 2) + "-0D"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-0U"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-"
//										+ (colums - 1) + "R"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((rows - 1) + "-1L"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-" + (colums - 1)
//										+ "CUDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i - 1) + "-"
//										+ (colums - 1) + "CDDR"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i - 1) + "-" + (j + 1)
//										+ "CDDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals("0-" + (j + 1) + "CUDL"))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//						} else if (i == rows - 1 && j == colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (colums - 1) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-"
//										+ (colums - 1) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-"
//										+ (colums - 2) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-0L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-"
//										+ (colums - 2) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-0CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (colums - 2) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-0CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//
//						}
//					}
//				}
//			}
//		}
//
//	}
//
//	/**
//	 * Test subscribed topics in sides of square mode toroidal.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testsubscribedTopicsSidesSquareModeToroidal() { // ance in
//																// questo
//																// test si
//																// evince che
//																// spesso la
//																// cella non si
//																// iscrive ad
//																// alcuni field
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//
//						setEnviroment(rows, colums, 1, i, j, true);
//						toTest.init_connection();
//
//						if (i == 0 && j > 0 && j < colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (j - 1) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (j + 1) + "L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-" + (j) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-"
//										+ (j - 1) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-"
//										+ (j + 1) + "CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j + 1) + "CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//
//						} else if (i == (rows - 1) && j > 0 && j < colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-"
//										+ (j - 1) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 1) + "-"
//										+ (j + 1) + "L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-" + (j) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (j) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-"
//										+ (j - 1) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((rows - 2) + "-"
//										+ (j + 1) + "CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (j - 1) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(("0-" + (j + 1) + "CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//						} else if (i > 0 && i < rows - 1 && j == colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i + "-" + (j - 1) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i + "-0L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j - 1) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-0CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-0CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//						} else if (i > 0 && i < rows - 1 && j == 0) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i + "-" + (colums - 1) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals((i + "-1L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-"
//										+ (colums - 1) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-"
//										+ (colums - 1) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-1CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-1CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//						}
//					}
//				}
//			}
//		}
//
//	}
//
//	/**
//	 * Test subscribed topics in internal of square mode toroidal.
//	 */
//	/**
//	 * 
//	 */
//	@Test
//	public void testsubscribedTopicsInternalSquareModeToroidal() {
//		for (int colums = 2; colums < numLoop; colums++) {
//			for (int rows = 2; rows < numLoop; rows++) {
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < colums; j++) {
//
//						setEnviroment(rows, colums, 1, i, j, true);
//						toTest.init_connection();
//
//						if (i > 0 && i < rows - 1 && j > 0 && j < colums - 1) {
//							int flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j) + "D")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j) + "U")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i) + "-" + (j + 1) + "L")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i) + "-" + (j - 1) + "R")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j - 1) + "CDDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i - 1) + "-" + (j + 1) + "CDDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j - 1) + "CUDR")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//							flag = 0;
//							for (String top : stConnection.topicsSubscribed)
//								if (top.contentEquals(((i + 1) + "-" + (j + 1) + "CUDL")))
//									flag = 1;
//							assertFalse("topic whith colums=" + colums
//									+ " rows=" + rows + " posi=" + 0 + " posj="
//									+ j, flag == 0);
//						}
//					}
//				}
//			}
//		}
//	}
}