/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field.grid.numeric.thin;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumeric;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.util.visualization.zoomviewerapp.ZoomArrayList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.engine.SimState;
import sim.util.Int2D;


/**
 *  <h3>This Field extends IntGrid2D, to be used in a distributed environment. All the necessary informations for 
 *  the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>horizontal mode</i>.
 *  It represents the field managed by a single peer.
 *  It adds to the superclass these following informations:
 *  <ul>
 *  <li> Upper Left corner's coordinates</li>
 *  <li> Width and Height of the Field of expertise</li>
 *  <li> The logic of synchronization among the peers, step by step</li>
 *  <li> The number of peers involved in the simulation</li>
 *  <li> The maximum distance of shift for the agents</li>
 *  <li> An arraylist for the neighborhoods topics</li>
 *  <li>	A Region that represents the field to simulate</li>
 *  <li>	A RegionMap that represents all the border Regions</li>
 *  <li> The simstate of sumulation</li>
 *  <li>	An UpdateMap for all the updates</li>
 *  <li> A Connection object for an abstract connection</li>
 *  <li> A CellType object for differentiate the field</li>
 *  </ul>
 *  This is an example for a horizontal mode distribution with 'NUM_PEERS' peers (only to distinguish the regions):
 *  (for code down)
 *  <p>
 * <ul>
 *	<li>MYFIELD : Region to be simulated by peer.</li>
 *
 * 	<li>LEFT_MINE, RIGHT_MINE :
 *	Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, RIGHT_OUT : 
 *	Boundaries Regions those must not be simulated and sent to neighbors to be simulated.
 * </li>
 * 	All peers subscribes to the topic of boundary region which want the information and run a asynchronous thread
 *	to receive the updates, then publish a topic for every their border (or neighbor), that can be :
 *<ul>	
 *<li> MYTOPIC L (LEFT BORDER)</li>
 *<li> MYTOPIC R (RIGHT BORDER)</li>
 *</ul>
 *</li>
 *</p>
 *
 * <PRE>
 * -----------------------------------------------------------------------------------------------------------
 * |             |  |  |            |  |  |           |  |  |                       |  |  |                  |
 * |             |  |  |            |  |  |           |  R  R                       L  L  |                  |
 * |             |  |  |            |  |  |           |  I  I                       E  E  |                  |
 * |    00       |  |  |     01     |  |  |    02     |  G  G                       F  F  |  NUM_PEERS - 1   |
 * |             |  |  |            |  |  |           |  H  H***********************T  T  |                  |
 * |             |  |  |            |  |  |  MYFIELD  |  T  T                       |  |  |                  |
 * |             |  |  |            |  |  |           |  |  |                       O  M  |                  |
 * |             |  |  |            |  |  |           |  M  O                       |  |  |                  |
 * |             |  |  |            |  |  |           |  |  |                       |  |  |                  |
 * ----------------------------------------------------------------------------------------------------------
 * </PRE>
 */
public class DIntGrid2DYThin extends DIntGrid2DThin {

	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private double initialValue;
	private ZoomArrayList<EntryNum<Integer, Int2D>> tmp_zoom=null;

	/**
	 * It's the name of the specific field
	 */
	private String NAME;
	private int numAgents;
	private int width,height,field_width,field_height;
	private String topicPrefix = "";

	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();

	// -----------------------------------------------------------------------
	// DEBUG -----------------------------------------------------------------
	// -----------------------------------------------------------------------
	private boolean checkReproducibility=false;
	private FileOutputStream file = null;
	private PrintStream ps = null;

