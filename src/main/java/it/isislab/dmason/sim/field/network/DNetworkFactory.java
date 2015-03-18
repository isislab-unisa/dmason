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

package it.isislab.dmason.sim.field.network;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;
import sim.engine.SimState;
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
/**
 * 
 * @author Ada Mancuso
 * @author Francesco Milone
 * @author Carmine Spagnuolo
 *
 */
public class DNetworkFactory {
	
	public static DNetwork createDNetworkField(SimState sm, int rows, int columns, int i, int j,GraphSubscribersEdgeList gprsub ,String graph_id, String prefix)
	{
		DNetwork network = new DNetwork(sm, rows, columns, i, j, gprsub, graph_id, prefix);
		((DistributedMultiSchedule)((DistributedState)sm).schedule).addNetworkField(network);
		return network;
	}

}
