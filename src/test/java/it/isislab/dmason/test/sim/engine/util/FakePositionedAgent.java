package it.isislab.dmason.test.sim.engine.util;

import java.io.Serializable;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import sim.engine.SimState;

public class FakePositionedAgent<E> implements RemotePositionedAgent<E>, Serializable {

	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	
	public FakePositionedAgent(DistributedState<E> state, E position){
		int i=state.nextId();
		this.id=state.getType().toString()+"-"+i;		
		pos = position;
	}

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
	}

	 //getters and setters
    @Override
	public E getPos() { return pos; }
    @Override
	public void setPos(E pos) { this.pos = pos; }
    @Override
	public String getId() {return id;	}
    @Override
	public void setId(String id) {this.id = id;}	

}
