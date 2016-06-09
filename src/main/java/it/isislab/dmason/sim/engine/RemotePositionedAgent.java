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

package it.isislab.dmason.sim.engine;

import java.io.Serializable;

/**
 * Interface for a remote agent
 *
 * @param <E> 
 */
public interface RemotePositionedAgent<E> extends RemoteAgent, Serializable{

	
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
