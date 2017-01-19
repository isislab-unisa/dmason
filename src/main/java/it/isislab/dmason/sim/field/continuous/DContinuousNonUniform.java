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

package it.isislab.dmason.sim.field.continuous;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
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
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. All the necessary informations 
 *  for the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>square mode</i>.
 *  It represents the field managed by a single peer.
 *  This is an example for a square mode distribution with 9 peers (only to distinguish the regions):
 *  (for code down)
 *  </p>
 *
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */

public class DContinuousNonUniform extends DContinuousGrid2D implements TraceableField
{	
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
	private ZoomArrayList<RemotePositionedAgent> tmp_zoom = new ZoomArrayList<RemotePositionedAgent>();

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


	/**
	 * Constructor of class with paramaters:
	 * 
	 * @param discretization the discretization of the field
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param id the id 
	 * @param P number of partitioning
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DContinuousNonUniform(double discretization, double width, double height, SimState sm, int max_distance, int id, int P, String name, String prefix) {
		super(discretization, width, height);
		this.width=width;
		this.height=height;
		this.sm = sm;	
		this.AOI = max_distance;
		this.updates_cache = new ArrayList<Region<Double,Double2D>>();
		this.name = name;
		this.topicPrefix = prefix;


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

		// non funziona inseriamo agenti su una hash per id
		//		if(checkAgentDuplication)
		//		{
		//			try {
		//				fileDup = new FileOutputStream("99) "+cellType+".txt");
		//				psDup = new PrintStream(fileDup);
		//			} catch (FileNotFoundException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}

		numAgents=0;


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
		//GlobalParametersHelper.buildGlobalsList((DistributedState)sm, ((ConnectionJMS)((DistributedState)sm).getCommunicationVisualizationConnection()), topicPrefix, globalsNames, globalsMethods);
	}
	private QuadTree myCell;
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	public  boolean createRegions(QuadTree... cell)
	{		
		if(cell.length > 1 ) return false; 
		myCell=cell[0];
		
		/* CHANGE
		 * for(ORIENTATION neighbors:cell[0].neighborhood.keySet())
		{
			for(QuadTree neighbor:cell[0].neighborhood.get(neighbors))
			{
				numNeighbors++;
			}
		}
		 * */
		
		for(ORIENTATION neighbors: cell[0].toSubscribe.keySet())
		{
			for(QuadTree neighbor: cell[0].toSubscribe.get(neighbors))
			{
				numNeighbors++;
			}
		}
		System.out.println(myCell.ID+" created regions for "+numNeighbors+" messages");
		own_x=cell[0].getX1();
		own_y=cell[0].getY1();

		my_width=cell[0].getX2()-cell[0].getX1();
		my_height=cell[0].getY2()-cell[0].getY1();


		// Building the regions

		myfield=new RegionDouble(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);
		System.out.println(myCell.ID+" "+myfield);

