/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.field.grid.numeric;

import sim.util.Int2D;
import dmason.sim.field.EntryNum;
import dmason.sim.field.RegionNumeric;

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
