package dmason.sim.field;

import java.io.Serializable;

/**
 * A wrapper class for the regions those must be swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 *
 */
public class DistributedRegionNumeric<E,F> implements Serializable, DistributedRegionInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int LEFT=1;
	public static int RIGHT=2;
	public static int UP=3;
	public static int DOWN=4;
	
	public static int CORNER_DIAG_DOWN_RIGHT=5;
	public static int CORNER_DIAG_DOWN_LEFT=6;
	public static int CORNER_DIAG_UP_RIGHT=7;
	public static int CORNER_DIAG_UP_LEFT=8;
	
	public int POSITION;
	
	//the regions swapped
	public RegionNumeric<E,F> mine;
	public RegionNumeric<E,F> out;
	
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
	public DistributedRegionNumeric(RegionNumeric<E,F> mine, RegionNumeric<E,F> out,long step,CellType type) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
	}
	
	public DistributedRegionNumeric(RegionNumeric<E,F> mine, RegionNumeric<E,F> out,long step,CellType type,int position) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
		this.POSITION=position;
	}
	
	//getters and setters
	public RegionNumeric<E,F> getmine() { return mine; }
	public void setmine(RegionNumeric<E,F> mine) { this.mine = mine; }
	public RegionNumeric<E,F> getout() { return out; }
	public void setout(RegionNumeric<E,F> out) { this.out = out;}
	
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
