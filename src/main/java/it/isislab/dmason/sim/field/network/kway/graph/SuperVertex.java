package it.isislab.dmason.sim.field.network.kway.graph;

import java.util.HashSet;

/**
 * this class represents a superVertex and stores a collection of vertex.
 * 
 * @author aleant
 */
public class SuperVertex extends Vertex{

	private static final long serialVersionUID = 5347448844810974692L;
	
	private HashSet<Vertex> original_vertex;
	private int number_selfEdges;		//number of self edges

	public SuperVertex(int _id) {
		super(_id);
		original_vertex = new HashSet<Vertex>();
		number_selfEdges = 0;
	}
	
	public SuperVertex(int _id,HashSet<Vertex> original_vertex) {
		super(_id);
		this.original_vertex = original_vertex;
		number_selfEdges = 0;
	}
	
	public HashSet<Vertex> getOriginal_vertex() {
		return original_vertex;
	}

	public void setOriginal_vertex(HashSet<Vertex> original_vertex) {
		this.original_vertex = original_vertex;
	}
	
	public String toString() {
		return super.toString()+ " vts: " + original_vertex + " num self edges " + number_selfEdges;
	}
	
	public void incrementSelfEdges(){
		number_selfEdges++;
	}
	
	public void decrementselfEdges(){
		number_selfEdges--;
	}

	public int getNumberSelfEdges() {
		return number_selfEdges;
	}
	
}
