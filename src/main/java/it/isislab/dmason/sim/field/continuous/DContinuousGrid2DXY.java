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

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.support.globals.GlobalInspectorHelper;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. All the necessary informations 
 *  for the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>square mode</i>.
 *  It represents the field managed by a single peer.
 *  This is an example for a square mode distribution with 9 peers (only to distinguish the regions):
 *  (for code down)
 *  <p>
 *
 *	<ul>
 *	<li>MYFIELD : Region to be simulated by peer.</li>
 *
 *	<li>LEFT_MINE, RIGHT_MINE, UP_MINE, DOWN_MINE,CORNER_MINE_LEFT_UP,CORNER_MINE_LEFT_DOWN,
 *		CORNER_MINE_RIGHT_UP,CORNER_MINE_RIGHT_DOWN :Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, RIGHT_OUT, UP_OUT, DOWN_OUT, CORNER_OUT_LEFT_UP_DIAG, CORNER_OUT_LEFT_DOWN_DIAG,
 *		CORNER_OUT_RIGHT_UP_DIAG, CORNER_OUT_RIGHT_DOWN_DIAG : Boundaries Regions those must not be simulated and sent to neighbors to be simulated.</li>
 *   <li>
 *	All peers subscribes to the topic of boundary region which want the information and run a asynchronous thread
 *	to receive the updates, then publish a topic for every their border (or neighbor), that can be :
 *	<ul>
 *	<li> MYTOPIC L (LEFT BORDER)</li>
 *	<li> MYTOPIC R (RIGHT BORDER)</li>
 *	<li> MYTOPIC U (UPPER BORDER)</li>
 *	<li> MYTOPIC D (LOWER BORDER)</li>
 *
 *	<li> MYTOPIC CUDL (Corner Up Diagonal Left)</li>
 *	<li> MYTOPIC CUDR (Corner Up Diagonal Right)</li>
 *	<li> MYTOPIC CDDL (Corner Down Diagonal Left)</li>
 *	<li> MYTOPIC CDDR (Corner Down Diagonal Right)</li>
 *</ul>
 *</li>
 *	</ul></p>
 *	
 * <PRE>
 ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   CORNER DIAG           |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______NORTH_OUT_______|__|__|/________________________|
 * |________________________|__|__|______NORTH_MINE______|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        O  O  |                    / |  E  E                         |
 * |                        V  V  |                   /  |  S  S                         |
 * |         10             E  E  |         11   CORNER  |  T  T         12              |
 * |                        S  S  |               MINE   |  |  |                         |
 * |                        T  T  |                      |  M  O                         |
 * |                        O  M  |       MYFIELD        |  |  U                         |
 * |                        U  I  |                      |  N  T                         |
 * |                        T  N  |                      |  E  |                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_MINE_________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_OUT__________|__|__|_________________________|
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |       20               |  |  |          21          |  |  |           22            |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * ---------------------------------------------------------------------------------------
 * </PRE>
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */

