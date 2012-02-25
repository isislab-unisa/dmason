package dmason.sim.field.grid.numeric;

import java.util.ArrayList;

import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.util.Int2D;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionMapNumeric;
import dmason.sim.field.RegionNumeric;
import dmason.sim.field.UpdateMap;


/**
 *  Abstract class for Distributed Double Grid 2D
 *
 *  <h3>This Field extends DoubleGrid2D, to be used in a distributed environment. 
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
public abstract class DDoubleGrid2D extends DoubleGrid2D implements DistributedField<Int2D>{

	/**
	 * The number of peers that will perform the simulation
	 */
	public int NUMPEERS;
	/**
	 * x coordinate of north-west corner
	 */
	public int own_x;
	/**
	 * y coordinate of north-west corner
	 */
	public int own_y;
	/**
	 * effective width of the peer
	 */
	public int my_width;
	/**
	 * effective height of the peer
	 */
	public int my_height;
	/**
	 * It's the region represents the central field of the peer 
	 */
	public RegionDoubleNumeric myfield;
	/**
	 * It represents all the border Regions of the peer
	 */
	public RegionMapNumeric<Integer,EntryNum<Double,Int2D>> rmap=new RegionMapNumeric<Integer,EntryNum<Double,Int2D>>();
	/**
	 * It contains all the region out of the peer
	 */
	public ArrayList<RegionNumeric<Integer, EntryNum<Double,Int2D>>> updates_cache;
	/**
	 * It's the distance used for the updates in the simulation
	 */
	public int MAX_DISTANCE;
	/**
	 * It represents the cell
	 */
	public CellType cellType;
	/**
	 * It contains all the updates from the border regions
	 */
	public UpdateMap<Integer,EntryNum<Double,Int2D>> updates=new UpdateMap<Integer,EntryNum<Double,Int2D>>();
    public SimState sm ;
    /**
     * It contains the neighborhood of the peer
     */
	public ArrayList<String> neighborhood=new ArrayList<String>();

	
	public DDoubleGrid2D(int width, int height, double initialGridValue) {
		
		super(width, height);
	}
	
	/**
	 * Provide the double value shift logic among the peers
	 * @param d the double value we want to set in a specific location
	 * @param l is the location where we want to set the value
	 * @param sm is the simstate
	 * @return true if all will go in the right way, false otherwise
	 */
	public abstract boolean setDistributedObjectLocation(double d, Int2D l, SimState sm);
}
