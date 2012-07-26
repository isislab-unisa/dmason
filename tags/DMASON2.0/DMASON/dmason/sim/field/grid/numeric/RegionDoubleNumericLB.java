package dmason.sim.field.grid.numeric;

import sim.util.Int2D;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionNumeric;
import dmason.util.Util;

/**
 * A Region specialized to be used in a field with Integer coordinates and double value
 */
public class RegionDoubleNumericLB extends RegionNumeric<Integer, EntryNum<Double, Int2D>>{

	private static int height;
	private static int width;
	
	/**
	 * Constructor of class,it use the costructor of superclass and adds a width and a height
	 * 
	 * 
	 * @param upl_xx        x of left upper corner
	 * @param upl_yy        y of left upper corner
	 * @param down_xx       x of right down corner 
	 * @param down_yy       y of right down corner
	 * @param width         width of region
	 * @param height        height of region
	 */
	public RegionDoubleNumericLB(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy,int width, int height) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	

		this.width = width;
		this.height = height;

		if(down_xx == 0)
			super.down_xx = width;

		if(down_yy == 0)
			super.down_yy = height;	
	}
	
	@Override
	public RegionNumeric<Integer, EntryNum<Double, Int2D>> clone() 
	{
		RegionDoubleNumericLB r=new RegionDoubleNumericLB(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(EntryNum<Double, Int2D> e: this)
		{
			Double d = e.r;
			r.add(new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {

		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	public boolean addEntryNum(EntryNum<Double, Int2D> e) 
	{	
		return this.add(e);
	}
}
