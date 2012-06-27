package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;

import dmason.sim.engine.RemoteAgent;
import dmason.util.Util;

/**
 * An subclass of ArrayList of Entry that implements only the method clone().
 *
 * @param <E> the type of an agent location
 */
public class MyArrayList<E> extends ArrayList<Entry<E>> implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyArrayList<E> clone()
	{
		MyArrayList<E> r=new MyArrayList<E>();
		for(Entry<E> e: this)
			r.add(new Entry((RemoteAgent<E>)Util.clone(e.r),e.l));
	  return r;
	}
}