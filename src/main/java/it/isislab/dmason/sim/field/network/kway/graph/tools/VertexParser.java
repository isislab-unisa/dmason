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
package it.isislab.dmason.sim.field.network.kway.graph.tools;


import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
public class VertexParser {
	
	/**
	 * Reads a graph described as edgelist 
	 * and returns all the vertices of the graph
	 * with their id
	 * @param filepath - file.edgelist describing a graph
	 * @return ids - TreeMap containing vertices id, inserted as keys
	 */
	
	public static TreeMap<Integer, String> getOriginalIdFromEdgelist(String filepath){
		TreeMap<Integer, String> ids = new TreeMap<Integer, String>();
		String edge, separator;
		String vertices[] = new String[2];
	
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			
			while((edge = in.readLine()) != null){
				
				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);
				
				int firstId = Integer.parseInt(vertices[0]);
				int secondId = Integer.parseInt(vertices[1]);
				
				if(!ids.containsKey(firstId))
					ids.put(firstId, null);
				
				if(!ids.containsKey(secondId))
					ids.put(secondId, null);
				
			}
	
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ids;
	}
	

}
