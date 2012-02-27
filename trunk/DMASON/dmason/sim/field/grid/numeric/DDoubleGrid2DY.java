package dmason.sim.field.grid.numeric;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import sim.engine.SimState;
import sim.util.Int2D;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegionNumeric;
import dmason.sim.field.EntryNum;
import dmason.sim.field.MessageListener;
import dmason.sim.field.RegionNumeric;
import dmason.sim.field.UpdateMap;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.ZoomArrayList;


/**
 *  <h3>This Field extends DoubleGrid2D, to be used in a distributed environment. All the necessary informations for 
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
public class DDoubleGrid2DY extends DDoubleGrid2D {

	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	private ZoomArrayList<EntryNum<Double, Int2D>> tmp_zoom=new ZoomArrayList<EntryNum<Double, Int2D>>();
	/**
	 * It's the name of the specific field
	 */
	private String NAME;
	
	/**
	 * It represents the initial value in every position of the field
	 */
	private double initialValue;
	
	/**
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param num_peers number of the peers
	 * @param name the name that we give at topic for the ((DistributedState)sm).getConnection()
	 * @param initialGridValue is the initial value that we want to set at grid at begin simulation. 
	 */
	public DDoubleGrid2DY(int width, int height,SimState sm,int max_distance,int i,int j,int num_peers, double initialGridValue, String name) {
		
		super(width, height, initialGridValue);
	
		this.NAME = name;
		this.sm=sm;		 
		MAX_DISTANCE=max_distance;
		NUMPEERS=num_peers;	
		cellType = new CellType(i, j);
		this.initialValue = initialGridValue;
		
		updates_cache= new ArrayList<RegionNumeric<Integer,EntryNum<Double,Int2D>>>();

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
		
		// Building the regions
		rmap.left_out=RegionDoubleNumeric.createRegionNumeric(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height),my_width, my_height, width, height);
		if(rmap.left_out!=null) //this condition if is false, means that this peer have not a neighbour left.
			rmap.left_mine=RegionDoubleNumeric.createRegionNumeric(own_x,own_y,own_x + MAX_DISTANCE -1, (own_y+my_height)-1,my_width, my_height, width, height);
		
		rmap.right_out=RegionDoubleNumeric.createRegionNumeric(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, (own_y+my_height)-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
			rmap.right_mine=RegionDoubleNumeric.createRegionNumeric(own_x + my_width -MAX_DISTANCE,own_y,own_x +my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);
				
		if(rmap.left_out == null)
		{
			//peer 0
			myfield=new RegionDoubleNumeric(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);
			
			/**
			try 
			{
				//connection.createTopic(cellType+"R");
				connection.subscribeToTopic(cellType.getNeighbourRight()+"L");
			} catch (Exception e) {e.printStackTrace();}
			*/
		}
		
		if(rmap.right_out == null)
		{
			//peer NUMPEERS-1
			myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-1);
			/**
			try 
			{
				//connection.createTopic(cellType+"L");
				connection.subscribeToTopic(cellType.getNeighbourLeft()+"R");
			} catch (Exception e) {e.printStackTrace();}
			*/
		}
		
		if(rmap.left_out!=null && rmap.right_out!=null)
		{
			myfield=new RegionDoubleNumeric(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);
			/**
			try 
			{
				//connection.createTopic(cellType+"L");
				//connection.createTopic(cellType+"R");
				connection.subscribeToTopic(cellType.getNeighbourLeft()+"R");
				connection.subscribeToTopic(cellType.getNeighbourRight()+"L");
			} catch (Exception e) {e.printStackTrace();}
			*/
		}
		/**
		if(rmap.right_out!=null )
		{	// we create a thread for any side existent
			ut_right=new UpdaterThreadForListener(connection, cellType,this.NUMPEERS,this,neighborhood,cellType.getNeighbourRight()+"L", NAME,updates,listeners);
			ut_right.start();
		}
		
		if(rmap.left_out!=null)
		{
			ut_left=new UpdaterThreadForListener(connection, cellType,this.NUMPEERS,this,neighborhood,cellType.getNeighbourLeft()+"R", NAME,updates,listeners);
			ut_left.start();
		}
		*/
		return true;
	}
	
    /**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	public synchronized boolean  synchro() 
	{		 
		//every value in the myfield region is setted
				for(EntryNum<Double, Int2D> e: myfield)
				{			
					Int2D loc=e.l;
					double d = e.r;
					this.field[loc.getX()][loc.getY()]=d;	
					
					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
						tmp_zoom.add(new EntryNum<Double, Int2D>(d, loc));
				}     
				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				{
					try {
						tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
						connection.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,NAME);
						tmp_zoom=new ZoomArrayList<EntryNum<Double, Int2D>>();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
			
		updateFields(); //update fields with java reflect
		
		updates_cache=new ArrayList<RegionNumeric<Integer,EntryNum<Double,Int2D>>>();
			
		memorizeRegionOut();

			
		//--> publishing the regions to correspondent topics for the neighbors
		if( rmap.left_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr1=new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>(rmap.left_mine,rmap.left_out,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.LEFT);
			try 
			{	
				connection.publishToTopic(dr1,cellType+"L", NAME);
				
			} catch (Exception e1) { e1.printStackTrace(); }
		}
		if( rmap.right_out!=null )
		{
			DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> dr2=new DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>(rmap.right_mine,rmap.right_out,(sm.schedule.getSteps()-1),cellType,DistributedRegionNumeric.RIGHT);
			try 
			{			
				connection.publishToTopic(dr2,cellType+"R", NAME);
				
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
				DistributedRegionNumeric<Integer, EntryNum<Double,Int2D>> region=(DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); }
				
		for(RegionNumeric<Integer,EntryNum<Double,Int2D>> region : updates_cache){
			
			for(EntryNum<Double,Int2D> e_m: region)
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
	public void verifyUpdates(DistributedRegionNumeric<Integer,EntryNum<Double,Int2D>> box)
	{
		RegionNumeric<Integer,EntryNum<Double,Int2D>> r_mine=box.out;
		RegionNumeric<Integer,EntryNum<Double,Int2D>> r_out=box.mine;
		
		for(EntryNum<Double,Int2D> e_m: r_mine)
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
		    	 RegionNumeric<Integer,EntryNum<Double,Int2D>> region=((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);
		    	 
		    	 if(name.contains("out"))
			  	 {
		    		 for(EntryNum<Double,Int2D> e : region){
		    			 
		    			 Int2D pos = new Int2D(e.l.getX(), e.l.getY());
		    			 double d = e.r;
		    			 this.field[pos.getX()][pos.getY()]=d;
		    		 }
			  	  }
		    	  else
		    		  if(name.contains("mine"))
		    		  {
		    			  for(EntryNum<Double,Int2D> e : region){
				    			 
				    			 Int2D pos = new Int2D(e.l.getX(), e.l.getY());
				    			 double d = e.r;
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
	
	public boolean setDistributedObjectLocationForPeer(Int2D location,
			RemoteAgent<Int2D> rm, SimState sm) 
	{
	  return false;
	}
	@Override
	public boolean setDistributedObjectLocation(Int2D location,
			RemoteAgent<Int2D> rm, SimState sm) {
    	return false;
	}
	
	/**
	 * Provide the double value shift logic among the peers
	 * @param d
	 * @param l
	 * @param sm
	 * @return
	 */
	public boolean setDistributedObjectLocation(double d, Int2D l, SimState sm){
		
		if(myfield.isMine(l.getX(), l.getY()))
    	{    		
    		return myfield.addEntryNum(new EntryNum<Double,Int2D>(d, l));
    	}
    	else
    		if(setValue(d, l))
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
	public boolean setValue(double value, Int2D l){
		
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
	       			RegionNumeric<Integer,EntryNum<Double,Int2D>> region = ((RegionNumeric<Integer,EntryNum<Double,Int2D>>)returnValue);
	       			if(region.isMine(l.getX(),l.getY()))
	       			{   	  
						if(name.contains("mine")){
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(new EntryNum<Double, Int2D>(value, l));
	       				}
	    	    		return region.addEntryNum(new EntryNum<Double,Int2D>(value, l));
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
		connection=(ConnectionNFieldsWithActiveMQAPI)con;
		
	}
}
