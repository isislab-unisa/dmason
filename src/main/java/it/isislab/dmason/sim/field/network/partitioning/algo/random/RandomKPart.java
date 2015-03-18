package it.isislab.dmason.sim.field.network.partitioning.algo.random;

import it.isislab.dmason.sim.field.network.partitioning.interfaces.LabelVertex;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

public class RandomKPart {
	
	SimpleGraph<LabelVertex, DefaultWeightedEdge> graph;
	
	//Fisher-Yates
	static void shuffleArray(int[] ar)
	  {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	
	public RandomKPart(SimpleGraph<LabelVertex, DefaultWeightedEdge> g) {
		graph = g;
	}
	
	public void start(int numPart) {
		Set<LabelVertex> vertex = graph.vertexSet();
		Iterator<LabelVertex> vertex_i = vertex.iterator();
		
		//int communityAvgSize = vertex.size()/numPart;
		//System.out.println("Community average size: " + communityAvgSize);
		
		int labels[] = new int[vertex.size()];
		
		for(int i = 0; i < labels.length; i++) {
			labels[i] = i % numPart;
		}

		RandomKPart.shuffleArray(labels);
		
		int i = 0;
		while(vertex_i.hasNext()) {
			vertex_i.next().setLabel(labels[i]);
			i++;
		}
	}
	
//	public static void main(String args[]) {
//		GephiImporter GI = new GephiImporter("datasetGML/polblogs.gml");
//		SimpleGraph<LabelVertex, DefaultWeightedEdge> g = GI.convertForDendrogram();
//		RandomKPart r = new RandomKPart(g);
//		r.start(400);
//	}
}
