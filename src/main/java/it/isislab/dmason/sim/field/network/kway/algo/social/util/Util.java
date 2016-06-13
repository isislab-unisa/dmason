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
package it.isislab.dmason.sim.field.network.kway.algo.social.util;



import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.SuperVertex;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;

public class Util {
	public static Graph loadDiagram(String s)  {
		Graph	graph=null;
		if (s.endsWith("graph")){
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream(s);
				in = new ObjectInputStream(fis);
				try {
					graph= (Graph) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				in.close();
				return graph;
			} catch (IOException ex) {
				System.out.println("Loading  diagram failed");
				return null;
			}
		}
		else
		{	
			System.out.println("ERRORE il file non finisce con graph");
			return null;
		}
	}
	public static Graph createSuperGraph(Graph g, HashMap<Vertex, Integer> vToc)  {
		
		Graph SG=new Graph();
		HashMap<Integer, HashSet<Vertex>> cTov =new HashMap<Integer,HashSet<Vertex>>();
		
		for(Vertex v: g.vertexSet())
		{
			if(cTov.get(vToc.get(v))==null)cTov.put(vToc.get(v), new HashSet<Vertex>());
			cTov.get(vToc.get(v)).add(v);
		}
		
		HashMap<Vertex, SuperVertex> gTOsg =new HashMap<Vertex,SuperVertex>();
		
		for (Integer comm : cTov.keySet()) {
			
			SuperVertex sgv=new SuperVertex(comm,cTov.get(comm));
			SG.addVertex(sgv);
		
			for(Vertex v : cTov.get(comm))
				gTOsg.put(v, sgv);
		}
		
		for (Edge e :g.edgeSet()) {
			
			Vertex u=e.getSource();
			Vertex v=e.getTarget();
			
			SuperVertex su=gTOsg.get(u);
			SuperVertex sv=gTOsg.get(v);
			
			if(!su.equals(sv)) SG.addEdge(su, sv);
			
		}
	
		return SG;
	}
	
	
}
