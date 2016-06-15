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
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@AuthorAnnotation(author = { "Alessia Antelmi", "Carmine Spagnuolo" }, date = "20/7/2015")
/**
 * auxiliary class for converting 
 * between different representation formats of a graph
 */
public class GraphFormatConverter {

	/**
	 * converts a file from edgelist to csv
	 * 
	 * @param filepath - file edgelist
	 * @param outFilename - file csv
	 */
	public static void edgelist2csv(String filepath, String outFilename) {
		String edge = "";
		String info = "";
		String separator = "";
		String[] vertices = new String[2];
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			while ((edge = in.readLine()) != null) {
				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);
				info = vertices[0] + "," + vertices[1] + "\n";
				out.write(info.getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * converts a file from csv to edgelist
	 * 
	 * @param filepath - file csv
	 * @param outFilename - file edgelist
	 */
	public static void csv2edgelist(String filepath, String outFilename) {
		String edge = "", separator = "";
		String info = "";
		String[] vertices;
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			edge = in.readLine();
			separator = Utility.findSeparator(edge);

			vertices = edge.split(separator);
			info = vertices[0] + " " + vertices[1] + "\n";

			out.write(info.getBytes());

			while ((edge = in.readLine()) != null) {

				vertices = edge.split(separator);
				info = vertices[0] + " " + vertices[1] + "\n";

				out.write(info.getBytes());
			}

		} catch (IOException exc) {
			exc.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}

	/**
	 * converts a file from edgelist to graph
	 * 
	 * @param filepath - file edgelist
	 * @param outFilename - file graph
	 * @param vertex_names list of vertices name
	 */
	public static void edgelist2dotGraph(String filepath, String outFilename, Integer[] vertex_names) {
		String edge = "", info = "", separator = "";
		int numE = 0, firstID = 0, secondID = 0;
		boolean isInv = false;
		String[] vertices = new String[2];
		BufferedReader in = null;
		BufferedOutputStream out = null;
		/* mapping between a vertex(key) and its neighbor */
		TreeMap<Integer, ArrayList<Integer>> verticesAdj = new TreeMap<Integer, ArrayList<Integer>>();

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename + ".graph"));

			while ((edge = in.readLine()) != null) {

				isInv = false;
				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);

				firstID = vertex_names[Integer.parseInt(vertices[0])];
				secondID = vertex_names[Integer.parseInt(vertices[1])];

				if (firstID == secondID)
					continue;

				/* if the first vertex has never been processed */
				if (!verticesAdj.containsKey(firstID)) {
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(secondID);
					verticesAdj.put(firstID, adj);
				} else {
					ArrayList<Integer> adj = verticesAdj.get(firstID);
					if (!adj.contains(secondID)) {
						adj.add(secondID);
					} else
						isInv = true;
				}

				/* if the second vertex has never been processed */
				if (!verticesAdj.containsKey(secondID)) {
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(firstID);
					verticesAdj.put(secondID, adj);
				} else {
					ArrayList<Integer> adj = verticesAdj.get(secondID);
					if (!adj.contains(firstID)) {
						adj.add(firstID);
					} else
						isInv = true;
				}

				if (!isInv)
					numE++;

			}

			/* first row: #nodes #edges */
			info = vertex_names.length + " " + numE + "\n";
			out.write(info.getBytes());
			numE = 0;

