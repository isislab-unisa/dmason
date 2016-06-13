/**
 * Copyright 2016 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.field.network.region;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;

import java.util.HashMap;

/**
 * 
 * @author Ada Mancuso
 * @author Francesco Milone
 * @author Carmine Spagnuolo
 * 
 * A Region specialized to be used in a DNetwork field
 */
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class RegionNetwork extends HashMap<String,RemoteUnpositionedAgent> 
{
	private static final long serialVersionUID = 3202864390775335689L;
	public Integer community;
	public RegionNetwork(Integer labelCommunity) 
	{		
		community=labelCommunity;
	}

	@Override
	public RegionNetwork clone() 
	{
		RegionNetwork r=new RegionNetwork(community);

		for(RemoteUnpositionedAgent e: this.values())
		{
			r.put(e.getId(), e);
		}
		return r;
	}

	public boolean add(RemoteUnpositionedAgent rm) {
		return this.put(rm.getId(),rm)==null;
	}
}