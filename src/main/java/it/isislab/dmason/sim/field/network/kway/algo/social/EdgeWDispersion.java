/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.field.network.kway.algo.social;

import it.isislab.dmason.sim.field.network.kway.graph.Edge;

public class EdgeWDispersion extends Edge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected double dispersion;
	protected int dv;
	protected int embedness;

	public int getDv() {
		return dv;
	}

	public void setDv(int dv) {
		this.dv = dv;
	}

	public int getEmbedness() {
		return embedness;
	}

	public void setEmbedness(int embedeness) {
		this.embedness = embedeness;
	}

	public double getDispersion() {
		return dispersion;
	}

	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}
	

}
