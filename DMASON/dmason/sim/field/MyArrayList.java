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