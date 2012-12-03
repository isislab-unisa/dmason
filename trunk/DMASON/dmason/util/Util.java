package dmason.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generic static methods
 *
 */
public final class Util 
{
	/**
	 * A generic method clone implemented with Java Reflection.
	 * It's necessary when you have to clone a class which you don't know
	 * instance variables
	 * @param o The object that have to be cloned
	 * @return The cloned object
	 */
	public static final Object clone(Object o)
	{
	  Object clone = null;
	 
	  try
	  {
	     clone = o.getClass().newInstance();
	  }
	  catch (InstantiationException e){e.printStackTrace();}
	  catch (IllegalAccessException e){e.printStackTrace();}
	  
	  for (Class obj = o.getClass();!obj.equals(Object.class);obj = obj.getSuperclass())
	  {
	    Field[] fields = obj.getDeclaredFields();
	    for (int i = 0; i < fields.length; i++)
	    {
	      fields[i].setAccessible(true);
	      try
	      {	     
	        fields[i].set(clone, fields[i].get(o));
	        
	      }
	      catch (IllegalArgumentException e){}
	      catch (IllegalAccessException e){}
	    }
	  }
	  return clone;
	}
	
	// chops a list into non-view sublists of length L
	public static <T> List<List<T>> chopped(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	   
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}
}