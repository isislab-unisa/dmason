package dmason.sim.field.continuous;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.Region;
import dmason.sim.field.RegionMap;
import dmason.sim.field.UpdateMap;
import dmason.util.connection.ConnectionWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.connection.ProxyConnection;

/**
 *  Abstract class for Distributed Continuous 2D
 *  
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. 
 *  All the necessary informations for the distribution of simulation are wrapped in this class.</h3>
 *  It represents the field managed by a single peer.
 *  It adds to the superclass these following informations:
 *  <ul>
 *  <li> Upper Left corner's coordinates</li>
 *  <li> Width and Height of the Field of expertise</li>
 *  <li> The logic of synchronization among the peers, step by step</li>
 *  <li> The number of peers involved in the simulation</li>
 *  <li> The maximum distance of shift for the agents</li>
 *  <li> An arraylist for the neighborhoods topics</li>
 *  <li> A Region that represents the field to simulate</li>
 *  <li> A RegionMap that represents all the border Regions</li>
 *  <li> The simstate of sumulation</li>
 *  <li> An UpdateMap for all the updates</li>
 *  <li> A Connection object for an abstract connection</li>
 *  <li> A CellType object for differentiate the field</li>
 *  </ul>
 */
public abstract class DContinuous2D extends Continuous2D  implements DistributedField<Double2D>
{
	// --> instance variables
	public CellType cellType;
	public int NUMPEERS;	//the number od peers involved in the simulation
	public double own_x;	//x coordinate of north-west corner
	public double own_y;	//y coordinate of north-west corner
	public double my_width;
	public double my_height;
	public RegionDouble myfield;
	public RegionMap<Double,Double2D> rmap=new RegionMap<Double,Double2D>();
	public ArrayList<Region<Double, Double2D>> updates_cache;
	public int MAX_DISTANCE;
	public UpdateMap<Double,Double2D> updates=new UpdateMap<Double,Double2D>();
    public SimState sm ;
    public ConnectionWithJMS connection=new ConnectionWithActiveMQAPI();
	public ArrayList<String> neighborhood=new ArrayList<String>();
	public ContinuousPortrayal2D p;
	public boolean gui=true;
	// <-- instance variables
		
	public DContinuous2D(double discretization, double width, double height) 
	{
		super(discretization, width, height);
	}
	
	public  void attachPortrayal(ContinuousPortrayal2D p){this.p=p;}
	public  ContinuousPortrayal2D getAttachedPortrayal(){return p;}
	public abstract boolean setPortrayalForObject(Object o);	
}