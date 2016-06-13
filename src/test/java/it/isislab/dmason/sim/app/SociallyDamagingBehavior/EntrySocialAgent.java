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

public class EntrySocialAgent <D, H> implements Serializable{

	private D fitSum;
	private H h;
	
	public D getFitSum() {
		return fitSum;
	}

	public void setFitSum(D fitSum) {
		this.fitSum = fitSum;
	}

	public H getH() {
		return h;
	}

	public void setH(H h) {
		this.h = h;
	}

	public EntrySocialAgent(D fitsum, H h) {

		this.fitSum = fitsum;
		this.h = h;
	}
}
