package it.isislab.dmason.sim.field.network.partitioning;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DTree;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DendrogramExtractSubGraph;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DendrogramFixCommunity;
import it.isislab.dmason.sim.field.network.partitioning.algo.random.RandomKPart;
import it.isislab.dmason.sim.field.network.partitioning.algo.ukwaypart.UKWayPartRuntimeExec;
import it.isislab.dmason.sim.field.network.partitioning.interfaces.GraphVertex;
import it.isislab.dmason.sim.field.network.partitioning.interfaces.LabelVertex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

@Deprecated
public class Partitioner {
	
	@SuppressWarnings("rawtypes")
	public static Graph<LabelVertex, DefaultWeightedEdge> executeRandomAlgorithm(Graph<LabelVertex, DefaultWeightedEdge> g, int nPart) {
		RandomKPart rm = new RandomKPart((SimpleGraph<LabelVertex, DefaultWeightedEdge>) g);
		rm.start(nPart);
		
		return g;
	}
	
	
	//This method return a partitioned graph
	//partitioning obtained using the "UKWayPart Algorithm" by Andrea Solda', inspired by METIS UKWayPart
	public static<V, E> Graph<LabelVertex<V>, E> executeUKWayPart(CellType ID,Graph<V, E> g, int nPart) throws IOException, InterruptedException, DMasonException{		
		return getLabeledGraph(g, UKWayPartRuntimeExec.runUKWayPart(ID,g, nPart));
	}

	//Using Dendogram Fix Method by Francesco Milone 
	public static<V, E> Graph<LabelVertex<V>, E> executeDendogramFixMethod(Graph<V, E> g, int num_worker, double scaling_factor, int numberOfIterations, double resol) throws Exception {

		DendrogramFixCommunity<V, E> df = new DendrogramFixCommunity<V, E>(g, num_worker, scaling_factor, numberOfIterations, resol); 

		df.start();

		DTree<V, E> ts = df.getDendrogram();
		TreeSet<V>[] clusters = ts.getWorkersLoad(df.getLoad(), df.getNumWorkers());
		HashMap<V, Integer> vertexToCluster = new HashMap<V, Integer>();
		TreeSet<V> current;
		for(int i = 0; i < clusters.length; i++) {
			current = clusters[i];
			for(V v : current) {
				vertexToCluster.put(v, i);
			}
		}

		return getLabeledGraph(g, vertexToCluster);
	}

	//Using Dendogram Extract Method by Francesco Milone
	public static<V, E> Graph<LabelVertex<V>, E> executeDendogramExtractMethod(Graph<V, E> g, int num_worker, double scaling_factor, int numberOfIterations, double resol, int num_proc) throws Exception {

		DendrogramExtractSubGraph<V, E> df = new DendrogramExtractSubGraph<V, E>(g, num_worker, scaling_factor, numberOfIterations, resol, num_proc); 

		df.start();

		DTree<V, E> ts = df.getDendrogram();
		TreeSet<V>[] clusters = ts.getWorkersLoad(df.getLoad(), df.getNumWorkers());
		HashMap<V, Integer> vertexToCluster = new HashMap<V, Integer>();
		TreeSet<V> current;
		for(int i = 0; i < clusters.length; i++) {
			current = clusters[i];
			for(V v : current) {
				vertexToCluster.put(v, i);
			}
		}

		return getLabeledGraph(g, vertexToCluster);
	}

	//Using Balanced Label Propagation by Ada Mancuso
	//	public static<V, E> SimpleGraph<LabelVertex, DefaultWeightedEdge> executeBLP(String filename, int now) throws Exception
	//	{
	//		GraphMLImporter<V, E> imp = new GraphMLImporter<V, E>(filename);
	//		@SuppressWarnings("unchecked")
	//		SimpleGraph<V, E> graph = new SimpleGraph<V, E>((Class<? extends E>) DefaultEdge.class);
	//		Graph<V, DefaultWeightedEdge> g = imp.getGraph();
	//		for(DefaultWeightedEdge e : g.edgeSet()) {
	//			V source = g.getEdgeSource(e);
	//			V target = g.getEdgeTarget(e);
	//			graph.addVertex(source);
	//			graph.addVertex(target);
	//			graph.addEdge(source, target);
	//		}
	//		FileWriter fw = new FileWriter("tmp.gml");
	//		new GmlExporter<V, E>().export(fw, graph);
	//		fw.close();
	//		ArrayList<SimpleGraph<Integer,Integer>> queueGraphs = PPllLPAlgo.importGraph("tmp.gml");
	//		PPllLPAlgo<V, E> algo = new PPllLPAlgo<V, E>("tmp.gml", now, queueGraphs);
	//		algo.start();
	//		GephiImporter gi = new GephiImporter(now + "_result.gml");
	//		return gi.convertForDendrogram();
	//	}

	public static<V, E> Graph<LabelVertex<V>, E> getLabeledGraph(Graph<V, E> g, HashMap<V, Integer> vertexToCluster) {
		@SuppressWarnings("unchecked")
		SimpleGraph<LabelVertex<V>, E> toReturn = new SimpleGraph<LabelVertex<V>, E>((Class<? extends E>) DefaultWeightedEdge.class);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HashMap<V, LabelVertex<V>> labelToLabelVertex = new HashMap();
		for(E e : g.edgeSet())
		{
			V source = g.getEdgeSource(e);
			V target = g.getEdgeTarget(e);
			if(labelToLabelVertex.get(source)==null)
			{
				GraphVertex<V> gv = new GraphVertex<V>(source, 0);
				if(vertexToCluster.get(source)==null)System.out.println(source);
				gv.setCommunity(vertexToCluster.get(source));
				labelToLabelVertex.put(source,gv);
				toReturn.addVertex(gv);
			}

			if(labelToLabelVertex.get(target)==null)
			{
				GraphVertex<V> gv = new GraphVertex<V>(target, 0);
				gv.setCommunity(vertexToCluster.get(target));
				labelToLabelVertex.put(target,gv);
				toReturn.addVertex(gv);
			}

			toReturn.addEdge(labelToLabelVertex.get(source), labelToLabelVertex.get(target));
		}
		return toReturn;
	}
	

