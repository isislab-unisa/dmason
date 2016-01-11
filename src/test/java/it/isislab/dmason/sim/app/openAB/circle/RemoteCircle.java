package it.isislab.dmason.sim.app.openAB.circle;

import java.io.Serializable;


import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import sim.portrayal.simple.OvalPortrayal2D;

public abstract class RemoteCircle<E> extends OvalPortrayal2D implements Serializable, RemotePositionedAgent<E> {

	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field
	
	
	public RemoteCircle() {}
	
	public RemoteCircle(DistributedState<E> state, double diameter) {
		super(diameter);
		int i=state.nextId();
		this.id=state.getType().toString()+"-"+i;
	}
	

	 @Override
		public E getPos() { return pos; }
	    @Override
		public void setPos(E pos) { this.pos = pos; }
	    @Override
		public String getId() {return id;	}
	    @Override
		public void setId(String id) {this.id = id;}	
	    
	    @Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RemoteCircle other = (RemoteCircle) obj;
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
