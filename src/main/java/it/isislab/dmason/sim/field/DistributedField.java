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

package it.isislab.dmason.sim.field;

import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An interface for all Distributed Fields 2D
 * @param <E> the type of locations
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
public interface DistributedField<E> extends Serializable
{	
	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 * @return true if the synchronization was successful.
	 */
	public boolean synchro();

	/**
	 * Return the DistributedState that creates this field.
	 * @return the DistributedState
	 */
	public DistributedState<E> getState();


	//public ArrayList<MessageListener> getLocalListener();
	
	@SuppressWarnings("rawtypes")
	public void setTable(HashMap table);
	
	public String getDistributedFieldID();
	
	@SuppressWarnings("rawtypes")
	public UpdateMap getUpdates();
		
	/**
	 * User fot global parameters synchronization.
	 * @return
	 */
	public VisualizationUpdateMap<String, Object> getGlobals();
	
}