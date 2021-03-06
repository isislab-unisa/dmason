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

public class StateVariable {
	public String name;
	public Class type;
	
	/**
	 * @author Michele Carillo
	 * @author Carmine Spagnuolo
	 * @author Flavio Serrapica
	 * 
	 * Class constructor
	 * @param name variable name
	 * @param type variable type 
	 *
	 */
	public StateVariable(String name, Class type) {
		super();
		this.name = name;
		this.type = type;
	}
	/**
	 * @return the name
	 */
	protected String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	protected Class getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	protected void setType(Class type) {
		this.type = type;
	}
	

}
