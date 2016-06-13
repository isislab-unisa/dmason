/**
 * Copyright 2016 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.field.grid.numeric;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.support.globals.GlobalInspectorHelper;
import it.isislab.dmason.experimentals.sim.field.support.globals.GlobalParametersHelper;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.nonuniform.QuadTree.ORIENTATION;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.continuous.DContinuousNonUniform;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumeric;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;

public class DIntGrid2DNonUniform extends DIntGrid2D implements TraceableField{

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DContinuousNonUniform.class.getCanonicalName());

	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();

	/** Name of the field. Used to identify fields in simulation using several fields. */
	private String name;

	private int numAgents;

	private String topicPrefix = "";


	// -----------------------------------------------------------------------
	// GLOBAL INSPECTOR ------------------------------------------------------
	// -----------------------------------------------------------------------
	/** List of parameters to trace */
	private ArrayList<String> tracingFields;
	/** The image to send */
	private BufferedImage currentBitmap;
	/** Simulation's time when currentBitmap was generated */
	private double currentTime;
	/** Statistics to send */
	HashMap<String, Object> currentStats;
	/** True if the global inspector requested graphics **/
	boolean isTracingGraphics;

	// -----------------------------------------------------------------------
	// GLOBAL PARAMETERS -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Java class of current simulation */
	protected Class<? extends SimState> simClass;
	/** List of global parameters. These must be synchronized among fields at each step */
	protected ArrayList<String> globalsNames;
	/** List of methods called for global parameters. Used for increased speed */
	protected ArrayList<Method> globalsMethods;
	/** Will contain globals properties */

	// -----------------------------------------------------------------------
	// ZOOM VIEWER -----------------------------------------------------------
	// -----------------------------------------------------------------------
	private ZoomArrayList<EntryNum<Integer, Int2D>> tmp_zoom=new ZoomArrayList<EntryNum<Integer, Int2D>>();

	// -----------------------------------------------------------------------
	// DEBUG -----------------------------------------------------------------
	// -----------------------------------------------------------------------
	private boolean checkReproducibility = false;
	private FileOutputStream file = null;
	private PrintStream ps = null;
	private FileOutputStream fileDup = null;
	private PrintStream psDup = null;
	private boolean checkAgentDuplication = false;

	private int numNeighbors;
	private QuadTree myCell;
	
	/**
	 * 
	 * @author Michele Carillo
	 * @author Carmine Spagnuolo
	 * @author Flavio Serrapica
	 *
	 */
	public DIntGrid2DNonUniform(int width, int height, SimState sm, int aoi, int id, int P, Integer initialGridValue, String name, String prefix) {
		super(width, height,initialGridValue);
		this.width=width;
		this.height=height;
		this.sm = sm;	
		this.AOI = aoi;
		this.updates_cache = new ArrayList<RegionNumeric<Integer,EntryNum<Integer, Int2D>>>();
		this.name = name;
		this.topicPrefix = prefix;

		// Initialize variables for GlobalInspector
		tracingFields = new ArrayList<String>();
		try
		{
			currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		}
		catch(Exception e)
		{
			System.out.println("Do not use the GlobalViewer, the requirements of the simulation exceed the limits of the BufferedImage.\n");
		}
		currentTime = sm.schedule.getTime();
		currentStats = new HashMap<String, Object>();
		isTracingGraphics = false;

		// Initialize variables for GlobalParameters
		globals = new VisualizationUpdateMap<String, Object>();
		globalsNames = new ArrayList<String>();
		globalsMethods = new ArrayList<Method>();
		GlobalParametersHelper.buildGlobalsList((DistributedState)sm, ((ConnectionJMS)((DistributedState)sm).getCommunicationVisualizationConnection()), topicPrefix, globalsNames, globalsMethods);
	}
	
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	public  boolean createRegions(QuadTree... cell)
	{		
		
		if(cell.length > 1 ) return false; 
		myCell=cell[0];
		for(ORIENTATION neighbors:cell[0].neighborhood.keySet())
		{
			for(QuadTree neighbor:cell[0].neighborhood.get(neighbors))
			{
				numNeighbors++;
			}
		}
		System.out.println(myCell.ID+" created regions for "+numNeighbors+" messages");
		own_x=(int)cell[0].getX1();
		own_y=(int)cell[0].getY1();

		my_width=(int)(cell[0].getX2()-cell[0].getX1());
		my_height=(int)(cell[0].getY2()-cell[0].getY1());


		// Building the regions

		myfield=new RegionIntegerNumeric(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);
		System.out.println(myCell.ID+" "+myfield);

		//corner up left
		rmap.NORTH_WEST_OUT=new RegionIntegerNumeric((own_x-AOI + width)%width, (own_y-AOI+height)%height, 
				(own_x+width)%width==0?width:(own_x+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_WEST_MINE=new RegionIntegerNumeric(own_x, own_y, own_x+AOI, own_y+AOI);

		//corner up right
		rmap.NORTH_EAST_OUT = new RegionIntegerNumeric((own_x+my_width+width)%width, (own_y-AOI+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_EAST_MINE=new RegionIntegerNumeric(own_x+my_width-AOI, own_y, own_x+my_width, own_y+AOI);

		//corner down left
		rmap.SOUTH_WEST_OUT=new RegionIntegerNumeric((own_x-AOI+width)%width, (own_y+my_height+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_WEST_MINE=new RegionIntegerNumeric(own_x, own_y+my_height-AOI,own_x+AOI, own_y+my_height);

		//corner down right
		rmap.SOUTH_EAST_OUT=new RegionIntegerNumeric((own_x+my_width+width)%width, (own_y+my_height+height)%height, 
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_EAST_MINE=new RegionIntegerNumeric(own_x+my_width-AOI, own_y+my_height-AOI,own_x+my_width,own_y+my_height);

		rmap.WEST_OUT=new RegionIntegerNumeric((own_x-AOI+width)%width,(own_y+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width, ((own_y+my_height)+height)%height==0?height:((own_y+my_height)+height)%height);
		rmap.WEST_MINE=new RegionIntegerNumeric(own_x,own_y,own_x + AOI , own_y+my_height);

		rmap.EAST_OUT=new RegionIntegerNumeric((own_x+my_width+width)%width,(own_y+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height);
		rmap.EAST_MINE=new RegionIntegerNumeric(own_x + my_width - AOI,own_y,own_x +my_width , own_y+my_height);

		rmap.NORTH_MINE=new RegionIntegerNumeric(own_x ,own_y,own_x+my_width, own_y + AOI);

		rmap.SOUTH_MINE=new RegionIntegerNumeric(own_x,own_y+my_height-AOI,own_x+my_width, (own_y+my_height));

		rmap.NORTH_OUT=new RegionIntegerNumeric((own_x+width)%width, (own_y - AOI+height)%height,
				(own_x+ my_width +width)%width==0?width:(own_x+ my_width +width)%width,(own_y+height)%height==0?height:(own_y+height)%height);

		rmap.SOUTH_OUT=new RegionIntegerNumeric((own_x+width)%width,(own_y+my_height+height)%height,
				(own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width, (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);


		return true;
	}
	
	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	@Override
	public Int2D getAvailableRandomLocation()
	{
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();

		int x= (int)((own_x+AOI)+((my_width-2*AOI))*shiftx);
		int y= (int)((own_y+AOI)+((my_height-2*AOI))*shifty);

		return (new Int2D(x, y));
	}
	
	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 * @throws DMasonException 
	 */
	@Override
	public boolean setDistributedObjectLocation(final Int2D l, Object remoteValue ,SimState sm) throws DMasonException
	{
		int d= 0;


		if(remoteValue instanceof Integer){
			d=(Integer) remoteValue;
		}else
		{throw new DMasonException("Cast Exception setDistributedObjectLocation, second parameter must be a integer");}

		if(setValue(d, l)) return true;
		else{
			String errorMessage = String.format("Unable to set value on position (%d, %d): out of boundaries on cell %s. (ex OH MY GOD!)",
					l.x, l.y, cellType);
			logger.severe( errorMessage ); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)

		}
		return false;
	}
	
	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public synchronized boolean synchro() 
	{

		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();
		// If there is any viewer, send a snap
		if(conn!=null &&((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
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

		// -------------------------------------------------------------------
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		//CLEAR FIELD 
		clear_ghost_regions();
		//SAVE AGENTS IN THE GHOST SECTION
		memorizeRegionOut();

		// Schedule agents in "myField" region
		for(EntryNum<Integer, Int2D> e : myfield.values())
		{
			int d=e.r;
			Int2D loc=e.l;
			this.field[loc.getX()][loc.getY()]=d;

			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Integer, Int2D>(d, loc));
		}   



		ArrayList<String> actualVar=null;
		if(conn!=null)
			actualVar=((DistributedState<?>)sm).upVar.getAllGlobalVarForStep(sm.schedule.getSteps());
		//upVar.getAllGlobalVarForStep(sm.schedule.getSteps()-1);
		if (conn!=null
				&& actualVar != null)
		{

			// Update and send global parameters
			GlobalParametersHelper.sendGlobalParameters(
					sm,
					conn,
					topicPrefix,
					cellType,
					currentTime,
					actualVar
					);

			// Receive global parameters from previous step and update the model
			GlobalParametersHelper.receiveAndUpdate(
					this,
					actualVar,
					globalsMethods);

		}

		// Publish the regions to correspondent topics for the neighbors
		publishRegions(connWorker);

		// Process information received from neighbor 
		processUpdates();

		// -------------------------------------------------------------------
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		// Update ZoomViewer (if any)
		if(conn!=null &&
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				conn.publishToTopic(tmp_zoom,topicPrefix+"GRAPHICS"+cellType,name);
				tmp_zoom=new ZoomArrayList<EntryNum<Integer, Int2D>>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * Method written with Java Reflect that memorizes the agent of out regions in the update cache
	 */
	private void memorizeRegionOut()
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
					RegionNumeric<Integer,EntryNum<Integer, Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer, Int2D>>)returnValue);

					if(name.contains("OUT"))
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
	 * This method removes the agents in the ghost regions; 
	 * and removes all scheduled agents that in the last step was moved in the a ghost region.
	 */
	private void clear_ghost_regions() {

		updateFields();
		updates_cache=new ArrayList<RegionNumeric<Integer,EntryNum<Integer, Int2D>>>();

	}
	
	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	private void updateFields()
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
					RegionNumeric<Integer,EntryNum<Integer, Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer, Int2D>>)returnValue);
					if(name.contains("OUT"))
					{

						for(String agent_id : region.keySet())
						{ 
							EntryNum<Integer, Int2D> e = region.get(agent_id);
							Int2D pos = new Int2D(e.l.getX(), e.l.getY());
							int d = e.r;
							this.field[pos.getX()][pos.getY()]=d;
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
	
	protected void publishRegions(Connection connWorker)
	{
		for(ORIENTATION ori:myCell.neighborhood.keySet())
		{
			String to_publish=topicPrefix+myCell.ID + ori;

			switch (ori) {
			case N:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.NORTH_MINE,rmap.NORTH_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case S:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.SOUTH_MINE,rmap.SOUTH_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case E:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.EAST_MINE,rmap.EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case W:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.WEST_MINE,rmap.WEST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.WEST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case NW:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.NORTH_WEST_MINE, rmap.NORTH_WEST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_WEST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case NE:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.NORTH_EAST_MINE,rmap.NORTH_EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case SE:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.SOUTH_EAST_MINE,rmap.SOUTH_EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case SW:
				try 
				{
					DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> dr=new DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>>(rmap.SOUTH_WEST_MINE,rmap.SOUTH_WEST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_WEST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			default:
				break;
			}
		}

	}
	
	protected void processUpdates()
	{
		// Take from UpdateMap the updates for current last terminated step and use 
		// verifyUpdates() to elaborate informations
		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, numNeighbors);
			while(!q.isEmpty())
			{
				DistributedRegionNumeric<Integer, EntryNum<Integer, Int2D>> region=(DistributedRegionNumeric<Integer, EntryNum<Integer, Int2D>>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {e1.printStackTrace(); }

		for(RegionNumeric<Integer,EntryNum<Integer, Int2D>> region : updates_cache)
			for(EntryNum<Integer, Int2D> e_m:region.values()){
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;	
			}

		this.reset();
	}
	
	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	private boolean reset()
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
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	private void verifyUpdates(DistributedRegionNumeric<Integer,EntryNum<Integer, Int2D>> box)
	{
		RegionNumeric<Integer,EntryNum<Integer, Int2D>> r_mine=box.out;
		RegionNumeric<Integer,EntryNum<Integer, Int2D>> r_out=box.mine;		


		for(String agent_id : r_mine.keySet())
		{
			EntryNum<Integer, Int2D> e_m = r_mine.get(agent_id);
			if(verifyPosition(e_m.l))
			{
				Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;
			}
		}
		RegionNumeric<Integer,EntryNum<Integer, Int2D>> tomemorize=r_out.clone();
		tomemorize.clear();
		for(String agent_id : r_out.keySet())
		{
			EntryNum<Integer, Int2D> e_m = r_out.get(agent_id);
			if(verifyOutPosition(e_m.l))
			{
				tomemorize.put(e_m.l.toString(), e_m);
			}
		}
		updates_cache.add(tomemorize);
		//	updates_cache.add(r_out);
	}
	
	public boolean verifyOutPosition(Int2D pos) {

		return (rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.WEST_OUT != null && rmap.WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.EAST_OUT != null && rmap.EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.NORTH_OUT != null && rmap.NORTH_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_OUT != null && rmap.SOUTH_OUT.isMine(pos.x,pos.y));

	}
	
	/**
	 * This method, written with Java Reflect, provides to add the value
	 * in the right Region.
	 * @param value The value to add
	 * @param l The new location of the value
	 * @return true if the value is added in right way
	 */
	private boolean setValue(int value, Int2D location){

		if(rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(location.x,location.y))
		{
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));


			rmap.NORTH_WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
			rmap.WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
			myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
			return rmap.NORTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
		}
		else
			if(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(location.x,location.y))
			{
				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
					tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

				rmap.NORTH_EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
				rmap.EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
				myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
				return rmap.NORTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
			}
			else
				if(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(location.x,location.y))
				{
					//System.exit(0);
					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
						tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

					rmap.SOUTH_WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
					rmap.WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
					myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
					return rmap.SOUTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
				}
				else
					if(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(location.x,location.y))
					{
						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
							tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

						rmap.SOUTH_EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
						rmap.EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
						myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
						return rmap.SOUTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
					}
					else
						if(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(location.x,location.y))
						{
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

							myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
							return rmap.WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
						}
						else
							if(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(location.x,location.y))
							{
								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
									tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

								myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
								return rmap.EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
							}
							else
								if(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(location.x,location.y))
								{
									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
										tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

									myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
									return rmap.NORTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
								}
								else
									if(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(location.x,location.y))
									{
										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
											tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

										myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
										return rmap.SOUTH_MINE.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
									}
									else
										if(myfield.isMine(location.x,location.y))
										{
											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
												tmp_zoom.add(new EntryNum<Integer,Int2D>(value, location));

											return myfield.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
										}
										else
											if(rmap.WEST_OUT!=null && rmap.WEST_OUT.isMine(location.x,location.y)) 
												return rmap.WEST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
											else
												if(rmap.EAST_OUT!=null && rmap.EAST_OUT.isMine(location.x,location.y)) 
													return rmap.EAST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
												else
													if(rmap.NORTH_OUT!=null && rmap.NORTH_OUT.isMine(location.x,location.y))
														return rmap.NORTH_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
													else
														if(rmap.SOUTH_OUT!=null && rmap.SOUTH_OUT.isMine(location.x,location.y))
															return rmap.SOUTH_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
														else
															if(rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(location.x,location.y)) 
																return rmap.NORTH_WEST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
															else 
																if(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(location.x,location.y)) 
																	return rmap.SOUTH_WEST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
																else
																	if(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(location.x,location.y)) 
																		return rmap.NORTH_EAST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));
																	else
																		if(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(location.x,location.y))
																			return rmap.SOUTH_EAST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(value, location));

		return false;
	}
	
	@Override
	public boolean verifyPosition(Int2D pos) {
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
	@Override
	public DistributedState<Int2D> getState() { return (DistributedState)sm; }


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
	public VisualizationUpdateMap<String, Object> getGlobals() {
		return globals;
	}


	@Override
	public Bag clear() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void trace(String param) {
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = true;
		else
			tracingFields.add(param);

	}


	@Override
	public void untrace(String param) {
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = false;
		else
		{
			tracingFields.remove(param);
			currentStats.remove(param);
		}

	}
}
