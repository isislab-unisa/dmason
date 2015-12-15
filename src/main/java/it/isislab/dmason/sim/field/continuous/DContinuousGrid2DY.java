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

package it.isislab.dmason.sim.field.continuous;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.sim.field.support.globals.GlobalInspectorHelper;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.util.visualization.zoomviewerapp.ZoomArrayList;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 *  <h3>This Field extends Continuous2D, to be used in a distributed
 *  environment. All the necessary informations for 
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
 *  This is an example for a horizontal mode distribution with 'NUM_PEERS'
 *  peers (only to distinguish the regions):  (for code down)
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
 * |             |  |  |            |  |  |           |  E  E                       O  O  |                  |
 * |             |  |  |            |  |  |           |  S  S                       V  V  |                  |
 * |    00       |  |  |     01     |  |  |    02     |  T  T                       E  E  |  NUM_PEERS - 1   |
 * |             |  |  |            |  |  |           |  |  |***********************S  S  |                  |
 * |             |  |  |            |  |  |  MYFIELD  |  M  O                       T  T  |                  |
 * |             |  |  |            |  |  |           |  |  U                       O  M  |                  |
 * |             |  |  |            |  |  |           |  N  T                       U  I  |                  |
 * |             |  |  |            |  |  |           |  E  |                       T  N  |                  |
 * ----------------------------------------------------------------------------------------------------------
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * </PRE>
 */
