/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field.grid.numeric.region;

import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import sim.util.Int2D;

/**
 * A Region specialized to be used in a field with Integer coordinates
 */
public class RegionIntegerNumericLB extends RegionNumeric<Integer, EntryNum<Integer, Int2D>>{


	private static final long serialVersionUID = 1L;
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
	public RegionIntegerNumericLB(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy,Integer width, Integer height) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	

		RegionIntegerNumericLB.width = width;
		RegionIntegerNumericLB.height = height;

		if(down_xx == 0)
			super.down_xx = width;

		if(down_yy == 0)
			super.down_yy = height;	
	}

	/**
	 * @deprecated
	 * Static method to create a Region.
	 *  
	 */
	@Deprecated
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

		return new RegionIntegerNumericLB(upl_xx,upl_yy,down_xx,down_yy,MY_WIDTH,MY_HEIGHT);
	}

	@Override
	public RegionNumeric<Integer, EntryNum<Integer, Int2D>> clone() 
	{
		RegionIntegerNumericLB r=new RegionIntegerNumericLB(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(EntryNum<Integer, Int2D> e: this)
		{
			int d = e.r;
			r.add(new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {

		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	@Override
	public boolean addEntryNum(EntryNum<Integer, Int2D> e) 
	{	
		return this.add(e);
	}
}
