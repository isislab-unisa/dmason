package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util;

import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DNode;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.DTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class FixWorker<V,E> implements Runnable{

	private HashMap<Integer, ArrayList<V>> new_community;
	private DNode dNode;
	private long mean;
	private HashMap<V, Integer> modularityNode;
	private double resolution;
	private DTree<V,E> tree;

	public FixWorker(double resolution, DNode dNode, long mean, HashMap<V, Integer> modularityNode,DTree tree)
	{
		this.dNode = dNode;
		this.mean = mean;
		this.modularityNode = modularityNode;
		this.resolution = resolution;
		this.tree = tree;
	}

	@Override
	public void run() {
		ArrayList<V> c = dNode.getCommunity();
		new_community = new HashMap<Integer, ArrayList<V>>();

		for (Iterator<V> iterator = c.iterator(); iterator.hasNext();) {

			V label =  iterator.next();
			Integer mod=modularityNode.get(label);

			if(new_community.get(mod)==null)
				new_community.put(mod, new ArrayList<V>());
			new_community.get(mod).add(label);
		}
		if(new_community.size()>1)
		{
			Set<Integer> ks = new_community.keySet();
			for(Integer s : ks)
			{
				ArrayList<V> toAdd=new_community.get(s);
				if(dNode.getCommunity().size()!=toAdd.size())
				{
					tree.addChild(dNode, resolution, toAdd, null);
				}
			}
		}
	}
}
