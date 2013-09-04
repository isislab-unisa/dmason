package dmason.sim.app.DStudents;

import java.io.Serializable;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.network.RemoteNetwork;

public abstract class RemoteStudent<E> extends RemoteNetwork implements Serializable, RemoteAgent<E> {

	private static final long serialVersionUID = 1L;
	
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 

	public RemoteStudent() {}	
	
	/**
	 * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
	public RemoteStudent(DistributedState<E> state){
		int i=state.nextId();
		this.id=state.getType().toString()+"-"+i;		
	}

    //getters and setters
    public E getPos() { return pos; }
    public void setPos(E pos) { this.pos = pos; }
    public String getId() {return id;	}
    public void setId(String id) {this.id = id;}	
	
}
