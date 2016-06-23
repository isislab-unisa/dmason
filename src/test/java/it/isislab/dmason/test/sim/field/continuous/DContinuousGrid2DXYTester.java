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
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
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

		fillAllLPs();
			
			
		for (int i = 0; i < 9; i++) assertEquals(100, toTest[i].myfield.size());
		
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
	
	@Test
	public void test_resetAddAll(){
		
		fillAllLPs();
		 
		ArrayList<RemotePositionedAgent<Double2D>> toAdd;
		RegionDouble actualMyfield;
		for (int i = 0; i < 9; i++) {
			toAdd = new ArrayList<>();
			ss.field = toTest[i];
			actualMyfield = (RegionDouble) toTest[i].myfield.clone();
			for(EntryAgent<Double2D> e: toTest[i].myfield.values()){
				toAdd.add(e.r);
			}
			assertEquals(toAdd.size(),toTest[i].myfield.size());
			toTest[i].resetAddAll(toAdd);
			
			assertEquals("new myfield doesn't match with old myfield",actualMyfield.size() ,toTest[i].myfield.size());
		}
	}

	@Test
	public void test_createRegion_rows_equals_columns(){
		DContinuousGrid2DXY grid ;
		for (int i = 0; i < 9; i++) {
			grid = toTest[i];
			grid.createRegions();
			switch(grid.cellType.toString()){
				case "0-0":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNull(grid.rmap.NORTH_WEST_OUT);
					assertNull(grid.rmap.NORTH_EAST_OUT);
					assertNull(grid.rmap.SOUTH_WEST_OUT);
					assertNull(grid.rmap.NORTH_OUT     );
					
					assertNotNull(grid.rmap.SOUTH_EAST_OUT);
					assertNotNull(grid.rmap.SOUTH_OUT     );
					
					assertNull(grid.rmap.WEST_OUT      );
					
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
				case "0-1":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNull(grid.rmap.NORTH_WEST_OUT);
					assertNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNotNull(grid.rmap.SOUTH_WEST_OUT);
					
					assertNull(grid.rmap.NORTH_OUT     );
					
					assertNotNull(grid.rmap.SOUTH_EAST_OUT);
					assertNotNull(grid.rmap.SOUTH_OUT     );
					assertNotNull(grid.rmap.WEST_OUT      );
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
			    case "0-2":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNull(grid.rmap.NORTH_WEST_OUT);
					assertNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNotNull(grid.rmap.SOUTH_WEST_OUT);

					assertNull(grid.rmap.NORTH_OUT     );
					assertNull(grid.rmap.SOUTH_EAST_OUT);
					
					assertNotNull(grid.rmap.SOUTH_OUT     );
					assertNotNull(grid.rmap.WEST_OUT      );
					
					assertNull(grid.rmap.EAST_OUT      );
					break;
			    case "1-0":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNull(grid.rmap.NORTH_WEST_OUT);
					
					assertNotNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNull(grid.rmap.SOUTH_WEST_OUT);
					
					assertNotNull(grid.rmap.NORTH_OUT     );
					assertNotNull(grid.rmap.SOUTH_EAST_OUT);
					assertNotNull(grid.rmap.SOUTH_OUT     );
					
					assertNull(grid.rmap.WEST_OUT      );
					
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
			    case "1-1":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNotNull(grid.rmap.NORTH_WEST_OUT);
					assertNotNull(grid.rmap.NORTH_EAST_OUT);
					assertNotNull(grid.rmap.SOUTH_WEST_OUT);
					assertNotNull(grid.rmap.NORTH_OUT     );
					assertNotNull(grid.rmap.SOUTH_EAST_OUT);
					assertNotNull(grid.rmap.SOUTH_OUT     );
					assertNotNull(grid.rmap.WEST_OUT      );
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
			    case "1-2":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNotNull(grid.rmap.NORTH_WEST_OUT);
					
					assertNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNotNull(grid.rmap.SOUTH_WEST_OUT);
					assertNotNull(grid.rmap.NORTH_OUT     );
					
					assertNull(grid.rmap.SOUTH_EAST_OUT);
					
					assertNotNull(grid.rmap.SOUTH_OUT     );
					assertNotNull(grid.rmap.WEST_OUT      );
					
					assertNull(grid.rmap.EAST_OUT      );
					break;
			    case "2-0":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNull(grid.rmap.NORTH_WEST_OUT);
					
					assertNotNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNull(grid.rmap.SOUTH_WEST_OUT);
					
					assertNotNull(grid.rmap.NORTH_OUT     );

					assertNull(grid.rmap.SOUTH_EAST_OUT);
					assertNull(grid.rmap.SOUTH_OUT     );
					
					assertNull(grid.rmap.WEST_OUT      );
					
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
			    case "2-1":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNotNull(grid.rmap.NORTH_WEST_OUT);
					assertNotNull(grid.rmap.NORTH_EAST_OUT);
					
					assertNull(grid.rmap.SOUTH_WEST_OUT);
					
					assertNotNull(grid.rmap.NORTH_OUT     );
					
					assertNull(grid.rmap.SOUTH_EAST_OUT);
					
					assertNull(grid.rmap.SOUTH_OUT     );
					
					assertNotNull(grid.rmap.WEST_OUT      );
					assertNotNull(grid.rmap.EAST_OUT      );
					break;
				case "2-2":
					assertNotNull(grid.rmap);
					assertNotNull(grid.rmap.NORTH_WEST_MINE);
					assertNotNull(grid.rmap.NORTH_EAST_MINE);
					assertNotNull(grid.rmap.SOUTH_WEST_MINE);
					assertNotNull(grid.rmap.SOUTH_EAST_MINE);
					assertNotNull(grid.rmap.WEST_MINE  );
					assertNotNull(grid.rmap.EAST_MINE  );
					assertNotNull(grid.rmap.NORTH_MINE );
					assertNotNull(grid.rmap.SOUTH_MINE );
					
					assertNotNull(grid.rmap.NORTH_WEST_OUT);
					
					assertNull(grid.rmap.NORTH_EAST_OUT);
					assertNull(grid.rmap.SOUTH_WEST_OUT);

					assertNotNull(grid.rmap.NORTH_OUT     );

					assertNull(grid.rmap.SOUTH_EAST_OUT);
					assertNull(grid.rmap.SOUTH_OUT     );
					
					assertNotNull(grid.rmap.WEST_OUT      );
					
					assertNull(grid.rmap.EAST_OUT      );
					break;
			}
		}
	}

	/**
	 * 	accessory method
	 * 		fill all LPs
	 */
	private void fillAllLPs() {

		for (int i = 0; i < 9; i++) {
			
			for (int attempts = 0; attempts < 100; attempts++) {
				Double2D location = toTest[i].getAvailableRandomLocation();
				fakeAgent = new FakePositionedAgent<Double2D>(ss, location);
				try{
					toTest[i].setDistributedObjectLocation(location, fakeAgent, ss);
				}catch(DMasonException error){
					fail("Somethig was wrong");
				}
			}
		}
	}
}
