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
package it.isislab.dmason.sim.field.network.kway.algo.kaffpa;

import java.io.IOException;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.algo.interfaces.PartitioningAlgorithm;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "22/7/2015"
		)
public class KaffpaEProcessBinding implements PartitioningAlgorithm {

	/**
	 * @param bin_path - bin path of the executable
	 * @param graph_path - path of the graph to partition
	 * @param K - number of partitions
	 */
	public KaffpaEProcessBinding(String bin_path, String graph_path, int K) {
		this.bin_path = bin_path;
		this.graph_path = graph_path;
		this.nPart = K;
	}

	@Override
	public String partitioning() throws IOException, InterruptedException {
		String output_file = graph_path + ".part." + nPart;
		Process part = Runtime.getRuntime().exec(
				bin_path + " " + graph_path + " " + nPart
						+ " --output_filename=" + output_file);
		int status = part.waitFor();

		if (status != 0) {
			System.err.println("Error in calling kaffpaE!");
			System.exit(0);
		}

		return output_file;
	}

	private String graph_path;
	private String bin_path;
	private Integer nPart;
}
