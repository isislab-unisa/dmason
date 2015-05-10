package it.isislab.dmason.sim.field.network.kway.graph.tools;



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
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * auxiliary class for converting 
 * between different representation formats of a graph
 * @author aleant
 */

public class GraphFormatConverter {

	private static Logger logger = Logger.getLogger("graph-converter");

	public static void main(String[] args) {

		if(args.length < 1){
			logger.info("Insert filepath and output filename..");
			System.exit(1);
		}

		String filepath = args[0];
		String outFilename = args[1];

		edgelist2dotGraph(filepath, outFilename);
	}

	/**
	 * converts a file from edgelist to csv
	 * saving the minimum vertex id
	 * @param filepath - file edgelist
	 * @param outFilename - file csv 
	 */
	public static void edgelist2csvMinId(String filepath, String outFilename){
		String edge = "";
		String info = "";
		String separator = "";
		int id_min = 0, id_one, id_two;
		String[] vertices = new String[2];

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			edge = in.readLine();
			separator = findSeparator(edge);		

			vertices = edge.split(separator);
			info = vertices[0] + ";" + vertices[1] + ";";	

			out.write(info.getBytes());

			id_one = Integer.parseInt(vertices[0]);
			id_two = Integer.parseInt(vertices[1]);

			id_min = id_one;

			if(id_two < id_min)
				id_min = id_two;

			while((edge = in.readLine()) != null){

				vertices = edge.split(separator);
				info = vertices[0] + ";" + vertices[1] + ";";	

				id_one = Integer.parseInt(vertices[0]);
				id_two = Integer.parseInt(vertices[1]);

				if(id_one < id_min)
					id_min = id_one;

				if(id_two < id_min)
					id_min = id_two;

				out.write(info.getBytes());
			}


			String min = id_min + "";
			//info = info.substring(0, info.length()-1);
			out.write(min.getBytes());

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * converts a file from edgelist to csv
	 * @param filepath - file edgelist
	 * @param outFilename - file csv 
	 */
	public static void edgelist2csv(String filepath, String outFilename){
		String edge = "";
		String info = "";
		String separator = "";
		String[] vertices = new String[2];

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			edge = in.readLine();
			separator = findSeparator(edge);		

			vertices = edge.split(separator);
			info += vertices[0] + ";" + vertices[1] + ";";	

			while((edge = in.readLine()) != null){

				vertices = edge.split(separator);
				info += vertices[0] + ";" + vertices[1] + ";";				
			}

			info = info.substring(0, info.length()-1);
			out.write(info.getBytes());

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * converts a file from csv to edgelist
	 * @param baseline - file csv
	 * @param outFilename - file edgelist 
	 */
	public static void csv2edgelist(String filepath, String outFilename){
		String edges = "", separator = "";
		String e = "";
		String[] vertices;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			edges = in.readLine();
			separator = findSeparator(edges);
			vertices = edges.split(separator);

			for(int i=0; i<vertices.length - 1; i+=2){
				e = vertices[i] + " " + vertices[i+1] + "\n";

				out.write(e.getBytes());
			}

			in.close();
			out.close();

		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}



	/**
	 * converts a file from edgelist to graph
	 * @param filepath - file edgelist
	 * @param outFilename - file graph
	 */
	public static void edgelist2dotGraph(String filepath, String outFilename){
		String edge = "", info = "", separator = "";
		int numE = 0, firstID, secondID;
		boolean isInv;
		String[] vertices = new String[2];
		/* mapping between a vertex(key) and its neighbor */
		TreeMap<Integer, ArrayList<Integer>> verticesAdj = new TreeMap<Integer, ArrayList<Integer>>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".graph"));

			while((edge = in.readLine()) != null){

				isInv = false;
				separator = findSeparator(edge);
				vertices = edge.split(separator);

				/* vertex ids in a file.graph start from 1 */
				firstID = Integer.parseInt(vertices[0]) + 1;
				secondID = Integer.parseInt(vertices[1]) + 1;

				/* if the first vertex has never been processed */
				if(!verticesAdj.containsKey(firstID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(secondID);
					verticesAdj.put(firstID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(firstID);
					if(!adj.contains(secondID)){
						adj.add(secondID);
					}
					else
						isInv = true;
				}

				/* if the second vertex has never been processed */
				if(!verticesAdj.containsKey(secondID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(firstID);
					verticesAdj.put(secondID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(secondID);
					if(!adj.contains(firstID)){
						adj.add(firstID);
					}
					else 
						isInv = true;
				}

				if(!isInv)
					numE++;
			}

			/* first row: #nodes #edges */
			info = verticesAdj.size() + " " + numE + "\n";
			out.write(info.getBytes());

			/* such an adjacency list  */
			for(Integer key: verticesAdj.keySet()){
				info = "";
				ArrayList<Integer> adjV = verticesAdj.get(key);

				for(Integer node : adjV){
					info += node + " ";
				}
				info += "\n";
				out.write(info.getBytes());	
			}

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * converts a file from edgelist to graph
	 * @param filepath - file edgelist
	 * @param outFilename - file graph
	 */
	public static void edgelist2dotGraphMinId(String filepath, String outFilename, int min_id){
		String edge = "", info = "", separator = "";
		int numE = 0, firstID = 0, secondID = 0;
		boolean isInv;
		String[] vertices = new String[2];
		/* mapping between a vertex(key) and its neighbor */
		TreeMap<Integer, ArrayList<Integer>> verticesAdj = new TreeMap<Integer, ArrayList<Integer>>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".graph"));

			while((edge = in.readLine()) != null){

				isInv = false;
				separator = findSeparator(edge);
				vertices = edge.split(separator);

				switch (min_id) {
				case 0:
					/* vertex ids in a file.graph start from 1 */
					firstID = Integer.parseInt(vertices[0]) + 1;
					secondID = Integer.parseInt(vertices[1]) + 1;
					break;

				case 1:
					/* vertex ids in a file.graph start from 1 */
					firstID = Integer.parseInt(vertices[0]);
					secondID = Integer.parseInt(vertices[1]);
					break;

				default:
					break;
				}

				/* if the first vertex has never been processed */
				if(!verticesAdj.containsKey(firstID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(secondID);
					verticesAdj.put(firstID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(firstID);
					if(!adj.contains(secondID)){
						adj.add(secondID);
					}
					else
						isInv = true;
				}

				/* if the second vertex has never been processed */
				if(!verticesAdj.containsKey(secondID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(firstID);
					verticesAdj.put(secondID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(secondID);
					if(!adj.contains(firstID)){
						adj.add(firstID);
					}
					else 
						isInv = true;
				}

				if(!isInv)
					numE++;
			}

			/* first row: #nodes #edges */
			info = verticesAdj.size() + " " + numE + "\n";
			out.write(info.getBytes());

			/* such an adjacency list  */
			for(Integer key: verticesAdj.keySet()){
				info = "";
				ArrayList<Integer> adjV = verticesAdj.get(key);

				for(Integer node : adjV){
					info += node + " ";
				}
				info += "\n";
				out.write(info.getBytes());	
			}

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * converts a file from graph to edgelist
	 * @param filepath - file graph
	 * @param outFilename - file edgelist
	 */
	public static void dotGraph2edgelist(String filepath, String outFilename, Integer[] vertex_names){
		String vertexInfo = "", edge = "", separator = ""; 
		String[] adj;
		int id = 0, realVertexId;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			separator = findSeparator(in.readLine()); //first row is an info one

			while((vertexInfo = in.readLine()) != null){
				adj = vertexInfo.split(separator);
				
				if(vertexInfo.length()<=0){
					id++;
					continue;
				}
				
				for(String v : adj){
					if(v.length()<=0) continue;
					realVertexId = (Integer.parseInt(v));
					edge = vertex_names[id] + " " + realVertexId + "\n";
					out.write(edge.getBytes());
				}

				id++;
			}

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * Reads a graph described as an edgelist
	 * and conctructs the object Graph associated with it
	 * @param filepath - edgelist file representing a graph
	 * @return g - the object Graph associated with this representation
	 */
	public static Graph edgelist2graph(String filepath){
		Graph g = new Graph();
		String edge = "", separator = "";
		String[] vertices;
		Vertex firstV, secondV;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));

			while((edge = in.readLine()) != null){

				separator = findSeparator(edge);
				vertices = edge.split(separator);

				String id_one = vertices[0];
				String id_two = vertices[1];

				firstV = new Vertex(Integer.parseInt(id_one));
				secondV = new Vertex(Integer.parseInt(id_two));

				if(!g.containsVertex(firstV))  			
					g.addVertex(firstV);				

				if(!g.containsVertex(secondV)){
					g.addVertex(secondV);
				}					

				Edge e = new Edge();
				e.setWeight(1);
				g.addEdge(firstV, secondV, e);

			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return g;
	}

	/**
	 * produces a file describing
	 * the graph in edgelist mode
	 * @param graph - the object graph 
	 * @param outFilename - file edgelist
	 */
	public static void graph2edgelist(Graph graph, String outFilename){

		Set<Edge> edges = graph.edgeSet();
		String edge = "";

		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			for(Edge e : edges){

				edge = e.getSource() + " " + e.getTarget() + "\n";
				out.write(edge.getBytes());
			}

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * converts a file from csv to graph
	 * @param filepath - csv file
	 * @param outFilename - file graph
	 */
	public static void csv2dotGraph(String filepath, String outFilename){
		String edges = "", info = "", separator = "";
		int numE = 0, firstID, secondID;
		boolean isInv;
		String[] vertices;
		/* mapping between a vertex(key) and its neighbor */
		TreeMap<Integer, ArrayList<Integer>> verticesAdj = new TreeMap<Integer, ArrayList<Integer>>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".graph"));

			edges = in.readLine();
			separator = findSeparator(edges);
			vertices = edges.split(separator);

			for(int i=0; i<vertices.length - 1; i+=2){

				isInv = false;

				/* vertex ids in a file.graph start from 1 */
				firstID = Integer.parseInt(vertices[i]) + 1;
				secondID = Integer.parseInt(vertices[i+1]) + 1;

				/* if the first vertex has never been processed */
				if(!verticesAdj.containsKey(firstID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(secondID);
					verticesAdj.put(firstID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(firstID);
					if(!adj.contains(secondID)){
						adj.add(secondID);
					}
					else
						isInv = true;
				}

				/* if the second vertex has never been processed */
				if(!verticesAdj.containsKey(secondID)){
					ArrayList<Integer> adj = new ArrayList<Integer>();
					adj.add(firstID);
					verticesAdj.put(secondID, adj);
				}else{
					ArrayList<Integer> adj = verticesAdj.get(secondID);
					if(!adj.contains(firstID)){
						adj.add(firstID);
					}
					else 
						isInv = true;
				}

				if(!isInv)
					numE++;
			}

			/* first row: #nodes #edges */
			info = verticesAdj.size() + " " + numE + "\n";
			out.write(info.getBytes());

			/* such an adjacency list  */
			for(Integer key: verticesAdj.keySet()){
				info = "";
				ArrayList<Integer> adjV = verticesAdj.get(key);

				for(Integer node : adjV){
					info += node + " ";
				}
				info += "\n";
				out.write(info.getBytes());	
			}

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * converts a file from graph to csv
	 * @param filepath - file graph
	 * @param outFilename - csv file
	 */
	public static void dotGraph2csv(String filepath, String outFilename){
		String vertexInfo = "", edge = "", separator = ""; 
		String[] adj;
		int id = 0, realVertexId;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			separator = findSeparator(in.readLine()); //first row is an info one

			while((vertexInfo = in.readLine()) != null){

				adj = vertexInfo.split(separator);

				for(String v : adj){
					realVertexId = (Integer.parseInt(v) - 1);
					edge += id + ";" + realVertexId + ";";
				}

				id++;
			}

			edge = edge.substring(0, edge.length()-1);
			out.write(edge.getBytes());

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * converts a file from graph to csv
	 * saving the minimum vertex id
	 * @param filepath - file graph
	 * @param outFilename - csv file
	 */
	public static void dotGraph2csvMinId(String filepath, String outFilename){
		String vertexInfo = "", edge = "", separator = ""; 
		String[] adj;
		int id = 1, realVertexId, minId = Integer.MAX_VALUE;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			separator = findSeparator(in.readLine()); //first row is an info one

			while((vertexInfo = in.readLine()) != null){
				
				adj = vertexInfo.split(separator);

				for(String v : adj){
					if(v.length()<=0)continue;
					realVertexId = Integer.parseInt(v);
					edge = id + ";" + realVertexId + ";";

					out.write(edge.getBytes());

					if(realVertexId < minId)
						minId = realVertexId;
				}

				id++;
			}

			String min = minId + "";
			out.write(min.getBytes());
			//edge = edge.substring(0, edge.length()-1)

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//csv con minimo
	/**
	 * Reads a graph in csv format
	 * and constructs the object Graph associated with it
	 * @param filepath - csv file representing a graph
	 * @return g - the object Graph associated with this representation
	 */
	public static Graph csv2graph(String filepath){
		Graph g = new Graph();
		String edges = "", separator = "";
		String[] vertices;
		Vertex firstV, secondV;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));

			edges = in.readLine();
			separator = findSeparator(edges);
			vertices = edges.split(separator);

			for(int i=0; i<vertices.length - 2; i+=2){
				String id_one = vertices[i];
				String id_two = vertices[i+1];

				firstV = new Vertex(Integer.parseInt(id_one));
				secondV = new Vertex(Integer.parseInt(id_two));

				if(!g.containsVertex(firstV))  			
					g.addVertex(firstV);				

				if(!g.containsVertex(secondV)){
					g.addVertex(secondV);
				}					

				Edge e = new Edge();
				e.setWeight(1);
				g.addEdge(firstV, secondV, e);
			}

			in.close();

		} catch (IOException exc) {
			exc.printStackTrace();
		}

		return g;
	}

	/**
	 * produces a file describing
	 * the graph in csv mode
	 * @param graph - the object graph 
	 * @param outFilename - file csv
	 */
	public static void graph2csv(Graph graph, String outFilename){

		Set<Edge> edges = graph.edgeSet();
		String edge = "";

		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".csv"));

			for(Edge e : edges){

				edge += e.getSource() + ";" + e.getTarget() + ";";

			}

			edge = edge.substring(0, edge.length()-1);
			out.write(edge.getBytes());

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * converts a file from csv to part
	 * @param filepath - csv file
	 * @param outFilename - part file
	 */
	public static void csv2part(String filepath, String outFilename){
		String edges = "", separator = "", e = "", partition;
		HashMap<String, String> part = new HashMap<String, String>();
		String[] vertices;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(filepath))));

			edges = in.readLine();
			separator = findSeparator(edges);
			vertices = edges.split(separator);

			for(int i=0; i<vertices.length - 1; i+=2){
				partition = vertices[i+1];
				e += partition + "\n";

				if(!part.containsKey(partition))
					part.put(partition, null);
			}

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(
							outFilename + ".graph.part." + part.size()));
			out.write(e.getBytes());

			in.close();
			out.close();

		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * converts a file from part to csv
	 * @param filepath - part file
	 * @param outFilename - csv file
	 */
	public static void part2CSV(String filepath, String outFilename, Integer[] vertex_names){
		int id_node = 0;
		String part, info = "";

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename));

			while((part = in.readLine()) != null){
				info += vertex_names[id_node] + ";" + part + ";";

				id_node++;
			}

			info = info.substring(0, info.length()-1);
			out.write(info.getBytes());

			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Reads a graph in graph format
	 * and constructs the object Graph associated with it
	 * @param filepath - graph file representing a graph
	 * @return g - the object Graph associated with this representation
	 */
	public static Graph dotGraph2graph(String filepath){
		Graph g = new Graph();
		String verticesAdj = "", separator = "";
		Vertex firstV;
		Vertex secondV;
		int id = 0;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(filepath))));

			separator = findSeparator(in.readLine()); //la prima riga contiene informazioni sul grafo

			while((verticesAdj = in.readLine()) != null){

				String[] vertices = verticesAdj.split(separator);

				firstV = new Vertex(id);

				if(!g.containsVertex(firstV))  			
					g.addVertex(firstV);	

				for(int i=0; i<vertices.length - 1; i++){
					String id_two = vertices[i];

					secondV = new Vertex(Integer.parseInt(id_two) - 1);

					if(!g.containsVertex(secondV)){
						g.addVertex(secondV);
					}					

					Edge e = new Edge();
					e.setWeight(1);
					g.addEdge(firstV, secondV, e);
				}

				id++;
			}

			in.close();

		} catch (IOException exc) {
			exc.printStackTrace();
		}

		return g;
	}





