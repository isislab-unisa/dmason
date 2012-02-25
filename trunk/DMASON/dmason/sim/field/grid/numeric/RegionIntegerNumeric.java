package dmason.sim.field.grid.numeric;

import sim.util.Int2D;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionNumeric;

/**
 * A Region specialized to be used in a field with Integer coordinates
 */
public class RegionIntegerNumeric extends RegionNumeric<Integer, EntryNum<Integer, Int2D>>{

	public RegionIntegerNumeric(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	
	}
	
	/**
	 * Static method to create a Region.
	 * @return null if the parameters are not 
	 */
	public static RegionNumeric<Integer, EntryNum<Integer, Int2D>> createRegionNumeric(Integer upl_xx, Integer upl_yy, Integer down_xx,
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
	
	  return new RegionIntegerNumeric(upl_xx,upl_yy,down_xx,down_yy);
	}
	
	@Override
	public RegionNumeric<Integer, EntryNum<Integer, Int2D>> clone() 
	{
		RegionIntegerNumeric r=new RegionIntegerNumeric(upl_xx, upl_yy, down_xx, down_yy);
		for(EntryNum<Integer, Int2D> e: this)
		{
			int d = e.r;
			r.add(new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {

		return (x >= upl_xx) && (x <= down_xx) && (y >= upl_yy) && (y <= down_yy);
	}

	public boolean addEntryNum(EntryNum<Integer, Int2D> e) 
	{	
		return this.add(e);
	}
}