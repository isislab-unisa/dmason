package dmason.sim.field.grid;

import java.net.Proxy;
import java.util.ArrayList;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.Region;
import dmason.sim.field.RegionMap;
import dmason.sim.field.UpdateMap;
import dmason.util.connection.ConnectionWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.connection.ProxyConnection;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

/**
 *  Abstract class for Distributed Sparse Grid 2D
 *
 *  <h3>This Field extends SparseGrid2D, to be used in a distributed environment. 
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

public abstract class DSparseGrid2D extends SparseGrid2D implements DistributedField<Int2D>
{
	public int NUMPEERS;
	public int own_x;	//x coordinate of north-west corner
	public int own_y;	//y coordinate of north-west corner
	public int my_width;
	public int my_height;
	public RegionInteger myfield;
	public RegionMap<Integer,Int2D> rmap=new RegionMap<Integer,Int2D>();
	public ArrayList<Region<Integer, Int2D>> updates_cache;
	public int MAX_DISTANCE;
	public CellType cellType;
	public UpdateMap<Integer,Int2D> updates=new UpdateMap<Integer,Int2D>();
    public SimState sm ;
    public ConnectionWithJMS connection=new ConnectionWithActiveMQAPI();
	public ArrayList<String> neighborhood=new ArrayList<String>();
	public boolean gui = true;
	
	public DSparseGrid2D(int width, int height) 
	{
		super(width, height);
	}
}