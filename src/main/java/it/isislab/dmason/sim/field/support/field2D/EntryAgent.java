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

package it.isislab.dmason.sim.field.support.field2D;

import it.isislab.dmason.sim.engine.RemotePositionedAgent;

import java.io.Serializable;

/**
 * A wrapper class for a Remote Agent and corresponding location.
 * @param <E> the type of location
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class EntryAgent<E> implements Serializable, Cloneable
{	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  RemotePositionedAgent<E> r;
	public  E l;
	
	
	
	public EntryAgent(final RemotePositionedAgent<E> r,final E l)
	{
		this.r=r;
		this.l=l;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		EntryAgent other = (EntryAgent) obj;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
	}

	
}