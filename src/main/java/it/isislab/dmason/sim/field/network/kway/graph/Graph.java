package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.annotation.AuthorAnnotation;
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

@AuthorAnnotation(author = { "Alessia Antelmi", "Gennaro Cordasco", "Carmine Spagnuolo" }, date = "20/7/2015")
public class Graph extends SimpleGraph<Vertex, Edge> {

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

	public static Graph generateGraphOptimal(int p) {
		Graph g = new Graph();
		HashMap<Integer, Vertex> gvertex = new HashMap<Integer, Vertex>();
		for (int i = 1; i <= (p * 4); i++) {
			Vertex v = new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p * 4); i++) {
			switch (i % 4) {
			case 0:
				if (i != (p * 4))
					g.addEdge(gvertex.get(i), gvertex.get(i + 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 2));
				g.addEdge(gvertex.get(i), gvertex.get(i - 3));
				break;

			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 2));
				break;
			default:
				break;
			}
		}
		return g;
	}

	public static Graph generateGraphSubOptimal(int p) {
		p *= 4;
		Graph g = new Graph();
		HashMap<Integer, Vertex> gvertex = new HashMap<Integer, Vertex>();

		for (int i = 1; i <= (p * 4); i++) {
			Vertex v = new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p * 4); i++) {
			switch (i % 4) {
			case 0:
				if (i != (p * 4))
					g.addEdge(gvertex.get(i), gvertex.get(i + 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 2));
				g.addEdge(gvertex.get(i), gvertex.get(i - 3));
				break;
			case 1:
				if (i % 16 == 1)
					g.addEdge(gvertex.get(i), gvertex.get(i + 15));
				break;
			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i - 1));
				g.addEdge(gvertex.get(i), gvertex.get(i - 2));
				break;
			default:
				break;
			}
		}
		return g;
	}

	/**
	 * creates a super graph where the id of the super vertex are partitions ids
	 * and nodes contained in the partitions are contained in super vertex. If
	 * exists an edge between two nodes contained in the same super vertex, will
	 * be incremented the number of self loops. Otherwise, if exists an edge
	 * between two nodes contained in two different super vertex, a super edge
	 * will be created.
	 * 
	 * @param partitionFilename
	 *            - .part file describing a graph partitioning
	 * @param graphFilename
	 *            - file describing a graph
	 * @param totPartNumb
	 *            - the total number of partitions
	 * @return superGraph - the super graph created
	 */
	public static Graph generateSupergraph(String partitionFilename,
			String graphFilename, Integer[] vertex_names, int totPartNumb) {

		// mapping vertex-partition
		HashMap<Integer, Integer> vertex_part = new HashMap<Integer, Integer>();
		Graph originalGraph = null;
		
		String[] info = graphFilename.split("\\.");
		String format = info[info.length - 1];
		
		if (format.equalsIgnoreCase("edgelist")) {
			originalGraph = GraphFormatConverter.edgelist2graph(graphFilename);
		} else if(format.equalsIgnoreCase("graph")) {
			originalGraph = GraphFormatConverter.dotGraph2graph(graphFilename);
		}else {
			throw new IllegalArgumentException("Format Unknown.");
		}
		
		// creates a graph constructing super vertices
		Graph superGraph = Graph.createSuperVertices(partitionFilename,vertex_names, vertex_part, totPartNumb);
		// adds super edges to super graph
		addSuperEdges(originalGraph, superGraph, vertex_part);

		return superGraph;

	}

	/**
	 * creates a super graph where the id of the super vertex are partitions ids
	 * and nodes contained in the partitions are contained in super vertex.
	 * 
	 * @param partitionFilename
	 *            - .part file describing a graph partitioning
	 * @param vertex_part
	 *            - HashMap<Integer, Integer> relationship vertex-partition
	 * @param totPartNumb
	 *            - the total number of partitions
	 * @return graph - graph containing only superVertexs
	 */
	private static Graph createSuperVertices(String partitionFilename,
			Integer[] vertex_names, HashMap<Integer, Integer> vertex_part, int totPartNumb) {

		BufferedReader br = null;
		ArrayList<HashSet<Vertex>> partitionVertex = new ArrayList<HashSet<Vertex>>();
		String part = "";
		int i = 0, id_node = 0;
		Graph graph = new Graph();

		for (i = 0; i < totPartNumb; i++)
			partitionVertex.add(new HashSet<Vertex>());

		try {
			br = new BufferedReader(new FileReader(partitionFilename));

			while ((part = br.readLine()) != null) {

				// vertex creation
				Vertex v = new Vertex(vertex_names[id_node]);
				int numPart = Integer.parseInt(part);
				
				// mapping vertex-partition
				vertex_part.put(vertex_names[id_node], numPart);

				partitionVertex.get(numPart).add(v);

				id_node++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* creation of super vertices and their addition to the graph */
		for (i = 0; i < partitionVertex.size(); i++) {
			SuperVertex sv = new SuperVertex(i, partitionVertex.get(i)); // id = numPart
																			 
			graph.addVertex(sv);
		}

		return graph;

	}

	/**
	 * Modifies the super graph adding super edges. If exists an edge between two
	 * nodes contained in the same super vertex, will be incremented the number
	 * of self loops. Otherwise, if exists an edge between two nodes contained
	 * in two different super vertex, a super edge will be created.
	 * 
	 * @param originalGraph
	 *            - graph on which partitions have been calculated
	 * @param superGraph
	 *            - a graph containing only superVertexs
	 * @param vertex_part
	 *            - map between vertex-partition
	 */
	private static void addSuperEdges(Graph originalGraph, Graph superGraph,
			HashMap<Integer, Integer> vertex_part) {

		Set<Edge> original_edges = originalGraph.edgeSet();
		SuperEdge superE;

		for (Edge e : original_edges) {

			int id_from = e.getSource().getId();
			int id_to = e.getTarget().getId();

			int p_from = vertex_part.get(id_from);
			int p_to = vertex_part.get(id_to);

			/* if the nodes belong to the same partition */
			if (p_to == p_from) {
				Iterator<Vertex> iterVertex = superGraph.vertexSet().iterator();

				while (iterVertex.hasNext()) {
					SuperVertex v = (SuperVertex) iterVertex.next();

					if (v.getId() == p_to) {
						v.incrementSelfLoops();
						break;
					}
				}
			}
			/* if nodes belong to different partitions */
			else {
				SuperVertex from = null;
				SuperVertex to = null;
				Iterator<Vertex> iterVertex = superGraph.vertexSet().iterator();

				while (iterVertex.hasNext()) {
					SuperVertex v = (SuperVertex) iterVertex.next();

					if (v.getId() == p_from) {
						from = v;
						continue;
					}
					if (v.getId() == p_to) {
						to = v;
						continue;
					}
				}

				// creates the SuperEdge or adds a new edge to it
				if (superGraph.containsEdge(from, to)) {
					superE = (SuperEdge) superGraph.getEdge(from, to);
					superE.addEdge(e);

				} else {
					superE = new SuperEdge();
					superE.addEdge(e);

					superGraph.addEdge(from, to, superE);
				}

			}
		}

	}

}
