 package dmason.sim.field.grid;

import sim.util.Int2D;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.*;
import dmason.util.Util;

/**
 * A Region specialized to be used in a field with Integer coordinates
 */
public class RegionInteger extends Region<Integer,Int2D>
{
	public RegionInteger(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	
	}

	/**
	 * Static method to create a Region.
	 * @return null if the parameters are not 
	 */
	public static Region<Integer,Int2D> createRegion(Integer upl_xx, Integer upl_yy, Integer down_xx,
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
	
	  return new RegionInteger(upl_xx,upl_yy,down_xx,down_yy);
	}

	public Region<Integer,Int2D> clone() 
	{
		RegionInteger r=new RegionInteger(upl_xx, upl_yy, down_xx, down_yy);
		for(Entry<Int2D> e: this)
		{
			r.add(new Entry(((RemoteAgent<Int2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

	public boolean isMine(Integer x ,Integer y)
	{		
		return (x >= upl_xx) && (x <= down_xx) && (y >= upl_yy) && (y <= down_yy);
	}

	public boolean addAgents(Entry<Int2D> e) 
	{	
		return this.add(e);
	}
}