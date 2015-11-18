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
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
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
	@Deprecated
	public static RegionNumeric<Integer, EntryNum<Integer, Int2D>> createRegionNumeric(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy, Integer MY_WIDTH, Integer MY_HEIGHT, Integer WIDTH,
			Integer HEIGHT) 
	{
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
		for(EntryNum<Integer, Int2D> e: this.values())
		{
			int d = e.r;
			r.put(e.l.toString(),new EntryNum(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x, Integer y) {
		
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
		//return (x >= upl_xx) && (x <= down_xx) && (y >= upl_yy) && (y <= down_yy);
	}

	@Override
	public boolean addEntryNum(EntryNum<Integer, Int2D> e) 
	{	
		if(e == null || e.l == null || e.r == null) return false;
		if(this.containsKey(e.l.toString()) && this.get(e.l.toString()).equals(e) )
		   return true;
		this.put(e.l.toString(),e);
		return true;
	}
}
