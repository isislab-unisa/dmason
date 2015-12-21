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
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RegionDouble(Double upl_xx, Double upl_yy, Double down_xx,Double down_yy) 
	{		
		super(upl_xx, upl_yy, down_xx, down_yy);
	}



	@Override
	public Region<Double, Double2D> clone() 
	{
		RegionDouble r=new RegionDouble(upl_xx, upl_yy, down_xx, down_yy);
		for(String agent_id : this.keySet())
		{
			EntryAgent<Double2D> e = this.get(agent_id);
			r.put(e.r.getId(), new EntryAgent<Double2D>(((RemotePositionedAgent<Double2D>)(Util.clone(e.r))),e.l));
		}
		return r;
	}

	@Override
	public boolean isMine(Double x, Double y) 
	{
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}

	@Override
	public boolean addAgents(EntryAgent<Double2D> e)  
	{
		if(e == null || e.l == null || e.r == null) return false;

		if(this.containsKey(e.r.getId()) && this.get(e.r.getId()).equals(e)) return true;

		this.put(e.r.getId(),e);
		return true;
	}	

}