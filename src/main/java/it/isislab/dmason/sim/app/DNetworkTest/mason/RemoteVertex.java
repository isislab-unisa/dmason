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
package it.isislab.dmason.sim.app.DNetworkTest.mason;

import java.io.Serializable;

import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;

public abstract class RemoteVertex<Double2D> extends SimplePortrayal2D implements Serializable {

	private static final long serialVersionUID = 1L;
	public Double2D pos;     // Location of agents  
	public String label; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	public String id;
	public int nextID=0;
	public RemoteVertex() {}

	/**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public RemoteVertex(SimState state,String label){
			int i=nextID;
			nextID++;
			this.id=state.toString()+"-"+i;	
			this.label=label;
	}

    //getters and setters
    public Double2D getPos() { return pos; }
    public void setPos(Double2D pos) { this.pos = pos; }
	public String getLabel() {
		// TODO Auto-generated method stub
		return this.label;
	}
	public void setLabel(String label) {
		this.label=label;
		
	}
 
}
