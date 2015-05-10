package it.isislab.dmason.sim.field.network.kway.graph;

import java.util.HashSet;

/**
 * this class represents a superEdge, that spans over multiple partitions.
 * It stores a collection of edges, 
 * and its weight is the sum of the weights of the edges it stores
 * 
 * @author aleant
 */

public class SuperEdge extends Edge{

	private static final long serialVersionUID = -73145959729902958L;
	
	private HashSet<Edge> original_edges;
	private int weight_superEdge;		
	
	public SuperEdge() {
		original_edges= new HashSet<Edge>();
		weight_superEdge = 0;
	}
	
	public SuperEdge(HashSet<Edge> original_edges) {
		this.original_edges = original_edges;
		weight_superEdge = 0;
	}
	
	public HashSet<Edge> getOriginal_edges() {
		return original_edges;
	}

	public void setOriginal_edges(HashSet<Edge> original_edges) {
		this.original_edges = original_edges;
	}
	
	public String toString() {
		return super.toString()+ " ets: " + original_edges + " edge weight " + weight_superEdge;
	}

	public int getWeight_Egde() {
		return weight_superEdge;
	}

	/**
	 * adds an edge to superEdge
	 * and increments superEdge weight 
	 * @param e - new edge to add
	 */
	public void addEdge(Edge e){
		original_edges.add(e);
		this.setOriginal_edges(original_edges);
		weight_superEdge += e.getWeight();
	}
	
	/**
	 * remove the edge to superEdge
	 * and decrements superEdge weight 
	 * @param e - edge to remove
	 */
	public void removeEdge(Edge e){
		original_edges.remove(e);
		this.setOriginal_edges(original_edges);
		weight_superEdge -= e.getWeight();
	}
	
}







