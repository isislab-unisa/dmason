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

 package it.isislab.dmason.sim.field.continuous.region;

import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.Util;
import sim.util.Double2D;

/**
 * A Region specialized to be used in a field with Double coordinates
 */
public class RegionDoubleLB extends Region<Double,Double2D>
{
	public static double width,height;
	
	
	/**
	 * Constructor of class,it use the costructor of superclass and adds two parameters : width and height
	 * 
	 * 
	 * @param upl_xx        x of left upper corner
	 * @param upl_yy        y of left upper corner
	 * @param down_xx       x of right down corner 
	 * @param down_yy       y of right down corner
	 * @param width         width of region
	 * @param height        height of region
	 */
	public RegionDoubleLB(Double upl_xx, Double upl_yy, Double down_xx,
			Double down_yy,double width,double height) 
	{		
		super(upl_xx, upl_yy, down_xx, down_yy);
		RegionDoubleLB.width=width;
		RegionDoubleLB.height=height;
		
		if(down_xx==0.0)
			super.down_xx=width;
		if(down_yy==0.0)
			super.down_yy=height;
	}


	
	@Override
	public Region<Double, Double2D> clone() 
	{
		RegionDoubleLB r=new RegionDoubleLB(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(it.isislab.dmason.sim.field.support.field2D.EntryAgent<Double2D> e: this.values())
		{
			r.put(e.r.getId(),new it.isislab.dmason.sim.field.support.field2D.EntryAgent(((RemotePositionedAgent<Double2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

     @Override
	public boolean isMine(Double x, Double y) 
	{
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

     @Override
	public boolean addAgents(it.isislab.dmason.sim.field.support.field2D.EntryAgent<Double2D> e) 
	{
    	 if(e == null || e.l == null || e.r == null) return false;
 		
 		if(this.containsKey(e.r.getId()) && this.get(e.r.getId()).equals(e)) return true;
 			
 		this.put(e.r.getId(),e);
 		return true;
	}	
     
     
}