		//corner up left
		rmap.NORTH_WEST_OUT=new RegionDouble((own_x-AOI + width)%width, (own_y-AOI+height)%height, 
				(own_x+width)%width==0?width:(own_x+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_WEST_MINE=new RegionDouble(own_x, own_y, own_x+AOI, own_y+AOI);

		//corner up right
		rmap.NORTH_EAST_OUT = new RegionDouble((own_x+my_width+width)%width, (own_y-AOI+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y, own_x+my_width, own_y+AOI);

		//corner down left
		rmap.SOUTH_WEST_OUT=new RegionDouble((own_x-AOI+width)%width, (own_y+my_height+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_WEST_MINE=new RegionDouble(own_x, own_y+my_height-AOI,own_x+AOI, own_y+my_height);

		//corner down right
		rmap.SOUTH_EAST_OUT=new RegionDouble((own_x+my_width+width)%width, (own_y+my_height+height)%height, 
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y+my_height-AOI,own_x+my_width,own_y+my_height);

		rmap.WEST_OUT=new RegionDouble((own_x-AOI+width)%width,(own_y+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width, ((own_y+my_height)+height)%height==0?height:((own_y+my_height)+height)%height);
		rmap.WEST_MINE=new RegionDouble(own_x,own_y,own_x + AOI , own_y+my_height);

		rmap.EAST_OUT=new RegionDouble((own_x+my_width+width)%width,(own_y+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height);
		rmap.EAST_MINE=new RegionDouble(own_x + my_width - AOI,own_y,own_x +my_width , own_y+my_height);

		rmap.NORTH_MINE=new RegionDouble(own_x ,own_y,own_x+my_width, own_y + AOI);

		rmap.SOUTH_MINE=new RegionDouble(own_x,own_y+my_height-AOI,own_x+my_width, (own_y+my_height));

		rmap.NORTH_OUT=new RegionDouble((own_x+width)%width, (own_y - AOI+height)%height,
				(own_x+ my_width +width)%width==0?width:(own_x+ my_width +width)%width,(own_y+height)%height==0?height:(own_y+height)%height);

		rmap.SOUTH_OUT=new RegionDouble((own_x+width)%width,(own_y+my_height+height)%height,
				(own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width, (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);



		//		System.out.println(myCell.ID+" "+myfield);
		//		System.out.println(myCell.ID+" NW"+rmap.corner_out_up_left_diag_center);
		//		System.out.println(myCell.ID+" NE"+rmap.corner_out_up_right_diag_center);
		//		System.out.println(myCell.ID+" SW"+rmap.corner_out_down_left_diag_center);
		//		System.out.println(myCell.ID+" SE"+rmap.corner_out_down_right_diag_center);
		//		System.out.println(myCell.ID+" N"+rmap.up_out);
		//		System.out.println(myCell.ID+" S"+rmap.down_out);
		//		System.out.println(myCell.ID+" W"+rmap.left_out);
		//		System.out.println(myCell.ID+" E"+rmap.right_out);
		return true;
	}

	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	@Override
	public Double2D getAvailableRandomLocation()
	{
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();
		//	double x=(own_x+AOI)+((/*(*/my_width/*+own_x-AOI)-(own_x+AOI)*/)*shiftx)-AOI;
		//	double y=(own_y+AOI)+((/*(*/my_height/*+own_y-AOI)-(own_y+AOI)*/)*shifty)-AOI;

		double x= ((own_x+AOI)+((my_width-2*AOI))*shiftx);
		//		if(x >= 0 && x <= 10 || x >= 290 && x<=300)
		//		System.out.println(own_x + " "+ my_width+" "+AOI + " "+shiftx + " prima somma "+ (own_x+AOI)+ " seconda somma "+((my_width-2*AOI))*shiftx + " "+x);
		//		
		double y= ((own_y+AOI)+((my_height-2*AOI))*shifty);

		//rm.setPos(new Double2D(x,y));

		return (new Double2D(x, y));
	}

	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param remoteObject The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 * @throws DMasonException the exception
	 */
	@Override
	public boolean setDistributedObjectLocation(final Double2D location,Object remoteObject,SimState sm) throws DMasonException
	{
		RemotePositionedAgent<Double2D> rm= null;

		if(remoteObject instanceof RemotePositionedAgent ){
			if(((RemotePositionedAgent)remoteObject).getPos() instanceof Double2D){

				rm=(RemotePositionedAgent<Double2D>) remoteObject;	
			}
			else{throw new DMasonException("Cast Exception setDistributedObjectLocation					//, second input parameter RemotePositionedAgent<E>, E must be a Double2D");}
		}
		else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter must be a RemotePositionedAgent<>");}

		if(setAgents(rm, location))
		{
			return true;
		}
		else
		{
			String errorMessage = String.format(myCell.ID+"] Agent %s tried to set position (%f, %f): out of boundaries on cell %s. (ex OH MY GOD!)",
					rm.getId(), location.x, location.y, cellType);

			System.err.println( errorMessage );
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
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();
		/*ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();

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
		 */
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		//CLEAR FIELD 
		clear_ghost_regions();
		//SAVE AGENTS IN THE GHOST SECTION
		memorizeRegionOut();

		// Schedule agents in "myField" region
		for(EntryAgent<Double2D> e : myfield.values())
		{
			RemotePositionedAgent<Double2D> rm=e.r;
			Double2D loc=e.l;
			rm.setPos(loc);
			sm.schedule.scheduleOnce(rm);
			((DistributedState<Double2D>)sm).addToField(rm,e.l);
		}   



		ArrayList<String> actualVar=null;
		/*

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
		 */
		// Publish the regions to correspondent topics for the neighbors
		publishRegions(connWorker);

		// Process information received from neighbor 
		processUpdates();

		// -------------------------------------------------------------------
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		// Update ZoomViewer (if any)
		/*
		if(conn!=null &&
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				conn.publishToTopic(tmp_zoom,topicPrefix+"GRAPHICS"+cellType,name);
				tmp_zoom=new ZoomArrayList<RemotePositionedAgent>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}*/
		return true;
	}
	/**
	 * This method removes the agents in the ghost regions; 
	 * and removes all scheduled agents that in the last step was moved in the a ghost region.
	 */
	private void clear_ghost_regions() {
		// Remove agents in "ghost" sections
		for(Region<Double, Double2D> region : updates_cache)
			for(EntryAgent<Double2D> e:region.values())
				this.remove(e.r);

		updateFields();
		updates_cache=new ArrayList<Region<Double,Double2D>>();

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
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_MINE,rmap.NORTH_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case S:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_MINE,rmap.SOUTH_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case E:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.EAST_MINE,rmap.EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case W:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.WEST_MINE,rmap.WEST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.WEST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case NW:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_WEST_MINE, rmap.NORTH_WEST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_WEST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case NE:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_EAST_MINE,rmap.NORTH_EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case SE:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_EAST_MINE,rmap.SOUTH_EAST_OUT,
							(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_EAST);
					connWorker.publishToTopic(dr,to_publish,name);

				} catch (Exception e1) { e1.printStackTrace();}

				break;
			case SW:
				try 
				{
					DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_WEST_MINE,rmap.SOUTH_WEST_OUT,
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
				DistributedRegion<Double, Double2D> region=(DistributedRegion<Double,Double2D>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {e1.printStackTrace(); }

		for(Region<Double, Double2D> region : updates_cache)
			for(EntryAgent<Double2D> e_m:region.values())
				((DistributedState<Double2D>)sm).addToField(e_m.r,e_m.l);


		this.reset();
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);

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
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	private void verifyUpdates(DistributedRegion<Double,Double2D> box)
	{
		Region<Double,Double2D> r_mine=box.out;
		Region<Double,Double2D> r_out=box.mine;		


		for(String agent_id : r_mine.keySet())
		{
			EntryAgent<Double2D> e_m = r_mine.get(agent_id);
			if(verifyPosition(e_m.l))
			{
				RemotePositionedAgent<Double2D> rm=e_m.r;
				((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
				rm.setPos(e_m.l);
				sm.schedule.scheduleOnce(rm);
			}
		}
		Region<Double,Double2D> tomemorize=r_out.clone();
		tomemorize.clear();
		for(String agent_id : r_out.keySet())
		{
			EntryAgent<Double2D> e_m = r_out.get(agent_id);
			if(verifyOutPosition(e_m.l))
			{
				tomemorize.put(e_m.r.getId(), e_m);
			}
		}
		updates_cache.add(tomemorize);
		//	updates_cache.add(r_out);
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
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
					if(name.contains("OUT"))
					{

						for(String agent_id : region.keySet())
						{ 
							EntryAgent<Double2D> e = region.get(agent_id);
							RemotePositionedAgent<Double2D> rm=e.r;
							rm.setPos(e.l);
							this.remove(rm);
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
	private boolean setAgents(RemotePositionedAgent<Double2D> rm,Double2D location)
	{
		if(rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(location.x,location.y))
		{
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
			if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
				GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

			rmap.NORTH_WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
			rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
			myfield.addAgents(new EntryAgent<Double2D>(rm, location));	
			return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
		}
		else
			if(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(location.x,location.y))
			{
				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
					tmp_zoom.add(rm);
				if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
					GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
				rmap.NORTH_EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
				rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
				myfield.addAgents(new EntryAgent<Double2D>(rm, location));
				return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
			}
			else
				if(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(location.x,location.y))
				{
					//System.exit(0);
					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
						tmp_zoom.add(rm);
					if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
						GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
					rmap.SOUTH_WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
					rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
					myfield.addAgents(new EntryAgent<Double2D>(rm, location));
					return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
				}
				else
					if(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(location.x,location.y))
					{
						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
							tmp_zoom.add(rm);
						if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
							GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
						rmap.SOUTH_EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
						rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
						myfield.addAgents(new EntryAgent<Double2D>(rm, location));
						return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
					}
					else
						if(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(location.x,location.y))
						{
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(rm);
							if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
								GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
							myfield.addAgents(new EntryAgent<Double2D>(rm, location));
							return rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
						}
						else
							if(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(location.x,location.y))
							{
								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
									tmp_zoom.add(rm);
								if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
									GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
								myfield.addAgents(new EntryAgent<Double2D>(rm, location));
								return rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
							}
							else
								if(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(location.x,location.y))
								{
									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
										tmp_zoom.add(rm);
									if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
										GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
									myfield.addAgents(new EntryAgent<Double2D>(rm, location));
									return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
								}
								else
									if(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(location.x,location.y))
									{
										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
											tmp_zoom.add(rm);
										if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
											GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
										myfield.addAgents(new EntryAgent<Double2D>(rm, location));
										return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, location));
									}
									else
										if(myfield.isMine(location.x,location.y))
										{
											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
												tmp_zoom.add(rm);
											if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
												GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
											return myfield.addAgents(new EntryAgent<Double2D>(rm, location));
										}
										else
											if(rmap.WEST_OUT!=null && rmap.WEST_OUT.isMine(location.x,location.y)) 
												return rmap.WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
											else
												if(rmap.EAST_OUT!=null && rmap.EAST_OUT.isMine(location.x,location.y)) 
													return rmap.EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
												else
													if(rmap.NORTH_OUT!=null && rmap.NORTH_OUT.isMine(location.x,location.y))
														return rmap.NORTH_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
													else
														if(rmap.SOUTH_OUT!=null && rmap.SOUTH_OUT.isMine(location.x,location.y))
															return rmap.SOUTH_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
														else
															if(rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(location.x,location.y)) 
																return rmap.NORTH_WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
															else 
																if(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(location.x,location.y)) 
																	return rmap.SOUTH_WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
																else
																	if(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(location.x,location.y)) 
																		return rmap.NORTH_EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
																	else
																		if(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(location.x,location.y))
																			return rmap.SOUTH_EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));

		return false;
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


	/**
	 * Implemented method from the abstract class.
	 */
	@Override
	public DistributedState getState() { return (DistributedState)sm; }

	//getters and setters
	public double getOwn_x() { return own_x; }
	public void setOwn_x(double own_x) { this.own_x = own_x; }
	public double getOwn_y() {	return own_y; }
	public void setOwn_y(double own_y) { this.own_y = own_y; }



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
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}



	/**
	 * Used by SociallyDamaginBehaviour because
	 * @return all visible agents
	 */
	public HashMap<String,EntryAgent<Double2D>> getAllVisibleAgent() {

		HashMap<String,EntryAgent<Double2D>> thor=myfield.clone();
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
					for(String agent_id: region.keySet()){
						EntryAgent<Double2D> e = region.get(agent_id);
						thor.put(agent_id,e);		    		
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}

		return thor;
	}
	/*
	public ArrayList<Entry<Double2D>> getMineAgent() {
		ArrayList<Entry<Double2D>> thor=myfield.clone();
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
					if(name.contains("MINE"))
						for(Entry<Double2D> e: region)
							thor.add(e);		    		
					//					thor.addAll(region);

				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}

		return thor;
	}*/
	/**
	 * Used by SociallyDamaginBehaviour
	 * 
	 * This method insert all agents in the field and in the corresponding region,
	 * for this method you must use position of the actual cell
	 * @param agents the agents
	 * @return true if correct
	 */
	public boolean resetAddAll(ArrayList<RemotePositionedAgent<Double2D>> agents)
	{
		reset();
		int x=0;
		for (RemotePositionedAgent<Double2D> remoteAgent : agents) {

			Double2D pos=remoteAgent.getPos();
			boolean inserito=false;
			ArrayList<RemotePositionedAgent<Double2D>> tmp=new ArrayList<RemotePositionedAgent<Double2D>>();
			if(myfield.isMine(pos.x, pos.y))
			{
				this.remove(remoteAgent);
				sm.schedule.scheduleOnce(remoteAgent);
				((DistributedState<Double2D>)sm).addToField(remoteAgent,pos);
				inserito=true;
				x++;
			}
			else{
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
							if(region.isMine(pos.x,pos.y))
							{   

								if(!tmp.contains(remoteAgent)) 
								{
									tmp.add(remoteAgent);
									if(name.contains("MINE")){

										this.remove(remoteAgent);
										sm.schedule.scheduleOnce(remoteAgent);
										((DistributedState<Double2D>)sm).addToField(remoteAgent,pos);
										inserito=true;
										x++;
									}
									else if(name.contains("OUT"))
									{
										region.addAgents(new EntryAgent<Double2D>(remoteAgent,pos));
										((DistributedState<Double2D>)sm).addToField(remoteAgent,pos);
										inserito=true;
										x++;
									}
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
			if(!inserito) return false;
		}

		return true;
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

	public boolean verifyOutPosition(Double2D pos) {

		return (rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.WEST_OUT != null && rmap.WEST_OUT.isMine(pos.x,pos.y))
				||(rmap.EAST_OUT != null && rmap.EAST_OUT.isMine(pos.x,pos.y))
				||(rmap.NORTH_OUT != null && rmap.NORTH_OUT.isMine(pos.x,pos.y))
				||(rmap.SOUTH_OUT != null && rmap.SOUTH_OUT.isMine(pos.x,pos.y));
		//||(myfield.isMine(pos.x,pos.y));


	}


}