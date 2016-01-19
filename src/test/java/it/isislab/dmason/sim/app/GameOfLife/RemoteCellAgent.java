package it.isislab.dmason.sim.app.GameOfLife;

import java.io.Serializable;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;

public abstract class RemoteCellAgent<E> implements Serializable, RemotePositionedAgent<E> {

	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field
	
	
	public RemoteCellAgent() {}
	
	public RemoteCellAgent(DistributedState<E> state, double diameter) {
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
	    
}
