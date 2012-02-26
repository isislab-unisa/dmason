package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.util.connection.Connection;

/**
 * An interface for all Distributed Fields 2D
 * @param <E> the type of locations
 */
public interface DistributedField<E> extends Serializable
{	
	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
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
	public DistributedState getState();
		
	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	public E setAvailableRandomLocation(RemoteAgent<E> rm);
	
	public void setConnection(Connection con);

	public ArrayList<MessageListener> getLocalListener();
	
	public void setTable(HashMap table);
	
	public String getID();
	
	public UpdateMap getUpdates();
}