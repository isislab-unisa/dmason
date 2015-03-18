package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram;

import java.util.ArrayList;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.graph.Subgraph;


public class DTree<V,E> {

	private DNode<V,E> root;
	private int size;
	public DTree() {
		root=null;
		size=0;
	}

	public int size()
	{
		return size;
	}

	public DNode<V,E> getRoot()
	{
		return root;
	}

	public DNode<V,E> addRoot(double res, Subgraph<V, E, Graph<V,E>> subg) throws Exception
	{
		if(root==null)
		{
			root=new DNode<V,E>(size,res,subg);
			size++;
			return root;
		} else throw new Exception("root gia' inserita...");
	}

	public DNode<V,E> addChild(DNode<V,E> n, double res, ArrayList<V> community, Subgraph<V, E, Graph<V,E>> subg)
	{
		DNode child = new DNode<V,E>(size, res, community, n, subg);
		n.getChildren().add(child);
		size++;
		return child;
	}

//	public ArrayList<DNode<V>> getNodeNotProcessedAtLevel(ArrayList<DNode<V>> list, DNode<V> root)
//	{
//		if(!root.isProcessed())
//		{
//			list.add(root);
//			return list;
//		}
//		else
//		{
//			if(root.getChildren().size()>1)
//				for(Object n : root.getChildren())
//					list = getNodeNotProcessedAtLevel(list, (DNode<V>)n);
//			return list;
//		}
//	}
//
//	private String myString(String x, DNode r)
//	{
//		x+=r.toString()+"\n";
//
//		for(DNode<V> n : r.getChildren())
//			x=myString(x,n);
//		return x;
//
//	}

//	public String toString()
//	{	
//		String x="";
//		return myString(x, root);
//	}

	private ArrayList<DNode<V,E>> getLeafs(ArrayList<DNode<V,E>> list, DNode<V,E> root)
	{
		if(root.getChildrenCount()==0)
		{
			list.add(root);
		}
		else
		{
			ArrayList<DNode<V,E>> children = root.getChildren();
			for (DNode<V,E> dNode : children) {
				list = getLeafs(list,dNode);
			}
		}
		return list;
	}
	public TreeSet<V>[] getWorkersLoad(double load, int worker)
	{
		ArrayList<DNode<V,E>> leafs = getLeafs(new ArrayList<DNode<V,E>>(), root);
		TreeSet<V>[] final_cut = new TreeSet[worker];
		int j=0;
		for(int i=0; i<final_cut.length; i++)
			final_cut[i]=new TreeSet<V>();
		boolean flag=false;
		for (int i = 0; i < leafs.size(); i++) {
				int min=0;
				for(j=0; j<final_cut.length; j++)
				{
					if(final_cut[j].size()<final_cut[min].size())
						min=j;
				}
				final_cut[min].addAll(leafs.get(i).getCommunity());
		}
		return final_cut;
	}
	
//	public TreeSet<String>[] getWorkersLoadBalanced(double load, int worker)
//	{
//		ArrayList<DNode> leafs = getLeafs(new ArrayList<DNode>(), root);
//		TreeSet<V>[] final_cut = new TreeSet[worker];
//		int j=0;
//		for(int i=0; i<final_cut.length; i++)
//			final_cut[i]=new TreeSet<V>();
//		
//		for (int i = 0; i < leafs.size(); i++) {
//			
//			if(final_cut[j].size()+leafs.get(i).getCommunity().size() < load)
//			{
//				final_cut[j].addAll(leafs.get(i).getCommunity());
//				j = (j + 1) % worker;
//			} 
//			else
//			{
//				j = (j + 1) % worker;	
//				final_cut[j].addAll(leafs.get(i).getCommunity());
//			}
//		}
//		return final_cut;
//	}
	
//	public void saveGraphByClusters(String source, TreeSet<String>[] clusters, String save_filename)
//	{
//		GephiSuperGraph gsg = new GephiSuperGraph(source);
//		NodeIterable nodes = gsg.getGraph().getNodes();
//		//gsg.calculateModularity(1.0);
//		for (Node n : nodes)
//		{
//			for (int i = 0; i < clusters.length; i++) {
//				if(clusters[i].contains(n.getNodeData().getLabel()))
//				{
//					n.getAttributes().setValue(Modularity.MODULARITY_CLASS, i);
//					//System.out.println(n.getAttributes().getValue(Modularity.MODULARITY_CLASS) + " "+n.getNodeData().getLabel());
//					break;
//				}
//			}
//		}
//		//gsg.export("politicalSemiResult.gexf");
//		//gsg.toSuperGraph();
//		//gsg.visualize();
//		gsg.export(save_filename+".gexf");
//		//gsg.save(save_filename);
//		gsg.close();
//	}

	//	public static void main(String[] args) throws Exception {
	//
	//		DTree t = new DTree();
	//		DNode root = t.addRoot(1.0, "1");
	//		DNode c1 = new DNode(2.0, "2", root);
	//		DNode c2 = new DNode(2.0, "3", root);
	//		//c2.setProcessed(true);
	//		DNode c3 = new DNode(2.0, "4", root);
	//		ArrayList<DNode> child = new ArrayList<DNode>();
	//		child.add(c1);
	//		child.add(c2);
	//		child.add(c3);
	//		t.addChildren(root, child);
	//		child = new ArrayList<DNode>();
	//		child.add(new DNode(3.0, "5",c2));
	//		child.add(new DNode(3.0, "6",c2));
	//		child.add(new DNode(3.0, "7",c2));
	//		child.add(new DNode(3.0, "8",c2));
	//		t.addChildren(c2, child);
	//		child = new ArrayList<DNode>();
	//		System.out.println(t.getNodeNotProcessedAtLevel(2, child, root));
	//	}

}
