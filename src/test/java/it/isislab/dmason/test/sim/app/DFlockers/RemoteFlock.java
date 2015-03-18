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
package it.isislab.dmason.test.sim.app.DFlockers;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;

import java.io.Serializable;
/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public abstract class RemoteFlock<E> implements Serializable, RemotePositionedAgent<E> {

	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	
	public RemoteFlock() {}

	/**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public RemoteFlock(DistributedState<E> state){
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;		
	}

    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "ID= "+id+" Pos: "+pos;
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