	@SuppressWarnings("rawtypes")
	public static<E, V> Graph<E, V> getCommunity(Graph<E, V> partGraph, Class<V> cl, int label/*, int aoi*/) {
		SimpleGraph<E, V> toReturn = new SimpleGraph<E, V>(cl);
		Iterator<E> vertex_i = partGraph.vertexSet().iterator();
		NeighborIndex<E, V> ni = new NeighborIndex<E, V>(partGraph);
		E next;

//		while(vertex_i.hasNext()) {
//			next = vertex_i.next();
//			if(((LabelVertex)next).getCommunity() == label) {
//				toReturn.addVertex(next);
//				List<E> neighbor = ni.neighborListOf(next);
//				for(int j = 0; j < neighbor.size(); j++) {
//					if(((LabelVertex) neighbor.get(j)).getCommunity() == label) {
//						toReturn.addVertex(neighbor.get(j));
//						toReturn.addEdge(next, neighbor.get(j));
//						for(int i = 0; i < aoi; i++) {
//							neighbor_i = ni.neighborListOf(next).iterator();
//							while(neighbor_i.hasNext()) {
//								next_neighbor = neighbor_i.next();
//								if(((LabelVertex) next_neighbor).getCommunity() != label) {
//									toReturn.addVertex(next_neighbor);
//									toReturn.addEdge(next, next_neighbor);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
		
		//aggiunta nodi
		while(vertex_i.hasNext()) {
			next = vertex_i.next();
			if(((LabelVertex)next).getCommunity() == label) {
				toReturn.addVertex(next);
			}
		}
		
		Iterator<E> temp_i = toReturn.vertexSet().iterator();
		
		//aggiunta archi interni alla community
		while(temp_i.hasNext()) {
			next = temp_i.next();
			List<E> list = ni.neighborListOf(next);
			for(int i = 0; i < list.size(); i++) {
				if(((LabelVertex)list.get(i)).getCommunity() == label) toReturn.addEdge(next, list.get(i));
			}
		}
		
		@SuppressWarnings("unchecked")
		SimpleGraph<E, V> yolo = (SimpleGraph<E, V>) toReturn.clone();
		temp_i = yolo.vertexSet().iterator();
		
		while(temp_i.hasNext()) {
			next = temp_i.next();
			List<E> list = ni.neighborListOf(next);
			for(int i = 0; i < list.size(); i++) {
				if(((LabelVertex)list.get(i)).getCommunity() != label) {
					toReturn.addVertex(list.get(i));
					toReturn.addEdge(next, list.get(i));
				}
			}
		}
		
		return toReturn;
	}
	
	public static SuperGraphStats generateSuperGraphStat(Graph<LabelVertex<Integer>, DefaultWeightedEdge> graph) {
		int pesoSupergrafo=0;

		SimpleGraph<Integer, DefaultWeightedEdge> superG = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		for(DefaultWeightedEdge e : graph.edgeSet())
		{
			int a = (int)Double.parseDouble(String.valueOf(graph.getEdgeSource(e).getCommunity()));
			int b = (int)Double.parseDouble(String.valueOf(graph.getEdgeTarget(e).getCommunity()));

			if(!superG.containsEdge(a, b) && a != b) {
				superG.addVertex(a);
				superG.addVertex(b);
				superG.addEdge(a, b);
			}

			if(a != b) {
				pesoSupergrafo++;
			}
		}

		 return new SuperGraphStats(pesoSupergrafo, superG.edgeSet().size(),superG);
				
//		System.err.println("\n------------------Metriche supergrafo indotto-------------------");
//		System.err.println("Peso supergrafo: "+ pesoSupergrafo);
//		System.err.println("Archi supergrafo: " + superG.edgeSet().size());
//		System.err.println("Componenti connesse supregrafo: " + superG.vertexSet().size());	
//		System.err.println("\n------------------Metriche supergrafo indotto-------------------");
	}
	
	public static SuperGraphStats generateSuperGraphStatString(Graph<LabelVertex<String>, DefaultWeightedEdge> graph) {
		int pesoSupergrafo=0;

		SimpleGraph<Integer, DefaultWeightedEdge> superG = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		for(DefaultWeightedEdge e : graph.edgeSet())
		{
			int a = (int)Double.parseDouble(String.valueOf(graph.getEdgeSource(e).getCommunity()));
			int b = (int)Double.parseDouble(String.valueOf(graph.getEdgeTarget(e).getCommunity()));

			if(!superG.containsEdge(a, b) && a != b) {
				superG.addVertex(a);
				superG.addVertex(b);
				superG.addEdge(a, b);
			}

			if(a != b) {
				pesoSupergrafo++;
			}
		}

		 return new SuperGraphStats(pesoSupergrafo, superG.edgeSet().size(),superG);
				
//		System.err.println("\n------------------Metriche supergrafo indotto-------------------");
//		System.err.println("Peso supergrafo: "+ pesoSupergrafo);
//		System.err.println("Archi supergrafo: " + superG.edgeSet().size());
//		System.err.println("Componenti connesse supregrafo: " + superG.vertexSet().size());	
//		System.err.println("\n------------------Metriche supergrafo indotto-------------------");
	}
	
	
}
