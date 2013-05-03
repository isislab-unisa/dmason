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

 package dmason.sim.field.grid;

import sim.util.Int2D;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.*;
import dmason.util.Util;

/**
 * A Region specialized to be used in a field with Integer coordinates
 */
public class RegionIntegerLB extends Region<Integer,Int2D>
{

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
	public RegionIntegerLB(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy, Integer width, Integer height) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	
		
		this.width = width;
		this.height = height;
		
		if(down_xx == 0)
			super.down_xx = width;
		
		if(down_yy == 0)
			super.down_yy = height;
	}
	
	public static int getHeight() {return height;}
	public static int getWidth() {return width;}
	public int getDX(){return down_xx;}
	public int getDY(){return down_yy;}
	public int getUX(){return upl_xx;}
	public int getUY(){return upl_yy;}
	
	/**
	 * @deprecated
	 *  Static method to create a Region
	 * 	
	 *  */
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
	
	  return new RegionIntegerLB(upl_xx,upl_yy,down_xx,down_yy, MY_HEIGHT, MY_WIDTH);
	}

	@Override
	public Region<Integer,Int2D> clone() 
	{
		RegionIntegerLB r=new RegionIntegerLB(upl_xx, upl_yy, down_xx, down_yy, width, height);
		for(Entry<Int2D> e: this)
		{
			r.add(new Entry(((RemoteAgent<Int2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x ,Integer y)
	{		
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	@Override
	public boolean addAgents(Entry<Int2D> e) 
	{	
		return this.add(e);
	}
}