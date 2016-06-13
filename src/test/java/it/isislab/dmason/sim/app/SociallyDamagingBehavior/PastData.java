/**
  Copyright 2016 Universita' degli Studi di Salerno

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
package it.isislab.dmason.sim.app.SociallyDamagingBehavior;
import java.io.Serializable;

public class PastData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public double numNeighPunished = 0;
	public double numNeighDamager = 0;
	public double dna = 0;
	
	public PastData(double numNeighPunished, double numNeighDamager,
			double dna) {
		super();
		this.numNeighPunished = numNeighPunished;
		this.numNeighDamager = numNeighDamager;
		this.dna = dna;
	}

	public double getNumNeighPunished() {
		return numNeighPunished;
	}

	public void setNumNeighPunished(double numNeighPunished) {
		this.numNeighPunished = numNeighPunished;
	}

	public double getNumNeighDamager() {
		return numNeighDamager;
	}

	public void setNumNeighDamager(double numNeighDamager) {
		this.numNeighDamager = numNeighDamager;
	}

	public double getDna() {
		return dna;
	}

	public void setDna(double dna) {
		this.dna = dna;
	}
}