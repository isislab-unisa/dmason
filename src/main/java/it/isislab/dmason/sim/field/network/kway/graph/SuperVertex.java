package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.util.HashSet;

/**
 * this class represents a superVertex and stores a collection of vertex.
 */
@AuthorAnnotation(author = { "Alessia Antelmi","Gennaro Cordasco", "Carmine Spagnuolo" }, date = "20/7/2015")
public class SuperVertex extends Vertex{

	private static final long serialVersionUID = 5347448844810974692L;
	
	private HashSet<Vertex> original_vertex;
	private int number_selfLoops;		//number of self edges

	public SuperVertex(int _id) {
		super(_id);
		original_vertex = new HashSet<Vertex>();
		number_selfLoops = 0;
	}
	
	public SuperVertex(int _id,HashSet<Vertex> original_vertex) {
		super(_id);
		this.original_vertex = original_vertex;
		number_selfLoops = 0;
	}
	
	public HashSet<Vertex> getOriginal_vertex() {
		return original_vertex;
	}

	public void setOriginal_vertex(HashSet<Vertex> original_vertex) {
		this.original_vertex = original_vertex;
	}
	
	@Override
	public String toString() {
		return super.toString()+ " vts: " + original_vertex + " num self edges " + number_selfLoops;
	}
	
	public void incrementSelfLoops(){
		number_selfLoops++;
	}
	
	public void decrementselfLoops(){
		number_selfLoops--;
	}

	public int getNumberSelfLoops() {
		return number_selfLoops;
	}
	
}
