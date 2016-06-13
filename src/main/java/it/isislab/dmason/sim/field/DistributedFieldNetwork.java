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

package it.isislab.dmason.sim.field;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;
import it.isislab.dmason.sim.field.support.network.UpdateNetworkMap;
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
public interface DistributedFieldNetwork<E> extends DistributedField<E> {

	public void setNumberOfUpdatesToSynchro(int number);
	
	public boolean updateNode( RemoteUnpositionedAgent rm, SimState sm);
	
	public UpdateNetworkMap getNetworkUpdates() ;
	
}
