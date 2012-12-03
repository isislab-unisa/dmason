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

package dmason.batch.data;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Batch")
public class Batch 
{
	
	private String simulationName;
	private ArrayList<Param> simulationParams;
	private ArrayList<Param> generalParams;
	private int neededWorkers;
	
	
	public Batch(ArrayList<Param> simulationParams,
			ArrayList<Param> generalParams, String simName) {
		super();
		this.simulationParams = simulationParams;
		this.generalParams = generalParams;
		this.simulationName = simName;
	}

	
	public int getNeededWorkers() {
		return neededWorkers;
	}


	public void setNeededWorkers(int neededWorkers) {
		this.neededWorkers = neededWorkers;
	}


	public ArrayList<Param> getSimulationParams() {
		return simulationParams;
	}


	public void setSimulationParams(ArrayList<Param> simulationParams) {
		this.simulationParams = simulationParams;
	}


	public ArrayList<Param> getGeneralParams() {
		return generalParams;
	}

	public void setGeneralParams(ArrayList<Param> generalParams) {
		this.generalParams = generalParams;
	}

	public String getSimulationName() {
		return simulationName;
	}

	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
	}
	
	
	
	
	

}