	private static String findSeparator(String info){
		String separator = "";

		for(int i=0; i<info.length(); i++){
			char c = info.charAt(i);

			if(!Character.isDigit(c)){
				separator = c + "";
				break;
			}	
		}

		return separator;
	}
	
	
	public static Integer[] createMappingNamesGraphDotGraph(String pathGraphName) {

		String edge = "";
		String separator = "";

		String[] vertices = new String[1];

		TreeSet<Integer> _a=new TreeSet<Integer>();
		int last_id=1;
		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathGraphName))));
			edge = in.readLine();
			while((edge = in.readLine()) != null){
				separator = findSeparator(edge);		
				
				if(edge.length()<=0)
				{
					_a.add(last_id);
					last_id++;
					continue;
				}
				vertices = edge.split(separator);
				
				for (int i = 0; i < vertices.length; i++) {
					if(vertices[i].length()>0)
					{
						_a.add(Integer.parseInt(vertices[i]));	   
					}
				}
				last_id++;
			}
			
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return _a.toArray(new Integer[_a.size()]);
	}
	
	
	
	
	public static Integer[] createMappingNamesGraphEdgelist(String pathGraphName) {


		String edge = "";
		String separator = "";

		String[] vertices = new String[2];

		TreeSet<Integer> _a=new TreeSet<Integer>();

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathGraphName))));
			while((edge = in.readLine()) != null){
				separator = findSeparator(edge);		

				vertices = edge.split(separator);
				_a.add(Integer.parseInt(vertices[0]));
				_a.add(Integer.parseInt(vertices[1]));
			}
			
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return _a.toArray(new Integer[_a.size()]);
	}
}






































