package dmason.sim.field.grid.numeric;

import sim.util.Int2D;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionNumeric;
import dmason.util.Util;

/**
 * A Region specialized to be used in a field with Integer coordinates and double value
 */
public class RegionDoubleNumeric extends RegionNumeric<Integer, EntryNum<Double, Int2D>>{

	public RegionDoubleNumeric(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	
	}
	
	/**
	 * Static method to create a Region.
	 * @return null if the parameters are not 
	 */
	public static RegionNumeric<Integer, EntryNum<Double, Int2D>> createRegionNumeric(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy, Integer MY_WIDTH, Integer MY_HEIGHT, Integer WIDTH,
			Integer HEIGHT) {
		if(upl_xx < 0 || upl_yy < 0)
		{				
				return null;
		}
		
		if( upl_xx>= WIDTH || upl_yy >= HEIGHT)
		{
			return null;
		}
	
	  return new RegionDoubleNumeric(upl_xx,upl_yy,down_xx,down_yy);
	}
	
	@Override
	public RegionNumeric<Integer, EntryNum<Double, Int2D>> clone() 
	{
		RegionDoubleNumeric r=new RegionDoubleNumeric(upl_xx, upl_yy, down_xx, down_yy);
		for(EntryNum<Double, Int2D> e: this)
		{
			Double d = e.r;
			r.add(new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {

		return (x >= upl_xx) && (x <= down_xx) && (y >= upl_yy) && (y <= down_yy);
	}

	public boolean addEntryNum(EntryNum<Double, Int2D> e) 
	{	
		return this.add(e);
	}
}
