package it.isislab.dmason.sim.field.network.kway.algo.social;

import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.util.HashMap;

public class GraphWDispersion extends Graph {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GraphWDispersion(){
		super(EdgeWDispersion.class);
	}
	public static GraphWDispersion generateGraphOptimal(int p)
	{
		GraphWDispersion g = new GraphWDispersion();
		HashMap<Integer, Vertex> gvertex=new HashMap<Integer, Vertex>();
		for (int i = 1; i <= (p*4); i++) {
			Vertex v=new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p*4); i++) {
			switch (i%4) {
			case 0:
				if(i!=(p*4))
				g.addEdge(gvertex.get(i), gvertex.get(i+1));
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				g.addEdge(gvertex.get(i), gvertex.get(i-3));
				break;

			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				break;
			default:
				break;
			}
		}
		return g;
	}
	public static GraphWDispersion generateGraphSubOptimal(int p)
	{
		p*=4;
		GraphWDispersion g = new GraphWDispersion();
		HashMap<Integer, Vertex> gvertex=new HashMap<Integer, Vertex>();
		for (int i = 1; i <= (p*4); i++) {
			Vertex v=new Vertex(i);
			g.addVertex(v);
			gvertex.put(i, v);
		}

		for (int i = 1; i <= (p*4); i++) {
			switch (i%4) {
			case 0:
				if(i!=(p*4))
				g.addEdge(gvertex.get(i), gvertex.get(i+1));
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				g.addEdge(gvertex.get(i), gvertex.get(i-3));
				break;
			case 1:
				if(i%16==1)g.addEdge(gvertex.get(i), gvertex.get(i+15));
				break;
			case 2:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				break;

			case 3:
				g.addEdge(gvertex.get(i), gvertex.get(i-1));
				g.addEdge(gvertex.get(i), gvertex.get(i-2));
				break;
			default:
				break;
			}
		}
		return g;
	}
}
