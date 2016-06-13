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
package it.isislab.dmason.sim.field.network.kway.algo.interfaces;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.IOException;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "22/7/2015"
		)
public  interface PartitioningAlgorithm {

	/**
	 * Executes the partition of a graph using the algorithm specified 
	 * by the class which implements this interface.
	 * @return the path of the file which describes the partition calculated.
	 */
	public  String partitioning() throws IOException,InterruptedException;
}
