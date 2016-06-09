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

import it.isislab.dmason.annotation.AuthorAnnotation;
@AuthorAnnotation(
		author = {"Michele Carillo","Ada Mancuso",
				  "Dario Mazzeo","Francesco Milone",
				  "Francesco Raia","Flavio Serrapica",
				  "Carmine Spagnuolo"},
		date = "6/3/2014"
	)

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
 **/
public interface RemoteUnpositionedAgent<E> extends RemotePositionedAgent<E>{
	
	public int getCommunityId();

	public void setCommunityId(int community);
	
	public String getLabel();
	
	public void  setLabel(String label);
	
}
