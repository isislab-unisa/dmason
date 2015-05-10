package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.sim.field.network.kway.graph.tools.GraphFormatConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class Graph extends SimpleGraph<Vertex,Edge>{

	private static final long serialVersionUID = 2120237606386340761L;
	protected Random r;

	public Graph() {
		super(Edge.class);
		r = new Random(System.currentTimeMillis());
			 
	}

	public Graph(Class<? extends Edge> edgeClass) {
		super(edgeClass);
		r = new Random(System.currentTimeMillis());
	}
	
	public static Graph generateGraphOptimal(int p)
	{
		Graph g = new Graph();
		HashMap<Integer, Vertex> gvertex=new HashMap<Integer, Vertex>();
		for (int i = 1; i <= (p*4); i++) {
			Vertex v=new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p*4); i++) {
			switch (i%4) {
			case 0:
				if(i!=(p*4))
				g.addEdge(gvertex.get(i), gvertex.get(i+1));
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				g.addEdge(gvertex.get(i), gvertex.get(i-3));
				break;

			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				break;
			default:
				break;
			}
		}
		return g;
	}
	
	public static Graph generateGraphSubOptimal(int p)
	{
		p *= 4;
		Graph g = new Graph();
		HashMap<Integer, Vertex> gvertex = new HashMap<Integer, Vertex>();

		for (int i = 1; i <= (p*4); i++) {
			Vertex v = new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p*4); i++) {
			switch (i%4) {
			case 0:
				if(i!=(p*4))
				g.addEdge(gvertex.get(i), gvertex.get(i+1));
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				g.addEdge(gvertex.get(i), gvertex.get(i-3));
				break;
			case 1:
				if(i%16==1)g.addEdge(gvertex.get(i), gvertex.get(i+15));
				break;
			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				break;
			default:
				break;
			}
		}
		return g;
	}

	
	/**
	 * creates a supergraph where the id of the supervertex are partitions ids
	 * and nodes contained in the partitions are contained in supervertex.
	 * If exists an edge between two nodes contained in the same supervetex,
	 * will be incremented the number of selfedges.
	 * Otherwise, if exists an edge between two nodes contained in two different supervertex,
	 * a superedge will be created.  
	 * 
	 * @param partitionFilename - csv file describing a graph partitioning
	 * @param csvGraphFilename - csv file describing a graph
	 * @param vertex_names 
	 * @return superGraph - the supergraph created 
	 */
	public static Graph generateSupergraph (String partitionFilename, String csvGraphFilename){
		
		HashMap<Integer, Integer> vertex_part = new HashMap<Integer, Integer>();	//map vertex-partition
		
		Graph originalGraph = GraphFormatConverter.csv2graph(csvGraphFilename);		//original graph
		
		Graph superGraph = Graph.createSuperVertex(partitionFilename, vertex_part);	//creates a graph constructing supervertexs
		
		addSuperEdges(originalGraph, superGraph, vertex_part);						//adds superedges to supergraph
		
		return superGraph;
	}

	/**
	 * creates a supergraph where the id of the supervertex are partitions ids
	 * and nodes contained in the partitions are contained in supervertex.
	 * 
	 * @param partitionFilename - csv file describing a graph partitioning
	 * @param vertex_part - HashMap<Integer, Integer> relatinship vertex-partition
	 * @return graph - graph containing only superVertexs
	 */
	public static Graph createSuperVertex(String partitionFilename, HashMap<Integer, Integer> vertex_part){
		BufferedReader br = null;
		String [] vertexPart;
		HashMap<Integer, String> ids = new HashMap<Integer, String>();
		ArrayList<HashSet<Vertex>> partitionVertex =  new ArrayList<HashSet<Vertex>>();
		String info = ""; int i = 0; String id;
		
		Graph graph = new Graph();
		
		try {
			br = new BufferedReader(new FileReader(partitionFilename));
			info = br.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		 
		 vertexPart = info.split(";");
		 
		 /* calculate the total number of partitions */
		 for(i=0; i<vertexPart.length-2; i+=2){
			
			 id = vertexPart[i+1]; 			// partition number (id)
			 int idI = Integer.parseInt(id);
			 
			 if(!ids.containsKey(idI)){							//if not already entered
				 ids.put(idI, null);							//add the new partition id
				 partitionVertex.add(new HashSet<Vertex>());	//creates a new vertex collection for a supervertex
			 }
		 }
		 
		 /*creation hashSet per partition*/
		 for(i=0; i<vertexPart.length-1; i+=2){
			 String vertexId = vertexPart[i]; 		//vertex id
			 String numP = vertexPart[i+1]; 		//partition number
			 
			 int vertexI = Integer.parseInt(vertexId);
			 Vertex v = new Vertex(vertexI);		//vertex creation
			 int numPart = Integer.parseInt(numP);	
			 
			 vertex_part.put(vertexI, numPart);		//mapping vertex-partition
			 
			partitionVertex.get(numPart).add(v);
		 }
		 
		 /* crea i supervertex ed aggiungili al grafo */
		 for(i=0; i<partitionVertex.size() ; i++){
			 SuperVertex sv = new SuperVertex(i, partitionVertex.get(i));	//id = numPart
			 graph.addVertex(sv);
		 }
		 
		 return graph;
		 
	}//end method create supervertex

	/**
	 * Modifies the supergraph adding superedges
	 * If exists an edge between two nodes contained in the same supervetex,
	 * will be incremented the number of selfedges.
	 * Otherwise, if exists an edge between two nodes contained in two different supervertex,
	 * a superedge will be created.  
	 * 
	 * @param originalGraph - graph on which partitions have been calculated
	 * @param superGraph - a graph containing only superVertexs
	 * @param vertex_part - map between vertex-partition
	 */
	public static void addSuperEdges(Graph originalGraph, Graph superGraph, HashMap<Integer, Integer> vertex_part){
		
		Set<Edge> original_edges = originalGraph.edgeSet();				//all edges in the graph
		SuperEdge superE;
		
		for(Edge e : original_edges){
			
			int id_from = e.getSource().getId();
			int id_to = e.getTarget().getId();
			 
			//System.out.println(id_from + " -- " + id_to);
			
			int p_from = vertex_part.get(id_from);
			int p_to = vertex_part.get(id_to);
			
			//System.out.println( "Part " + p_from + " -- " + p_to);
			 
			/* if the nodes belong to the same partition */
			if(p_to == p_from){
				Iterator<Vertex> iterVertex =  superGraph.vertexSet().iterator();
				 
				while(iterVertex.hasNext()){
					SuperVertex v = (SuperVertex) iterVertex.next();
					 
					if(v.getId() == p_to){
						v.incrementSelfEdges();
						break;
					}
				}
			}
			/* if nodes belong to different partitions */
			else{
				SuperVertex from = null;
				SuperVertex to = null;
				Iterator<Vertex> iterVertex =  superGraph.vertexSet().iterator();
				 
				while(iterVertex.hasNext()){
					SuperVertex v = (SuperVertex) iterVertex.next();
					 
					if(v.getId() == p_from){
						from = v;
						continue;
					}
					if(v.getId() == p_to){
						to = v;
						continue;
					}
				}//end while
				 
				// creates the SuperEdge or adds a new edge to it
				if(superGraph.containsEdge(from, to)){
					superE = (SuperEdge) superGraph.getEdge(from, to);
					superE.addEdge(e);	
					
				}else{
					superE = new SuperEdge();
					superE.addEdge(e);
					 
					superGraph.addEdge(from, to, superE);
				}
				
			}//end else	 
		}//end for
		 
	}//end method addsuperEdges
	
}






