	/**
	 * Constructor of class with paramaters:
	 * 
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param initialGridValue the initial value that we want to set at grid at begin simulation 
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DIntGrid2DYThin(int width, int height,int field_width,int field_height,SimState sm,int max_distance,int i,int j,int rows, int columns, 
			int initialGridValue, String name, String prefix) {

		super(field_width, field_height,width,height, initialGridValue);
		this.width=width;
		this.height=height;
		this.field_width=field_width;
		this.field_height=field_height;
		this.NAME = name;
		this.sm=sm;
		MAX_DISTANCE=max_distance;
		//NUMPEERS=num_peers;
		this.rows = rows;
		this.columns = columns;	
		cellType = new CellType(i, j);
		this.topicPrefix = prefix;
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
		numAgents=0;
		if(checkReproducibility)
		{
			try {
				file = new FileOutputStream(name+"-"+cellType+".txt");
				ps = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		createRegion();		

	}

	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion()
	{
		//upper left corner's coordinates
		if(cellType.pos_j<(width%columns))
			own_x=(int)Math.floor(width/columns+1)*cellType.pos_j; 
		else
			own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(cellType.pos_j-((width%columns))); 

		own_y=0; // in this mode the y coordinate is ever 0

		// own width and height
		if(cellType.pos_j<(width%columns))
			my_width=(int) Math.floor(width/columns+1);
		else
			my_width=(int) Math.floor(width/columns);
		my_height=height;


		//calculating the neighbors
		int v1 = cellType.pos_j - 1;
		int v2 = cellType.pos_j + 1;
		if(isToroidal())
		{
			if( v1 >= 0 )
			{
			
				neighborhood.add(cellType.getNeighbourLeft());
						
				
			}
			if( v2 <= columns - 1 )
			{
				
				neighborhood.add(cellType.getNeighbourRight());
			}	
		}else{
			
			if( v1 >= 0 )
			{
				
				neighborhood.add(cellType.getNeighbourLeft());
				
			}
			if( v2 < columns  )
			{
				
				neighborhood.add(cellType.getNeighbourRight());
			}	
		}

		// Building the regions
		rmap.left_out=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height),my_width, my_height, width, height);
		if(rmap.left_out!=null)
			rmap.left_mine=RegionIntegerNumeric.createRegionNumeric(own_x,own_y,own_x + MAX_DISTANCE -1, (own_y+my_height)-1,my_width, my_height, width, height);

		rmap.right_out=RegionIntegerNumeric.createRegionNumeric(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, (own_y+my_height)-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
			rmap.right_mine=RegionIntegerNumeric.createRegionNumeric(own_x + my_width -MAX_DISTANCE,own_y,own_x +my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);

		if(rmap.left_out == null)
		{
			//peer 0
			myfield=new RegionIntegerNumeric(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		if(rmap.right_out == null)
		{
			//peer NUMPEERS-1
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-1);

		}

		if(rmap.left_out!=null && rmap.right_out!=null)
		{
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		return true;
	}

	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	@Override
	public boolean synchro() {

		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();

		if(((DistributedMultiSchedule)sm.schedule).isEnableZoomView)
		{
			tmp_zoom=new ZoomArrayList<EntryNum<Integer, Int2D>>();
			tmp_zoom.STEP=sm.schedule.getSteps()-1;
		}

		//every value in the myfield region is setted
		for(EntryNum<Integer, Int2D> e: myfield)
		{			
			Int2D loc=e.l;
			int i = e.r;
			setThin(loc.getX(), loc.getY(), i);
			//this.field[loc.getX()][loc.getY()]=i;	
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Integer,Int2D>(i, loc));
		}     
		if(conn!=null
				&& ((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				conn.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,NAME);
				tmp_zoom=new ZoomArrayList<EntryNum<Integer,Int2D>>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		updateFields(); //update fields with java reflect

		updates_cache=new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();

		memorizeRegionOut();


		//--> publishing the regions to correspondent topics for the neighbors
		if( rmap.left_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr1=new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(rmap.left_mine,rmap.left_out,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.LEFT);
			try 
			{	
				connWorker.publishToTopic(dr1,topicPrefix+cellType+"L", NAME);	
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if( rmap.right_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr2=new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(rmap.right_mine,rmap.right_out,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.RIGHT);
			try 
			{			
				connWorker.publishToTopic(dr2,topicPrefix+cellType+"R", NAME);		
			} catch (Exception e1) {e1.printStackTrace();}
		}			
		//<--

		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, neighborhood.size());
			while(!q.isEmpty())
			{
				DistributedRegionNumeric<Integer, EntryNum<Integer,Int2D>> region=(DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); }

		for(RegionNumeric<Integer,EntryNum<Integer,Int2D>> region : updates_cache)
		{
			for(EntryNum<Integer,Int2D> e_m: region)
			{
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				setThin(i.getX(), i.getY(), e_m.r);
				//field[i.getX()][i.getY()]=e_m.r;
			}
		}

		this.reset();

		return true;
	}

	/**
	 * This method takes updates from box and set every value in the regions out.
	 * Every value in the regions mine is compared with every value in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> box)
	{
		RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_mine=box.out;
		RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_out=box.mine;

		for(EntryNum<Integer,Int2D> e_m: r_mine)
		{
			Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());
			setThin(i.getX(), i.getY(), e_m.r);
			//field[i.getX()][i.getY()]=e_m.r;		  		
		}		
		updates_cache.add(r_out);
	}

	/**
	 * with java Reflect expect to memorize the region out
	 */
	public void memorizeRegionOut()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
					if(name.contains("out"))
					{

						updates_cache.add(region.clone());
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}	     
	}


	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	public void updateFields()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);

					if(name.contains("out"))
					{
						for(EntryNum<Integer,Int2D> e : region){

							Int2D pos = new Int2D(e.l.getX(), e.l.getY());
							int i = e.r;
							setThin(pos.getX(), pos.getY(), i);

							//this.field[pos.getX()][pos.getY()]=i;
						}
					}
					else
						if(name.contains("mine"))
						{
							for(EntryNum<Integer,Int2D> e : region){

								Int2D pos = new Int2D(e.l.getX(), e.l.getY());
								int i = e.r;
								setThin(pos.getX(), pos.getY(), i);

								//this.field[pos.getX()][pos.getY()]=i;
							}

						}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}	 
	}


	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	public  boolean reset()
	{
		myfield.clear();

		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);

				if(returnValue!=null)
				{
					RegionNumeric<Integer,EntryNum<Integer, Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer, Int2D>>)returnValue);
					region.clear();    
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace(); return false;} 
			catch (IllegalAccessException e) {e.printStackTrace();return false;} 
			catch (SecurityException e) {e.printStackTrace();return false;} 
			catch (NoSuchMethodException e) {e.printStackTrace();return false;} 
			catch (InvocationTargetException e) {e.printStackTrace();return false;}
		}
		return true;
	}

	/**
	 * Provide the double value shift logic among the peers
	 * @param d
	 * @param l
	 * @param sm
	 * @return
	 */
	public boolean setDistributedObjectLocation(int i, Int2D l, SimState sm){
		
		//This 'if' is for debug 
		if(checkReproducibility)
			ps.println(i+" "+l.x+" "+l.y);
		
		numAgents++;
		if(myfield.isMine(l.getX(), l.getY()))
		{    		
			return myfield.addEntryNum(new EntryNum<Integer,Int2D>(i, l));
		}
		else
			if(setValue(i, l))
				return true;
			else
				System.out.println(cellType+")OH MY GOD!"); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)

		return false;
	}

	/**
	 * This method, written with Java Reflect, provides to add the value
	 * in the right Region.
	 * @param value The value to add
	 * @param l The new location of the value
	 * @return true if the value is added in right way
	 */
	public boolean setValue(int value, Int2D l){

		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);

				if(returnValue!=null)
				{ 
					RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = ((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
					if(region.isMine(l.getX(),l.getY()))
					{   	  
						if(name.contains("mine")){
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(new EntryNum<Integer,Int2D>(value, l));
							return region.addEntryNum(new EntryNum<Integer,Int2D>(value, l));
						}
						if(name.contains("out"))
							return region.addEntryNum(new EntryNum<Integer,Int2D>(value, l));
					}

				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}

		return false;	       			       			
	}

	@Override
	public DistributedState getState() {

		return (DistributedState)sm;
	}

	@Override
	public ArrayList<MessageListener> getLocalListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	@Override
	public HashMap<Integer, MyCellInterface> getToSendForBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsSplitted(boolean isSplitted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSplitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrepareForBalance() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUnited() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void prepareForBalance(boolean prepareForBalance) {
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap<Integer, MyCellInterface> getToSendForUnion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareForUnion(boolean prepareForUnion) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumAgents() {
		return numAgents;
	}

	@Override
	public void resetParameters() {
		numAgents=0;
	}

	@Override
	public int getLeftMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRightMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setThin(int i,int j, int val){
		if(i-own_x+2*MAX_DISTANCE>=0 && i-own_x+2*MAX_DISTANCE<field_width)
			field[i-own_x+2*MAX_DISTANCE][j]=val;
	}
	@Override
	public int getThin(int i, int j){
		if(i-own_x+2*MAX_DISTANCE>=0 && i-own_x+2*MAX_DISTANCE<field_width)
			return field[i-own_x+2*MAX_DISTANCE][j];
		return Integer.MIN_VALUE;
	}

	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}

	@Override
	public boolean setDistributedObjectLocation(Int2D location,
			RemotePositionedAgent<Int2D> rm, SimState sm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Int2D getAvailableRandomLocation() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean verifyPosition(Int2D pos) {
		
		//we have to implement this
		return false;

	}
}
