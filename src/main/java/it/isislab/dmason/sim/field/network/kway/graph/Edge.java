package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.Serializable;

import org.jgrapht.graph.DefaultEdge;
@AuthorAnnotation(author = { "Alessia Antelmi", "Gennaro Cordasco", "Carmine Spagnuolo" }, date = "20/7/2015")
public class Edge extends DefaultEdge implements Serializable{

	private static final long serialVersionUID = 3449952080817016948L;
	private int weight;

	@Override
	public Vertex getSource(){
		return  (Vertex) super.getSource();
	}

	@Override
	public Vertex getTarget(){
		return (Vertex) super.getTarget();
	}
	
	public Vertex getNeighbor(Vertex v){
		 return (getSource() == v)? getTarget() : getSource();
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
