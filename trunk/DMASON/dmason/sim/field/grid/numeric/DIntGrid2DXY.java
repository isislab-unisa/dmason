package dmason.sim.field.grid.numeric;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegionNumeric;
import dmason.sim.field.EntryNum;
import dmason.sim.field.MessageListener;
import dmason.sim.field.RegionNumeric;
import dmason.sim.field.UpdateMap;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.ZoomArrayList;

import sim.engine.SimState;
import sim.util.Int2D;


/**
 *  <h3>This Field extends IntGrid2D, to be used in a distributed environment. All the necessary informations for 
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

public class DIntGrid2DXY extends DIntGrid2D {

	/**
	 * It's the name of the specific field
	 */
	private String NAME;
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private double initialValue;
	private ZoomArrayList<EntryNum<Integer, Int2D>> tmp_zoom=null;
	
	/**
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param num_peers number of the peers
	 * @param name the name that we give at topic for the connection
	 * @param initialGridValue is the initial value that we want to set at grid at begin simulation. 
	 */
	public DIntGrid2DXY(int width, int height,SimState sm,int max_distance,int i,int j,int num_peers, 
			Integer initialGridValue, String name) 
	{		
		super(width, height, initialGridValue);
		this.NAME = name;
		this.sm=sm;		
		cellType = new CellType(i, j);
		MAX_DISTANCE=max_distance;
		NUMPEERS=num_peers;
		updates_cache = new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
		
		
		setConnection(((DistributedState)sm).getConnection());
		
		createRegion();	

	}
	
	
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion()
	{
		//upper left corner's coordinates
		own_x=(width/((int)Math.sqrt(NUMPEERS)))*cellType.pos_j; //inversione
		own_y=(width/((int)Math.sqrt(NUMPEERS)))*cellType.pos_i;
		
		// own width and height
		my_width=(int) (width/Math.sqrt(NUMPEERS));
		my_height=(int) (height/Math.sqrt(NUMPEERS));
		
		//calculating the neighbors
		for (int k = -1; k <= 1; k++) 
		{
			for (int k2 = -1; k2 <= 1; k2++) 
			{				
				int v1=cellType.pos_i+k;
				int v2=cellType.pos_j+k2;
				if(v1>=0 && v2 >=0 && v1<Math.sqrt(NUMPEERS) && v2<Math.sqrt(NUMPEERS))
					if( v1!=cellType.pos_i || v2!=cellType.pos_j)
					{
						neighborhood.add(v1+""+v2);
					}	
			}
		}
		
		// Building the regions
		rmap.left_out=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height-1),my_width, my_height, width, height);
		if(rmap.left_out!=null)
		{
			rmap.left_mine=RegionIntegerNumeric.createRegionNumeric(own_x,own_y,own_x + MAX_DISTANCE -1, own_y+my_height-1,my_width, my_height, width, height);

		}
		
		rmap.right_out=RegionIntegerNumeric.createRegionNumeric(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
		{
			rmap.right_mine=RegionIntegerNumeric.createRegionNumeric(own_x + my_width - MAX_DISTANCE,own_y,own_x +my_width - 1, own_y+my_height-1,my_width, my_height, width, height);

		}
		
		rmap.up_out=RegionIntegerNumeric.createRegionNumeric(own_x, own_y - MAX_DISTANCE,own_x+ my_width -1,own_y-1,my_width, my_height, width, height);
		if(rmap.up_out!=null)
		{
			rmap.up_mine=RegionIntegerNumeric.createRegionNumeric(own_x ,own_y,own_x+my_width-1, own_y + MAX_DISTANCE -1,my_width, my_height, width, height);

		}
		
		rmap.down_out=RegionIntegerNumeric.createRegionNumeric(own_x,own_y+my_height,own_x+my_width-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
		if(rmap.down_out!=null)
		{
			rmap.down_mine=RegionIntegerNumeric.createRegionNumeric(own_x,own_y+my_height-MAX_DISTANCE,own_x+my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);

		}
				
		if(rmap.left_out == null)
		{
			if(rmap.up_out == null)
			{
				//peer 0
				myfield=new RegionIntegerNumeric(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);
								
				//corner down right
				rmap.corner_out_down_right_diag=RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
				rmap.corner_mine_down_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);

			}
			else
			if(rmap.down_out==null)
			{
				//peer 6
				myfield=new RegionIntegerNumeric(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);
				
				//corner up right
				rmap.corner_out_up_right_diag = RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
				rmap.corner_mine_up_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
				
			}
			else
			{
				//peer 3
			    myfield=new RegionIntegerNumeric(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);
									
				//corner up right
				rmap.corner_out_up_right_diag = RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1,my_width, my_height, width, height);
				rmap.corner_mine_up_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);
				
				//corner down right
				rmap.corner_out_down_right_diag=RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
				rmap.corner_mine_down_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);
					
			}			
		}
		else
		if(rmap.right_out==null)
		{
			if(rmap.up_out==null)
			{
				//peer 2
				myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);
					
				//corner down left
				rmap.corner_out_down_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
				rmap.corner_mine_down_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);

			}
			else
			if(rmap.down_out==null)
			{
				//peer 8
				myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-1);
			
				//corner up left	
				rmap.corner_out_up_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
				rmap.corner_mine_up_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
					
			}
			else
			{	
				//peer 5
				myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);
				
				//corner up left					
				rmap.corner_out_up_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
				rmap.corner_mine_up_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
				
				//corner down left
				rmap.corner_out_down_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
				rmap.corner_mine_down_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1, my_width, my_height, width, height);

			}
		}
		else
		if(rmap.up_out==null)
		{
			//peer 1
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);
	
			//corner down left
			rmap.corner_out_down_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
			rmap.corner_mine_down_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
			
			//corner down right
			rmap.corner_out_down_right_diag=RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
			rmap.corner_mine_down_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);

		}
		else
		if(rmap.down_out==null)
		{
			//peer 7
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-1);
			
			//corner up left	
			rmap.corner_out_up_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
			rmap.corner_mine_up_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

			//corner up right
			rmap.corner_out_up_right_diag = RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
			
		}
		else
		{
			myfield=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);
		
			//corner up left
			rmap.corner_out_up_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);
						
			//corner up right
			rmap.corner_out_up_right_diag = RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
			
			//corner down left
			rmap.corner_out_down_left_diag=RegionIntegerNumeric.createRegionNumeric(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
			rmap.corner_mine_down_left=RegionIntegerNumeric.createRegionNumeric(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
			
			//corner down right
			rmap.corner_out_down_right_diag=RegionIntegerNumeric.createRegionNumeric(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
			rmap.corner_mine_down_right=RegionIntegerNumeric.createRegionNumeric(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);
						
		}
	  return true;
	}
	
	@Override
	public boolean synchro() {

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
			this.field[loc.getX()][loc.getY()]=i;	
			if(((DistributedMultiSchedule)sm.schedule).isEnableZoomView)
			{
				if(tmp_zoom!=null)tmp_zoom.add(new EntryNum<Integer, Int2D>(i, loc));
			}
		}     
		
		if(((DistributedMultiSchedule)sm.schedule).isEnableZoomView)
		{
			try {
				
				connection.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,NAME);
				System.out.println("pubblico per cella con step"+tmp_zoom.STEP+" campo:"+NAME);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
			
		updateFields(); //update fields with java reflect
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
			
		memorizeRegionOut();
			
		//--> publishing the regions to correspondent topics for the neighbors
		if(rmap.left_out!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr =
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.left_mine,rmap.left_out, (sm.schedule.getSteps()-1),
							cellType,DistributedRegionNumeric.LEFT);
			try 
			{				
				connection.publishToTopic(dr,cellType+"L", NAME);
				 
			 } catch (Exception e1) { e1.printStackTrace();}
		}
		
		if(rmap.right_out!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.right_mine,rmap.right_out,(sm.schedule.getSteps()-1),
								cellType,DistributedRegionNumeric.RIGHT);	
			try 
			{				
				connection.publishToTopic(dr,cellType.toString()+"R", NAME);
				
			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.up_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new  DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.up_mine,rmap.up_out,(sm.schedule.getSteps()-1),
								cellType,DistributedRegionNumeric.UP);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"U", NAME);
								 
			 } catch (Exception e1) {e1.printStackTrace();}
		}
		
		if(rmap.down_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr =
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.down_mine,rmap.down_out,(sm.schedule.getSteps()-1),
								cellType,DistributedRegionNumeric.DOWN);

			try 
			{				
				connection.publishToTopic(dr,cellType.toString()+"D", NAME);
				
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		
		if(rmap.corner_out_up_left_diag!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.corner_mine_up_left,rmap.corner_out_up_left_diag,
							(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_UP_LEFT);

			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CUDL", NAME);
										 
			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.corner_out_up_right_diag!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>	
						(rmap.corner_mine_up_right,rmap.corner_out_up_right_diag,
							(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_UP_RIGHT);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CUDR", NAME);
									 
			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.corner_out_down_left_diag!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.corner_mine_down_left, rmap.corner_out_down_left_diag,
							(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_DOWN_LEFT);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CDDL", NAME);
								 
			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.corner_out_down_right_diag!=null)
		{
			DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr = 
					new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>
						(rmap.corner_mine_down_right,rmap.corner_out_down_right_diag,
							(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.CORNER_DIAG_DOWN_RIGHT);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CDDR", NAME);
			
			} catch (Exception e1) { e1.printStackTrace(); }
		}	
		//<--
			
		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, neighborhood.size());
			while(!q.isEmpty())
			{
				DistributedRegionNumeric<Integer, EntryNum<Integer,Int2D>> region =
						(DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>)q.poll();
				verifyUpdates(region);	
			}			
						
		}catch (InterruptedException e1) {e1.printStackTrace(); }
			
		for(RegionNumeric<Integer,EntryNum<Integer,Int2D>> region : updates_cache){
			for(EntryNum<Integer,Int2D> e_m: region)
			{
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;	
			}
		}	
		
		this.reset();

		return true;
	}

	@Override
	public boolean setDistributedObjectLocationForPeer(Int2D location,
			RemoteAgent<Int2D> rm, SimState sm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setDistributedObjectLocation(Int2D location,
			RemoteAgent<Int2D> rm, SimState sm) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Provide the int value shift logic among the peers
	 * @param d
	 * @param l
	 * @param sm
	 * @return
	 */
	public boolean setDistributedObjectLocation(int i, Int2D l, SimState sm) {

		if(myfield.isMine(l.getX(), l.getY()))
		{    		
			return myfield.addEntryNum(new EntryNum<Integer,Int2D>(i, l));
		}
		else
			if(setValue(i, l))
				return true;
			else
				System.out.println(cellType+")OH MY GOD! from:" +this.getClass()); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)

		return false;
	}
	
	
	
	@Override
	public DistributedState getState() {
		
		return (DistributedState)sm;
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
		    	 RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
		    	 
		    	 if(name.contains("out"))
			  	 {
		    		 for(EntryNum<Integer,Int2D> e : region){
		    			 
		    			 Int2D pos = new Int2D(e.l.getX(), e.l.getY());
		    			 int i = e.r;
		    			 this.field[pos.getX()][pos.getY()]=i;
		    		 }
			  	  }
		    	  else
		    		  if(name.contains("mine"))
		    		  {
		    			  for(EntryNum<Integer,Int2D> e : region){
				    			 
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
				
				field[i.getX()][i.getY()]=e_m.r;		  		
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
	
	@Override
	public Int2D setAvailableRandomLocation(RemoteAgent<Int2D> rm) {
		// TODO Auto-generated method stub
		return null;
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
	public void setConnection(Connection con) {
		// TODO Auto-generated method stub
		connection=(ConnectionNFieldsWithActiveMQAPI)con;
	}

}
