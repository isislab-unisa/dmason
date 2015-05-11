package it.isislab.dmason.sim.field.network.kway.util;

import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.SuperVertex;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;
import it.isislab.dmason.sim.field.network.kway.graph.tools.GraphFormatConverter;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;

import java.util.HashMap;
import java.util.Set;

public class PartitionManager {

	public static NetworkPartition getNetworkPartition(String graph_file_name,String partition_filename,int super_vertex_id)
	{
		String format = graph_file_name.split("\\.")[1];
		Graph g=null;
		if(format.equalsIgnoreCase("edgelist"))
		{
			g = GraphFormatConverter.edgelist2graph(graph_file_name);
		}else
			if(format.equalsIgnoreCase("graph"))
			{
				g=  GraphFormatConverter.dotGraph2graph(graph_file_name);
			}else return null;

		GraphSubscribersEdgeList grpsub = new GraphSubscribersEdgeList();
		HashMap<Integer, Integer> vertex_part = new HashMap<Integer, Integer>();
		Graph superGraph = Graph.createSuperVertex(partition_filename, vertex_part);
		Graph.addSuperEdges(g, superGraph, vertex_part);

		for(Vertex v: superGraph.vertexSet())
		{
//			if(v.getId()==super_vertex_id)
//			{
					Set<Edge> edges=superGraph.edgesOf(v);
					for(Edge e: edges)
					{
						int source=e.getSource().getId();
						int target=e.getTarget().getId();
						if(source!=target)
						   grpsub.addEdge(source, target, false);
					}
				
//				break;
//			}
		}
		HashMap<Integer, Vertex> parts2graph=new HashMap<Integer, Vertex>();
		HashMap<Integer, Integer> parts2graph_id=new HashMap<Integer, Integer>();
		HashMap<Vertex, Integer> graph2parts=new HashMap<Vertex, Integer>();
		HashMap<Integer, SuperVertex> parts2SuperGraph=new HashMap<Integer, SuperVertex> ();
		
		for(Vertex v: superGraph.vertexSet())
		{
			SuperVertex sv=(SuperVertex)v;
			parts2SuperGraph.put(sv.getId(), sv);
			
			for(Vertex vv:sv.getOriginal_vertex())
			{
				parts2graph.put(sv.getId(), vv);
				parts2graph_id.put(sv.getId(), vv.getId());
				graph2parts.put(vv,sv.getId());
			}
		}
		return new NetworkPartition(g, superGraph, parts2graph, parts2graph_id,graph2parts,parts2SuperGraph, grpsub);
	}
}
