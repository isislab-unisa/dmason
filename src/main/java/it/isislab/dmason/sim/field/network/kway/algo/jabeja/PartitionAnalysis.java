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
package it.isislab.dmason.sim.field.network.kway.algo.jabeja;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.graph.tools.VertexParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import edu.cmu.graphchi.preprocessing.VertexIdTranslate;
import edu.cmu.graphchi.vertexdata.VertexAggregator;
import edu.cmu.graphchi.vertexdata.VertexIdValue;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
public class PartitionAnalysis {

	/**
	 * this method writes the output in a file .part,
	 * where the number of the line represents a node id
	 * while the content of the line the partition associated.
	 * 
	 * @param logger
	 * @param baseFilename - a file edgelist describing the graph
	 * @param outFilename - output file 
	 * @param numVertices - total number of vertices
	 * @param numPartitions - number of partitions
	 * @param vertexIdTranslate 
	 * @param vertex_names - mapping vertices with consecutive ids 
	 */
	public static void writeVertexPartitions(Logger logger, String baseFilename, String outFilename, int numVertices, 
							int numPartitions, VertexIdTranslate vertexIdTranslate, Integer[] vertex_names) throws IOException {

		TreeMap<Integer, String> originalIds = VertexParser.getOriginalIdFromEdgelist(baseFilename);
		String outputFilename = outFilename;
		String s = "";
		Iterator<VertexIdValue<Integer[]>> iter = VertexAggregator.vertexIterator(numVertices, baseFilename,
																		new IntArrayConverter(2), VertexIdTranslate.identity());

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFilename));
		VertexIdValue<Integer[]> idVal;
		int ghost_part = 0;
		HashMap<Integer, Integer> mapjabejaparts = new HashMap<Integer, Integer>();
		
		/* removal ghost nodes - added by GraphChi */
		while(iter.hasNext()) {
			idVal = iter.next();

			int id = idVal.getVertexId();
			int real_id = vertexIdTranslate.backward(id);
			
			if(!originalIds.containsKey(real_id))
				continue;
			
			mapjabejaparts.put(real_id, idVal.getValue()[1]);
		}
	
		/* writing output */
		for(int i=0; i<vertex_names.length; i++){

			if(mapjabejaparts.containsKey(vertex_names[i])){

				s = mapjabejaparts.get(vertex_names[i]) + "\n";
				bos.write(s.getBytes());
				
			}else{
				/* this node does not exist in the graph - writing it in a random partition in a balanced way*/
				s = ghost_part + "\n";
				bos.write(s.getBytes());

				ghost_part = (ghost_part + 1) % numPartitions;
			}
		}
		
		bos.flush();
		bos.close();

	}

}














