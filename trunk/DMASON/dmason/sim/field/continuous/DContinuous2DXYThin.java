/**
 * Copyright 2012 Universit� degli Studi di Salerno


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

package dmason.sim.field.continuous;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegion;
import dmason.sim.field.Entry;
import dmason.sim.field.MessageListener;
import dmason.sim.field.Region;
import dmason.sim.field.TraceableField;
import dmason.sim.field.UpdateMap;
import dmason.sim.loadbalancing.MyCellInterface;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.RemoteSnap;
import dmason.util.visualization.VisualizationUpdateMap;
import dmason.util.visualization.ZoomArrayList;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;

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

public class DContinuous2DXYThin extends DContinuous2DThin implements TraceableField
{	
	private static Logger logger = Logger.getLogger(DContinuous2DYThin.class.getCanonicalName());
	
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private String name;

	private WritableRaster writer;
	private int white[]={255,255,255};
	
	/*
	private FileOutputStream file;
	private PrintStream ps;
	*/
	
	/** List of parameters to trace */
	private ArrayList<String> tracing = new ArrayList<String>();
	/** The image to send */
	private BufferedImage actualSnap;
	
	/** Simulation's time when the image was generated */
	private double actualTime;
	
	/** Statistics to send */
	HashMap<String, Object> actualStats;
	
	/** True if the global viewer requested graphics **/
	boolean isSendingGraphics;
	
	private ZoomArrayList<RemoteAgent> tmp_zoom=new ZoomArrayList<RemoteAgent>();
	private int numAgents;
	private double width,height,field_width,field_height;
	private String topicPrefix = "";
	
	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();


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
	public DContinuous2DXYThin(double discretization, double width, double height, double field_width, double field_height, SimState sm, int max_distance, int i, int j,int rows,int columns, String name, String prefix) {
		super(discretization, field_width, field_height,width,height);
		this.width=width;
		this.height=height;
		this.field_width=field_width;
		this.field_height=field_height;
		this.sm = sm;	
		this.jumpDistance = max_distance;
		//this.numPeers = num_peers;	
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.updates_cache = new ArrayList<Region<Double,Double2D>>();
		this.name = name;
		this.topicPrefix = prefix;

		/*
		try {
			file = new FileOutputStream("Region-"+cellType+".txt");
			ps = new PrintStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		setConnection(((DistributedState)sm).getConnection());
		numAgents=0;
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
		
		actualSnap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		actualTime = sm.schedule.getTime();
		actualStats = new HashMap<String, Object>();
		isSendingGraphics = false;
		writer=actualSnap.getRaster();
		
		// Building the regions
		
		myfield=new RegionDouble(own_x+jumpDistance,own_y+jumpDistance, own_x+my_width-jumpDistance , own_y+my_height-jumpDistance,width,height);
		
		//corner up left
		rmap.corner_out_up_left_diag_center=new RegionDouble((own_x-jumpDistance + width)%width, (own_y-jumpDistance+height)%height, 
				(own_x+width)%width, (own_y+height)%height,width,height);
		rmap.corner_mine_up_left=new RegionDouble(own_x, own_y, 
				own_x+jumpDistance, own_y+jumpDistance,width,height);
					
		//corner up right
		rmap.corner_out_up_right_diag_center = new RegionDouble((own_x+my_width+width)%width, (own_y-jumpDistance+height)%height,
				(own_x+my_width+jumpDistance+width)%width, (own_y+height)%height,width,height);
		rmap.corner_mine_up_right=new RegionDouble(own_x+my_width-jumpDistance, own_y, 
				own_x+my_width, own_y+jumpDistance,width,height);
		
		//corner down left
		rmap.corner_out_down_left_diag_center=new RegionDouble((own_x-jumpDistance+width)%width, (own_y+my_height+height)%height,
				(own_x+width)%width,(own_y+my_height+jumpDistance+height)%height,width,height);
		rmap.corner_mine_down_left=new RegionDouble(own_x, own_y+my_height-jumpDistance,
				own_x+jumpDistance, own_y+my_height,width,height);
		
		//corner down right
		rmap.corner_out_down_right_diag_center=new RegionDouble((own_x+my_width+width)%width, (own_y+my_height+height)%height, 
				(own_x+my_width+jumpDistance+width)%width,(own_y+my_height+jumpDistance+height)%height,width,height);
		rmap.corner_mine_down_right=new RegionDouble(own_x+my_width-jumpDistance, own_y+my_height-jumpDistance,
				own_x+my_width,own_y+my_height,width,height);
					
		rmap.left_out=new RegionDouble((own_x-jumpDistance+width)%width,(own_y+height)%height,
				(own_x+width)%width, ((own_y+my_height)+height)%height,width,height);
		rmap.left_mine=new RegionDouble(own_x,own_y,
				own_x + jumpDistance , own_y+my_height,width,height);
		
		rmap.right_out=new RegionDouble((own_x+my_width+width)%width,(own_y+height)%height,
				(own_x+my_width+jumpDistance+width)%width, (own_y+my_height+height)%height,width,height);
		rmap.right_mine=new RegionDouble(own_x + my_width - jumpDistance,own_y,
				own_x +my_width , own_y+my_height,width,height);
		
		rmap.up_out=new RegionDouble((own_x+width)%width, (own_y - jumpDistance+height)%height,
				(own_x+ my_width +width)%width,(own_y+height)%height,width,height);
		rmap.up_mine=new RegionDouble(own_x ,own_y,
				own_x+my_width, own_y + jumpDistance ,width,height);
		
		rmap.down_out=new RegionDouble((own_x+width)%width,(own_y+my_height+height)%height,
				(own_x+my_width+width)%width, (own_y+my_height+jumpDistance+height)%height,width,height);
		rmap.down_mine=new RegionDouble(own_x,own_y+my_height-jumpDistance,
				own_x+my_width, (own_y+my_height),width,height);
			
		return true;
	}

	/**
	 * Set up the new position in a distributed environment in the space of expertise.
	 * It uses the Java Reflect to find the region which 
	 * the new location belongs.
	 * The method uses the method isMine() of class Region to do this.
	 * 
	 * @param location The new location
	 * @param rm The remote agent that have be stepped
	 * @param sm The SimState of simulation
	 * @return false if the object is null (null objects cannot be put into the grid)
	 */
	public boolean setDistributedObjectLocationForPeer(final Double2D location,RemoteAgent<Double2D> rm,SimState sm)
	{			
		if(myfield.isMine(location.x,location.y) && this.getObjectsAtLocation(location)==null)
	    	return this.setObjectLocation(rm,new Double2D(location.x,location.y));  
	 
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
		    		RegionDouble region=((RegionDouble)returnValue);
		 	    	if(region.isMine(location.x,location.y) && this.getObjectsAtLocation(location)==null)
		    				return this.setObjectLocation(rm, new Double2D(location.x,location.y)); 
 		    	  }
		     }
		     catch (IllegalArgumentException e){ e.printStackTrace();} 
		     catch (IllegalAccessException e) { e.printStackTrace();} 
		     catch (SecurityException e) { e.printStackTrace();} 
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
	public Double2D setAvailableRandomLocation(RemoteAgent<Double2D> rm)
	{
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();
		double x=(own_x+jumpDistance)+((my_width+own_x-jumpDistance)-(own_x+jumpDistance))*shiftx;
        double y=(own_y+jumpDistance)+((my_height+own_y-jumpDistance)-(own_y+jumpDistance))*shifty;
      
        rm.setPos(new Double2D(x,y));
        
        return (new Double2D(x, y));
  	}
	
	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
    public boolean setDistributedObjectLocation(final Double2D location,RemoteAgent<Double2D> rm,SimState sm)
    {
    	numAgents++;
    	if(myfield.isMine(location.x,location.y))
    	{    		
    		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
    			writer.setPixel((int)(location.x%my_width), (int)(location.y%my_height), white);
    		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
			
    		return myfield.addAgents(new Entry<Double2D>(rm,  new Double2D(location.x-own_x+2*jumpDistance, location.y-own_y+2*jumpDistance)));
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
	public synchronized boolean synchro() 
	{
		// If there is any viewer, send a snap
		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
		{
			RemoteSnap snap = new RemoteSnap(cellType, sm.schedule.getSteps() - 1, actualTime);
			actualTime = sm.schedule.getTime();
			
			if (isSendingGraphics)
			{
				try
				{
					ByteArrayOutputStream by = new ByteArrayOutputStream();
					ImageIO.write(actualSnap, "png", by);
					by.flush();
					snap.image = by.toByteArray();
					by.close();
					actualSnap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
					writer=actualSnap.getRaster();
				} catch (Exception e) {
					logger.severe("Error while serializing the snapshot");
					e.printStackTrace();
				}
			}
			
			//if (isSendingGraphics || tracing.size() > 0)
			/* The above line is commented because if we don't send the
			 * RemoteSnap at every simulation step, the global viewer
			 * will block waiting on the queue.
			 */
			{
				try
				{
					snap.stats = actualStats;
					connection.publishToTopic(snap, "GRAPHICS", "GRAPHICS");
				} catch (Exception e) {
					logger.severe("Error while publishing the snap message");
					e.printStackTrace();
				}
			}
			
			// Update statistics
			Class<?> simClass = sm.getClass();
			for (int i = 0; i < tracing.size(); i++)
			{
				try
				{
					Method m = simClass.getMethod("get" + tracing.get(i), (Class<?>[])null);
					Object res = m.invoke(sm, new Object [0]);
					snap.stats.put(tracing.get(i), res);
				} catch (Exception e) {
					logger.severe("Reflection error while calling get" + tracing.get(i));
					e.printStackTrace();
				}
			}

		} //numViewers > 0
		
//		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
//		{
//			RemoteSnap snap = new RemoteSnap(cellType, sm.schedule.getSteps() - 1, actualTime);
//			
//			try {
//				ByteArrayOutputStream by = new ByteArrayOutputStream();
//				ImageIO.write(actualSnap, "png", by);
//				by.flush();
//				
//				connection.publishToTopic(new RemoteSnap(cellType, sm.schedule.getSteps()-1, by.toByteArray()), "GRAPHICS", "GRAPHICS");
//				//System.out.println("PUBBLICO AL VISUALIZZATORE");
//				by.close();
//				actualSnap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
//				writer=actualSnap.getRaster();
//
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
		
		for(Region<Double, Double2D> region : updates_cache)
		{
			for(Entry<Double2D> remote_agent : region)
			{
				this.remove(remote_agent.r);
			}
		}
	
		//every agent in the myfield region is scheduled
		for(Entry<Double2D> e: myfield)
		{
			RemoteAgent<Double2D> rm=e.r;
			Double2D loc=e.l;
			rm.setPos(loc);
		    this.remove(rm);
			sm.schedule.scheduleOnce(rm);
			setObjectLocation(rm,loc);
		
		}   
	
		updateFields(); //update fields with java reflect
		updates_cache=new ArrayList<Region<Double,Double2D>>();
		
		memorizeRegionOut();
		
		//--> publishing the regions to correspondent topics for the neighbors
		if(rmap.left_out!=null)
		{
		 try 
		 {
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.left_mine,rmap.left_out,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.LEFT);
		
			connection.publishToTopic(dr,topicPrefix+cellType+"L",name);
			
		 } catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.right_out!=null)
		{
		 try 
		 {
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.right_mine,rmap.right_out,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.RIGHT);				

			connection.publishToTopic(dr,topicPrefix+cellType.toString()+"R",name);
			
		 } catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.up_out!=null )
		{
		 try 
		 {
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.up_mine,rmap.up_out,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.UP);

			connection.publishToTopic(dr,cellType.toString()+"U",name);
			
		 } catch (Exception e1) {e1.printStackTrace();}
		}
	
		if(rmap.down_out!=null )
		{
		 try 
		 {
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.down_mine,rmap.down_out,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.DOWN);

			connection.publishToTopic(dr,cellType.toString()+"D",name);
			
		 } catch (Exception e1) { e1.printStackTrace(); }
		}

		if(rmap.corner_out_up_left_diag_center!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.corner_mine_up_left,
					rmap.corner_out_up_left_diag_center,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_UP_LEFT);
			try 
			{
					connection.publishToTopic(dr,cellType.toString()+"CUDL",name);
			
			} catch (Exception e1) { e1.printStackTrace();}

		}
		if(rmap.corner_out_up_right_diag_center!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.corner_mine_up_right,
					rmap.corner_out_up_right_diag_center,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_UP_RIGHT);
			try 
			{
			
				connection.publishToTopic(dr,cellType.toString()+"CUDR",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.corner_out_down_left_diag_center!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.corner_mine_down_left,
					rmap.corner_out_down_left_diag_center,(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_DOWN_LEFT);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CDDL",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.corner_out_down_right_diag_center!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.corner_mine_down_right,
					rmap.corner_out_down_right_diag_center,(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_DOWN_RIGHT);
			
			try 
			{				
				
				connection.publishToTopic(dr,cellType.toString()+"CDDR",name);
				
			} catch (Exception e1) { e1.printStackTrace(); }
		}//<--
		
		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, 8);
			while(!q.isEmpty())
			{
				DistributedRegion<Double, Double2D> region=(DistributedRegion<Double,Double2D>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); }
		
		for(Region<Double, Double2D> region : updates_cache)
		for(Entry<Double2D> e_m: region)
		{
			RemoteAgent<Double2D> rm=e_m.r;
			((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
		}
			
		this.reset();
		/*
		/ps.println(sm.schedule.getSteps()+";"+System.currentTimeMillis());
		*/
		
		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				connection.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,name);
				tmp_zoom=new ZoomArrayList<RemoteAgent>();
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
		
		for(Entry<Double2D> e_m: r_mine)
		{
			RemoteAgent<Double2D> rm=e_m.r;
			((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
		  		rm.setPos(e_m.l);
		  		setPortrayalForObject(rm);
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
		    	 if(name.contains("out"))
			  	 {
		    		 for(Entry<Double2D> e: region)
		   			 {
		    			 RemoteAgent<Double2D> rm=e.r;
		    			 rm.setPos(e.l);
			    		 this.remove(rm);
		   			 } 
			  	  }
		    	  else
			      if(name.contains("mine"))
			      {
			    	 for(Entry<Double2D> e: region)
			 	     {
			    		RemoteAgent<Double2D> rm=e.r;
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
	
	/**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 */
	private boolean setAgents(RemoteAgent<Double2D> rm,Double2D location)
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
	    	    			writer.setPixel((int)(location.x%my_width), (int)(location.y%my_height), white);
	    				return region.addAgents(new Entry<Double2D>(rm, new Double2D(location.x-own_x+2*jumpDistance,location.y-own_y+2*jumpDistance)));
	    			}
	    			if(name.contains("out"))
	    				return region.addAgents(new Entry<Double2D>(rm,location));
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
	 * A method that must be used when the specific simulation 
	 * set the portrayal for the remote agents.
	 * This method is used to repaint the portrayal when a remote agent moves.
	 */
	public boolean setPortrayalForObject(Object o)
	{
		if(p!=null)
		{
			((DistributedState<Double2D>)sm).setPortrayalForObject(o);
			return true;
		}
		return false;
	}
	
	/**
	 * Implemented method from the abstract class.
	 */
	public DistributedState getState() { return (DistributedState)sm; }
    
	//getters and setters
	public double getOwn_x() { return own_x; }
	public void setOwn_x(double own_x) { this.own_x = own_x; }
	public double getOwn_y() {	return own_y; }
	public void setOwn_y(double own_y) { this.own_y = own_y; }
	
	@Override
	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}
	 
	 @Override
	public void setTable(HashMap table) {
		 connection.setTable(table);
	 }

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return name;
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
			isSendingGraphics = true;
		else
			tracing.add(param);
	}

	@Override
	public void untrace(String param)
	{
		if (param.equals("-GRAPHICS"))
			isSendingGraphics = false;
		else
		{
			tracing.remove(param);
			actualStats.remove(param);
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
	public Double2D getObjectLocationThin(Object obj) {
		Double2D loc=super.getObjectLocation(obj);
		return new Double2D(loc.x+own_x-2*jumpDistance,loc.y+own_y-2*jumpDistance);
	}

	@Override
	public Double2D getObjectLocationAsDouble2DThin(Object obj) {
		Double2D loc=super.getObjectLocationAsDouble2D(obj);
		return new Double2D(loc.x+own_x-2*jumpDistance,loc.y+own_y-2*jumpDistance);
	}

	@Override
	public boolean setObjectLocationThin(Object obj, Double2D location) {
		 Double2D loc=new Double2D(location.x-own_x+2*jumpDistance,location.y-own_y+2*jumpDistance);
		 return super.setObjectLocation(obj, loc);
	}

	@Override
	public Bag getRawObjectsAtLocationThin(MutableInt2D loc) {
		 int ownxD= (int)((own_x+2*jumpDistance) / discretization);
		    int ownyD= (int)((own_y+2*jumpDistance) / discretization);
			return super.getRawObjectsAtLocation(new Double2D((loc.x-ownxD), (loc.y-ownyD)));
	}

	@Override
	public Bag getRawObjectsAtLocationThin(Int2D loc) {
		 int ownxD= (int)((own_x+2*jumpDistance) / discretization);
		    int ownyD= (int)((own_y+2*jumpDistance) / discretization);
			return super.getRawObjectsAtLocation(new Double2D((loc.x-ownxD), (loc.y-ownyD)));
	}

	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}
}