			for (int i = 0; i < vertex_names.length; i++) {
				if (verticesAdj.containsKey(vertex_names[i])) {
					info = "";
					ArrayList<Integer> adjV = verticesAdj.get(vertex_names[i]);

					for (Integer node : adjV) {
						info += (node + 1) + " ";
						numE++;
					}

					info += "\n";
					out.write(info.getBytes());
				} else {
					info = "\n";
					out.write(info.getBytes());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * converts a file from graph to edgelist
	 * 
	 * @param filepath - file graph
	 * @param outFilename - file edgelist
	 * @param vertex_names list of vertices 
	 */
	public static void dotGraph2edgelist(String filepath, String outFilename, Integer[] vertex_names) {
		String vertexInfo = "", edge = "", separator = "";
		String[] adj;
		int id = 0, realVertexId;
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream( new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			separator = Utility.findSeparator(in.readLine()); // first row is an info one
			
			while ((vertexInfo = in.readLine()) != null) {
				adj = vertexInfo.split(separator);

				if (vertexInfo.length() <= 0) {
					id++;
					continue;
				}

				for (String v : adj) {
					if (v.length() <= 0)
						continue;
					realVertexId = (Integer.parseInt(v));
					edge = vertex_names[id] + " " + realVertexId + "\n";
					out.write(edge.getBytes());
				}

				id++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads a graph described as an edgelist and constructs the object Graph
	 * associated with it. This method can be used even with csv format
	 * 
	 * @param filepath - edgelist or csv file representing a graph
	 * @return g - the object Graph associated with this representation
	 */
	public static Graph edgelist2graph(String filepath) {
		Graph g = new Graph();
		String edge = "", separator = "";
		String[] vertices;
		Vertex firstV, secondV;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(filepath))));

			while ((edge = in.readLine()) != null) {

				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);

				String id_one = vertices[0];
				String id_two = vertices[1];

				firstV = new Vertex(Integer.parseInt(id_one));
				secondV = new Vertex(Integer.parseInt(id_two));

				if (!g.containsVertex(firstV))
					g.addVertex(firstV);

				if (!g.containsVertex(secondV)) {
					g.addVertex(secondV);
				}

				Edge e = new Edge();
				e.setWeight(1);
				g.addEdge(firstV, secondV, e);

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return g;
	}

	/**
	 * produces a file describing the graph in edgelist mode
	 * 
	 * @param graph - the object graph
	 * @param outFilename - file edgelist
	 */
	public static void graph2edgelist(Graph graph, String outFilename) {

		Set<Edge> edges = graph.edgeSet();
		String edge = "";
		BufferedOutputStream out = null;

		try {
			out = new BufferedOutputStream(new FileOutputStream(outFilename
					+ ".edgelist"));

			for (Edge e : edges) {

				edge = e.getSource() + " " + e.getTarget() + "\n";
				out.write(edge.getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads a graph in graph format 
	 * and constructs the object Graph associated with it
	 * 
	 * @param filepath - graph file representing a graph
	 * @return g - the object Graph associated with this representation
	 */
	public static Graph dotGraph2graph(String filepath) {
		Graph g = new Graph();
		String verticesAdj = "", separator = "";
		Vertex firstV;
		Vertex secondV;
		int id = 1;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));

			// first line contains information about the graph
			separator = Utility.findSeparator(in.readLine());

			while ((verticesAdj = in.readLine()) != null) {

				String[] vertices = verticesAdj.split(separator);

				firstV = new Vertex(id);

				if (!g.containsVertex(firstV))
					g.addVertex(firstV);

				for (int i = 0; i < vertices.length - 1; i++) {
					String id_two = vertices[i];
					
					if (id_two.length() <= 0)
						continue;

					secondV = new Vertex(Integer.parseInt(id_two));

					if (!g.containsVertex(secondV)) {
						g.addVertex(secondV);
					}

					Edge e = new Edge();
					e.setWeight(1);
					g.addEdge(firstV, secondV, e);
				}

				id++;
			}

		} catch (IOException exc) {
			exc.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return g;
	}

	/**
	 * reads a graph in graph format and returns all node ids - 
	 * helpful when node ids are not consecutive
	 * 
	 * @param pathGraphName - graph file representing a graph
	 * @return - array with node ids
	 */
	public static Integer[] createMappingNamesGraphDotGraph(String pathGraphName) {

		String edge = "";
		String separator = "";

		String[] vertices = new String[1];
		BufferedReader in = null;

		TreeSet<Integer> _a = new TreeSet<Integer>();
		int last_id = 1;

		try {

			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(pathGraphName))));
			edge = in.readLine();

			while ((edge = in.readLine()) != null) {
				separator = Utility.findSeparator(edge);

				if (edge.length() <= 0) {
					_a.add(last_id);
					last_id++;
					continue;
				}

				vertices = edge.split(separator);

				for (int i = 0; i < vertices.length; i++) {
					if (vertices[i].length() > 0) {
						_a.add(Integer.parseInt(vertices[i]));
					}
				}
				last_id++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return _a.toArray(new Integer[_a.size()]);
	}

	/**
	 * reads a graph in edgelist format and returns all node ids - 
	 * helpful when node ids are not consecutive
	 *
	 * @param pathGraphName - graph file representing a graph
	 * @return - array with node ids
	 */
	public static Integer[] createMappingNamesGraphEdgelist(String pathGraphName) {
		String edge = "";
		String separator = "";
		int maxId = 0;

		String[] vertices = new String[2];
		BufferedReader in = null;

		TreeSet<Integer> _a = new TreeSet<Integer>();

		try {

			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathGraphName))));

			while ((edge = in.readLine()) != null) {
				separator = Utility.findSeparator(edge);

				vertices = edge.split(separator);

				_a.add(Integer.parseInt(vertices[0]));
				_a.add(Integer.parseInt(vertices[1]));

				if (Integer.parseInt(vertices[0]) > Integer.parseInt(vertices[1])) {
					if (Integer.parseInt(vertices[0]) > maxId)
						maxId = Integer.parseInt(vertices[0]);
				} else {
					if (Integer.parseInt(vertices[1]) > maxId)
						maxId = Integer.parseInt(vertices[1]);
				}
			}

			for (int i = 0; i <= maxId; i++) {
				_a.add(i);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return _a.toArray(new Integer[_a.size()]);
	}

}
