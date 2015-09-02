package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram;


import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.louvain.Louvain;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.FixWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jgrapht.Graph;
@Deprecated
public class DendrogramFixCommunity<V,E> {

	private int worker;
	private double scale;
	private int noi;
	private long mean;
	private DTree<V,E> tree;
	private String fileName;
	private double resolution;
	private Graph<V, E> graph;

	public DendrogramFixCommunity(Graph<V, E> g, int num_worker, double scaling_factor, int numberOfIterations, double resol)
	{
		graph = g;		
		worker = (int) num_worker;
		scale = scaling_factor;
		noi = numberOfIterations;
		mean = Math.round(scale*(graph.vertexSet().size()/num_worker));
		if(mean<=0) mean=1;
		tree = new DTree<V,E>();
		//fileName=filename;
		resolution = resol; 
	}

	public void start() throws Exception
	{
		ArrayList<DNode<V,E>> levelToExplore = new ArrayList<DNode<V,E>>();
		ArrayList<DNode<V,E>> nextLevelToExplore = new ArrayList<DNode<V,E>>();
		ArrayList<FixWorker<V,E>> workerList = new ArrayList<FixWorker<V,E>>();

		Louvain<V, E> louvain = new Louvain<V, E>(1.0);
		HashMap<V, Integer> modularityNode = louvain.execute(graph);
		HashMap<Integer,ArrayList<V>> cluster = louvain.getArrayListCommunity();

		try {
			tree.addRoot(resolution, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<Integer> ks = cluster.keySet();
		DNode<V,E> root = tree.getRoot();
		for (Iterator<Integer> iterator = ks.iterator(); iterator.hasNext();) {
			Integer res = iterator.next();
			ArrayList<V> k = cluster.get(res);
			tree.addChild(root, resolution, k, null);
		}

		int iter=0;
		for(DNode<V,E> dNode : tree.getRoot().getChildren())
			if(dNode.getCommunity().size() > mean)
			{
				levelToExplore.add(dNode);
			}


		int sizeOfLevel = levelToExplore.size();
		
		while(sizeOfLevel>0 && iter<noi)
		{ 
			resolution/=2.0;
			louvain.setResolution(resolution);
			modularityNode=louvain.execute(graph);
			
			ExecutorService executor = Executors.newFixedThreadPool(sizeOfLevel);
			workerList.clear();
			for (DNode<V,E> dNode : levelToExplore) 
				executor.execute(new FixWorker<V,E>(resolution ,dNode, mean, modularityNode, tree));

			executor.shutdown();
			executor.awaitTermination(60, TimeUnit.SECONDS);
					
			nextLevelToExplore = new ArrayList<DNode<V,E>>();
			for (DNode<V,E> currLevel : levelToExplore) 
			{
				if(currLevel.getCommunity().size() > mean && currLevel.getChildrenCount()>0)
				{
					ArrayList<DNode<V,E>> l = currLevel.getChildren();
					for (DNode<V,E> dNode : l) {
						if(dNode.getCommunity().size() > mean)
							nextLevelToExplore.add(dNode);
					}
				}
				else
				{
					if(currLevel.getCommunity().size()!=1)
						nextLevelToExplore.add(currLevel);
				}
			}
			levelToExplore = nextLevelToExplore.size() > 0 ? nextLevelToExplore : new ArrayList<DNode<V,E>>();
			sizeOfLevel = levelToExplore.size();
			iter++;
		}
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

	public static void main(String[] args) throws Exception {

		String filename="LesMiserables.gml";
		int numIter=10;
		int[] workers = new int[1];
		workers[0]=400;
		//		workers[1]=49;
		//		workers[2]=100;
		//		workers[3]=225;
		//		workers[4]=400;

//		for(int w = 0; w < workers.length; w++) 
//		{	
//			for(int iter=0 ; iter<numIter ; iter++)
//			{
//				DendrogramFixCommunity dfc = new DendrogramFixCommunity(filename,workers[w], 2.0, 1000, 1.0);
//
//				long start_time=System.currentTimeMillis();
//				dfc.start();
//				long end_time=System.currentTimeMillis();
//				System.out.println("eseguito in: "+((end_time-start_time)/1000.0)/60.0+" minuti");
//				DTree result = dfc.getDendrogram();
//				//FileTree ft = new FileTree(result.getRoot());
//				TreeSet<String>[] a = result.getWorkersLoad(dfc.getLoad(), dfc.getNumWorkers());
//				int unload_machine=0;
//				for (int i = 0; i < a.length; i++) {
//					if(a[i].size()==0)
//						unload_machine++;
					//System.out.println(a[i]+" "+a[i].size());
				}
//				dfc.saveResults(a, "Fix_Method_"+filename.substring(0,filename.length()-4)+"_iter "+w);
//				GephiSuperGraph gsg = new GephiSuperGraph("Fix_Method_"+filename.substring(0,filename.length()-4)+"_iter "+w+".gexf");
//				System.err.println(workers[w]+"-"+iter+"-------------------------------------------------------------");
//				Double[] metrics = gsg.getMetric();
//				//			System.err.println("Archi - Nodi - TotalWeight");
//				//			for (int i = 0; i < metrics.length; i++) {
//				//				System.out.println(metrics[i]);
//				//			}
//				System.err.println("Peso totale "+metrics[2]);
//				System.err.println("Macchine non cariche: "+unload_machine);
//				//System.out.println("Tempo: "+(end_time-start_time));
//				System.err.println("-----------------------------------------------------------------");
//				gsg.close();
//			}
//		}
//	}//Close Main
}//Close Class
