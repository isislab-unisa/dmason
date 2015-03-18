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

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;

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


	public ArrayList<MessageListener> getLocalListener();
	
	@SuppressWarnings("rawtypes")
	public void setTable(HashMap table);
	
	public String getID();
	
	@SuppressWarnings("rawtypes")
	public UpdateMap getUpdates();
	
	//public  UpdateMapNet  getUpdatesNet();

	//Methods for Load Balancing, if you don't need these methods you must "Add Unimplement Method"
	/**
	 * Return a list of MyCell
	 * @return list of cell  
	 */
	public HashMap<Integer, MyCellInterface> getToSendForBalance();

	/**
	 * Set parameter for split
	 * @param isSplitted true if cell will split   
	 */
	public void setIsSplitted(boolean isSplitted);
	
	/**
	 * get parameter on field for split
	 * @param isSplitted true if cell is splitted   
	 */
	public boolean isSplitted();
	
	/**
	 * get parameter on field for split
	 * @param isPrepareForBalance true if cell is splitting   
	 */
	public boolean isPrepareForBalance();
	
	/**
	 * get parameter on field for union
	 * @param isUnited true if cell is united   
	 */
	public boolean isUnited();

	/**
	 * Set setForBalance 
	 * @param setForBalance true for loadbalancing
	 */
	public void prepareForBalance(boolean prepareForBalance);
	
	public HashMap<Integer, MyCellInterface> getToSendForUnion();


	public void prepareForUnion(boolean prepareForUnion);
	
	/**
	 * Get number of agents of a region
	 * @return number of agents of a region
	 */
	public int getNumAgents();
	
	
	/**
	 * Reset all the local parameters of a region for a step
	 */
	public void resetParameters();
	
	/**
	 * Get number of agents in the left mine
	 * @return number of agents in the left mine
	 */
	public int getLeftMineSize();
	
	
	/**
	 * Get number of agents in the right mine
	 * @return number of agents in the right mine
	 */
	public int getRightMineSize();
	
	/**
	 * User fot global parameters synchronization.
	 * @return
	 */
	public VisualizationUpdateMap<String, Object> getGlobals();
	
}