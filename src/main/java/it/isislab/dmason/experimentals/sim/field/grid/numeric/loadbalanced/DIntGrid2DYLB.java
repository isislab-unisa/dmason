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

package it.isislab.dmason.experimentals.sim.field.grid.numeric.loadbalanced;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.support.field2D.loadbalanced.DistributedRegionNumericLB;
import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2DLB;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

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
 * 	<li>LEFT_MINE, EAST_MINE :
 *	Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, EAST_OUT : 
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
 * </PRE>
 */
public class DIntGrid2DYLB extends DIntGrid2D implements DistributedField2DLB{

	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private int initialValue;
	private ZoomArrayList<EntryNum<Integer, Int2D>> tmp_zoom=null;

	/**
	 * It's the name of the specific field
	 */
	private String NAME;


	private DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>> region;

	private static volatile int numAgents;
	private int balanceL;
	private int balanceR;
	private boolean isLeft;
	private int prevBalanceR;
	private int prevBalanceL;
	private int leftMineSize;
	private int rightMineSize;
	private int width,height;
	private String topicPrefix = "";

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
	 * @param initialGridValue the initial value that we want to set at grid at begin simulation 
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DIntGrid2DYLB(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns, 
			int initialGridValue, String name, String prefix) {

		super(width, height, initialGridValue);
		this.width=width;
		this.height=height;
		this.NAME = name;
		this.sm=sm;
		AOI=max_distance;
		//NUMPEERS=num_peers;
		this.rows = rows;
		this.columns = columns;	
		cellType = new CellType(i, j);
		initialValue=initialGridValue;
		this.topicPrefix = prefix;
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();

		numAgents=0;
		balanceR=0;
		balanceL=0;
		prevBalanceL=0;
		prevBalanceR=0;
		leftMineSize=0;
		rightMineSize=0;
		createRegion();		

	}


	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion()
	{
		int jumpDistance=AOI;
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

				myfield = new RegionIntegerNumeric(
						own_x + jumpDistance,            // MyField's x0 coordinate
						own_y,                           // MyField's y0 coordinate
						own_x + my_width - jumpDistance, // MyField x1 coordinate
						height                          // MyField y1 coordinate
						);                  // Global width and height 

				rmap.WEST_OUT = new RegionIntegerNumeric(
						(own_x - jumpDistance + width) % width, // Left-out x0
						0,									// Left-out y0
						(own_x + width) % (width),				// Left-out x1
						height									// Left-out y1
						);

				rmap.WEST_MINE = new RegionIntegerNumeric(
						(own_x + width) % width,				// Left-mine x0
						0,									// Left-mine y0
						(own_x + jumpDistance + width) % width,	// Left-mine x1
						height									// Left-mine y1
						);

				rmap.EAST_OUT = new RegionIntegerNumeric(
						(own_x + my_width + width) % width,                // Right-out x0
						0,                                               // Right-out y0
						(own_x + my_width + jumpDistance + width) % width, // Right-out x1
						height                                            // Right-out y1
						);

				rmap.EAST_MINE = new RegionIntegerNumeric(
						(own_x + my_width - jumpDistance + width) % width, // Right-mine x0
						0,											   // Right-mine y0
						(own_x + my_width + width) % width,                // Right-mine x1
						height                                            // Right-mine y1
						);


				return true;
		
		/*//upper left corner's coordinates
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
		rmap.WEST_OUT=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height),my_width, my_height, width, height);
		if(rmap.WEST_OUT!=null)
			rmap.WEST_MINE=RegionIntegerNumeric.createRegionNumeric(own_x,own_y,own_x + MAX_DISTANCE -1, (own_y+my_height)-1,my_width, my_height, width, height);

		rmap.EAST_OUT=RegionIntegerNumeric.createRegionNumeric(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, (own_y+my_height)-1,my_width, my_height, width, height);
		if(rmap.EAST_OUT!=null)
			rmap.EAST_MINE=RegionIntegerNumeric.createRegionNumeric(own_x + my_width -MAX_DISTANCE,own_y,own_x +my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);

		if(rmap.WEST_OUT == null)
		{
			//peer 0
			myfield=new RegionIntegerNumeric(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		if(rmap.EAST_OUT == null)
		{
			//peer NUMPEERS-1
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-1);

		}

		if(rmap.WEST_OUT!=null && rmap.EAST_OUT!=null)
		{
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

		}

		return true;*/
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
		for(EntryNum<Integer, Int2D> e: myfield.values())
		{			
			Int2D loc=e.l;
			int i = e.r;
			this.field[loc.getX()][loc.getY()]=i;	
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Integer,Int2D>(i, loc));
		}     
		if(conn!=null &&
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
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

		if(isLeft){ 

			if(balanceR<0){

				rmap.EAST_OUT.clear();

				for (int i = 0; i < height; i++) {
					for (int j = rmap.EAST_OUT.upl_xx; j <= rmap.EAST_OUT.down_xx-AOI; j++) {
						rmap.EAST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(
								field[j][i], new Int2D(j, i)));
					}
				}


				for (int i = 0; i < height; i++) 
					for (int j = rmap.EAST_MINE.upl_xx; j <= rmap.EAST_MINE.down_xx; j++) 
						rmap.EAST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(
								field[j][i], new Int2D(j, i)));




			}
			else if(balanceR>0){

				rmap.EAST_MINE.clear();

				rmap.EAST_OUT.clear();

			}
		}else if(!isLeft) {

			if(balanceL<0){

				rmap.WEST_OUT.clear();

				for (int i = 0; i < height; i++) {
					for (int j = rmap.WEST_OUT.upl_xx+AOI; j <= rmap.WEST_OUT.down_xx; j++) {
						rmap.WEST_OUT.addEntryNum(new EntryNum<Integer, Int2D>(
								field[j][i], new Int2D(j, i)));
					}
				}

				for (int i = 0; i < height; i++) 
					for (int j = rmap.WEST_MINE.upl_xx; j <= rmap.WEST_MINE.down_xx; j++) 
						rmap.WEST_MINE.addEntryNum(new EntryNum<Integer, Int2D>(
								field[j][i], new Int2D(j, i)));

			}
			else if (balanceL>0){

				rmap.WEST_MINE.clear();



				rmap.WEST_OUT.clear();

			}
		}
		//--> publishing the regions to correspondent topics for the neighbors
		if( rmap.WEST_OUT!=null )
		{
			DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>> dr1=new DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>>(rmap.WEST_MINE,rmap.WEST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumericLB.LEFT,((DistributedField2DLB)((DistributedState)sm).getField()).getNumAgents(), getLeftMineSize(),my_width);
			try 
			{	
				connWorker.publishToTopic(dr1,topicPrefix+cellType+"L", NAME);	
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if( rmap.EAST_OUT!=null )
		{
			DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>> dr2=new DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>>(rmap.EAST_MINE,rmap.EAST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumericLB.RIGHT,((DistributedField2DLB)((DistributedState)sm).getField()).getNumAgents(),getRightMineSize(),my_width);
			try 
			{			
				connWorker.publishToTopic(dr2,topicPrefix+cellType+"R", NAME);		
			} catch (Exception e1) {e1.printStackTrace();}
		}			

		//This block restores the mine and the out modified for the exchange of agents during the load balancing phase 
		if(isLeft){
			//The region was the left in the previous step
			if(balanceR>0){
				//The width of the region was increased in the previous step
				//So the EAST_MINE must be restored on the start's dimensions but maintaining the new positions
				rmap.EAST_MINE.setUpl_xx(own_x + my_width -AOI);
			}
			else if(balanceR<0){

				//The width of the region was decreased in the previous step
				//So the EAST_OUT must be restored on the start's dimensions but maintaining the new positions
				rmap.EAST_OUT.setDown_xx(own_x+my_width+AOI-1);
				prevBalanceR=balanceR;

			}
			balanceR=0;
		}
		else{
			//The region was the right in the previous step
			if(balanceL>0){
				//The width of the region was increased in the previous step
				//So the left_mine must be restored on the start's dimensions but maintaining the new positions
				rmap.WEST_MINE.setDown_xx(own_x + AOI -1);
			}
			else if(balanceL<0){

				//The width of the region was increased in the previous step
				//So the left_out must be restored on the start's dimensions but maintaining the new positions
				rmap.WEST_OUT.setUpl_xx(own_x-AOI);
				prevBalanceL=balanceL;
			}
			balanceL=0;
		}

		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;

		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, neighborhood.size());
			while(!q.isEmpty())
			{
				region=(DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>>)q.poll();
				if(sm.schedule.getSteps()%1==0){
					if((sm.schedule.getSteps() % 2 == 0 && cellType.pos_j % 2 == 0)||(sm.schedule.getSteps() % 2 != 0 && cellType.pos_j % 2 != 0)){
						//this region is the left
						if(region.type.pos_j == cellType.pos_j+1 && cellType.pos_j!=columns-1){
							isLeft=true;
							if (prevBalanceR<0){
								for (int i = 0; i < height; i++) {
									for (int j = rmap.EAST_OUT.upl_xx+AOI; j <= rmap.EAST_OUT.down_xx-prevBalanceR; j++) {
										field[j][i]=initialValue;

									}
								}
								prevBalanceR=0;
							}

							balanceR=dynamic3(my_width, ((DistributedField2DLB)((DistributedState)sm).getField()).getNumAgents(), region.numAgents, region.mineNumAgents, getRightMineSize())-my_width;



							//The balance can't be bigger than neighbor's width-AOI-1 otherwise the neighbor's region becames null
							if(balanceR>region.width-AOI-1){
								balanceR=region.width-AOI-1;
							}
							//The balance can't be smaller than its width+AOI+1 otherwise this region becames null
							if(balanceR<-my_width+AOI+1){
								balanceR=-my_width+AOI+1;

							}


							if(balanceR<0){
								//the region becames smaller
								//the EAST_OUT becames bigger so the agents can be passed to the right neighbor
								rmap.EAST_OUT.setUpl_xx(own_x+my_width+balanceR);
								rmap.EAST_OUT.setDown_xx(own_x+my_width+AOI-1);
								my_width=my_width+balanceR;
								rmap.EAST_MINE.setUpl_xx(own_x + my_width-AOI);
								rmap.EAST_MINE.setDown_xx(own_x +my_width-1);
								if(rmap.WEST_OUT == null)
								{
									//peer 0
									myfield.setUpl_xx(own_x);
									myfield.setDown_xx(own_x+my_width-AOI-1);

								}


								if(rmap.WEST_OUT!=null && rmap.EAST_OUT!=null)
								{
									myfield.setUpl_xx(own_x+AOI);
									myfield.setDown_xx(own_x+my_width-AOI-1);

								}

							}else 
								if(balanceR>0){
									//the region becames bigger
									//the EAST_MINE becames bigger so the agents can be received from the right neighbor
									rmap.EAST_MINE.setUpl_xx(own_x + my_width-AOI);
									rmap.EAST_MINE.setDown_xx(own_x +my_width-1+balanceR);
									my_width=my_width+balanceR;
									rmap.EAST_OUT.setUpl_xx(own_x + my_width);
									rmap.EAST_OUT.setDown_xx(own_x+my_width+AOI-1);

									if(rmap.WEST_OUT == null)
									{
										//peer 0
										myfield.setUpl_xx(own_x);
										myfield.setDown_xx(own_x+my_width-AOI-1);

									}

									if(rmap.WEST_OUT!=null && rmap.EAST_OUT!=null)
									{
										myfield.setUpl_xx(own_x+AOI);
										myfield.setDown_xx(own_x+my_width-AOI-1);

									}

								}
						}
					}
					else{
						//this region is the right
						if(region.type.pos_j == cellType.pos_j-1 && cellType.pos_j!=0){
							isLeft=false;
							if(prevBalanceL<0){
								for (int i = 0; i < height; i++) {
									for (int j = rmap.WEST_OUT.upl_xx+prevBalanceL; j <= rmap.WEST_OUT.down_xx-AOI; j++) {
										field[j][i]=initialValue;

									}
								}
								prevBalanceL=0;
							}

							balanceL=region.width-dynamic3(region.width,region.numAgents,((DistributedField2DLB)((DistributedState)sm).getField()).getNumAgents(), getLeftMineSize(), region.mineNumAgents);


							//The balance can't be bigger than neighbor's width-AOI-1 otherwise the neighbor's region becames null
							if(balanceL>region.width-AOI-1)
								balanceL=region.width-AOI-1;
							//The balance can't be smaller than its width+AOI+1 otherwise this region becames null
							if(balanceL<-my_width+AOI+1)
								balanceL=-my_width+AOI+1;
							if(balanceL<0){
								//the region becames smaller
								//the left_out becames bigger so the agents can be passed to the left neighbor
								rmap.WEST_OUT.setUpl_xx(own_x-AOI);
								rmap.WEST_OUT.setDown_xx(own_x-1-balanceL);
								own_x=own_x-balanceL;
								my_width=my_width+balanceL;
								rmap.WEST_MINE.setUpl_xx(own_x);
								rmap.WEST_MINE.setDown_xx(own_x + AOI -1);

								if(rmap.EAST_OUT == null)
								{
									//peer NUMPEERS-1
									myfield.setUpl_xx(own_x+AOI);
									myfield.setDown_xx(own_x+my_width-1);

								}

								if(rmap.WEST_OUT!=null && rmap.EAST_OUT!=null)
								{
									myfield.setUpl_xx(own_x+AOI);
									myfield.setDown_xx( own_x+my_width-AOI-1);

								}			

							}else 
								if(balanceL>0){
									//the region becames bigger
									//the left_mine becames bigger so the agents can be received from the left neighbor
									rmap.WEST_MINE.setUpl_xx(own_x-balanceL);
									rmap.WEST_MINE.setDown_xx(own_x + AOI -1);
									own_x=own_x-balanceL;

									my_width=my_width+balanceL;
									rmap.WEST_OUT.setUpl_xx(own_x-AOI);
									rmap.WEST_OUT.setDown_xx(own_x-1);



									if(rmap.EAST_OUT == null)
									{
										//peer NUMPEERS-1
										myfield.setUpl_xx(own_x+AOI);
										myfield.setDown_xx(own_x+my_width-1);

									}

									if(rmap.WEST_OUT!=null && rmap.EAST_OUT!=null)
									{
										myfield.setUpl_xx(own_x+AOI);
										myfield.setDown_xx( own_x+my_width-AOI-1);

									}			
								}
						}
					}
				}	
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(RegionNumeric<Integer,EntryNum<Integer,Int2D>> region : updates_cache)
		{
			for(EntryNum<Integer,Int2D> e_m: region.values())
			{
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;
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
	public void verifyUpdates(DistributedRegionNumericLB<Integer,EntryNum<Integer,Int2D>> box)
	{
		RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_mine=box.out;
		RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_out=box.mine;

		for(EntryNum<Integer,Int2D> e_m: r_mine.values())
		{
			Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());

			field[i.getX()][i.getY()]=e_m.r;		  		
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
						for(EntryNum<Integer,Int2D> e : region.values()){

							Int2D pos = new Int2D(e.l.getX(), e.l.getY());
							int i = e.r;
							this.field[pos.getX()][pos.getY()]=i;
						}
					}
					else
						if(name.contains("mine"))
						{
							for(EntryNum<Integer,Int2D> e : region.values()){

								Int2D pos = new Int2D(e.l.getX(), e.l.getY());
								int i = e.r;
								this.field[pos.getX()][pos.getY()]=i;
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
	//public boolean setDistributedObjectLocation(int i, Int2D l, SimState sm){
	public boolean setDistributedObjectLocation( Int2D l, Object remoteValue ,SimState sm) throws DMasonException{



		if(!(remoteValue instanceof Integer)) throw new DMasonException("Cast Exception setDistributedObjectLocation, second parameter must be a int");

		int i=(Integer) remoteValue;
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
						if(name.contains("left_mine"))
							leftMineSize++;
						if(name.contains("EAST_MINE"))
							rightMineSize++;
						if(name.contains("mine")){
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(new EntryNum<Integer,Int2D>(value, l));
						}
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
	public Int2D getAvailableRandomLocation() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDistributedFieldID() {
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

	
	public void resetParameters() {
		numAgents=0;
		leftMineSize=0;
		rightMineSize=0;
	}

	@Override
	public int getLeftMineSize() {
		// TODO Auto-generated method stub
		return leftMineSize;
	}

	@Override
	public int getRightMineSize() {
		// TODO Auto-generated method stub
		return rightMineSize;
	}


	private int dynamic3(int al, int sl, int sr, int el, int er){

		if(sr>sl){
			if(el==0)
				return al + AOI;
			else
				return al + Math.min(AOI, (int) Math.ceil((sr-sl)/2.0)*AOI/el);
		}
		else if(sl>sr){
			if(er==0)
				return al - AOI;
			else
				return al + Math.max(-AOI, (int) Math.ceil((sr-sl)/2.0)*AOI/er);
		}
		return al;
	}


	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}
	@Override
	public boolean verifyPosition(Int2D pos) {

		//we have to implement this
		return false;

	}	
}
