package it.isislab.dmason.test.sim.field.continuous;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DXY;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.test.sim.engine.util.FakePositionedAgent;
import it.isislab.dmason.test.sim.engine.util.StubDistributedState;
import it.isislab.dmason.util.connection.ConnectionType;

// TODO: Auto-generated Javadoc
/**
 * Test the Class DContinuous2DXY
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public class DContinuousGrid2DXYTester {

	/** The to test. */
	DContinuousGrid2DXY[] toTest;

	/** The distributed state. */
	StubDistributedState<Double2D> ss;

	/** The remote agent. */
	FakePositionedAgent<Double2D> fakeAgent;

	/** The width. */
	int width;

	/** The height. */
	int height;

	/** The max distance. */
	int max_distance;

	/** The rows. */
	int rows;

	/** The columns. */
	int columns;

	/** The num agents. */
	int numAgents;

	/** The mode. */
	int MODE;

	/** The connection type. */
	int connectionType;
  
	boolean isToroidal;
	double discretization;

	String name,topicPrefix;
	/**
	 * Sets the enviroment.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {

		discretization = 0.5;
		width = 800;
		height = 600;
		max_distance = 10;
		rows = 3;
		columns = 3;
		numAgents = 1000;
		MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;
		connectionType = ConnectionType.fakeUnitTestJMS;
		isToroidal =false;
		name="name";
		topicPrefix="prefix";
		GeneralParam genParam = new GeneralParam(width, height, max_distance,
				rows, columns, numAgents, MODE, connectionType);

		ss = new StubDistributedState(genParam);
		
		fakeAgent = new FakePositionedAgent<Double2D>(ss,new Double2D());
		toTest = new DContinuousGrid2DXY[9];
		int cnt=0;
		for(int i=0; i<3;i++)
			for(int j=0; j<3;j++){
				toTest[cnt++] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, ss, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			}

	}

	
	@Test
	public void test_getOwn_x_y(){
		double own_x,own_y;
		int cnt=0;
		for(int i=0; i<3;i++)
			for(int j=0; j<3;j++){
				
				if(j<(width%columns))
					own_x=(int)Math.floor(width/columns+1)*j; 
				else
					own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(j-((width%columns))); 
	
				if(i<(height%rows))
					own_y=(int)Math.floor(height/rows+1)*i; 
				else
					own_y=(int)Math.floor(height/rows+1)*((height%rows))+(int)Math.floor(height/rows)*(i-((height%rows))); 
				
				assertEquals(own_x, toTest[cnt].getOwn_x(),0);
				assertEquals(own_y, toTest[cnt].getOwn_y(),0);
				cnt++;
			}
	}
	
	/**
	 * Test set distributed object location.
	 * @throws DMasonException 
	 */
	@Test
	public void test_setDistributedObjectLocation() {

		for (int i = 0; i < 9; i++) {
					
			// i'm moving an agent in the field
			for (int attempts = 0; attempts < 100; attempts++) {
				Double2D location = toTest[i].getAvailableRandomLocation();
				try{
					toTest[i].setDistributedObjectLocation(location, fakeAgent, ss);
				}catch(DMasonException error){
					fail("Somethig was wrong");
				}
			}
			assertEquals(1, toTest[i].myfield.size());
		}
	}
	
	/**
	 * Test set distributed object location.
	 * @throws DMasonException 
	 */
	@Test
	public void test_setDistributedObjectLocation_with_different_agents() {

		for (int i = 0; i < 9; i++) {
			// i'm moving an agent in the field
			for (int attempts = 0; attempts < 100; attempts++) {
				Double2D location = toTest[i].getAvailableRandomLocation();
				fakeAgent = new FakePositionedAgent<Double2D>(ss, location);
				try{
					toTest[i].setDistributedObjectLocation(location, fakeAgent, ss);
				}catch(DMasonException error){
					fail("Somethig was wrong");
				}
			}
			assertEquals(100, toTest[i].myfield.size());
		}
	}

	/**
	 * Test get state.
	 * @throws DMasonException 
	 */
	@Test
	public void test_GetState() throws DMasonException {
		// i'm moving an agent in the DistributedState
		for (int i = 0; i < 9; i++) assertSame(ss, toTest[i].getState());
		
	}
	

	@Test
	public void test_getAllVisibleAgent(){
		HashMap<String,EntryAgent<Double2D>> expecteds= new HashMap<>();
		for (int i = 0; i < 9; i++) {
			for (int attempts = 0; attempts < 100; attempts++) {
				Double2D location = toTest[i].getAvailableRandomLocation();
				fakeAgent = new FakePositionedAgent<Double2D>(ss, location);
				try{
					toTest[i].setDistributedObjectLocation(location, fakeAgent, ss);
					expecteds.put(fakeAgent.id, new EntryAgent<Double2D>(fakeAgent, location));
				}catch(DMasonException error){
					fail("Somethig was wrong");
				}
			}
			
			for(EntryAgent<Double2D> e: toTest[i].myfield.values()){
				assertTrue(expecteds.containsKey(e.r.getId()));
				assertEquals(expecteds.get(e.r.getId()),e);
			}
		}
	}



