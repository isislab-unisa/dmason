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
