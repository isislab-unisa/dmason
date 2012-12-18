/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.field.grid;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegion;
import dmason.sim.field.Entry;
import dmason.sim.field.MessageListener;
import dmason.sim.field.MessageListener;
import dmason.sim.field.Region;
import dmason.sim.field.RegionMap;
import dmason.sim.field.TraceableField;
import dmason.sim.field.UpdateMap;
import dmason.sim.field.UpdaterThreadForListener;
import dmason.sim.field.util.GlobalInspectorUtils;
import dmason.sim.loadbalancing.MyCellInterface;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.RemoteSnap;
import dmason.util.visualization.VisualizationUpdateMap;
import dmason.util.visualization.ZoomArrayList;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;


/**
 *  <h3>This Field extends SparseGrid2D, to be used in a distributed environment. All the necessary informations for 
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
 *  <li> A ((DistributedState)sm).getConnection() object for an abstract ((DistributedState)sm).getConnection()</li>
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
public class DSparseGrid2DY extends DSparseGrid2D implements TraceableField
{	
	private String NAME;
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	private ZoomArrayList<RemoteAgent> tmp_zoom=new ZoomArrayList<RemoteAgent>();
	private int numAgents;
	private int width,height;

	
	// --> only for testing
			public PrintWriter printer;
			public ArrayList<RemoteAgent<Int2D>> buffer_print=new ArrayList<RemoteAgent<Int2D>>();
			// <--
	
	private String topicPrefix = "";
	
	// -----------------------------------------------------------------------
	// GLOBAL INSPECTOR ------------------------------------------------------
	// -----------------------------------------------------------------------
	/** List of parameters to trace */
	private ArrayList<String> tracingFields = new ArrayList<String>();
	/** The image to send */
	private BufferedImage currentBitmap;
	/** Simulation's time when currentBitmap was generated */
	private double currentTime;
	/** Statistics to send */
	HashMap<String, Object> currentStats;
	/** True if the global inspector requested graphics **/
	boolean isTracingGraphics;
	
	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();
	
	
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
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DSparseGrid2DY(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns, String name, String prefix) 
	{		
		super(width, height);
		this.width=width;
		this.height=height;
		this.NAME = name;
		this.sm=sm;
		MAX_DISTANCE=max_distance;
		//NUMPEERS=num_peers;
		this.rows = rows;
		this.columns = columns;	
		cellType = new CellType(i, j);
		updates_cache= new ArrayList<Region<Integer,Int2D>>();
		this.topicPrefix = prefix;
		setConnection(((DistributedState)sm).getConnection());
		numAgents=0;
		createRegion();	
		
		//RIPRODUCIBILITA'
		// --> only for testing
		/*String curDir = System.getProperty("user.dir");
	    try 
	    {
			printer=new PrintWriter(new FileOutputStream(curDir+"/test_"+cellType+"_"+columns+"_one.txt"));
		} catch (FileNotFoundException e) { e.printStackTrace();}
		// <--
		*/
		
		// Initialize variables for GlobalInspector
		currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		currentTime = sm.schedule.getTime();
		currentStats = new HashMap<String, Object>();
		isTracingGraphics = false;
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
		if( v1 >= 0 )
		{
			neighborhood.add(cellType.getNeighbourLeft());
		}
		if( v2 <= columns - 1 )
		{
			neighborhood.add(cellType.getNeighbourRight());
		}	


		// Building the regions
		rmap.left_out=RegionInteger.createRegion(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height),my_width, my_height, width, height);
		if(rmap.left_out!=null)
			rmap.left_mine=RegionInteger.createRegion(own_x,own_y,own_x + MAX_DISTANCE -1, (own_y+my_height)-1,my_width, my_height, width, height);

		rmap.right_out=RegionInteger.createRegion(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, (own_y+my_height)-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
			rmap.right_mine=RegionInteger.createRegion(own_x + my_width -MAX_DISTANCE,own_y,own_x +my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);

		if(rmap.left_out == null)
		{
			//peer 0
			myfield=new RegionInteger(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		if(rmap.right_out == null)
		{
			//peer NUMPEERS-1
			myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-1);

		}

		if(rmap.left_out!=null && rmap.right_out!=null)
		{
			myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		return true;
	}
	
	/**
	 * Set up the new position in a distributed environment
	 * 
	 * It uses the Java Reflect to find the region which 
	 * the new location belongs.
	 * The method uses the method isMine() of class Region to do this.
	 * 
	 * @param location The new location
	 * @param rm The remote agent that have be stepped
	 * @param sm The SimState of simulation
	 * @return false if the object is null (null objects cannot be put into the grid)
	 */
	@Deprecated
	public boolean setDistributedObjectLocationForPeer(final Int2D location,RemoteAgent<Int2D> rm,SimState sm)
	{		
	    if(myfield.isMine(location.x,location.y) && this.getObjectsAtLocation(location)==null)
	    	return super.setObjectLocation(rm,
        			new Int2D(location.x,location.y));  
	 
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
 			  if(returnValue!=null && name.contains("mine"))
 		      {
 				  RegionInteger region=((RegionInteger)returnValue);
		 	      
 				  if(region.isMine(location.x,location.y) && this.getObjectsAtLocation(location.x, location.y)==null)
		 	    	  return super.setObjectLocation(rm,new Int2D(location.x,location.y)); 
 		      }
		        
		  }catch (IllegalArgumentException e){ e.printStackTrace(); } 
		  catch (IllegalAccessException e) {e.printStackTrace();}
		  catch (SecurityException e) {e.printStackTrace();} 
		  catch (NoSuchMethodException e) {e.printStackTrace();} 
		  catch (InvocationTargetException e) {e.printStackTrace();}
		}
	  return false;
	}
	
	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	public Int2D setAvailableRandomLocation(RemoteAgent<Int2D> rm)
	{
		int x=(((DistributedState)sm).random.nextInt(width)%(my_width-1))+own_x;
    	if(x>(width-1)) x--;
        int y=(((DistributedState)sm).random.nextInt(height)%(my_height-1))+own_y;
        if(y>(height-1)) y--;
        rm.setPos(new Int2D(x, y));
        
        return (new Int2D(x, y));
	}
	
	/** 
	 * Provide the agents' shift logic among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
    public boolean setDistributedObjectLocation(final Int2D location,RemoteAgent<Int2D> rm,SimState sm)
    {  	
    	/*if(sm.schedule.getSteps()==8000){
			printer.write(sm.schedule.getSteps()+" "+rm.getId()+" "+rm.getPos().x+" "+rm.getPos().y+"\r\n");
			printer.flush();
		}*/
		numAgents++;

    	if(myfield.isMine(location.x,location.y))
    	{    		
    		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
    			GlobalInspectorUtils.updateBitmap(currentBitmap, rm, location, own_x, own_y);
    		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
    		return myfield.addAgents(new Entry<Int2D>(rm, location));
    	}
    	else
    		if(setAgents(rm, location))
    			return true;
    		else
    				System.out.println(cellType+")OH MY GOD!"); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)
    	return false;
    }

    /**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	public synchronized boolean  synchro() 
	{	
		// Send to Global Inspector
		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
		{
			GlobalInspectorUtils.synchronizeInspector(
					(DistributedState<?>)sm,
					connection,
					topicPrefix,
					cellType,
					own_x,
					own_y,
					currentTime,
					currentBitmap,
					currentStats,
					tracingFields,
					isTracingGraphics);
			currentTime = sm.schedule.getTime();
		}
			
		

		for(Region<Integer,Int2D> region : updates_cache)
		{
			for(Entry<Int2D> remote_agent : region)
			{
				this.remove(remote_agent.r);
			}
		}
		
		//every agent in the myfield region is scheduled
		for(Entry<Int2D> e: myfield)
		{
			RemoteAgent<Int2D> rm=e.r;
			Int2D loc=e.l;
			rm.setPos(loc);
		    this.remove(rm);
			sm.schedule.scheduleOnce(rm);
			super.setObjectLocation(rm,loc);		
		}   
		
		updateFields(); //update fields with java reflect
	
		updates_cache=new ArrayList<Region<Integer,Int2D>>();
		
		memorizeRegionOut();

		//--> publishing the regions to correspondent topics for the neighbors
		if( rmap.left_out!=null )
		{
			
			DistributedRegion<Integer,Int2D> dr1 = 
					new DistributedRegion<Integer,Int2D>(rmap.left_mine,rmap.left_out,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.LEFT);
			try 
			{	
				connection.publishToTopic(dr1,topicPrefix+cellType+"L", NAME);
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if( rmap.right_out!=null )
		{
			
			DistributedRegion<Integer,Int2D> dr2 = 
					new DistributedRegion<Integer,Int2D>(rmap.right_mine,rmap.right_out,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.RIGHT);
			try 
			{			
				connection.publishToTopic(dr2,topicPrefix+cellType+"R", NAME);	
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
				DistributedRegion<Integer, Int2D> region=(DistributedRegion<Integer, Int2D>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); }
			
		for(Region<Integer,Int2D> region : updates_cache)
			for(Entry<Int2D> e_m: region)
			{
				RemoteAgent<Int2D> rm=e_m.r;
				((DistributedState<Int2D>)sm).addToField(rm,e_m.l);	
			}
			
		this.reset();

		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				connection.publishToTopic(tmp_zoom,topicPrefix+"GRAPHICS"+cellType,NAME);
				tmp_zoom=new ZoomArrayList<RemoteAgent>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return true;
	}
	
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
		    	 Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
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
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegion<Integer,Int2D> box)
	{
		Region<Integer,Int2D> r_mine=box.out;
		Region<Integer,Int2D> r_out=box.mine;
		
		for(Entry<Int2D> e_m: r_mine)
		{
				RemoteAgent<Int2D> rm=e_m.r;
				((DistributedState<Int2D>)sm).addToField(rm,e_m.l);
		  		rm.setPos(e_m.l);
		  		sm.schedule.scheduleOnce(rm);
		  		
		}		
		updates_cache.add(r_out);
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
		    	 Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
		    	 if(name.contains("out"))
			  	 {
		    		 for(Entry<Int2D> e: region)
		   			 {
		    			 RemoteAgent<Int2D> rm=e.r;
		    			 rm.setPos(e.l);
			    		 this.remove(rm);
		   			 }
			  	  }
		    	  else
			      if(name.contains("mine"))
			      {
			    	 for(Entry<Int2D> e: region)
			 	     {
			    		 RemoteAgent<Int2D> rm=e.r;
			 	    	 Int2D loc=e.l;
			 	    	 rm.setPos(loc);
			 	    	 this.remove(rm);
			 	    	 sm.schedule.scheduleOnce(rm);
			 	    	 super.setObjectLocation(rm,loc);
			 	    						
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
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 */
	public boolean setAgents(RemoteAgent<Int2D> rm,Int2D location)
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
	       			Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
	       			if(region.isMine(location.x,location.y))
	       			{   	 
		    			if(name.contains("mine")){
		    				if(((DistributedMultiSchedule)sm.schedule).isEnableZoomView)
		    				{
		    					if(tmp_zoom!=null)tmp_zoom.add(rm);
		    				}
		    				if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
		    				{
		    	    			GlobalInspectorUtils.updateBitmap(currentBitmap, rm, location, own_x, own_y);		    	    			
		    	    		}
		    			}
	    	    		return region.addAgents(new Entry<Int2D>(rm, location));
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
		    		Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
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
	 * Implemented method from the abstract class.
	 */
	public DistributedState<Int2D> getState() { return (DistributedState)sm; }
    
	//getters and setters
	public int getOwn_x() { return own_x; }
	public void setOwn_x(int own_x) { this.own_x = own_x; }
	public int getOwn_y() {	return own_y; }
	public void setOwn_y(int own_y) { this.own_y = own_y; }

	@Override
	public ArrayList<MessageListener> getLocalListener() {
		return null;//listeners;
	}
	
	@Override
	public void setTable(HashMap table) {
		((DistributedState)sm).getConnection().setTable(table);
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
	public void setConnection(Connection con) {
		connection=(ConnectionNFieldsWithActiveMQAPI)con;
		
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
	public void trace(String param)
	{
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = true;
		else
			tracingFields.add(param);
	}

	@Override
	public void untrace(String param)
	{
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = false;
		else
		{
			tracingFields.remove(param);
			currentStats.remove(param);
		}
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
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}	
	
	
}
