/**
  Copyright 2016 Universita' degli Studi di Salerno

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
package it.isislab.dmason.sim.app.SIRStateReflection;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;

import java.io.Serializable;

import sim.portrayal.simple.OvalPortrayal2D;

public abstract class RemoteHuman<E> extends OvalPortrayal2D implements DHumanState, Serializable, RemotePositionedAgent<E> {

	private static final long serialVersionUID = 1L;

	public E pos;
	public String id;



	public RemoteHuman() {}



	public RemoteHuman(DistributedState<E> state) {
		int i = state.nextId();
		this.id = state.getType().toString()+"-"+i;
	}


	public E getPos() {
		return pos;
	}

	public void setPos(E pos) {
		this.pos = pos;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteHuman other = (RemoteHuman) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}
}
