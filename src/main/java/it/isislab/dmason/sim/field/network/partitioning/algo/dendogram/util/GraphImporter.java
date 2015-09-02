package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
@Deprecated
public interface GraphImporter<V,E> {
	
	public Graph<V,DefaultWeightedEdge> getGraph();

}
