package dmason.sim.engine;

import java.io.Serializable;

import sim.engine.Steppable;

/**
 * Interface for a remote agent
 *
 * @param <E> 
 */
public interface RemoteAgent<E> extends Steppable, Serializable{

	
	/**
	 * 
	 * @return position on field of agent
	 */
	public E getPos() ; 
	
	/**
	 * set agent on position
	 * 
	 * @param pos position of 
	 */
	public void setPos(E pos) ; 
	
	/**
	 * Return the id of an agent
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Set id of agent
	 * 
	 * @param id t
	 */
	public void setId(String id);
	
}
