package it.isislab.dmason.sim.field.network.kway.graph;

import java.io.Serializable;
import org.jgrapht.graph.DefaultEdge;

public class Edge extends DefaultEdge implements Serializable{

	private static final long serialVersionUID = 3449952080817016948L;
	
	private int weight;

	public Vertex getSource(){
		return  (Vertex) super.getSource();
	}

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
