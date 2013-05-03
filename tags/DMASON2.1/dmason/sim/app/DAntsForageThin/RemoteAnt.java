/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.app.DAntsForageThin;

import java.io.Serializable;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import sim.portrayal.simple.OvalPortrayal2D;

public abstract class RemoteAnt<E> extends OvalPortrayal2D implements Serializable,RemoteAgent<E>{

	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	
	public RemoteAnt() {}
	
	  /**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public RemoteAnt(DistributedState<E> state){
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;		
	}

    //getters and setters
    public E getPos() { return pos; }
    public void setPos(E pos) { this.pos = pos; }
    public String getId() {return id;	}
    public void setId(String id) {this.id = id;}	
}