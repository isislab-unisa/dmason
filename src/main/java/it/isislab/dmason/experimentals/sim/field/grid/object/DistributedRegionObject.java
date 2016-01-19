package it.isislab.dmason.experimentals.sim.field.grid.object;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;

import java.io.Serializable;

public class DistributedRegionObject implements Serializable,
		DistributedRegionInterface {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int WEST=1;
	public static int EAST=2;
	public static int NORTH=3;
	public static int SOUTH=4;
	
	public static int SOUTH_EAST/*CORNER_DIAG_DOWN_RIGHT*/=5;
	public static int SOUTH_WEST/*CORNER_DIAG_DOWN_LEFT*/=6;
	public static int NORTH_EAST/*CORNER_DIAG_UP_RIGHT*/=7;
	public static int NORTH_WEST/*CORNER_DIAG_UP_LEFT*/=8;
	
	public int POSITION;
	
	//the regions swapped
	public RegionObject mine;
	public RegionObject out;
	
	public long step;
	public CellType type;
	
	/**
	 * Constructor of class with parameters:
	 * 
	 * @param mine RegionNumeric into field that send the updates
	 * @param out RegionNumeric external field that send the updates
	 * @param step the number of step in which send the updates
	 * @param type the Celltype of cell that send the updates
	 */
	public DistributedRegionObject(RegionObject mine, RegionObject out,long step,CellType type) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
	}
	
	public DistributedRegionObject(RegionObject mine, RegionObject out,long step,CellType type,int position) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
		this.POSITION=position;
	}
	
	//getters and setters
	public RegionObject getmine() { return mine; }
	public void setmine(RegionObject mine) { this.mine = mine; }
	public RegionObject getout() { return out; }
	public void setout(RegionObject out) { this.out = out;}
	
	@Override
	public long getStep() {	return step;}
	public void setstep(long step) {this.step = step;}
	public CellType gettype() { return type; }
	public void settype(CellType type) { this.type = type; }

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return POSITION;
	}

}
