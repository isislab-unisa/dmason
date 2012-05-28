package dmason.sim.field;

import java.io.Serializable;

/**
 * A wrapper class for the regions those must be swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 *
 */
public class DistributedRegion<E,F> implements Serializable, DistributedRegionInterface
{
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
	public Region<E,F> mine;
	public Region<E,F> out;
	
	public long step;
	public CellType type;
	
	public DistributedRegion(Region<E,F> mine, Region<E,F> out,long step,CellType type,int position) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
		this.POSITION=position;
	}
	
	//getters and setters
	public Region<E,F> getmine() { return mine; }
	public void setmine(Region<E,F> mine) { this.mine = mine; }
	public Region<E,F> getout() { return out; }
	public void setout(Region<E,F> out) { this.out = out;}
	
	public long getstep() {	return step;}
	public void setstep(long step) {this.step = step;}
	public CellType gettype() { return type; }
	public void settype(CellType type) { this.type = type; }

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return POSITION;
	}
}