public class DContinuousGrid2DXY extends DContinuousGrid2D implements TraceableField
{	
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DContinuousGrid2DXY.class.getCanonicalName());

	/** Name of the field. Used to identify fields in simulation using several fields. */
	private String name;

	private String topicPrefix = "";

	/** Number of neighbors of this cell, that is also the number of regions to create and of topics to publish/subscribe */ 
	protected int numNeighbors;

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

	/**
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
	 */
	public DContinuousGrid2DXY(double discretization, double width, double height, SimState sm, int max_distance, int i, int j, int rows, int columns, String name, String prefix,boolean isToroidal) {
		super(discretization, width, height);
		this.width=width;
		this.height=height;
		this.sm = sm;	
		this.AOI = max_distance;
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.updates_cache = new ArrayList<Region<Double,Double2D>>();
		this.name = name;
		this.topicPrefix = prefix;

		setToroidal(isToroidal);
		createRegions();
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
		//ERROR GLOBAL PARAMETER
		//GlobalParametersHelper.buildGlobalsList((DistributedState)sm, ((ConnectionJMS)((DistributedState)sm).getCommunicationVisualizationConnection()), topicPrefix, globalsNames, globalsMethods);
	}
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	public  boolean createRegions(QuadTree... cell)
	{		
		if(cell.length > 1 ) return false; 	
		//upper left corner's coordinates
		if(cellType.pos_j<(width%columns))
			own_x=(int)Math.floor(width/columns+1)*cellType.pos_j; 
		else
			own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(cellType.pos_j-((width%columns))); 

		if(cellType.pos_i<(height%rows))
			own_y=(int)Math.floor(height/rows+1)*cellType.pos_i; 
		else
			own_y=(int)Math.floor(height/rows+1)*((height%rows))+(int)Math.floor(height/rows)*(cellType.pos_i-((height%rows))); 



		// own width and height
		if(cellType.pos_j<(width%columns))
			my_width=(int) Math.floor(width/columns+1);
		else
			my_width=(int) Math.floor(width/columns);

		if(cellType.pos_i<(height%rows))
			my_height=(int) Math.floor(height/rows+1);
		else
			my_height=(int) Math.floor(height/rows);


		//calculating the neighbors
		for (int k = -1; k <= 1; k++) 
		{
			for (int k2 = -1; k2 <= 1; k2++) 
			{				
				int v1=cellType.pos_i+k;
				int v2=cellType.pos_j+k2;
				if(v1>=0 && v2 >=0 && v1<rows && v2<columns)
					if( v1!=cellType.pos_i || v2!=cellType.pos_j)
					{
						neighborhood.add(v1+""+v2);
					}	
			}
		}



		if(isToroidal())
			makeToroidalSections();
		else
			makeNoToroidalSections();

		return true;
	}

	private void makeToroidalSections() {

		numNeighbors = 8;
		myfield=new RegionDouble(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);


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

		//if square partitioning
		if(rows==1 && columns >1){
			numNeighbors = 6;
			rmap.NORTH_OUT = null;
			rmap.SOUTH_OUT = null;
		}
		else if(rows > 1 && columns == 1){
			numNeighbors = 6;
			rmap.EAST_OUT = null;
			rmap.WEST_OUT = null;
		}
	}


	private void makeNoToroidalSections() {

		myfield=new RegionDouble(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);

		//corner up left

		rmap.NORTH_WEST_MINE=new RegionDouble(own_x, own_y, own_x+AOI, own_y+AOI);

		//corner up right

		rmap.NORTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y, own_x+my_width, own_y+AOI);

		//corner down left

		rmap.SOUTH_WEST_MINE=new RegionDouble(own_x, own_y+my_height-AOI,own_x+AOI, own_y+my_height);

		//corner down right

		rmap.SOUTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y+my_height-AOI,own_x+my_width,own_y+my_height);

		rmap.WEST_MINE=new RegionDouble(own_x,own_y,own_x + AOI , own_y+my_height);


		rmap.EAST_MINE=new RegionDouble(own_x + my_width - AOI,own_y,own_x +my_width , own_y+my_height);


		rmap.NORTH_MINE=new RegionDouble(own_x ,own_y,own_x+my_width, own_y + AOI);


		rmap.SOUTH_MINE=new RegionDouble(own_x,own_y+my_height-AOI,own_x+my_width, own_y+my_height);

		//Vertical partitioning
		if(rows==1){
			numNeighbors = 2;
			if(cellType.pos_j>0 && cellType.pos_j<columns-1){

				rmap.WEST_OUT=new RegionDouble(own_x-AOI,own_y,own_x, own_y+my_height);

				rmap.EAST_OUT=new RegionDouble(own_x+my_width,own_y,own_x+my_width+AOI,own_y+my_height);
			}

			else if(cellType.pos_j==0){
				numNeighbors = 1;
				rmap.EAST_OUT=new RegionDouble(own_x+my_width,own_y,own_x+my_width+AOI,own_y+my_height);
			}	


			else if(cellType.pos_j==columns-1){
				numNeighbors = 1;
				rmap.WEST_OUT=new RegionDouble(own_x-AOI,own_y,own_x, own_y+my_height);
			}

		}else 
			if(rows>1 && columns == 1){ // Horizontal partitionig
				numNeighbors =2;
				rmap.NORTH_OUT=new RegionDouble(own_x, own_y - AOI,	own_x+ my_width,own_y);
				rmap.SOUTH_OUT=new RegionDouble(own_x,own_y+my_height,own_x+my_width, own_y+my_height+AOI);
				if(cellType.pos_i == 0){
					numNeighbors =1;
					rmap.NORTH_OUT = null;
				}
				if(cellType.pos_i == rows-1){
					numNeighbors =1;
					rmap.SOUTH_OUT= null;
				}
			}
			else{ //sqare partitioning 

				/*
				 * In this case we use a different approach: Firt we make all ghost sections, after that
				 * we remove the useful ghost section
				 * 
				 * */
				numNeighbors = 8;
				//corner up left
				rmap.NORTH_WEST_OUT=new RegionDouble(own_x-AOI, own_y-AOI,own_x, own_y);


				//corner up right
				rmap.NORTH_EAST_OUT = new RegionDouble(own_x+my_width,own_y-AOI,own_x+my_width+AOI,own_y);


				//corner down left
				rmap.SOUTH_WEST_OUT=new RegionDouble(own_x-AOI, own_y+my_height,own_x,own_y+my_height+AOI);

				rmap.NORTH_OUT=new RegionDouble(own_x, own_y - AOI,	own_x+ my_width,own_y);

				//corner down right
				rmap.SOUTH_EAST_OUT=new RegionDouble(own_x+my_width, own_y+my_height,own_x+my_width+AOI,own_y+my_height+AOI);

				rmap.SOUTH_OUT=new RegionDouble(own_x,own_y+my_height,own_x+my_width, own_y+my_height+AOI);

				rmap.WEST_OUT=new RegionDouble(own_x-AOI,own_y,own_x, own_y+my_height);


				rmap.EAST_OUT=new RegionDouble(own_x+my_width,own_y,own_x+my_width+AOI,own_y+my_height);

				if(cellType.pos_i==0 ){
					numNeighbors = 5;
					rmap.NORTH_OUT = null;
					rmap.NORTH_WEST_OUT = null;
					rmap.NORTH_EAST_OUT = null;
				}

				if(cellType.pos_j == 0){
					numNeighbors = 5;
					rmap.SOUTH_WEST_OUT = null;
					rmap.NORTH_WEST_OUT=null;
					rmap.WEST_OUT = null;
				}

				if(cellType.pos_i == rows -1){
					numNeighbors = 5;
					rmap.SOUTH_WEST_OUT = null;
					rmap.SOUTH_OUT = null;
					rmap.SOUTH_EAST_OUT = null;
				}

				if(cellType.pos_j == columns -1){
					numNeighbors = 5;
					rmap.NORTH_EAST_OUT = null;
					rmap.EAST_OUT = null;
					rmap.SOUTH_EAST_OUT = null;
				}

				if((cellType.pos_i == 0 && cellType.pos_j == 0) || 
						(cellType.pos_i == rows-1 && cellType.pos_j==0) || 
						(cellType.pos_i == 0 && cellType.pos_j == columns -1) || 
						(cellType.pos_i == rows-1 && cellType.pos_j == columns -1))
					numNeighbors = 3;
			}
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

		//		double x= ((own_x+AOI)+((my_width-(2*AOI)))*shiftx);	
		//		double y= ((own_y+AOI)+((my_height-(2*AOI)))*shifty);
		double x= own_x+my_width*shiftx;	
		double y= own_y+my_height*shifty;

		return (new Double2D(x, y));
	}

	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
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
			//numAgents++;
			return true;
		}
		else
		{
			String errorMessage = String.format("Agent %s tried to set position (%f, %f): out of boundaries on cell %s. (ex OH MY GOD!)",
					rm.getId(), location.x, location.y, cellType);
			logger.severe( errorMessage );
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
		if(this.getState().schedule.getSteps() !=0){
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


			//ERROR GLOBAL PARAMETER
			/*	ArrayList<String> actualVar=null;
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

		}*/
		}
		// Publish the regions to correspondent topics for the neighbors
		publishRegions(connWorker);

		// Process information received from neighbor 
		processUpdates();

		// -------------------------------------------------------------------
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		if(this.getState().schedule.getSteps() !=0){
			// Update ZoomViewer (if any)
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
			}
		}
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

	protected void publishRegions(Connection connWorker)
	{
		if(rmap.WEST_OUT!=null)
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.WEST_MINE,rmap.WEST_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.WEST);

				connWorker.publishToTopic(dr,topicPrefix+cellType+"W",name);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.EAST_OUT!=null)
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.EAST_MINE,rmap.EAST_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.EAST);				

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"E",name);

			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.NORTH_OUT!=null )
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_MINE,rmap.NORTH_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH);

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"N",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}

		if(rmap.SOUTH_OUT!=null )
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_MINE,rmap.SOUTH_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH);

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"S",name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}

		if(rmap.NORTH_WEST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_WEST_MINE,
					rmap.NORTH_WEST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_WEST);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"NW",name);

			} catch (Exception e1) { e1.printStackTrace();}

		}
		if(rmap.NORTH_EAST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_EAST_MINE,
					rmap.NORTH_EAST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_EAST);
			try 
			{

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"NE",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.SOUTH_WEST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_WEST_MINE,
					rmap.SOUTH_WEST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_WEST);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"SW",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.SOUTH_EAST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_EAST_MINE,
					rmap.SOUTH_EAST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_EAST);

			try 
			{				

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"SE",name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}

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
			RemotePositionedAgent<Double2D> rm=e_m.r;
			((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
			rm.setPos(e_m.l);
			sm.schedule.scheduleOnce(rm);
		}

		updates_cache.add(r_out);
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
	 *	 
	 **/
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
	/**
	 * 
	 * This method insert all agents in the field and in the corresponding region,
	 * for this method you must use position of the actual cell
	 * @param agents
	 * @return
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
									else if(name.contains("out"))
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



}