//	/**
//	 * Test reset add all true.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testResetAddAllTrue() throws DMasonException {
//		for (int i = 0; i < 100; i++) {
//
//			Double2D location = toTest[0].getAvailableRandomLocation();
//
//			toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//					fakeAgent, /* SimState */ss);
//		}
//		ArrayList<RemotePositionedAgent<Double2D>> ag = new ArrayList<RemotePositionedAgent<Double2D>>();
//		for (int i = 0; i < 10; i++) {
//			ag.add(fakeAgent);
//		}
//
//		assertTrue(toTest[0].resetAddAll(ag));
//	}
//
//	// AGENTS IS MEMORIZED IN THE rmap
//
//	/**
//	 * Test corner mine up left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineUpLeft() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_WEST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.NORTH_WEST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.NORTH_WEST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.NORTH_WEST_MINE.down_xx - toTest[0].rmap.NORTH_WEST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.NORTH_WEST_MINE.down_yy - toTest[0].rmap.NORTH_WEST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.NORTH_WEST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.NORTH_WEST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value corner mine up left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueCornerMineUpLeft() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_WEST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1,
//				toTest[0].rmap.NORTH_WEST_MINE.size());
//
//	}
//
//	/**
//	 * Test corner mine up right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineUpRight() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_EAST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.NORTH_EAST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.NORTH_EAST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.NORTH_EAST_MINE.down_xx - toTest[0].rmap.NORTH_EAST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.NORTH_EAST_MINE.down_yy - toTest[0].rmap.NORTH_EAST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.NORTH_EAST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.NORTH_EAST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value corner mine up right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueCornerMineUpRight() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_EAST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1,
//				toTest[0].rmap.NORTH_EAST_MINE.size());
//
//	}
//
//	/**
//	 * Test corner mine down left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineDownLeft() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_WEST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.SOUTH_WEST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.SOUTH_WEST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.SOUTH_WEST_MINE.down_xx - toTest[0].rmap.SOUTH_WEST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.SOUTH_WEST_MINE.down_yy - toTest[0].rmap.SOUTH_WEST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.SOUTH_WEST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.SOUTH_WEST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value corner mine down left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueCornerMineDownLeft() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_WEST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1,
//				toTest[0].rmap.SOUTH_WEST_MINE.size());
//
//	}
//
//	/**
//	 * Test corner mine down right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineDownRight() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_EAST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.SOUTH_EAST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.SOUTH_EAST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.SOUTH_EAST_MINE.down_xx - toTest[0].rmap.SOUTH_EAST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.SOUTH_EAST_MINE.down_yy - toTest[0].rmap.SOUTH_EAST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.SOUTH_EAST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.SOUTH_EAST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value corner mine down right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueCornerMineDownRight() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_EAST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1,
//				toTest[0].rmap.SOUTH_EAST_MINE.size());
//
//	}
//
//	/**
//	 * Test down mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownMine() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.SOUTH_MINE.down_xx;
//		double jEnd = toTest[0].rmap.SOUTH_MINE.down_yy;
//
//		double stepI = (iEnd - i) / 100;
//		double stepJ = (jEnd - j) / 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.SOUTH_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.SOUTH_MINE.size());
//	}
//
//	/**
//	 * Test boundary value down mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueDownMine() throws DMasonException {
//
//		double i = toTest[0].rmap.SOUTH_MINE.upl_xx;
//		double j = toTest[0].rmap.SOUTH_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1, toTest[0].rmap.SOUTH_MINE.size());
//
//	}
//
//	/**
//	 * Test left mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftMine() throws DMasonException {
//
//		double i = toTest[0].rmap.WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.WEST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.WEST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.WEST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.WEST_MINE.down_xx - toTest[0].rmap.WEST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.WEST_MINE.down_yy - toTest[0].rmap.WEST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.WEST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.WEST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value left mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueLeftMine() throws DMasonException {
//
//		double i = toTest[0].rmap.WEST_MINE.upl_xx;
//		double j = toTest[0].rmap.WEST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1, toTest[0].rmap.WEST_MINE.size());
//
//	}
//
//	/**
//	 * Test right mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightMine() throws DMasonException {
//
//		double i = toTest[0].rmap.EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.EAST_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.EAST_MINE.down_xx;
//		double jEnd = toTest[0].rmap.EAST_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.EAST_MINE.down_xx - toTest[0].rmap.EAST_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.EAST_MINE.down_yy - toTest[0].rmap.EAST_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = toTest[0].rmap.EAST_MINE.upl_yy + stepJ;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//
//		assertEquals(count, toTest[0].rmap.EAST_MINE.size());
//	}
//
//	/**
//	 * Test boundary value right mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueRightMine() throws DMasonException {
//
//		double i = toTest[0].rmap.EAST_MINE.upl_xx;
//		double j = toTest[0].rmap.EAST_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1, toTest[0].rmap.EAST_MINE.size());
//
//	}
//
//	/**
//	 * Test up mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpMine() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_MINE.upl_yy;
//
//		double iEnd = toTest[0].rmap.NORTH_MINE.down_xx;
//		double jEnd = toTest[0].rmap.NORTH_MINE.down_yy;
//
//		double stepI = (toTest[0].rmap.NORTH_MINE.down_xx - toTest[0].rmap.NORTH_MINE.upl_xx)
//				/ 100;
//		double stepJ = (toTest[0].rmap.NORTH_MINE.down_yy - toTest[0].rmap.NORTH_MINE.upl_yy)
//				/ 100;
//
//		i += stepI;
//
//		int count = 0;
//		while (i < iEnd) {
//			j = stepJ + toTest[0].rmap.NORTH_MINE.upl_yy;
//			while (j < jEnd) {
//				Double2D location = new Double2D(i, j);
//				if (toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss))
//					count += 1;
//				j += stepJ;
//			}
//			i += stepI;
//		}
//		assertEquals(count, toTest[0].rmap.NORTH_MINE.size());
//
//	}
//
//	/**
//	 * Test boundary value up mine.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testBoundaryValueUpMine() throws DMasonException {
//
//		double i = toTest[0].rmap.NORTH_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_MINE.upl_yy;
//
//		Double2D location = new Double2D(i, j);
//
//		assertTrue("i=upl_xx j=upl_yy",
//				toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//						fakeAgent, /* SimState */ss));
//
//		assertEquals("agent is not created", 1, toTest[0].rmap.NORTH_MINE.size());
//
//	}
//
//	/**
//	 * Test set distributed object location congruence size.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testSetDistributedObjectLocationCongruenceSize() throws DMasonException {
//		double i = toTest[0].rmap.NORTH_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_MINE.upl_yy;
//
//		double stepI = (toTest[0].rmap.NORTH_MINE.down_xx - toTest[0].rmap.NORTH_MINE.upl_xx) / 3;
//		double stepJ = (toTest[0].rmap.NORTH_MINE.down_yy - toTest[0].rmap.NORTH_MINE.upl_yy) / 3;
//
//		Double2D location = new Double2D(i, j);
//
//		toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//				fakeAgent, /* SimState */ss);
//
//		i += stepI;
//		j += stepJ;
//
//		location = new Double2D(i, j);
//
//		toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//				fakeAgent, /* SimState */ss);
//
//		assertEquals("duplication of agents", 1, toTest[0].rmap.NORTH_MINE.size());
//
//	}
//
//	
//
//	/**
//	 * Test set distributed object location change position.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testSetDistributedObjectLocationChangePosition() throws DMasonException {
//		double i = toTest[0].rmap.NORTH_MINE.upl_xx;
//		double j = toTest[0].rmap.NORTH_MINE.upl_yy;
//
//		double stepI = (toTest[0].rmap.NORTH_MINE.down_xx - toTest[0].rmap.NORTH_MINE.upl_xx) / 4;
//		double stepJ = (toTest[0].rmap.NORTH_MINE.down_yy - toTest[0].rmap.NORTH_MINE.upl_yy) / 4;
//
//		Double2D location = new Double2D(i, j);
//
//		toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//				fakeAgent, /* SimState */ss);
//
//		i += stepI;
//		j += stepJ;
//
//		location = new Double2D(i, j);
//
//		toTest[0].setDistributedObjectLocation(location, /* RemotePositionedAgent */
//				fakeAgent, /* SimState */ss);
//
//		assertNotSame("the method has not changed the position",
//				toTest[0].rmap.NORTH_MINE.get(fakeAgent.id).l, toTest[0].rmap.NORTH_MINE.get(fakeAgent.id).l);
//	}
//
//	/**
//	 * test for the field partitioning.
//	 * @throws DMasonException 
//	 */
//
//	@Test
//	public void testMyFieldPartitioning() throws DMasonException {
//
//		// i need that w and h is equal for using the Pitagora's theorem
//		double w = 120;
//		double h = 120;
//		int maxD = 1; 
//		int rows=3;
//		int columns=rows; // for Pitagora's theorem rows and colums must be equal
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, rows, columns, MODE,
//				"test", "", true);
//
//
//		Double x1 = toTest[0].rmap.NORTH_WEST_MINE.getUpl_xx();//toTest[0].myfield.getUpl_xx();
//		Double y1 = toTest[0].rmap.NORTH_WEST_MINE.getUpl_yy();
//
//		Double x2 = toTest[0].rmap.SOUTH_EAST_MINE.getDown_xx();
//		Double y2 = toTest[0].rmap.SOUTH_EAST_MINE.getDown_yy();
//
//
//
//		// find diagonal with the theorem of distance between 2 points
//		Double diag = Math
//				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
//
//		// find diagonal with the theorem of Pitagora
//		Double diagwh = Math.sqrt(Math.pow(w, 2.0) + Math.pow(h, 2.0));
//		diagwh=diagwh/rows; // calculate only cell's diagonal
//		assertEquals(diag, diagwh);
//	}
//
//	/**
//	 * Test my field partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {
//
//		// i need that w and h is equal for using the Pitagora's theorem
//		double w = 100.0;
//		double h = 100.0;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 1, 1, 2, 2, MODE,
//				"test", "", true);
//
//
//
//
//		Double x1 = toTest[0].myfield.upl_xx;
//		Double y1 = toTest[0].myfield.upl_yy;
//		Double x2 = toTest[0].myfield.down_xx;
//		Double y2 = toTest[0].myfield.down_yy;
//
//
//		// find diagonal with the theorem of distance between 2 points
//		Double diag = Math
//				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
//
//		// find diagonal with the theorem of Pitagora
//		Double diagwh = Math.sqrt(Math.pow(w/2 - 2 * maxD, 2.0)+ Math.pow(h/2 - 2 * maxD, 2.0));
//
//		assertEquals(diag, diagwh);
//	}
//
//	/**
//	 * Test my field partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testMyFieldPartitioningWHX() throws DMasonException {
//
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; (j < i) && (j < k); j++) {
//					// i need that w and h is equal for using the Pitagora's
//					// theorem
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].myfield.down_xx;
//					Double x1 = toTest[0].myfield.upl_xx;
//					Double y2 = toTest[0].myfield.down_yy;
//					Double y1 = toTest[0].myfield.upl_yy;
//
//					// find diagonal with the theorem of distance between 2
//					// points
//					Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//							+ Math.pow(y2 - y1, 2.0));
//
//					// find diagonal with the theorem of Pitagora
//					Double diagwh = Math.sqrt(Math.pow(w - 2 * maxD, 2.0)
//							+ Math.pow(h - 2 * maxD, 2.0));
//
//					assertEquals(diag, diagwh);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test up mine partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpMinePartitioning() throws DMasonException {
//
//		double w = 10;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.NORTH_MINE.down_xx;
//		Double x1 = toTest[0].rmap.NORTH_MINE.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test down mine partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownMinePartitioningMaxDistance1() throws DMasonException {
//
//		double w = 10;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.NORTH_MINE.down_xx;
//		Double x1 = toTest[0].rmap.NORTH_MINE.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test down mine partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownMinePartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.NORTH_MINE.down_xx;
//					Double x1 = toTest[0].rmap.NORTH_MINE.upl_xx;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test down mine partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownMinePartitioning() throws DMasonException {
//
//		double w = 10;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.SOUTH_MINE.down_xx;
//		Double x1 = toTest[0].rmap.SOUTH_MINE.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test up mine partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpMinePartitioningMaxDistance1() throws DMasonException {
//
//		double w = 10;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.SOUTH_MINE.down_xx;
//		Double x1 = toTest[0].rmap.SOUTH_MINE.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test up mine partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpMinePartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.SOUTH_MINE.down_xx;
//					Double x1 = toTest[0].rmap.SOUTH_MINE.upl_xx;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test left mine partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftMinePartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.WEST_MINE.down_yy;
//		Double x1 = toTest[0].rmap.WEST_MINE.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test left mine partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftMinePartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.WEST_MINE.down_yy;
//		Double x1 = toTest[0].rmap.WEST_MINE.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test left mine partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftMinePartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.WEST_MINE.down_yy;
//					Double x1 = toTest[0].rmap.WEST_MINE.upl_yy;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test right mine partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightMinePartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.EAST_MINE.down_yy;
//		Double x1 = toTest[0].rmap.EAST_MINE.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test right mine partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightMinePartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.EAST_MINE.down_yy;
//		Double x1 = toTest[0].rmap.EAST_MINE.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test right mine partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightMinePartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.EAST_MINE.down_yy;
//					Double x1 = toTest[0].rmap.EAST_MINE.upl_yy;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test up out partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpOutPartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.NORTH_OUT.down_xx;
//		Double x1 = toTest[0].rmap.NORTH_OUT.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test down out partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownOutPartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.NORTH_OUT.down_xx;
//		Double x1 = toTest[0].rmap.NORTH_OUT.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test down out partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownOutPartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.NORTH_OUT.down_xx;
//					Double x1 = toTest[0].rmap.NORTH_OUT.upl_xx;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test down out partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testDownOutPartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.SOUTH_OUT.down_xx;
//		Double x1 = toTest[0].rmap.SOUTH_OUT.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test up out partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpOutPartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.SOUTH_OUT.down_xx;
//		Double x1 = toTest[0].rmap.SOUTH_OUT.upl_xx;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test up out partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testUpOutPartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.SOUTH_OUT.down_xx;
//					Double x1 = toTest[0].rmap.SOUTH_OUT.upl_xx;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_width, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test left out partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftOutPartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.WEST_OUT.down_yy;
//		Double x1 = toTest[0].rmap.WEST_OUT.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test left out partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftOutPartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.WEST_OUT.down_yy;
//		Double x1 = toTest[0].rmap.WEST_OUT.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test left out partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testLeftOutPartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.WEST_OUT.down_yy;
//					Double x1 = toTest[0].rmap.WEST_OUT.upl_yy;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test right out partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightOutPartitioning() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//		//		ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.EAST_OUT.down_yy;
//		Double x1 = toTest[0].rmap.EAST_OUT.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test right out partitioning max distance1.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightOutPartitioningMaxDistance1() throws DMasonException {
//
//		double w = 100;
//		double h = w;
//		int maxD = 1;
//
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//				"test", "", true);
//
//		Double x2 = toTest[0].rmap.EAST_OUT.down_yy;
//		Double x1 = toTest[0].rmap.EAST_OUT.upl_yy;
//
//		// find distance between 2 points
//		Double side = x2 - x1;
//
//		assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//	}
//
//	/**
//	 * Test right out partitioning whx.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testRightOutPartitioningWHX() throws DMasonException {
//		for (double i = 1; i < 100; i++) {
//			for (double k = 1; k < 100; k++) {
//				for (int j = 0; j < i; j++) {
//
//					double w = i*100;
//					double h = k*100;
//					int maxD = j;
//					if(maxD<1) maxD=1;
//					//					toTest[0] = new DContinuous2DXY(/* discretization */0.5, w, h, /* simState */
//					//					ss, maxD, /* i */0, /* j */0, 1, 1, /* name */
//					//					"test", /* prefix */"");
//					toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//							0.5, w, h, ss, maxD, 0, 0, 1, 1, MODE,
//							"test", "", true);
//
//					Double x2 = toTest[0].rmap.EAST_OUT.down_yy;
//					Double x1 = toTest[0].rmap.EAST_OUT.upl_yy;
//
//					// find distance between 2 points
//					Double side = x2 - x1;
//
//					assertEquals(toTest[0].my_height, side.doubleValue(), 0);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test corner mine down left partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineDownLeftPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = toTest[0].rmap.SOUTH_WEST_MINE.down_xx;
//			Double y1 = toTest[0].rmap.SOUTH_WEST_MINE.down_yy;
//			Double x2 = toTest[0].rmap.SOUTH_WEST_MINE.upl_xx;
//			Double y2 = toTest[0].rmap.SOUTH_WEST_MINE.upl_yy;
//
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner mine down right partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineDownRightPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = toTest[0].rmap.SOUTH_EAST_MINE.down_xx;
//			Double y1 = toTest[0].rmap.SOUTH_EAST_MINE.down_yy;
//			Double x2 = toTest[0].rmap.SOUTH_EAST_MINE.upl_xx;
//			Double y2 = toTest[0].rmap.SOUTH_EAST_MINE.upl_yy;
//
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner mine up left partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineUpLeftPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = toTest[0].rmap.NORTH_WEST_MINE.down_xx;
//			Double y1 = toTest[0].rmap.NORTH_WEST_MINE.down_yy;
//			Double x2 = toTest[0].rmap.NORTH_WEST_MINE.upl_xx;
//			Double y2 = toTest[0].rmap.NORTH_WEST_MINE.upl_yy;
//
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner mine up right partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerMineUpRightPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = toTest[0].rmap.NORTH_EAST_MINE.down_xx;
//			Double y1 = toTest[0].rmap.NORTH_EAST_MINE.down_yy;
//			Double x2 = toTest[0].rmap.NORTH_EAST_MINE.upl_xx;
//			Double y2 = toTest[0].rmap.NORTH_EAST_MINE.upl_yy;
//
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down left diag center partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerOutDownLeftDiagCenterPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = toTest[0].rmap.SOUTH_WEST_OUT.down_xx;
//			Double y1 = toTest[0].rmap.SOUTH_WEST_OUT.down_yy;
//			Double x2 = toTest[0].rmap.SOUTH_WEST_OUT.upl_xx;
//			Double y2 = toTest[0].rmap.SOUTH_WEST_OUT.upl_yy;
//
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down left diag down partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutDownLeftDiagDownPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_down_left_diag_down.upl_xx;
//				y2 = toTest[0].rmap.corner_out_down_left_diag_down.upl_yy;
//				x1 = toTest[0].rmap.corner_out_down_left_diag_down.down_xx;
//				y1 = toTest[0].rmap.corner_out_down_left_diag_down.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down left diag left partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutDownLeftDiagLeftPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_down_left_diag_left.upl_xx;
//				y2 = toTest[0].rmap.corner_out_down_left_diag_left.upl_yy;
//				x1 = toTest[0].rmap.corner_out_down_left_diag_left.down_xx;
//				y1 = toTest[0].rmap.corner_out_down_left_diag_left.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down rigth diag center partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerOutDownRigthDiagCenterPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.SOUTH_EAST_OUT.upl_xx;
//				y2 = toTest[0].rmap.SOUTH_EAST_OUT.upl_yy;
//				x1 = toTest[0].rmap.SOUTH_EAST_OUT.down_xx;
//				y1 = toTest[0].rmap.SOUTH_EAST_OUT.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down rigth diag down partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutDownRigthDiagDownPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_down_right_diag_down.upl_xx;
//				y2 = toTest[0].rmap.corner_out_down_right_diag_down.upl_yy;
//				x1 = toTest[0].rmap.corner_out_down_right_diag_down.down_xx;
//				y1 = toTest[0].rmap.corner_out_down_right_diag_down.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out down rigth diag right partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutDownRigthDiagRightPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_down_right_diag_right.upl_xx;
//				y2 = toTest[0].rmap.corner_out_down_right_diag_right.upl_yy;
//				x1 = toTest[0].rmap.corner_out_down_right_diag_right.down_xx;
//				y1 = toTest[0].rmap.corner_out_down_right_diag_right.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out up left diag center partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerOutUpLeftDiagCenterPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.NORTH_WEST_OUT.upl_xx;
//				y2 = toTest[0].rmap.NORTH_WEST_OUT.upl_yy;
//				x1 = toTest[0].rmap.NORTH_WEST_OUT.down_xx;
//				y1 = toTest[0].rmap.NORTH_WEST_OUT.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//	}
//
//	/**
//	 * Test corner out up left diag left partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutUpLeftDiagLeftPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			//			toTest[0].setToroidal(true);
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_up_left_diag_left.upl_xx;
//				y2 = toTest[0].rmap.corner_out_up_left_diag_left.upl_yy;
//				x1 = toTest[0].rmap.corner_out_up_left_diag_left.down_xx;
//				y1 = toTest[0].rmap.corner_out_up_left_diag_left.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out up left diag up partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutUpLeftDiagUpPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_up_left_diag_up.upl_xx;
//				y2 = toTest[0].rmap.corner_out_up_left_diag_up.upl_yy;
//				x1 = toTest[0].rmap.corner_out_up_left_diag_up.down_xx;
//				y1 = toTest[0].rmap.corner_out_up_left_diag_up.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out up right diag center partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerOutUpRightDiagCenterPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.NORTH_EAST_OUT.upl_xx;
//				y2 = toTest[0].rmap.NORTH_EAST_OUT.upl_yy;
//				x1 = toTest[0].rmap.NORTH_EAST_OUT.down_xx;
//				y1 = toTest[0].rmap.NORTH_EAST_OUT.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out up right diag right partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutUpRightDiagRightPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_up_right_diag_right.upl_xx;
//				y2 = toTest[0].rmap.corner_out_up_right_diag_right.upl_yy;
//				x1 = toTest[0].rmap.corner_out_up_right_diag_right.down_xx;
//				y1 = toTest[0].rmap.corner_out_up_right_diag_right.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner out up right diag up partitioning.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCornerOutUpRightDiagUpPartitioning() throws DMasonException {
//		for (int i = 1; i < 100; i++) {
//			//			toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//			//			ss, i, /* i */0, /* j */0, 1, 1, /* name */
//			//			"test", /* prefix */"");
//			toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//					0.5, 10, 10, ss, i, 0, 0, 1, 1, MODE,
//					"test", "", true);
//
//			Double x1 = null, x2 = null, y1 = null, y2 = null;
//			try {
//				x2 = toTest[0].rmap.corner_out_up_right_diag_up.upl_xx;
//				y2 = toTest[0].rmap.corner_out_up_right_diag_up.upl_yy;
//				x1 = toTest[0].rmap.corner_out_up_right_diag_up.down_xx;
//				y1 = toTest[0].rmap.corner_out_up_right_diag_up.down_yy;
//			} catch (NullPointerException e) {
//				fail("corner has no size");
//			}
//			// find diagonal with the theorem of distance between 2
//			// points
//			Double diag = Math.sqrt(Math.pow(x2 - x1, 2.0)
//					+ Math.pow(y2 - y1, 2.0));
//
//			// find diagonal with the theorem of Pitagora
//			Double realDiag = Math.sqrt(Math.pow(i, 2.0) + Math.pow(i, 2.0));
//
//			assertEquals(realDiag.doubleValue(), diag.doubleValue(), 0);
//		}
//
//	}
//
//	/**
//	 * Test corner congruence up right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerCongruenceUpRight() throws DMasonException {
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, 1000, 1000, ss, /*maxD*/1, 1, 1, 3, 3, MODE,
//				"test", "", true);
//
//		assertEquals("x", toTest[0].rmap.NORTH_EAST_MINE.upl_xx,
//				toTest[0].rmap.NORTH_EAST_OUT.down_xx - 2, 0);
//		assertEquals("y", toTest[0].rmap.NORTH_EAST_MINE.upl_yy,
//				toTest[0].rmap.NORTH_EAST_OUT.down_yy, 0);
//	}
//
//	/**
//	 * Test corner congruence up left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerCongruenceUpLeft() throws DMasonException {
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, 10, 10, ss, 1, 1, 1, 3, 3, MODE,
//				"test", "", true);
//
//		assertEquals("x", toTest[0].rmap.NORTH_WEST_MINE.upl_xx,
//				toTest[0].rmap.NORTH_WEST_OUT.down_xx, 0);
//		assertEquals("y", toTest[0].rmap.NORTH_WEST_MINE.upl_yy,
//				toTest[0].rmap.NORTH_WEST_OUT.down_yy, 0);
//	}
//
//	/**
//	 * Test corner congruence down left.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerCongruenceDownLeft() throws DMasonException {
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, 10, 10, ss, 1, 1, 1, 3, 3, MODE,
//				"test", "", true);
//
//		assertEquals("x", toTest[0].rmap.SOUTH_WEST_MINE.upl_xx,
//				toTest[0].rmap.SOUTH_WEST_OUT.down_xx, 0);
//		assertEquals("y", toTest[0].rmap.SOUTH_WEST_MINE.upl_yy,
//				toTest[0].rmap.SOUTH_WEST_OUT.down_yy - 2, 0);
//	}
//
//	/**
//	 * Test corner congruence down right.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testCornerCongruenceDownRight() throws DMasonException {
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, 10, 10, ss, 1, 1, 1, 3, 3, MODE,
//				"test", "", true);
//
//		assertEquals("x", toTest[0].rmap.SOUTH_EAST_MINE.down_xx,
//				toTest[0].rmap.SOUTH_EAST_OUT.upl_xx, 0);
//		assertEquals("y", toTest[0].rmap.SOUTH_EAST_MINE.down_yy,
//				toTest[0].rmap.SOUTH_EAST_OUT.upl_yy, 0);
//	}
//
//	/**
//	 * Test my field congruence.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testMyFieldCongruence() throws DMasonException {
//		//		toTest[0] = new DContinuous2DXY(/* discretization */0.5, 10, 10, /* simState */
//		//		ss, 1, /* i */1, /* j */1, /* rows */3, /* Colums */3, /* name */
//		//		"test", /* prefix */"");
//		toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(
//				0.5, 10, 10, ss, 1, 1, 1, 3, 3, MODE,
//				"test", "", true);
//
//		// upLeft
//		assertEquals("X Up Left", toTest[0].myfield.upl_xx,
//				toTest[0].rmap.NORTH_MINE.upl_xx + 1, 0);
//		assertEquals("Y Up Left", toTest[0].myfield.upl_yy,
//				toTest[0].rmap.NORTH_MINE.upl_yy + 1, 0);
//		// downRight
//		assertEquals("X Down Right", toTest[0].myfield.down_xx,
//				toTest[0].rmap.SOUTH_MINE.down_xx - 1, 0);
//		assertEquals("Y Down Right", toTest[0].myfield.down_yy,
//				toTest[0].rmap.SOUTH_MINE.down_yy - 1, 0);
//
//	}
//
//	/**
//	 * Test congruence for all xyrc not boundary.
//	 * @throws DMasonException 
//	 */
//	@Ignore
//	public void testCongruenceForAllXYRCNotBoundary() throws DMasonException {
//
//		int r, c;
//		int nLoop = 100;
//		r = nLoop;
//		c = nLoop;
//
//		for (int ir = 3; ir < r; ir++) {
//			for (int ic = 3; ic < c; ic++) {
//				for (int ii = 1; ii < ir - 1; ii++) {
//					for (int ij = 1; ij < ic - 1; ij++) {
//
//						toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(/* discretization */0.5,
//								10, 10, /* simState */
//								ss, 1, /* i */ii, /* j */ij, /* rows */ir, /* Colums */
//								ic,MODE, /* name */
//								"test", /* prefix */"",true);
//
//						// cornerUpRight
//						assertEquals(
//								"cornerUpRight x i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.NORTH_EAST_MINE.upl_xx,
//								toTest[0].rmap.NORTH_EAST_OUT.down_xx - 2,
//								0);
//						assertEquals(
//								"cornerUpRight y i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.NORTH_EAST_MINE.upl_yy,
//								toTest[0].rmap.NORTH_EAST_OUT.down_yy,
//								0);
//
//						// cornerUpLeft
//						assertEquals(
//								"cornerUpLeft x i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.NORTH_WEST_MINE.upl_xx,
//								toTest[0].rmap.NORTH_WEST_OUT.down_xx,
//								0);
//						assertEquals(
//								"cornerUpLeft y i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.NORTH_WEST_MINE.upl_yy,
//								toTest[0].rmap.NORTH_WEST_OUT.down_yy,
//								0);
//
//						// cornerDownLeft
//						assertEquals(
//								"cornerDownLeft x i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.SOUTH_WEST_MINE.upl_xx,
//								toTest[0].rmap.SOUTH_WEST_OUT.down_xx,
//								0);
//						assertEquals(
//								"cornerDownLeft y i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.SOUTH_WEST_MINE.upl_yy,
//								toTest[0].rmap.SOUTH_WEST_OUT.down_yy - 2,
//								0);
//
//						// cornerDownRight
//						assertEquals(
//								"cornerDownRight x i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.SOUTH_EAST_MINE.down_xx,
//								toTest[0].rmap.SOUTH_EAST_OUT.upl_xx,
//								0);
//						assertEquals(
//								"cornerDownRight y i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].rmap.SOUTH_EAST_MINE.down_yy,
//								toTest[0].rmap.SOUTH_EAST_OUT.upl_yy,
//								0);
//
//						// myField upLeft
//						assertEquals("myField X Up Left i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].myfield.upl_xx,
//								toTest[0].rmap.NORTH_MINE.upl_xx + 1, 0);
//						assertEquals("myField Y Up Left i=" + ii + " j=" + ij
//								+ " rows=" + ir + " colums=" + ic,
//								toTest[0].myfield.upl_yy,
//								toTest[0].rmap.NORTH_MINE.upl_yy + 1, 0);
//						// myField downRight
//						assertEquals("myField X Down Right i=" + ii + " j="
//								+ ij + " rows=" + ir + " colums=" + ic,
//								toTest[0].myfield.down_xx,
//								toTest[0].rmap.SOUTH_MINE.down_xx - 1, 0);
//						assertEquals("myField Y Down Right i=" + ii + " j="
//								+ ij + " rows=" + ir + " colums=" + ic,
//								toTest[0].myfield.down_yy,
//								toTest[0].rmap.SOUTH_MINE.down_yy - 1, 0);
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Test for all partitioning.
//	 * @throws DMasonException 
//	 */
//	@Test
//	public void testForAllPartitioning() throws DMasonException {
//		int r, c;
//		r = 100;
//		c = 100;
//		width = 563;
//		height = 491;
//		double discretization = 0.5;
//		int max_distance = 1;
//		String name = "test", prefix = "";
//
//		DContinuousGrid2DXY effective = null;
//		// iterate for all r*c cells each one with celltype ij
//		for (int i = 0; i < r; i++)
//
//			for (int j = 0; j < c; j++) {
//
//				toTest[0] = (DContinuousGrid2DXY) DContinuousGrid2DFactory.createDContinuous2D(/* discretization */0.5,
//						width, height, /* simState */ ss, max_distance, /* i */i, /* j */j, /* rows */r, /* Colums */c,MODE, /* name */"test", /* prefix */"",true);
//
//				effective = new DContinuousGrid2DXY(discretization, width, height, ss, max_distance, i, j, r, c, name, prefix,toTest[0].isToroidal());
//
//				assertEquals("Problems with myfield.down_xx",effective.myfield.down_xx,toTest[0].myfield.down_xx);
//				assertEquals("Problems with myfield.down_yy",effective.myfield.down_yy,toTest[0].myfield.down_yy);
//				assertEquals("Problems with myfield.upl_xx",effective.myfield.upl_xx,toTest[0].myfield.upl_xx);
//				assertEquals("Problems with myfield.upl_yy",effective.myfield.upl_yy,toTest[0].myfield.upl_yy);
//
//				assertEquals("Problems with rmap.left_out",effective.rmap.WEST_OUT,toTest[0].rmap.WEST_OUT);
//				assertEquals("Problems with rmap.left_mine",effective.rmap.WEST_MINE,toTest[0].rmap.WEST_MINE);
//				assertEquals("Problems with rmap.right_mine",effective.rmap.EAST_MINE,toTest[0].rmap.EAST_MINE);
//				assertEquals("Problems with rmap.right_out",effective.rmap.EAST_OUT,toTest[0].rmap.EAST_OUT);
//				assertEquals("Problems with rmap.down_mine",effective.rmap.SOUTH_MINE,toTest[0].rmap.SOUTH_MINE);
//				assertEquals("Problems with rmap.down_out",effective.rmap.SOUTH_OUT,toTest[0].rmap.SOUTH_OUT);
//				assertEquals("Problems with rmap.up_mine",effective.rmap.NORTH_MINE,toTest[0].rmap.NORTH_MINE);
//				assertEquals("Problems with rmap.up_out",effective.rmap.NORTH_OUT,toTest[0].rmap.NORTH_OUT);
//				assertEquals("Problems with rmap.corner_mine_up_left",effective.rmap.NORTH_WEST_MINE,toTest[0].rmap.NORTH_WEST_MINE);
//				assertEquals("Problems with rmap.corner_out_up_left_diag_center",effective.rmap.NORTH_WEST_OUT,toTest[0].rmap.NORTH_WEST_OUT);      
//				assertEquals("Problems with rmap.corner_out_up_left_diag_up",effective.rmap.corner_out_up_left_diag_up,toTest[0].rmap.corner_out_up_left_diag_up);                          
//				assertEquals("Problems with rmap.corner_out_up_left_diag_left",effective.rmap.corner_out_up_left_diag_left,toTest[0].rmap.corner_out_up_left_diag_left);
//				assertEquals("Problems with rmap.corner_mine_up_right",effective.rmap.NORTH_EAST_MINE,toTest[0].rmap.NORTH_EAST_MINE);
//				assertEquals("Problems with rmap.corner_out_up_right_diag_center",effective.rmap.NORTH_EAST_OUT,toTest[0].rmap.NORTH_EAST_OUT);
//				assertEquals("Problems with rmap.corner_out_up_right_diag_up",effective.rmap.corner_out_up_right_diag_up,toTest[0].rmap.corner_out_up_right_diag_up);                          
//				assertEquals("Problems with rmap.corner_out_up_right_diag_right",effective.rmap.corner_out_up_right_diag_right,toTest[0].rmap.corner_out_up_right_diag_right);
//				assertEquals("Problems with rmap.corner_mine_down_left",effective.rmap.SOUTH_WEST_MINE,toTest[0].rmap.SOUTH_WEST_MINE);
//				assertEquals("Problems with rmap.corner_out_down_left_diag_center",effective.rmap.SOUTH_WEST_OUT,toTest[0].rmap.SOUTH_WEST_OUT); 
//				assertEquals("Problems with rmap.corner_out_down_left_diag_left",effective.rmap.corner_out_down_left_diag_left,toTest[0].rmap.corner_out_down_left_diag_left);                    
//				assertEquals("Problems with rmap.corner_out_down_left_diag_down",effective.rmap.corner_out_down_left_diag_down,toTest[0].rmap.corner_out_down_left_diag_down);
//				assertEquals("Problems with rmap.corner_mine_down_right",effective.rmap.SOUTH_EAST_MINE,toTest[0].rmap.SOUTH_EAST_MINE);
//				assertEquals("Problems with rmap.corner_out_down_right_diag_center",effective.rmap.SOUTH_EAST_OUT,toTest[0].rmap.SOUTH_EAST_OUT);
//				assertEquals("Problems with rmap.corner_out_down_right_diag_right",effective.rmap.corner_out_down_right_diag_right,toTest[0].rmap.corner_out_down_right_diag_right);                
//				assertEquals("Problems with rmap.corner_out_down_right_diag_down",effective.rmap.corner_out_down_right_diag_down,toTest[0].rmap.corner_out_down_right_diag_down);
//
//			}
//
//	}
}
