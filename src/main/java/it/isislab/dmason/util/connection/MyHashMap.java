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
package it.isislab.dmason.util.connection;

import java.io.Serializable;
import java.util.HashMap;
/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class MyHashMap extends HashMap<String, Object> implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int full;
	public int NUMBER_FIELDS;
	
	public MyHashMap(int numFields){
		NUMBER_FIELDS = numFields;
		full = numFields;
	}
	
	@Override
	public Object put(String key, Object value){
		
		Object obj = super.put(key, value);
		full--;
		return obj;
	}
	
	public boolean isFull(){
		
		return full==0;
	}
	
	public int getFull(){
		
		return full;
	}
}