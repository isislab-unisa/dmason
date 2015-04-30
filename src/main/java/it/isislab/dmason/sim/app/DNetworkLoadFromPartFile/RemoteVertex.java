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
package it.isislab.dmason.sim.app.DNetworkLoadFromPartFile;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;

import java.io.Serializable;

import sim.portrayal.SimplePortrayal2D;

public abstract class RemoteVertex<Double2D> extends SimplePortrayal2D implements Serializable, RemoteUnpositionedAgent<Double2D> {

	private static final long serialVersionUID = 1L;
	public Double2D pos;     // Location of agents  
	public String label; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	public String id;
	public RemoteVertex() {}

	/**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public RemoteVertex(DistributedState<Double2D> state,String label){
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;	
			this.label=label;
	}

    //getters and setters
    @Override
	public Double2D getPos() { return pos; }
    @Override
	public void setPos(Double2D pos) { this.pos = pos; }
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return this.label;
	}
	@Override
	public void setLabel(String label) {
		this.label=label;
		
	}
 
}
