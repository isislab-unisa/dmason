package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram;

import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.graph.Subgraph;

public class DNode<V,E>{

	private boolean processed;
	private double resolution;
	private int level;
	private ArrayList<V> community;
	private DNode<V,E> parent;
	private ArrayList<DNode<V,E>> children;
	private Subgraph<V, E, Graph<V,E>> subGraph;
	private int id;
	
	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public int getLevel() {
		return level;
	}

	public DNode getParent() {
		return parent;
	}	
	
	public DNode(int ID,double res, ArrayList<V> community, DNode p, Subgraph<V, E, Graph<V,E>> subg) {
		id=ID;
		parent=p;
		resolution=res;
		level=p==null?0:p.getLevel()+1;
		this.community=community;
		subGraph=subg;
		processed=false;
		children=new ArrayList<DNode<V,E>>();
	}
	
	public DNode(int ID,double res, Subgraph<V, E, Graph<V,E>> subg)
	{
		id=ID;
		parent=null;
		resolution=res;
		level=0;
		community=null;
		subGraph=subg;
		processed=false;
		children=new ArrayList<DNode<V,E>>();
	}
	
	public int getId() {
		return id;
	}

	public String toString()
	{
		if(parent!=null)
			return "[Livello: "+level+" community: "+community+" resolution: "+ resolution+"]";
		return "";
	}
	
	public ArrayList<V> getCommunity() {
		return community;
	}

	public ArrayList<DNode<V,E>> getChildren()
	{
		return children;
	}
	
	public Subgraph<V, E, Graph<V,E>> getCommunityGraph()
	{
		return subGraph;
	}
	
	public void clearCommunityGraph()
	{
		subGraph = null;
	}
	
	public int getChildrenCount()
	{
		return children.size();
	}
}
