package it.isislab.dmason.sim.field.support.network;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class ImportSerializedSimpleGraph<V,E> {
	
	private String file;
	private FileInputStream fis = null;
	private ObjectInputStream in = null;
	private SimpleGraph<V, E> graph;
	private HashMap<Integer,SimpleGraph<V,E>> partition;
	private GraphSubscribersEdgeList group;
	
	public ImportSerializedSimpleGraph(String filename) {
		partition= new HashMap<Integer, SimpleGraph<V,E>>();
		 try {
		      fis = new FileInputStream(filename);
		      in = new ObjectInputStream(fis);
		      graph = (SimpleGraph<V,E>) in.readObject();
		      //in.close();
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Edges: "+graph.edgeSet().size());
		 System.out.println("Vertexes: "+graph.vertexSet().size());
		 group = new GraphSubscribersEdgeList();
		 process();
	}

	private void process() {
		Set<E> edgeSet = graph.edgeSet();
		for (E e : edgeSet) {
			BasicVertex source = (BasicVertex) graph.getEdgeSource(e);
			BasicVertex target = (BasicVertex) graph.getEdgeTarget(e);
			int sourceComm = source.getCommunity();
			int targetComm = target.getCommunity();
			if(sourceComm!=targetComm)
			{
				group.addEdge(sourceComm, targetComm, false);
				
				if(partition.get(sourceComm)==null)
					partition.put(sourceComm, new SimpleGraph<V, E>(((Class<? extends E>) DefaultEdge.class)));
				if(partition.get(targetComm)==null)
					partition.put(targetComm, new SimpleGraph<V, E>(((Class<? extends E>) DefaultEdge.class)));
				
				SimpleGraph<V, E> partGraph = partition.get(sourceComm);
				partGraph.addVertex(graph.getEdgeSource(e));
				partGraph.addVertex(graph.getEdgeTarget(e));
				partGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
				partition.put(sourceComm, partGraph);
				
				partGraph = partition.get(targetComm);
				partGraph.addVertex(graph.getEdgeSource(e));
				partGraph.addVertex(graph.getEdgeTarget(e));
				partGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
				partition.put(targetComm, partGraph);
			}
			else
			{
				if(partition.get(sourceComm)==null)
					partition.put(sourceComm, new SimpleGraph<V, E>(((Class<? extends E>) DefaultEdge.class)));
				SimpleGraph<V, E> partGraph = partition.get(sourceComm);
				partGraph.addVertex(graph.getEdgeSource(e));
				partGraph.addVertex(graph.getEdgeTarget(e));
				partGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
				partition.put(sourceComm, partGraph);
			}
			
		}
	}
	
	public SimpleGraph<V,E> getCommunity(int comm)
	{
		return partition.get(comm);
	}
	
	public int getNumberOfPartitions()
	{
		return partition.size();
	}
	
	public GraphSubscribersEdgeList getCommunicationLinks()
	{
		return group;
	}
	
	public static<V,E> void main(String[] args) {
		ImportSerializedSimpleGraph<V, E> imp = new ImportSerializedSimpleGraph<V, E>("karate.data");
		for (int i = 0; i < 4; i++) {
			System.out.println(imp.getCommunity(i));
		}
	}
	
}
