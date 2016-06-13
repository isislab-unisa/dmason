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
package it.isislab.dmason.sim.field.network.kway.algo.random;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.algo.interfaces.PartitioningAlgorithm;
import it.isislab.dmason.sim.field.network.kway.graph.tools.GraphFormatConverter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

@AuthorAnnotation(author = { "Alessia Antelmi", "Carmine Spagnuolo" }, date = "22/7/2015")
public class Random implements PartitioningAlgorithm {
	
	/**
	 * @param graph_path - path of the graph to partition
	 * @param k - number of partitions
	 */
	public Random(String graph_path, int k) {
		this.graph_path = graph_path;
		this.nPart = k;
	}

	@Override
	public String partitioning() throws IOException, InterruptedException {

		ArrayList<Integer> list = new ArrayList<Integer>();
		Integer[] vertices = null;
		String[] info = graph_path.split("\\.");
		String format = info[info.length - 1];
		
		// All vertices in the graph
		if (format.equalsIgnoreCase("edgelist")) {
			vertices = GraphFormatConverter.createMappingNamesGraphEdgelist(graph_path);
		} else if(format.equalsIgnoreCase("graph")) {
			vertices = GraphFormatConverter.createMappingNamesGraphDotGraph(graph_path);
		}else {
			throw new IllegalArgumentException("Format Unknown.");
		}
		
		Integer[] partitions = new Integer[nPart];
		Arrays.fill(partitions, 0);
		int p = 0;

		HashMap<Integer, Integer> mapVertexPart = new HashMap<Integer, Integer>();

		String output_file = graph_path + ".part." + nPart;
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(output_file));

		for (Integer i : vertices) {
			list.add(i);
		}

		Collections.shuffle(list);

		for (Integer i : list) {
			if (list.size() % nPart == 0) {
				if (partitions[p] < list.size() / nPart) {
					mapVertexPart.put(i, p);
					partitions[p]++;
				}
				p = (p + 1) % nPart;
			} else {

				if (partitions[p] <= list.size() / nPart) {
					mapVertexPart.put(i, p);
					partitions[p]++;
				}
				p = (p + 1) % nPart;
			}
		}

		for (Integer i : vertices) {
			String s = mapVertexPart.get(i) + "\n";
			out.write(s.getBytes());
		}

		out.close();

		return output_file;
	}

	private String graph_path;
	private Integer nPart;
}