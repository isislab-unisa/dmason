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
package it.isislab.dmason.util;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	
	public static String getCurrentDateTime(long now) {
		//
		// Create a DateFormatter object for displaying date information.
		//
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
		 

		//
		// Create a calendar object that will convert the date and time value
		// in milliseconds to date. We use the setTimeInMillis() method of the
		// Calendar object.
		//
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		return formatter.format(calendar.getTime());
	}
}