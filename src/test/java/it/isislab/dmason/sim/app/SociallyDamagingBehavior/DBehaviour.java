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

import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;

import java.io.Serializable;

import sim.util.Bag;
import sim.util.Double2D;

public abstract class DBehaviour implements Serializable{
	
	public abstract void action(DHuman agent, DSociallyDamagingBehavior state, Bag neigh, Bag entryNeigh);
	public abstract void calculateCEI(DHuman a, DSociallyDamagingBehavior sdb, Bag n);
	public abstract void socialInfluence(DHuman agent, Bag neigh);
	public abstract Double2D move(DSociallyDamagingBehavior state, Double2D loc, Bag neigh);
	public abstract Double2D consistency(DHuman agent, Bag b, DContinuousGrid2D humans);
	public abstract Double2D cohesion(DHuman agent, Bag b, DContinuousGrid2D humans);
	public abstract Double2D avoidance(DHuman agent, Bag b, DContinuousGrid2D humans);
	public abstract Double2D randomness(DSociallyDamagingBehavior state);
}