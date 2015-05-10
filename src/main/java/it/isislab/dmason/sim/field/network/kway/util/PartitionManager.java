package it.isislab.dmason.sim.field.network.kway.util;

import java.util.HashMap;
import java.util.Set;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;
import it.isislab.dmason.sim.field.network.kway.graph.tools.GraphFormatConverter;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;

public class PartitionManager {

	public static Graph getGraph(String part_file_name)
	{
		String format = part_file_name.split("\\.")[1];
		if(format.equalsIgnoreCase("edgelist"))
		{
			return GraphFormatConverter.edgelist2graph(part_file_name);
		}else
			if(format.equalsIgnoreCase("graph"))
			{
				return  GraphFormatConverter.dotGraph2graph(part_file_name);
			}
		return null;
	}
	public static GraphSubscribersEdgeList getGraphSubscribersEdgeList(Graph g,int super_vertex_id,String partition_filename)
	{
		GraphSubscribersEdgeList grpsub = new GraphSubscribersEdgeList();
		HashMap<Integer, Integer> vertex_part = new HashMap<Integer, Integer>();
		Graph superGraph = Graph.createSuperVertex(partition_filename, vertex_part);
		Graph.addSuperEdges(g, superGraph, vertex_part);
		
		for(Vertex v: superGraph.vertexSet())
		{
			if(v.getId()==super_vertex_id)
			{
				Set<Edge> edges=superGraph.edgesOf(v);
				for(Edge e: edges)
				{
					int source=e.getSource().getId();
					int target=e.getSource().getId();
					grpsub.addEdge(source, target, false);
				}
				break;
			}
		}

		return grpsub;
	}
}
