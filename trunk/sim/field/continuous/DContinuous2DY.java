package dmason.sim.field.continuous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQObjectMessage;
import sim.engine.SimState;
import sim.util.Double2D;
import sim.util.Int2D;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegion;
import dmason.sim.field.Entry;
import dmason.sim.field.MessageListener;
import dmason.sim.field.Region;
import dmason.sim.field.UpdaterThreadForListener;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.connection.MyMessageListener;

/**
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. All the necessary informations for 
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

public class DContinuous2DY extends DContinuous2D
{	
	public UpdaterThreadForListener<Double,Double2D> ut_left;
	public UpdaterThreadForListener<Double,Double2D> ut_right;
	public ArrayList<MessageListener> listeners;
	private ConnectionWithJMS con;
	private PrintWriter out;
	/**
	 * @param discretization the discretization of the field
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param num_peers number of the peers
	 */
	public DContinuous2DY(double discretization, double width, double height
			,SimState sm,int max_distance,int i,int j,int num_peers) {
		super(discretization, width, height);
		MAX_DISTANCE=max_distance;
		NUMPEERS=num_peers;	
		cellType = new CellType(i,j);
		listeners = new ArrayList<MessageListener>();
		updates_cache=new ArrayList<Region<Double,Double2D>>();
		
		try 
		{
			connection.setupConnection(new Address(((DistributedState)sm).ip, ((DistributedState)sm).port));
		} catch (Exception e) { e.printStackTrace(); }
				
		createRegion();		
		this.sm=sm;		
	}
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion()
	{
		//upper left corner's coordinates
		own_x=(width/NUMPEERS)*cellType.pos_j; 
		own_y=0; // in this mode the y coordinate is ever 0
		
		// own width and height
		my_width=(int) (width/NUMPEERS);
		my_height=height;
		
		//calculating the neighbors
		int v1=cellType.pos_j-1;
		int v2=cellType.pos_j+1;
		if( v1 >= 0 )
		{
			neighborhood.add(cellType.getNeighbourLeft());
		}
		if( v2 <= NUMPEERS-1 )
		{
			neighborhood.add(cellType.getNeighbourRight());
		}	
		
		myfield=new RegionDouble(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE, own_y+my_height,width,height);
		
		rmap.left_out=new RegionDouble((own_x-MAX_DISTANCE +width)%width,0.0,(own_x+width)%(width),height,width,height);		
		rmap.left_mine=new RegionDouble((own_x+width)%width,0.0,(own_x + MAX_DISTANCE +width)%(width),height,width,height);
		rmap.right_out=new RegionDouble((own_x+my_width+width)%width,0.0,(own_x+my_width+MAX_DISTANCE+width)%(width),height,width,height);
		rmap.right_mine=new RegionDouble((own_x + my_width -MAX_DISTANCE+width)%width,0.0,(own_x +my_width+width)%(width),height,width,height);
	
		try 
		{
			connection.createTopic(cellType.pos_i+"-"+cellType.pos_j+"L");
			connection.createTopic(cellType.pos_i+"-"+cellType.pos_j+"R");
		
			connection.subscribeToTopic(cellType.pos_i+"-"+((cellType.pos_j-1+ NUMPEERS)%NUMPEERS)+"R");
			connection.subscribeToTopic(cellType.pos_i+"-"+(((cellType.pos_j+1+ NUMPEERS))%NUMPEERS)+"L");
				
			ut_right=new UpdaterThreadForListener(connection, cellType,this.NUMPEERS,this,neighborhood,cellType.pos_i+"-"+(((cellType.pos_j-1+ NUMPEERS))%NUMPEERS)+"R",updates,listeners);
			ut_right.start();
			ut_left=new UpdaterThreadForListener(connection, cellType,this.NUMPEERS,this,neighborhood,cellType.pos_i+"-"+(((cellType.pos_j+1+ NUMPEERS))%NUMPEERS)+"L",updates,listeners);
			ut_left.start();
		} catch (Exception e) { e.printStackTrace(); }
		
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
	public boolean setDistributedObjectLocationForPeer(final Double2D location,RemoteAgent<Double2D> rm,SimState sm)
	{    	
	    if(myfield.isMine(location.x,location.y) )
	    { 
	    	return this.setObjectLocation(rm,new Double2D(location.x,location.y));  
	    }			
	 
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
		 	      
 				  if(region.isMine(location.x,location.y) )
 				  {
 					 return this.setObjectLocation(rm,new Double2D(location.x,location.y)); 
 				  } 
 		      }
		  }catch (IllegalArgumentException e){ e.printStackTrace(); } 
		  catch (IllegalAccessException e) { e.printStackTrace(); }
		  catch (SecurityException e) { e.printStackTrace(); } 
		  catch (NoSuchMethodException e) { e.printStackTrace(); } 
		  catch (InvocationTargetException e) { e.printStackTrace(); }
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
		double x=(own_x+MAX_DISTANCE)+((my_width+own_x-MAX_DISTANCE)-(own_x+MAX_DISTANCE))*shiftx;
	    double y=(own_y+MAX_DISTANCE)+((my_height+own_y-MAX_DISTANCE)-(own_y+MAX_DISTANCE))*shifty;
      
        rm.setPos(new Double2D(x,y));
        
        return (new Double2D(x, y));
	}

	/** 
	 * Provide the agents' shift logic among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
    public boolean setDistributedObjectLocation(final Double2D location,RemoteAgent<Double2D> rm,SimState sm)
    {  	
    	if(myfield.isMine(location.x,location.y))
    	{    
    		return myfield.addAgents(new Entry<Double2D>(rm, location));
    	}
    	else if(setAgents(rm, location))
    		 {
    			 return true;
    		 }
    		 else
    				System.out.println(cellType+")OH MY GOD!"+ rm.id); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)
    	return false;
    }

    /**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	public synchronized boolean synchro() 
	{		
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
		if( rmap.left_out!=null )
		{
			DistributedRegion<Double,Double2D> dr1=new DistributedRegion<Double,Double2D>(rmap.left_mine,
					rmap.left_out,(sm.schedule.getSteps()-1),cellType,DistributedRegion.LEFT);
			try 
			{	
				connection.publishToTopic(dr1,cellType+"L");
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if( rmap.right_out!=null )
		{
			DistributedRegion<Double,Double2D> dr2=new DistributedRegion<Double,Double2D>(rmap.right_mine,rmap.
					right_out,(sm.schedule.getSteps()-1),cellType,DistributedRegion.RIGHT);
			try 
			{		
				connection.publishToTopic(dr2,cellType+"R");
			} catch (Exception e1) {e1.printStackTrace();}
		}		
		//<--
		
		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations
	
		PriorityQueue<DistributedRegion<Double,Double2D>> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, 2);
			while(!q.isEmpty())
			{
				DistributedRegion<Double, Double2D> region=q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) { e1.printStackTrace(); }
		
		for(Region<Double, Double2D> region : updates_cache)
		for(Entry<Double2D> e_m: region)
		{
				RemoteAgent<Double2D> rm=e_m.r;
				((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
		}
		
		this.reset();
	  return true;
	}
	
	/**
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegion<Double,Double2D> box)
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
	public boolean setAgents(RemoteAgent<Double2D> rm,Double2D location)
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
	    	    		 return region.addAgents(new Entry<Double2D>(rm, location));
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
	public DistributedState<Double2D> getState() { return (DistributedState<Double2D>)sm; }
    
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
}