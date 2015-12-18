package it.isislab.dmason.test.sim.field.grid.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Random;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2DXY;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sim.engine.SimState;
import sim.util.Int2D;
/**
* The Class DDoubleGrid2DXYTester. Tests the DDoubleGrid2DXY for the non toroidal distribution.
*
* * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
*/
public class DDoubleGrid2DXYTester {
	/** The to test. */
	DDoubleGrid2DXY toTest;
	
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
	
	CellType celltype;
	
	GeneralParam genParam;

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
		 * @param params the params
		 */
		public StubDistributedState(GeneralParam params) {
			
			super(params, new DistributedMultiSchedule<Int2D>(), "stub",
					params.getConnectionType());
			this.MODE = params.getMode();

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
		 */
		@Override
		public DistributedField<Int2D> getField() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#addToField(it.isislab.dmason.sim.engine.RemotePositionedAgent, java.lang.Object)
		 */
		@Override
		public void addToField(RemotePositionedAgent<Int2D> rm, Int2D loc) {
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

	}


	/**
	 * Sets the enviroment.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {

		width = 100;
		height = 100;
		maxDistance = 1;
		rows = 10;
		columns = 10;
		numAgents = numLoop;
		mode = DistributedField2D.UNIFORM_PARTITIONING_MODE;
		connectionType = ConnectionType.pureActiveMQ;
		celltype = new CellType(0, 0);
		genParam = new GeneralParam(width, height, maxDistance,
				rows, columns, numAgents, mode, connectionType);

		ss = new StubDistributedState(genParam);
		
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(width, height,/* simState */
		ss, maxDistance, /* i */celltype.pos_i, /* j */celltype.pos_j, rows, columns,mode,0,false,/* name */"test", /* prefix */"",false);


	}
	
	
	/**
	 * Test set distributed object location. 
	 * 	If the location is not available for region or a int value was inserted the method return false   
	 * @throws DMasonException 
	 */
	@Test
	public void testSetDistributedObjectLocation() throws DMasonException {
		double j=1.5;
		    
			Int2D location = new Int2D(toTest.myfield.upl_xx, toTest.myfield.upl_yy);
			assertTrue(toTest.setDistributedObjectLocation(location, 
					/* grid value */j, /* SimState */ss));
			
			
			/*
			 * Note: To test the method effects you should test also the real value in the location...
			 * 		but it is not possible, because the result will be available after method synchro invocation.   
			 * 
			 * assertEquals("The value in location "+location+" should be the same",j,toTest.field[location.x][location.y],0);
			*/
	}

	/**
	 * Test set distributed object location. 
	 * 	If the location is not available for region or a int value was inserted the method return false   
	 * @throws DMasonException 
	 */
	@Test
	public void testMultipleSetDistributedObjectLocationOnSameLocation() throws DMasonException {
		double j=1.5;
		
		Int2D location = new Int2D(toTest.myfield.upl_xx, toTest.myfield.upl_yy);
		for(int i=0; i<10; i++)
			toTest.setDistributedObjectLocation(location,/* grid value */j, /* SimState */ss);
		
		assertEquals("Multiple value setting in the same position shouldn't increase the region size",1,toTest.myfield.size());
	}
	
	@Test
	public void testMultipleSetDistributedObjectLocation() throws DMasonException {
		double j=1.5; 
		int x = toTest.myfield.upl_xx;
		int y = toTest.myfield.upl_yy;
		int shiftx,shifty;
		Random r = new Random();
		Int2D location = null;
		for(int i=0; i<10; i++){
			shiftx= Math.abs(x-toTest.myfield.down_xx);
			shifty = Math.abs(y-toTest.myfield.down_yy);
			location = new Int2D(x+r.nextInt(shiftx) , y+r.nextInt(shifty));
			
			assertTrue("The location should be rigth",toTest.setDistributedObjectLocation(location,/* grid value */j, /* SimState */ss));
		}
		
		assertEquals(10,toTest.myfield.size());
	}
	
	
	/**
	 * Test getAvailableRandomLocation. It should return a Int2D location in according to field creation. 
	 * @throws DMasonException 
	 */
	@Test
	public void testGetAvailableRandomLocation() throws DMasonException {
	
			Int2D location = toTest.getAvailableRandomLocation();
			
			assertTrue(toTest.setDistributedObjectLocation(location, 
					/* grid value */4.6, /* SimState */ss));
	}
	
	

	/**
	 * Test get state.
	 * @throws DMasonException 
	 */
	@Test
	public void testGetState() throws DMasonException {
	
		assertSame(ss, toTest.getState());
	}

	
	@Test
	public void testPublicVariables() throws DMasonException {
		
		assertEquals("The celltype should be "+celltype.pos_i,celltype.pos_i,toTest.cellType.pos_i);
		assertEquals("The celltype should be "+celltype.pos_j,celltype.pos_j,toTest.cellType.pos_j);
		assertEquals("The columns should be "+columns,columns,toTest.columns);
		assertEquals("The rows should be "+rows,rows,toTest.rows);
		assertEquals("The maxDistance should be "+maxDistance,maxDistance,toTest.AOI);
		assertEquals("The width should be "+width,width,toTest.getWidth());
		assertEquals("The height should be "+height,height,toTest.getHeight());
		int my_width,my_height;
		my_width = width / rows;
		my_height = height / columns;
		assertEquals("The my_height should be "+my_height,my_height,toTest.my_height);
		assertEquals("The my_width should be "+my_width,my_width,toTest.my_width);
		
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
         
		double v=1.4;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_left.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_left.size());
	}

	
	*//**
	 * Test corner mine up right.
	 * @throws DMasonException 
	 *//*
	*//**
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
        double v=1.4; 
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_up_right.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_up_right.size());
	}

	*//**
	 * Test corner mine down left.
	 * @throws DMasonException 
	 *//*
	*//**
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
        double v=1.6;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_left.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v+v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_down_left.size());
	}

	*//**
	 * Test corner mine down right.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
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

		double v=1.6;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.corner_mine_down_right.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.corner_mine_down_right.size());
	}

	*//**
	 * Test down mine.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDownMine() throws DMasonException {

		int i = toTest.rmap.down_mine.upl_xx;
		int j = toTest.rmap.down_mine.upl_yy;

		int iEnd = toTest.rmap.down_mine.down_xx;
		int jEnd = toTest.rmap.down_mine.down_yy;

		int stepI = (iEnd - i) / numLoop;
		int stepJ = (jEnd - j) / numLoop;

		i += stepI;

		double v=1.5;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.down_mine.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;

			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.down_mine.size());
	}

	*//**
	 * Test left mine.
	 * @throws DMasonException 
	 *//*
	*//**
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

		double v=1.9;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.left_mine.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						i,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.left_mine.size());
	}



	*//**
	 * Test right mine.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
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
        double v=1.9;
		int count = 0;
		while (i < iEnd) {
			j = toTest.rmap.right_mine.upl_yy + stepJ;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}

		assertEquals(count, toTest.rmap.right_mine.size());
	}

	*//**
	 * Test up mine.
	 * @throws DMasonException 
	 *//*
	*//**
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
         double v=1.6;
		int count = 0;
		while (i < iEnd) {
			j = stepJ + toTest.rmap.up_mine.upl_yy;
			while (j < jEnd) {
				v+=(v*v)/2;
				Int2D location = new Int2D(i, j);
				if (toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
						v,  SimState ss))
					count += 1;
				j += stepJ;
			}
			i += stepI;
		}
		assertEquals(count, toTest.rmap.up_mine.size());

	}


	*//**
	 * Test set distributed object location congruence size.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testSetDistributedObjectLocationCongruenceSize() throws DMasonException {
		int i = toTest.rmap.up_mine.upl_xx;
		int j = toTest.rmap.up_mine.upl_yy;

		int stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 3;
		int stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 3;

		Int2D location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
			i,  SimState ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
				i,  SimState ss);

		assertEquals("duplication of agents", 1, toTest.rmap.up_mine.size());

	}

	*//**
	 * Test double set distributed object location god agent.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDoubleSetDistributedObjectLocationGodAgent() throws DMasonException {
		int i = toTest.rmap.up_mine.upl_xx;
		int j = toTest.rmap.up_mine.upl_yy;

		int stepI = (toTest.rmap.up_mine.down_xx - toTest.rmap.up_mine.upl_xx) / 4;
		int stepJ = (toTest.rmap.up_mine.down_yy - toTest.rmap.up_mine.upl_yy) / 4;

		Int2D location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
				i,  SimState ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
				i,  SimState ss);

		i += stepI;
		j += stepJ;

		location = new Int2D(i, j);

		toTest.setDistributedObjectLocation(location,  RemotePositionedAgent 
				i,  SimState ss);

		assertNotSame("the agent is in two places at once",
				toTest.rmap.up_mine.get(0).r, toTest.rmap.up_mine.get(1).r);

	}


	*//**
	 * test for the field partitioning.
	 * @throws DMasonException 
	 *//*

	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testMyFieldPartitioning() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 100;
		int h = 10;
		int maxD = 0;
		
		
		
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.myfield.down_xx;
		Integer x1 = toTest.myfield.upl_xx;
		Integer y2 = toTest.myfield.down_yy;
		Integer y1 = toTest.myfield.upl_yy;

		// find diagonal with the theorem of distance between 2 points
		Double diag = Math
				.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));

		// find diagonal with the theorem of Pitagora
		Double diagwh = Math.sqrt(Math.pow(w-1, 2.0) + Math.pow(h-1, 2.0));

		assertEquals(diag, diagwh/10,1);
	}

	*//**
	 * Test my field partitioning max distance1.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testMyFieldPartitioningMaxDistance1() throws DMasonException {

		// i need that w and h is equal for using the Pitagora's theorem
		int w = 10;
		int h = 10;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 1, 1, mode,0,false, name 
		"test",  prefix "",true);

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

	
	*//**
	 * Test up mine partitioning.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testUpMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 1, 1, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w, side.intValue()+1);
	}

	*//**
	 * Test down mine partitioning max distance1.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDownMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.up_mine.down_xx;
		Integer x1 = toTest.rmap.up_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	

	*//**
	 * Test down mine partitioning.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDownMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.down_mine.down_xx;
		Integer x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	*//**
	 * Test up mine partitioning max distance1.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testUpMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.down_mine.down_xx;
		Integer x1 = toTest.rmap.down_mine.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	

	*//**
	 * Test left mine partitioning.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testLeftMinePartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 1, 1, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.left_mine.down_yy;
		Integer x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h, side.intValue()+1);
	}

	*//**
	 * Test left mine partitioning max distance1.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testLeftMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 1,  j 1, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.left_mine.down_yy;
		Integer x1 = toTest.rmap.left_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	

	*//**
	 * Test right mine partitioning.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testRightMinePartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.right_mine.down_yy;
		Integer x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	*//**
	 * Test right mine partitioning max distance1.
	 * @throws DMasonException 
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testRightMinePartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.right_mine.down_yy;
		Integer x1 = toTest.rmap.right_mine.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	

	*//**
	 * Test up out partitioning.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testUpOutPartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 1, 1, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.up_out.down_xx;
		Integer x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w, side.intValue()+1);
	}

	*//**
	 * Test down out partitioning max distance1.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDownOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 1,  j 1, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.up_out.down_xx;
		Integer x1 = toTest.rmap.up_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	

	*//**
	 * Test down out partitioning.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testDownOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.down_out.down_xx;
		Integer x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	*//**
	 * Test up out partitioning max distance1.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testUpOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 9,  j 9, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.down_out.down_xx;
		Integer x1 = toTest.rmap.down_out.upl_xx;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(w/10, side.intValue()+1);
	}

	

	*//**
	 * Test left out partitioning.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testLeftOutPartitioning() throws DMasonException {

		int w = 10;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 1, 1, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.left_out.down_yy;
		Integer x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h, side.intValue()+1);
	}

	*//**
	 * Test left out partitioning max distance1.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testLeftOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 1,  j 1, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.left_out.down_yy;
		Integer x1 = toTest.rmap.left_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	
	*//**
	 * Test right out partitioning.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testRightOutPartitioning() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 0;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.right_out.down_yy;
		Integer x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	*//**
	 * Test right out partitioning max distance1.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testRightOutPartitioningMaxDistance1() throws DMasonException {

		int w = 100;
		int h = w;
		int maxD = 1;

		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(w, h, simState 
		ss, maxD,  i 0,  j 0, 10, 10, mode,0,false, name 
		"test",  prefix "",true);

		Integer x2 = toTest.rmap.right_out.down_yy;
		Integer x1 = toTest.rmap.right_out.upl_yy;

		// find distance between 2 points
		Integer side = x2 - x1;

		assertEquals(h/10, side.intValue()+1);
	}

	

	*//**
	 * Test corner congruence up right.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testCornerCongruenceUpRight() throws DMasonException {
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(10,10, simState 
		ss, 1,  i 1,  j 1,  rows 3,  Colums 3, mode,0,false, name 
		"test",  prefix "",true);

		assertEquals("x", toTest.rmap.corner_mine_up_right.upl_xx,
				toTest.rmap.corner_out_up_right_diag_center.down_xx - 1, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_right.upl_yy,
				toTest.rmap.corner_out_up_right_diag_center.down_yy+1, 0);
	}

	*//**
	 * Test corner congruence up left.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testCornerCongruenceUpLeft() throws DMasonException {
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(10,10, simState 
		ss, 1,  i 1,  j 1,  rows 3,  Colums 3, mode,0,false, name 
		"test",  prefix "",true);

		assertEquals("x", toTest.rmap.corner_mine_up_left.upl_xx,
				toTest.rmap.corner_out_up_left_diag_center.down_xx+1, 0);
		assertEquals("y", toTest.rmap.corner_mine_up_left.upl_yy,
				toTest.rmap.corner_out_up_left_diag_center.down_yy+1, 0);
	}

	*//**
	 * Test corner congruence down left.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testCornerCongruenceDownLeft() throws DMasonException {
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(10,10, simState 
		ss, 1,  i 1,  j 1,  rows 3,  Colums 3, mode,0,false, name 
		"test",  prefix "",true);

		assertEquals("x", toTest.rmap.corner_mine_down_left.upl_xx,
				toTest.rmap.corner_out_down_left_diag_center.down_xx+1, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_left.upl_yy,
				toTest.rmap.corner_out_down_left_diag_center.down_yy - 1, 0);
	}

	*//**
	 * Test corner congruence down right.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testCornerCongruenceDownRight() throws DMasonException {
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(10, 10, simState 
		ss, 1,  i 1,  j 1,  rows 3,  Colums 3, mode,0,false, name 
		"test",  prefix "",true);

		assertEquals("x", toTest.rmap.corner_mine_down_right.down_xx,
				toTest.rmap.corner_out_down_right_diag_center.upl_xx-1, 0);
		assertEquals("y", toTest.rmap.corner_mine_down_right.down_yy,
				toTest.rmap.corner_out_down_right_diag_center.upl_yy-1, 0);
	}

	*//**
	 * Test my field congruence.
	 *//*
	*//**
	 * @throws DMasonException
	 *//*
	@Test
	public void testMyFieldCongruence() throws DMasonException {
		toTest = (DDoubleGrid2DXY) DDoubleGrid2DFactory.createDDoubleGrid2D(10, 10, simState 
		ss, 1,  i 1,  j 1,  rows 3,  Colums 3, mode,0,false, name 
		"test",  prefix "",true);

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

*/	
}