/*
public class DContinuousGrid2DY extends DContinuousGrid2D implements TraceableField
{	
	private static final long serialVersionUID = 1L;

	private double start,finish;

	public ArrayList<MessageListener> listeners;
	private String name;
	private ZoomArrayList<RemotePositionedAgent> tmp_zoom=new ZoomArrayList<RemotePositionedAgent>();
	private double width,height;

	private String topicPrefix = "";

	// -----------------------------------------------------------------------
	// GLOBAL INSPECTOR ------------------------------------------------------
	// -----------------------------------------------------------------------
	*//** List of parameters to trace *//*
	private ArrayList<String> tracingFields = new ArrayList<String>();
	*//** The image to send *//*
	private BufferedImage currentBitmap;
	*//** Simulation's time when currentBitmap was generated *//*
	private double currentTime;
	*//** Statistics to send *//*
	HashMap<String, Object> currentStats;
	*//** True if the global inspector requested graphics **//*
	boolean isTracingGraphics;

	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	*//** Will contain globals properties *//*
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();

	*//**
	 * Starts tracing a variable (or the graphic). To start tracing the graphic,
	 * the global viewer must set param = "-GRAPHICS". We choose this particular
	 * string because method names "get-GRAPHICS()" aren't allowed, so we are
	 * sure that we aren't hiding any real simulation getter.
	 **//*
	@Override
	public void trace(String param)
	{ 
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = true;
		else
			tracingFields.add(param);
	}

	*//** Stops tracing a variable (or the graphic) **//*
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

	*//**
	 * Constructor of class with paramaters:
	 * 
	 * @param discretization the discretization of the field
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
	 *//*
	public DContinuousGrid2DY(double discretization, double width, double height, SimState sm, int max_distance, int i, int j, int rows, int columns, String name, String prefix)
	{
		super(discretization, width, height);
		this.width=width;
		this.height=height;
		this.sm = sm;		
		this.AOI = max_distance;
		//this.numPeers = num_peers;
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.listeners = new ArrayList<MessageListener>();
		this.updates_cache = new ArrayList<Region<Double,Double2D>>();
		this.name = name;
		this.topicPrefix = prefix;
		//		setConnection(((DistributedState)sm).getConnection());
		createRegion();

		// Initialize variables for GloablInspector
		try{
			currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		}
		catch(Exception e)
		{
			System.out.println("Do not use the GlobalViewer, the requirements of the simulation exceed the limits of the BufferedImage.\n");
		}
		currentTime = sm.schedule.getTime();
		currentStats = new HashMap<String, Object>();
		isTracingGraphics = false;

	}
	*//**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 *//*
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

		myfield = new RegionDouble(
				own_x + AOI,            // MyField's x0 coordinate
				own_y,                           // MyField's y0 coordinate
				own_x + my_width - AOI, // MyField x1 coordinate
				height,                          // MyField y1 coordinate
				width, height);                  // Global width and height 

		rmap.WEST_OUT = new RegionDouble(
				(own_x - AOI + width) % width, // Left-out x0
				0.0,									// Left-out y0
				(own_x + width) % (width),				// Left-out x1
				height,									// Left-out y1
				width, height);

		rmap.WEST_MINE = new RegionDouble(
				(own_x + width) % width,				// Left-mine x0
				0.0,									// Left-mine y0
				(own_x + AOI + width) % width,	// Left-mine x1
				height,									// Left-mine y1
				width, height);

		rmap.EAST_OUT = new RegionDouble(
				(own_x + my_width + width) % width,                // Right-out x0
				0.0,                                               // Right-out y0
				(own_x + my_width + AOI + width) % width, // Right-out x1
				height,                                            // Right-out y1
				width, height);

		rmap.EAST_MINE = new RegionDouble(
				(own_x + my_width - AOI + width) % width, // Right-mine x0
				0.0,											   // Right-mine y0
				(own_x + my_width + width) % width,                // Right-mine x1
				height,                                            // Right-mine y1
				width, height);

		return true;
	}

	*//**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 *//*
	@Override
	public Double2D getAvailableRandomLocation()
	{		
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();
		double x=(own_x+AOI)+((my_width+own_x-AOI)-(own_x+AOI))*shiftx;
		double y=(own_y+AOI)+((my_height+own_y-AOI)-(own_y+AOI))*shifty;

		//rm.setPos(new Double2D(x,y));

		return (new Double2D(x, y));
	}

	*//** 
	 * Provide the agents' shift logic among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 *//*
	public boolean setDistributedObjectLocation(final Double2D location,Object remoteObject,SimState sm) throws DMasonException
	{	
		RemotePositionedAgent<Double2D> rm= null;
		
		if(remoteObject instanceof RemotePositionedAgent ){
			if(((RemotePositionedAgent)remoteObject).getPos() instanceof Double2D){
			
			rm=(RemotePositionedAgent<Double2D>) remoteObject;	
			}
			else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter RemotePositionedAgent<E>, E must be a Double2D");}
		}
		else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter must be a RemotePositionedAgent<>");}
		
		
			if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
				GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
		if(setAgents(rm, location))
		{
			return true;
		}
		else
		{
			String errorMessage = String.format("Agent %s tried to set position (%f, %f): out of boundaries on cell %s.",
					rm.getId(), location.x, location.y, cellType);
			System.err.println( errorMessage );
			return false;
		}
	}

	*//**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 *//*
	@Override
	public synchronized boolean synchro() 
	{
		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();


		// Send to Global Inspector
		if(conn!=null &&
				((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
		{
			GlobalInspectorHelper.synchronizeInspector(
					(DistributedState<?>)sm,
					conn,
					topicPrefix,
					cellType,
					(int)own_x,
					(int)own_y,
					currentTime,
					currentBitmap,
					currentStats,
					tracingFields,
					isTracingGraphics);
			currentTime = sm.schedule.getTime();
		}

		// Remove agents migrated to neighbor regions
		for(Region<Double, Double2D> region : updates_cache)
		{
			for( String agent_id : region.keySet())
			{
				EntryAgent<Double2D> remote_agent = region.get(agent_id);
				this.remove(remote_agent.r);
			}
		}

		// Schedule agents in MyField region
		for( String agent_id : myfield.keySet())
		{
			EntryAgent<Double2D> e = myfield.get(agent_id);
			RemotePositionedAgent<Double2D> rm = e.r;
			Double2D loc = e.l;
			rm.setPos(loc);
			this.remove(rm);
			sm.schedule.scheduleOnce(rm);
			setObjectLocation(rm, loc);	
		}   

		// Update fields using Java Reflection
		updateFields(); 

		// Clear update_cache
		updates_cache = new ArrayList<Region<Double,Double2D>>();

		memorizeRegionOut();

		// Publish left mine&out regions to correspondent topic
		if ( rmap.WEST_OUT != null )
		{
			DistributedRegion<Double,Double2D> dr1 = new DistributedRegion<Double,Double2D>(
					rmap.WEST_MINE,
					rmap.WEST_OUT,
					sm.schedule.getSteps() - 1,
					cellType,
					DistributedRegion.WEST);
			try 
			{				
				connWorker.publishToTopic(dr1, topicPrefix+cellType + "L", name);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		// Publish right mine&out regions to correspondent topic
		if ( rmap.EAST_OUT != null )
		{
			DistributedRegion<Double,Double2D> dr2 = new DistributedRegion<Double,Double2D>(
					rmap.EAST_MINE,
					rmap.EAST_OUT,
					sm.schedule.getSteps() - 1,
					cellType,
					DistributedRegion.EAST);
			try 
			{		
				connWorker.publishToTopic(dr2, topicPrefix+cellType + "R", name);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps() - 1, 2);
			while(!q.isEmpty())
			{
				DistributedRegion<Double, Double2D> region=(DistributedRegion<Double,Double2D>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(Region<Double, Double2D> region : updates_cache)
			for(String agent_id : region.keySet())
			{
				EntryAgent<Double2D> e_m = region.get(agent_id);
				RemotePositionedAgent<Double2D> rm = e_m.r;
				((DistributedState<Double2D>)sm).addToField(rm, e_m.l);
			}

		this.reset();

		// If there is a zoom viewer active...
		if(conn!=null && 
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try
			{
				tmp_zoom.STEP = ((DistributedMultiSchedule)sm.schedule).getSteps() - 1;
				conn.publishToTopic(tmp_zoom, topicPrefix+"GRAPHICS" + cellType, name);
				tmp_zoom = new ZoomArrayList<RemotePositionedAgent>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return true;
	}

	*//**
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 *//*
	public void verifyUpdates(DistributedRegion<Double,Double2D> box)
	{
		Region<Double,Double2D> r_mine=box.out;
		Region<Double,Double2D> r_out=box.mine;

		for(String agent_id: r_mine.keySet())
		{
			EntryAgent<Double2D> e_m = r_mine.get(agent_id);
			RemotePositionedAgent<Double2D> rm=e_m.r;
			((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
			rm.setPos(e_m.l);
			setPortrayalForObject(rm);
			sm.schedule.scheduleOnce(rm);
		}
		updates_cache.add(r_out);
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
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

	@Override
	public boolean setPortrayalForObject(Object o) 
	{
		if(p!=null)
		{
			((DistributedState<Double2D>)sm).setPortrayalForObject(o);
			return true;
		}
		return false;
	}
	*//**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 *//*
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
					if(name.contains("out"))
					{
						for(String anget_id: region.keySet())
						{
							EntryAgent<Double2D> e = region.get(anget_id);
							RemotePositionedAgent<Double2D> rm=e.r;
							rm.setPos(e.l);		    			
							this.remove(rm);
						} 
					}
					else
						if(name.contains("mine"))
						{
							for(String agent_id: region.keySet())
							{
								EntryAgent<Double2D> e = region.get(agent_id);
								RemotePositionedAgent<Double2D> rm=e.r;
								Double2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);					
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

	*//**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 *//*
	private boolean setAgents(RemotePositionedAgent<Double2D> rm,Double2D location)
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);

					if(region.isMine(location.x,location.y))
					{   	
						if(name.contains("mine")){
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)

								tmp_zoom.add(rm);
							if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
								GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
						}
						myfield.addAgents(new EntryAgent<Double2D>(rm, location));
						return region.addAgents(new EntryAgent<Double2D>(rm, location));
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

	*//**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 *//*
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
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

	*//**
	 * Implemented method from the abstract class.
	 *//*
	@Override
	public DistributedState<Double2D> getState() {
		return (DistributedState<Double2D>)sm; 
	}

	//getters and setters
	public double getOwn_x() {
		return own_x; 
	}

	public void setOwn_x(double own_x) {
		this.own_x = own_x; 
	}

	public double getOwn_y() {
		return own_y; 
	}
	public void setOwn_y(double own_y) {
		this.own_y = own_y; 
	}

	@Override
	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}

	@Override
	public void setTable(HashMap table) {
		ConnectionJMS conn = (ConnectionJMS) ((DistributedState<?>)sm).getCommunicationManagementConnection();
		if(conn!=null)
			conn.setTable(table);
	}
	@Override
	public String getDistributedFieldID() {
		// TODO Auto-generated method stub
		return name;
	}
	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	@Override
	public void resetParameters() {
		System.err.println("You are using a not implemented method (resetParameters) from "+this.getClass().getName());
	}

	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}

	@Override
	public boolean verifyPosition(Double2D pos) {
		
		return (rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(pos.x,pos.y))||
				
				(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(pos.x,pos.y))
				||
					(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(pos.x,pos.y))
					||(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(pos.x,pos.y))
						||(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(pos.x,pos.y))
							||(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(pos.x,pos.y))
								||(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(pos.x,pos.y))
									||(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(pos.x,pos.y))
										||(myfield.isMine(pos.x,pos.y));

	}

}*/