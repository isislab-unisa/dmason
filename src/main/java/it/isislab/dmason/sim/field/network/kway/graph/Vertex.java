package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.Serializable;

import org.jgrapht.VertexFactory;
@AuthorAnnotation(author = { "Alessia Antelmi", "Gennaro Cordasco","Carmine Spagnuolo" }, date = "20/7/2015")
public class Vertex implements Serializable, VertexFactory<Vertex> {

	private static final long serialVersionUID = -77148777124646414L;

	private int id;
	private int threshold;
	private int cost;
	private double dispersion;

	public Vertex() {
		threshold = 0;
		cost = 1;
	}

	public Vertex(int _id) {
		id = _id;
		threshold = 0;
		cost = 1;
	}

	public Vertex(int _id, int _threshold, int _cost) {
		id = _id;
		threshold = _threshold;
		cost = _cost;
	}

	public Vertex createVertex() {
		return new Vertex();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public double getDispersion() {
		return dispersion;
	}

	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}

	@Override
	public boolean equals(Object obj) {
		Vertex ver = null;

		if (obj instanceof Vertex) {
			ver = (Vertex) obj;
		} else
			return false;

		return ver.getId() == this.getId();
	}

	@Override
	public String toString() {
		return id + "";
	}

	@Override
	public int hashCode() {
		return id;
	}

}
