package dmason.sim.field;

import java.io.Serializable;

/**
 * A wrapper class for a Value and corresponding location.
 * @param <K> the type of value
 * @param <F> the type of location
 */
public class EntryNum<K, F> implements Serializable
{	
	public  K r;
	public  F l;
	
	public EntryNum(final K r,final F l)
	{
		this.r=r;
		this.l=l;
	}
}