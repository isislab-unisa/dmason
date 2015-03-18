package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util;

import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DNode;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DTree;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.louvain.Louvain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.graph.Subgraph;

public class ExtractWorker<V,E> implements Runnable{

	private DNode<V,E> dNode;
	private Louvain<V, E> louvain;
	private double resolution;
	private DTree<V,E> tree;
	private Graph<V, E> graph;
	private double mean;

	public ExtractWorker(double resolution,DNode<V,E> toProcess, DTree<V,E> tree, Graph<V,E> g, double mean) {
		dNode = toProcess;
		this.resolution = resolution;
		louvain = new Louvain<V, E>(resolution);
		this.tree=tree;
		graph=g;
		this.mean = mean;
	}

	@Override
	public void run() {
		louvain.execute(dNode.getCommunityGraph());
		HashMap<Integer, ArrayList<V>> tempClusters = louvain.getArrayListCommunity();
		ArrayList<DNode<V, E>> levelToExplore = dNode.getChildren();
		if(tempClusters.size()!=1)
			for(int i=0; i<tempClusters.size(); i++)
			{
				ArrayList<V> currCluster = tempClusters.get(i);
				DNode<V,E> toAdd = tree.addChild(dNode, dNode.getResolution(), currCluster, new Subgraph<V, E, Graph<V,E>>(graph, new TreeSet(currCluster)));
				if(currCluster.size() > mean)
					levelToExplore.add(toAdd);
			}	

		if(dNode.getChildrenCount()>0)
			dNode.clearCommunityGraph();

		ArrayList<DNode<V, E>> nextLevelToExplore = new ArrayList<DNode<V,E>>();
		while(levelToExplore.size() > 0)
		{
			resolution/=2.0;
			for (DNode<V, E> child : levelToExplore) {
				if(child.getCommunityGraph() != null)
				{
					louvain.setResolution(resolution);
					louvain.execute(child.getCommunityGraph());
					tempClusters = louvain.getArrayListCommunity();
					if(tempClusters.size()!=1)
						for(int i=0; i<tempClusters.size(); i++)
						{
							ArrayList<V> currCluster = tempClusters.get(i);
							DNode<V,E> toAdd = tree.addChild(child, resolution, currCluster, new Subgraph<V, E, Graph<V,E>>(graph, new TreeSet(tempClusters.get(i))));
							if(currCluster.size() > mean)
								nextLevelToExplore.add(toAdd);
						}
					
					if(child.getChildrenCount()>0)
						child.clearCommunityGraph();
					else
					{
						if(child.getCommunity().size() > mean)
						{
							 nextLevelToExplore.add(child);
						}
					}
				}	
			}

			levelToExplore = nextLevelToExplore.size() > 0 ? nextLevelToExplore : new ArrayList<DNode<V,E>>();
			nextLevelToExplore = new ArrayList<DNode<V,E>>();
		}
	}//end run
}
