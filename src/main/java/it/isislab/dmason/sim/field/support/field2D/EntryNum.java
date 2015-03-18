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

package it.isislab.dmason.sim.field.support.field2D;

import java.io.Serializable;

/**
 * A wrapper class for a Value and corresponding location.
 * @param <K> the type of value
 * @param <F> the type of location
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class EntryNum<K, F> implements Serializable
{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  K r;
	public  F l;
	
	public EntryNum(final K r,final F l)
	{
		this.r=r;
		this.l=l;
	}
}