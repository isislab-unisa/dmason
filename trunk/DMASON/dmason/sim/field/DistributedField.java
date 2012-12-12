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

package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.loadbalancing.MyCellInterface;
import dmason.util.connection.Connection;
import dmason.util.visualization.VisualizationUpdateMap;

/**
 * An interface for all Distributed Fields 2D
 * @param <E> the type of locations
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
	 * Set up the new position in a distributed environment  in the space of expertise.
	 * @param location The new location
	 * @param rm The remote agent that have be stepped
	 * @param sm The SimState of simulation
	 * @return false if the object is null (null objects cannot be put into the grid)
	 */
	public boolean setDistributedObjectLocationForPeer(final E location,RemoteAgent<E> rm,SimState sm);
	
	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
	public boolean setDistributedObjectLocation(final E location,RemoteAgent<E> rm,SimState sm);
	
	/**
	 * Return the DistributedState that creates this field.
	 * @return the DistributedState
	 */
	public DistributedState<E> getState();
		
	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	public E setAvailableRandomLocation(RemoteAgent<E> rm);
	
	public void setConnection(Connection con);

	public ArrayList<MessageListener> getLocalListener();
	
	@SuppressWarnings("rawtypes")
	public void setTable(HashMap table);
	
	public String getID();
	
	@SuppressWarnings("rawtypes")
	public UpdateMap getUpdates();
	
	
	
	
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
	
	public int getNumAgents();
	
	public void resetParameters();
	
	public int getLeftMineSize();
	
	public int getRightMineSize();
	
	/**
	 * User fot global parameters synchronization.
	 * @return
	 */
	public VisualizationUpdateMap<String, Object> getGlobals();
}