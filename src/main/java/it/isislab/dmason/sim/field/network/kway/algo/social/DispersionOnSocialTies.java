package it.isislab.dmason.sim.field.network.kway.algo.social;

import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DispersionOnSocialTies {
	private static final int MARKER_MAX_DISPERSION=-1;
	public static Double MAX_DISPERSION=new Double(MARKER_MAX_DISPERSION);
	
	public static void ComputeDispersion(Graph g, int dist){
		
		ExecutorService executor = Executors.newFixedThreadPool(8);

		for(Edge ed: g.edgeSet()){
			EdgeWDispersion e= (EdgeWDispersion) ed;
			executor.execute(new ComputeDispersionThread(g,e,dist));

		}
		executor.shutdown();
		while (!executor.isTerminated()) { }
		boolean update_dispersion=false;
		for(Edge ee : g.edgeSet())
		{
			EdgeWDispersion e=(EdgeWDispersion)ee;
			if(e.getDispersion()== MARKER_MAX_DISPERSION)
			{
				e.setDispersion(MAX_DISPERSION+1);
				update_dispersion=true;
			}
			
		}
		if(update_dispersion)MAX_DISPERSION++;
		System.out.println("Max Computed Dispersion "+MAX_DISPERSION);
	}
	
	public static void setDispersion(Graph g, EdgeWDispersion e, int dist) {
		Vertex u= e.getSource();
		Vertex v = e.getNeighbor(u);
		int embedness;
		int dv;
		Graph gu= computeGE(g,e,dist);
		//System.out.println(gu);
		HashSet<Vertex> Nu= computeNeighbor(gu,u, dist);
		//System.out.println(Nu);

		HashSet<Vertex> Nv= computeNeighbor(gu,v, dist);
		HashSet<Vertex> Cuv=new HashSet<Vertex>(Nu);

		Cuv.retainAll(Nv);
		embedness=Cuv.size();

		dv=computeDv(gu,Cuv,dist,e);

		e.setDv(dv);
		e.setEmbedness(embedness);
	
		if(dv==0 && embedness==0) e.setDispersion(-1);
		else e.setDispersion(((double) dv)/embedness); 
		
		synchronized (MAX_DISPERSION) {
			if( e.getDispersion() != MARKER_MAX_DISPERSION && MAX_DISPERSION < e.getDispersion())
				{
					MAX_DISPERSION=e.getDispersion();
				}
			
		}
	}
	//	private static int computeDv(Graph gu, HashSet<Vertex> Cuv, int dist, Edge e) {
	//		int dv=0;
	//		for (Vertex s: Cuv){
	//			for (Vertex t: Cuv){
	//				if (s!=t){
	//				
	//					HashSet<Vertex> Ns= computeNeighbor(gu,s,1);
	//					HashSet<Vertex> Nt= computeNeighbor(gu,t,1);
	//					HashSet<Vertex> Cst=new HashSet<Vertex>(Ns);
	//					Cst.retainAll(Nt);
	//					Cst.remove(e.getU());
	//					Cst.remove(e.getV());
	//					if ((!gu.containsEdge(s, t)) && (!gu.containsEdge(t, s)) && Cst.isEmpty())
	//						dv++;
	//				}
	//			}
	//		}
	//		return dv;
	//	}


	private static int computeDv(Graph gu, HashSet<Vertex> Cuv, int dist, Edge e) {
		int dv=0;
		for (Vertex s: Cuv){
			for (Vertex t: Cuv){
				if (s!=t && !((gu.containsEdge(s, t) || gu.containsEdge(t, s))) ){
					int i=1;
					for (; i<dist; i++){
						HashSet<Vertex> Ns= computeNeighbor(gu,s,i);
						HashSet<Vertex> Nt= computeNeighbor(gu,t,i);
						HashSet<Vertex> Cst=new HashSet<Vertex>(Ns);
						Cst.retainAll(Nt);
						Cst.remove(e.getSource());
						Cst.remove(e.getTarget());
						if (!Cst.isEmpty()){
							dv+=i;
							break;
						}
					}
					if (i==dist)
						dv+=dist;
				}
			}

		}
		return dv;
	}


	private static HashSet<Vertex> computeNeighbor(Graph gu, Vertex u, int dist) {
		HashSet<Vertex> Nu= new HashSet<Vertex>();
		HashSet<Vertex> lastLevel= new HashSet<Vertex>();
		lastLevel.add(u);
		for (int i=0;i<dist;i++){
			HashSet<Vertex> currentLevel= new HashSet<Vertex>();
			for(Vertex w: lastLevel){
				for (Edge e: gu.edgesOf(w)){
					Vertex z= e.getNeighbor(w);
					if(!Nu.contains(z)){
						Nu.add(z);
						currentLevel.add(z);
					}
				}
			}
			lastLevel=currentLevel;
		}
		return Nu;
	}

	private static Graph computeGE(Graph g, Edge edge, int dist) {
		Graph gu= new Graph();
		gu.addVertex(edge.getSource());
		gu.addVertex(edge.getTarget());
		HashSet<Vertex> lastLevel= new HashSet<Vertex>();
		lastLevel.add(edge.getSource());
		lastLevel.add(edge.getTarget());


		for (int i=0;i<dist;i++){
			HashSet<Vertex> currentLevel= new HashSet<Vertex>();
			for(Vertex w: lastLevel){
				for (Edge e  : g.edgesOf(w)){	
					Vertex z= e.getNeighbor(w);
					if (!gu.containsVertex(z)){
						gu.addVertex(z);
						currentLevel.add(z);
					}
				}
			}
			lastLevel=currentLevel;
		}

		for (Vertex vu: gu.vertexSet() )
			for (Edge ev : g.edgesOf(vu)){
				Vertex wu=ev.getNeighbor(vu);
				if (gu.containsVertex(wu))
					gu.addEdge(vu, wu);
			}

		return gu;
	}

}
