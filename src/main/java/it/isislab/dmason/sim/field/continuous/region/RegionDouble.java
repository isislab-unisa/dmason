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

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.support.field2D.Entry;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.Util;
import sim.util.Double2D;

/**
 * A Region specialized to be used in a field with Double coordinates
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class RegionDouble extends Region<Double,Double2D>
{
	public static double width,height;
	public RegionDouble(Double upl_xx, Double upl_yy, Double down_xx,
			Double down_yy,double width,double height) 
	{		
		super(upl_xx, upl_yy, down_xx, down_yy);
		RegionDouble.width=width;
		RegionDouble.height=height;
		if(down_xx==0.0)
			super.down_xx=width;
		if(down_yy==0.0)
			super.down_yy=height;
	}



	@Override
	public Region<Double, Double2D> clone() 
	{
		RegionDouble r=new RegionDouble(upl_xx, upl_yy, down_xx, down_yy,width,height);
		for(Entry<Double2D> e: this)
		{
			r.add(new Entry(((RemotePositionedAgent<Double2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

	@Override
	public boolean isMine(Double x, Double y) 
	{
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	@Override
	public boolean addAgents(Entry<Double2D> e)  
	{
		if(e == null || e.l == null || e.r == null) return false;
		if(this.contains(e)) return true;
		
		removeAgents(e.r);
		return this.add(e);
	}	
	
	private boolean removeAgents( RemotePositionedAgent<Double2D> r){
		
		Entry<Double2D> toRemove = null;
		
		for(Entry e: this)
			if(e.r.getId().equals(r.getId())){
				toRemove = e;
				break;
			}
		
		return toRemove!=null?this.remove(toRemove):false;
	}
	
}