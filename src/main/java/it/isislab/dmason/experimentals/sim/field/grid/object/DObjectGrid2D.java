package it.isislab.dmason.experimentals.sim.field.grid.object;

import java.util.ArrayList;
import it.isislab.dmason.experimentals.sim.field.grid.object.EntryObject;
import it.isislab.dmason.experimentals.sim.field.grid.object.RegionMapObject;
import it.isislab.dmason.experimentals.sim.field.grid.object.RegionObject;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import sim.engine.SimState;
import sim.field.grid.ObjectGrid2D;
import sim.util.Int2D;

public abstract class DObjectGrid2D extends ObjectGrid2D implements DistributedField2D<Int2D>{
	

	/**
	 * It's the number of peers will perform the simulation
	 */
	//public int NUMPEERS;
	
	public int rows;

	public int columns;
	/**
	 * x coordinate of north-west corner
	 */
	public int own_x;
	/**
	 * y coordinate of north-west corner
	 */
	public int own_y;
	/**
	 * The effective width of the peer
	 */
	public int my_width;
	/**
	 * The effective height of the peer
	 */
	public int my_height;
	/**
	 * It is the region represents the central field of the peer 
	 */
	public RegionObject myfield;
	/**
	 * It represents all the border Regions of the peer
	 */
	
	public ArrayList<String> neighborhood=new ArrayList<String>();
	
	public RegionMapObject rmap=new RegionMapObject();
	/**
	 * It contains all the region out of the peer
	 */
	public ArrayList<RegionObject> updates_cache;
	/**
	 * It's the distance used for the updates
	 */
	//public int AOI;
	public int AOI;
	/**
	 * It represents the cell
	 */
	public CellType cellType;
	/**
	 * It contains all the updates from the border regions
	 */
	public UpdateMap<Integer,EntryObject<Int2D>> updates=new UpdateMap<Integer,EntryObject<Int2D>>();
    public SimState sm ;
    /**
     * It contains the neighborhood of the peer
     */

	private boolean isToroidal;
	
	public boolean isToroidal()
	{
		return isToroidal;
	}
	
	public void setToroidal(boolean isToroidal)
	{
		this.isToroidal=isToroidal;
	}
	
	public DObjectGrid2D(int width, int height, Object initialGridValue) {
		super(width, height, initialGridValue);
	}
	
	public DObjectGrid2D(int width, int height) {
		super(width, height);
	}
	
	
}
