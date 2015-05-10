package it.isislab.dmason.sim.field.network.kway.graph;

import java.io.Serializable;

import org.jgrapht.VertexFactory;

public class Vertex implements Serializable, VertexFactory<Vertex> {

	private static final long serialVersionUID = -77148777124646414L;
	
	private int id;		 	
	private int threshold;
	private int cost;
	
	public Vertex() {
		threshold=0;
		cost=1;
	}
	
	public Vertex(int _id) {
		id=_id;
		threshold=0;
		cost=1;
	}

	public Vertex(int _id, int _threshold, int _cost) {
		id=_id;
		threshold=_threshold;
		cost=_cost;
	}
	
	public Vertex createVertex() {
		return new Vertex();
	}

	public int getId() {	return id;	}

	public void setId(int id) {	this.id = id;	}

	public int getThreshold() {	return threshold;	}

	public void setThreshold(int threshold) {	this.threshold = threshold;	}

	public int getCost() {	return cost;	}

	public void setCost(int cost) {	this.cost = cost;	}

	public boolean equals(Object obj) {
		Vertex ver = null;
		
		if (obj instanceof Vertex) {
			ver = (Vertex) obj;
		}
		else
			return false;
		 
		return ver.getId() == this.getId();
	}
	
	public String toString() {	return id + ""; }
	
	public int hashCode() {
		return id;
	}

}




