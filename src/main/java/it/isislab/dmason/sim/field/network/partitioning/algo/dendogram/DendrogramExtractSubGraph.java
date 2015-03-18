package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram;


import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.louvain.Louvain;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.ExtractWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgrapht.Graph;
import org.jgrapht.graph.Subgraph;

public class DendrogramExtractSubGraph<V,E> {

	private int worker;
	private double mean;
	private double scale;
	private int noi;
	private Graph<V, E> graph;
	private DTree<V,E> tree;
	private String fileName;
	private double resolution;
	private int numberOfCore;

	class MyEntry {
		private TreeSet<String> treeSet;
		private boolean mustSplit;
		public MyEntry(TreeSet<String> ts) 
		{
			treeSet=ts;
			mustSplit=true;
		}
		public boolean getMustSplit()
		{
			return mustSplit;
		}
		public void setMustSplit(boolean v)
		{
			mustSplit=v;
		}
		public TreeSet<String> getTree()
		{
			return treeSet;
		}

		public String toString(){ return treeSet.toString();}
	}

	public DendrogramExtractSubGraph(Graph<V, E> g, double num_worker, double scaling_factor, int numberOfIterations, double resol, int numProcess) throws Exception {
		graph = g;
		worker = (int)num_worker;
		scale = scaling_factor;
		noi = numberOfIterations;
		mean = Math.round(scale*(graph.vertexSet().size()/num_worker));
		if(mean<=0) mean=1;
		tree = new DTree<V,E>();
		//fileName = filename;
		resolution = resol; 
		numberOfCore = numProcess;
		System.out.println("Media carico per worker "+mean);
	}


	public void start() throws Exception
	{
		Louvain<V, E> louvain = new Louvain<V, E>(resolution);
		louvain.execute(graph);
		HashMap<Integer, TreeSet<V>> clusters = louvain.getSetCommunity();

		tree.addRoot(resolution, new Subgraph(graph, null,null));
		ArrayList<DNode<V,E>> firstLevel = new ArrayList<DNode<V,E>>();
		DNode<V,E> root = tree.getRoot();

		for(Integer i : clusters.keySet())
		{
			Subgraph<V, E, Graph<V,E>> subg = null;

			if(clusters.get(i).size()>mean)
			{
				subg = new Subgraph(graph, clusters.get(i),null);
			}
			ArrayList<V> supportArray = new ArrayList<V>();
			supportArray.addAll(clusters.get(i));
			DNode<V,E> toAdd = tree.addChild(root,resolution, supportArray, subg);
			if(supportArray.size()>mean)
				firstLevel.add(toAdd);
		}
	
		resolution/=2.0;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfCore);
		for(DNode<V,E> dNode : firstLevel)
			executor.execute(new ExtractWorker<V,E>(resolution,dNode, tree, graph,mean));
		
		executor.shutdown();
		while(!executor.isTerminated()){ Thread.yield(); }
		//executor.awaitTermination(60, TimeUnit.SECONDS);
	}

	public DTree<V,E> getDendrogram()
	{
		return tree;
	}

	public double getLoad()
	{
		return mean;
	}

	public int getNumWorkers()
	{
		return worker;
	}

//	public void saveResults(TreeSet<String>[] clusters, String filename)
//	{
//		tree.saveGraphByClusters(fileName,clusters, filename);
//	}

	public void cleanWorkspace()
	{
		File currDir = new File(".");
		File[] files = currDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if(files[i].getName().contains(".gexf") && !files[i].getName().contains("Method")){
				File f = new File(files[i].getName());	
				f.delete();
			}
		}
	}

//	public static void main(String[] args) throws Exception {
//
//		String filename="Internet.gml";
//		int numIter=1;
//		int[] workers = new int[5];
//		workers[0]=25;
//		workers[1]=49;
//		workers[2]=100;
//		workers[3]=225;
//		workers[4]=400;
//
//		for(int w = 0; w < workers.length; w++) 
//			for(int iter=0 ; iter<numIter ; iter++)
//			{
//				DendrogramExtractSubGraph d = new DendrogramExtractSubGraph(filename,workers[w], 1, 1000, 1.0,8);
//
//				long start_time=System.currentTimeMillis();
//				d.start();
//				long end_time=System.currentTimeMillis();
//				System.out.println("eseguito in: "+((end_time-start_time)/1000.0)/60.0+" minuti");
//				d.cleanWorkspace();
//				DTree result = d.getDendrogram();
//				//System.out.println(result);
//				//FileTree ft = new FileTree(result.getRoot());
//				//		System.out.println("-----------------------------------------------------------------");
//				TreeSet<String>[] a = result.getWorkersLoad(d.getLoad(), d.getNumWorkers());
//				int unload_machine=0;
//				for (int i = 0; i < a.length; i++) {
//					if(a[i].size()==0)
//						unload_machine++;
//					//System.out.println(a[i]+" "+a[i].size());
//				}
//
////				d.saveResults(a, "ExtractMethod_"+filename);
////				GephiSuperGraph gsg = new GephiSuperGraph("ExtractMethod_"+filename+".gexf");
////
////				Double[] metrics = gsg.getMetric();
////				System.err.println(workers[w]+"-"+iter+"---------------------------------------------------");
////				//			System.err.println("Archi - Nodi - TotalWeight");
////				//			for (int i = 0; i < metrics.length; i++) {
////				//				System.err.println(metrics[i]);
////				//			}
////				System.err.println("Peso totale "+metrics[2]);
////
////
////				System.err.println("Macchine non cariche: "+unload_machine);
////				System.err.println("-----------------------------------------------------------");
////				gsg.close();
//			}
//	}
//
}
