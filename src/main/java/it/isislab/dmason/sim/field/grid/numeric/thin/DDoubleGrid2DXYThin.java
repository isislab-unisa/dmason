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

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionDoubleNumeric;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.util.visualization.zoomviewerapp.ZoomArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.engine.SimState;
import sim.util.Int2D;


/**
 *  <h3>This Field extends DoubleGrid2D, to be used in a distributed environment. All the necessary informations for 
 *  the distribution of simulation are wrapped in this class.</h3>
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
 * ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   CORNER DIAG           |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______UP_OUT__________|__|__|/________________________|
 * |________________________|__|__|______UP_MINE_________|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        L  L  |                    / |  R  R                         |
 * |                        E  E  |                   /  |  I  I                         |
 * |         10             F  F  |         11   CORNER  |  G  G         12              |
 * |                        T  T  |               MINE   |  H  H                         |
 * |                        |  |  |                      |  T  T                         |
 * |                        O  M  |       MYFIELD        |  |  |                         |
 * |                        U  I  |                      |  M  O                         |
 * |                        T  N  |                      |  I  U                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___DOWN_MINE__________|__|__|_________________________|
 * |________________________|__|__|___DOWN_OUT___________|__|__|_________________________|
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
 */
public class DDoubleGrid2DXYThin extends DDoubleGrid2DThin {
	/**
	 * It's the name of the specific field
	 */
	private String NAME;
	/**
	 * It represents the initial value of the field
	 */	
	private double initialValue;
	private ZoomArrayList<EntryNum<Double, Int2D>> tmp_zoom=new ZoomArrayList<EntryNum<Double, Int2D>>();
	private int numAgents;
	private int width,height,field_width,field_height;
	private String topicPrefix = "";

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
	public DDoubleGrid2DXYThin(int width, int height,int field_width,int field_height,SimState sm,int max_distance,int i,int j,int rows,int columns, double initialGridValue, String name,String prefix) {

		super(field_width, field_height, width, height,initialGridValue);
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
		this.initialValue = initialGridValue;
		this.topicPrefix = prefix;
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Double,Int2D>>>();
		numAgents=0;
	
		createRegion();		

	}

	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();

	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion() {

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

		// Building the regions
		rmap.left_out=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height-1),my_width, my_height, width, height);
		if(rmap.left_out!=null)
		{
			rmap.left_mine=RegionDoubleNumeric.createRegionNumeric(own_x,own_y,own_x + MAX_DISTANCE -1, own_y+my_height-1,my_width, my_height, width, height);

		}
		rmap.right_out=RegionDoubleNumeric.createRegionNumeric(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
		{
			rmap.right_mine=RegionDoubleNumeric.createRegionNumeric(own_x + my_width - MAX_DISTANCE,own_y,own_x +my_width - 1, own_y+my_height-1,my_width, my_height, width, height);

		}
		rmap.up_out=RegionDoubleNumeric.createRegionNumeric(own_x, own_y - MAX_DISTANCE,own_x+ my_width -1,own_y-1,my_width, my_height, width, height);
		if(rmap.up_out!=null)
		{
			rmap.up_mine=RegionDoubleNumeric.createRegionNumeric(own_x ,own_y,own_x+my_width-1, own_y + MAX_DISTANCE -1,my_width, my_height, width, height);

		}
		rmap.down_out=RegionDoubleNumeric.createRegionNumeric(own_x,own_y+my_height,own_x+my_width-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
		if(rmap.down_out!=null)
		{
			rmap.down_mine=RegionDoubleNumeric.createRegionNumeric(own_x,own_y+my_height-MAX_DISTANCE,own_x+my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);

		}
		if(rmap.left_out == null)
		{
			if(rmap.up_out == null)
			{
				//peer 0
				myfield=new RegionDoubleNumeric(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);

				//corner down right
				rmap.corner_out_down_right_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
				rmap.corner_mine_down_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);

			}
			else
				if(rmap.down_out==null)
				{
					//peer 6
					myfield=new RegionDoubleNumeric(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);

					//corner up right
					rmap.corner_out_up_right_diag_center = RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
					rmap.corner_mine_up_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

				}
				else
				{
					//peer 3
					myfield=new RegionDoubleNumeric(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);

					//corner up right
					rmap.corner_out_up_right_diag_center = RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1,my_width, my_height, width, height);
					rmap.corner_mine_up_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);

					//corner down right
					rmap.corner_out_down_right_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
					rmap.corner_mine_down_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);

				}			
		}else
			if(rmap.right_out==null)
			{
				if(rmap.up_out==null)
				{
					//peer 2
					myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);

					//corner down left
					rmap.corner_out_down_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
					rmap.corner_mine_down_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);

				}
				else
					if(rmap.down_out==null)
					{
						//peer 8
						myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-1);

						//corner up left	
						rmap.corner_out_up_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
						rmap.corner_mine_up_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

					}
					else
					{	
						//peer 5
						myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);

						//corner up left					
						rmap.corner_out_up_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
						rmap.corner_mine_up_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

						//corner down left
						rmap.corner_out_down_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
						rmap.corner_mine_down_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1, my_width, my_height, width, height);

					}
			}
			else
				if(rmap.up_out==null)
				{
					//peer 1
					myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);

					//corner down left
					rmap.corner_out_down_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
					rmap.corner_mine_down_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);

					//corner down right
					rmap.corner_out_down_right_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
					rmap.corner_mine_down_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);

				}
				else
					if(rmap.down_out==null)
					{
						//peer 7
						myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-1);

						//corner up left	
						rmap.corner_out_up_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
						rmap.corner_mine_up_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

						//corner up right
						rmap.corner_out_up_right_diag_center = RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
						rmap.corner_mine_up_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

					}
					else
					{
						myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);

						//corner up left
						rmap.corner_out_up_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
						rmap.corner_mine_up_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);

						//corner up right
						rmap.corner_out_up_right_diag_center = RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
						rmap.corner_mine_up_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

						//corner down left
						rmap.corner_out_down_left_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
						rmap.corner_mine_down_left=RegionDoubleNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);

						//corner down right
						rmap.corner_out_down_right_diag_center=RegionDoubleNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
						rmap.corner_mine_down_right=RegionDoubleNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);
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
	@Override
	public boolean setDistributedObjectLocation(Int2D l, Object remoteValue, SimState sm) throws DMasonException{

		

		if(!(remoteValue instanceof Double)) throw new DMasonException("Cast Exception setDistributedObjectLocation, second parameter must be a double");

		double d=(Double) remoteValue;
		

		numAgents++;

		if(setValue(d, l)) return true;
		else
			System.out.println(cellType+")OH MY GOD! "+d+" "+l+" from:" +this.getClass()); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)

		return false;
	}

	@Override
	public boolean synchro() {


		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();


		//every value in the myfield region is setted
		for(EntryNum<Double, Int2D> e: myfield.values())
		{			
			Int2D loc=e.l;
			double d = e.r;
			setThin(loc.getX(), loc.getY(), d);
			//this.field[loc.getX()][loc.getY()]=d;
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Double, Int2D>(d, loc));
		}     

		if(conn!=null &&
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			//			for (int y = myfield.upl_yy; y < myfield.down_yy; y++) {
			//				for (int x = myfield.upl_xx; x < myfield.down_xx; x++) {
			//					
			//					double d = this.field[x][y];
			//					tmp_zoom.add(new EntryNum<Double, Int2D>(d, new Int2D(x, y)));
			//				}
			//			}

			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				conn.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,NAME);
				tmp_zoom=new ZoomArrayList<EntryNum<Double, Int2D>>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		updateFields(); //update fields with java reflect
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Double,Int2D>>>();

		memorizeRegionOut();

		//--> publishing the regions to correspondent topics for the neighbors			
		if(rmap.left_out!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr =
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.left_mine,rmap.left_out,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionNumeric.LEFT);
			try 
			{	
				connWorker.publishToTopic(dr,topicPrefix+cellType+"L", NAME);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.right_out!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.right_mine,rmap.right_out,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionNumeric.RIGHT);				
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"R", NAME);

			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.up_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr = 
					new  DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.up_mine,rmap.up_out,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionNumeric.UP);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"U", NAME);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.down_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr =
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.down_mine,rmap.down_out,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionNumeric.DOWN);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"D", NAME);

			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if(rmap.corner_out_up_left_diag_center!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.corner_mine_up_left,rmap.corner_out_up_left_diag_center,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_UP_LEFT);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CUDL", NAME);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.corner_out_up_right_diag_center!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr =
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.corner_mine_up_right,rmap.corner_out_up_right_diag_center,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_UP_RIGHT);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CUDR", NAME); 

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.corner_out_down_left_diag_center!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.corner_mine_down_left,rmap.corner_out_down_left_diag_center,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_DOWN_LEFT);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CDDL", NAME);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.corner_out_down_right_diag_center!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>
			(rmap.corner_mine_down_right, rmap.corner_out_down_right_diag_center,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_DOWN_RIGHT);				
			try 
			{	
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CDDR", NAME);

			} catch (Exception e1) { e1.printStackTrace(); }
		}
		//<--

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, neighborhood.size());

			while(!q.isEmpty())
			{
				DistributedRegionNumeric<Integer, EntryNum<Double,Int2D>> region=
						(DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>)q.poll();

				verifyUpdates(region);	
			}			

		}catch (InterruptedException e1) {
			e1.printStackTrace(); 
		} catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(RegionNumeric<Integer,EntryNum<Double,Int2D>> region : updates_cache){
			for(EntryNum<Double,Int2D> e_m: region.values())
			{
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				setThin(i.getX(), i.getY(), e_m.r);
			}
		}	
		this.reset();

		return true;
	}

	@Override
	public DistributedState getState() {

		return (DistributedState)sm;
	}

	@Override
	public Int2D getAvailableRandomLocation() {
		int x=(((DistributedState)sm).random.nextInt(width)%(my_width-1))+own_x;
		if(x>(width-1)) x--;
		int y=(((DistributedState)sm).random.nextInt(height)%(my_height-1))+own_y;
		if(y>(height-1)) y--;

		//rm.setPos(new Int2D(x, y));

		return (new Int2D(x, y));
	}

	/**
	 * This method, written with Java Reflect, provides to add the value
	 * in the right Region.
	 * @param value The value to add
	 * @param l The new location of the value
	 * @return true if the value is added in right way
	 */
	private boolean setValue(double value, Int2D l){


		if(rmap.corner_mine_up_left!=null && rmap.corner_mine_up_left.isMine(l.x,l.y))
		{
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
			rmap.corner_mine_up_left.addEntryNum(new EntryNum<Double,Int2D>(value, l));
			rmap.left_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
			myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
			return rmap.up_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
		}
		else
			if(rmap.corner_mine_up_right!=null && rmap.corner_mine_up_right.isMine(l.x,l.y))
			{
				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
					tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
				rmap.corner_mine_up_right.addEntryNum(new EntryNum<Double,Int2D>(value, l));
				rmap.right_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
				myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
				return rmap.up_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
			}
			else
				if(rmap.corner_mine_down_left!=null && rmap.corner_mine_down_left.isMine(l.x,l.y))
				{
					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
						tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
					rmap.corner_mine_down_left.addEntryNum(new EntryNum<Double,Int2D>(value, l));
					rmap.left_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
					myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
					return rmap.down_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
				}
				else
					if(rmap.corner_mine_down_right!=null && rmap.corner_mine_down_right.isMine(l.x,l.y))
					{
						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
							tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
						rmap.corner_mine_down_right.addEntryNum(new EntryNum<Double,Int2D>(value, l));
						rmap.right_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
						myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
						return rmap.down_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
					}
					else
						if(rmap.left_mine != null && rmap.left_mine.isMine(l.x,l.y))
						{
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
							myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
							return rmap.left_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
						}
						else
							if(rmap.right_mine != null && rmap.right_mine.isMine(l.x,l.y))
							{
								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
									tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
								myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
								return rmap.right_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
							}
							else
								if(rmap.up_mine != null && rmap.up_mine.isMine(l.x,l.y))
								{
									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
										tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
									myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
									return rmap.up_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
								}
								else
									if(rmap.down_mine != null && rmap.down_mine.isMine(l.x,l.y))
									{
										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
											tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
										myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
										return rmap.down_mine.addEntryNum(new EntryNum<Double,Int2D>(value, l));
									}
									else
										if(myfield.isMine(l.x,l.y))
										{
											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
												tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
											return myfield.addEntryNum(new EntryNum<Double,Int2D>(value, l));
										}
										else
											if(rmap.left_out!=null && rmap.left_out.isMine(l.x,l.y)) 
												return rmap.left_out.addEntryNum(new EntryNum<Double,Int2D>(value, l));
											else
												if(rmap.right_out!=null && rmap.right_out.isMine(l.x,l.y)) 
													return rmap.right_out.addEntryNum(new EntryNum<Double,Int2D>(value, l));
												else
													if(rmap.up_out!=null && rmap.up_out.isMine(l.x,l.y))
														return rmap.up_out.addEntryNum(new EntryNum<Double,Int2D>(value, l));
													else
														if(rmap.down_out!=null && rmap.down_out.isMine(l.x,l.y))
															return rmap.down_out.addEntryNum(new EntryNum<Double,Int2D>(value, l));
														else
															if(rmap.corner_out_up_left_diag_center!=null && rmap.corner_out_up_left_diag_center.isMine(l.x,l.y)) 
																return rmap.corner_out_up_left_diag_center.addEntryNum(new EntryNum<Double,Int2D>(value, l));
															else 
																if(rmap.corner_out_down_left_diag_center!=null && rmap.corner_out_down_left_diag_center.isMine(l.x,l.y)) 
																	return rmap.corner_out_down_left_diag_center.addEntryNum(new EntryNum<Double,Int2D>(value, l));
																else
																	if(rmap.corner_out_up_right_diag_center!=null && rmap.corner_out_up_right_diag_center.isMine(l.x,l.y)) 
																		return rmap.corner_out_up_right_diag_center.addEntryNum(new EntryNum<Double,Int2D>(value, l));
																	else
																		if(rmap.corner_out_down_right_diag_center!=null && rmap.corner_out_down_right_diag_center.isMine(l.x,l.y))
																			return rmap.corner_out_down_right_diag_center.addEntryNum(new EntryNum<Double,Int2D>(value, l));


		return false;	       			       			
	}

	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the value's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the value's location is updated and the value is setted.
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
					RegionNumeric<Integer,EntryNum<Double,Int2D>> region=((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);

					if(name.contains("out"))
					{
						for(EntryNum<Double,Int2D> e : region.values()){

							Int2D pos = new Int2D(e.l.getX(), e.l.getY());
							double d = e.r;
							setThin(pos.getX(), pos.getY(), d);
							//this.field[pos.getX()][pos.getY()]=d;
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
					RegionNumeric<Integer,EntryNum<Double,Int2D>> region=((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);
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
	 * This method takes updates from box and set every value in the regions out.
	 * Every value in the regions mine is compared with every value in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> box)
	{
		RegionNumeric<Integer,EntryNum<Double,Int2D>> r_mine=box.out;
		RegionNumeric<Integer,EntryNum<Double,Int2D>> r_out=box.mine;

		for(EntryNum<Double,Int2D> e_m: r_mine.values())
		{
			Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());
			setThin(i.getX(), i.getY(), e_m.r);
			//field[i.getX()][i.getY()]=e_m.r;		  		
		}		
		updates_cache.add(r_out);
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
					RegionNumeric<Integer,EntryNum<Double, Int2D>> region=((RegionNumeric<Integer,EntryNum<Double, Int2D>>)returnValue);
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
	public void setThin(int i,int j, double val){
		if(i-own_x+2*MAX_DISTANCE>=0 && i-own_x+2*MAX_DISTANCE<field_width && j-own_y+2*MAX_DISTANCE>=0 && j-own_y+2*MAX_DISTANCE<field_height)
			field[i-own_x+2*MAX_DISTANCE][j-own_y+2*MAX_DISTANCE]=val;
	}
	@Override
	public double getThin(int i, int j){
		if(i-own_x+2*MAX_DISTANCE>=0 && i-own_x+2*MAX_DISTANCE<field_width && j-own_y+2*MAX_DISTANCE>=0 && j-own_y+2*MAX_DISTANCE<field_height)
			return field[i-own_x+2*MAX_DISTANCE][j-own_y+2*MAX_DISTANCE];
		return Double.MIN_VALUE;

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
