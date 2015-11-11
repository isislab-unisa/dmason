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

		RegionDoubleNumericLB.width = width;
		RegionDoubleNumericLB.height = height;

		if(down_xx == 0)
			super.down_xx = width;

		if(down_yy == 0)
			super.down_yy = height;	
	}
	
	@Override
	public RegionNumeric<Integer, EntryNum<Double, Int2D>> clone() 
	{
		RegionDoubleNumericLB r=new RegionDoubleNumericLB(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(EntryNum<Double, Int2D> e: this.values())
		{
			Double d = e.r;
			r.put(e.l.toString(),new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {

		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	@Override
	public boolean addEntryNum(EntryNum<Double, Int2D> e) 
	{
		if(e == null || e.l == null || e.r == null) return false;
		if(this.containsKey(e.l.toString()) && this.get(e.l.toString()).equals(e) )
		   return true;
		
		return this.put(e.l.toString(),e)!=null?true:false;	}
}
