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

/**
 * This interface is meant to be implemented by distributed fields
 * that allows tracing simulation's statistics in a
 * distributed way.
 * 
 * @author Luca Vicidomini
 *
 */

public interface TraceableField
{
	/**
	 * Enables tracing. If the simulation instance offers methods such as
	 * "getCohesion", then a valid parameter name will be "Cohesion".
	 * @param param The parameter we want to start tracing
	 */
	public void trace(String param);
	
	/**
	 * Stops tracing.
	 * @param param The parameter we want to stop tracing
	 */
	public void untrace(String param);
}
