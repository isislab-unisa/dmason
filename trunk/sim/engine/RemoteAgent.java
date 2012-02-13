package dmason.sim.engine;

import java.io.Serializable;
import sim.engine.Steppable;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 * Abstract Class for Remote Steppable Objects that are aware 
 * about their position in 2D field. It contains also a unique identifier.
 */

public abstract class RemoteAgent<E> extends OvalPortrayal2D implements Steppable,Serializable
{
	public E pos;
	public String id;
	
    public  RemoteAgent(){}
	
    /**
	 * @param state The Distributed State of simulation
	 */
    public RemoteAgent(DistributedState<E> state)
	{
	
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;		
	}
	
	//getters and setters
	public E getPos() { return pos; }
	public void setPos(E pos) { this.pos = pos; }
	public String getId() {return id;	}
	public void setId(String id) {this.id = id;}
}