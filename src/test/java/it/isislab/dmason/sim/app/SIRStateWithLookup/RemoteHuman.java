package it.isislab.dmason.sim.app.SIRStateWithLookup;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;

import java.io.Serializable;

import sim.portrayal.simple.OvalPortrayal2D;

public abstract class RemoteHuman<E> extends OvalPortrayal2D implements Serializable, RemotePositionedAgent<E> {

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
