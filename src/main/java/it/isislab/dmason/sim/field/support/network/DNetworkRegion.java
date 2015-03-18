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

package it.isislab.dmason.sim.field.support.network;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.network.region.RegionNetwork;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;

import java.io.Serializable;
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
/**
 * 
 * @author Ada Mancuso
 * @author Francesco Milone
 * @author Carmine Spagnuolo
 *
 */
public class DNetworkRegion<E,F> implements Serializable, DistributedRegionInterface{

	private static final long serialVersionUID = 6523917802601556040L;
	public RegionNetwork out;
	public long step;
	public CellType type;
	public int communityID;

	public DNetworkRegion(RegionNetwork out,long step,CellType type, int communityID) 
	{
		super();
		this.out = out.clone();
		this.step = step;
		this.type = type;
	}

	@Override
	public long getStep() {
		return step;
	}

	@Override
	public String toString() {
		return step + " "+type+" "+out.toString();
	}

	//the field network is non-spatial field
	@Override
	public int getPosition() {
		return communityID;
	}
}
