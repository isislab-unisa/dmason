package dmason.sim.field;

import java.io.Serializable;

import dmason.sim.engine.RemoteAgent;

/**
 * A wrapper class for a Remote Agent and corresponding location.
 * @param <E> the type of location
 */
public class Entry<E> implements Serializable
{	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  RemoteAgent<E> r;
	public  E l;
	
	public Entry(final RemoteAgent<E> r,final E l)
	{
		this.r=r;
		this.l=l;
	}
}