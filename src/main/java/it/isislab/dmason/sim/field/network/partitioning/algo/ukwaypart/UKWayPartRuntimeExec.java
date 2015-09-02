package it.isislab.dmason.sim.field.network.partitioning.algo.ukwaypart;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.CellType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.GmlExporter;

/**
 * 
 * @author Andrea Solda' @ ISISLab
 *
 */
@Deprecated
public class UKWayPartRuntimeExec<E, V> {

	//WARNING!! Directory called "resource" must be in the current working directory

	public static<E, V> HashMap<E, Integer> runUKWayPart (CellType ID,Graph<E, V> graph, int nPart) throws IOException, InterruptedException, DMasonException {	
		InputStream exp = null;
		InputStream ukway = null;
		String remove = null;
		int os = 0;
		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			//WINDOWS!!
			remove = "del ";
			os = 1;
		} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//			exp = ClassLoader.getSystemClassLoader().getResourceAsStream("it.isislab.dmason/sim/field/network/partitioning/resources/MAC_OS_X_64/graphexp");
//			ukway = ClassLoader.getSystemClassLoader().getResourceAsStream("it.isislab.dmason/sim/field/network/partitioning/resources/MAC_OS_X_64/gpmetis");
//			exp = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/isislab/dmason/sim/field/network/partitioning/resources/MAC_OS_X_64/graphexp");
//			ukway = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/isislab/dmason/sim/field/network/partitioning/resources/MAC_OS_X_64/gpmetis");
			exp = new FileInputStream("resources/util/metis/MAC_OS_X_64/graphexp");
			ukway = new FileInputStream("resources/util/metis/MAC_OS_X_64/gpmetis");
			
			remove = "rm ";
			os = 2;
		} else {
//			exp = ClassLoader.getSystemClassLoader().getResourceAsStream("it.isislab.dmason/sim/field/network/partitioning/resource/LINUX_64/graphexp");
//			ukway = ClassLoader.getSystemClassLoader().getResourceAsStream("it.isislab.dmason/sim/field/network/partitioning/resource/LINUX_64/gpmetis");
//			exp = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/isislab/dmason/sim/field/network/partitioning/resources/LINUX_64/graphexp");
//			ukway = Thread.currentThread().getContextClassLoader().getResourceAsStream("it/isislab/dmason/sim/field/network/partitioning/resources/LINUX_64/gpmetis");
			exp = new FileInputStream("resources/util/metis/LINUX_64/graphexp");
			ukway =new FileInputStream("resources/util/metis/LINUX_64/gpmetis");
			remove = "rm ";
			os = 3;
		}

		//Generating .gml file
		GmlExporter<E, V> ge = new GmlExporter<E, V>();
		ge.setPrintLabels(GmlExporter.PRINT_VERTEX_LABELS);
		FileWriter fw = new FileWriter(ID.pos_i+"-"+ID.pos_j + "-tmp.gml");
		ge.export(fw, (UndirectedGraph<E, V>)graph);
		fw.close();

		String toInvoke = null;

		//Transforming .gml to .graph
		try {
			File expFile = new File(ID.pos_i+"-"+ID.pos_j + "-tmpGraphExp");
			FileOutputStream fosExp = new FileOutputStream(expFile); 
			fosExp.write(IOUtils.toByteArray(exp));
			toInvoke = expFile.getAbsolutePath();
			fosExp.close();
			expFile.setExecutable(true);
		} catch(NullPointerException e) {
			e.printStackTrace();
		}

		Process conv = Runtime.getRuntime().exec(toInvoke+ " " + ID.pos_i+"-"+ID.pos_j + "-tmp.gml " + ID.pos_i+"-"+ID.pos_j +"-tmp.graph");
		conv.waitFor();

		//Running UKWayPart and creating partitioning file
		try {
			File ukFile = new File(ID.pos_i+"-"+ID.pos_j + "-tmpUKWay");
			FileOutputStream fosUk = new FileOutputStream(ukFile); 
			fosUk.write(IOUtils.toByteArray(ukway));
			toInvoke = ukFile.getAbsolutePath();
			fosUk.close();
			ukFile.setExecutable(true);
		} catch(NullPointerException e) {
			e.printStackTrace();
		}

		Process part = Runtime.getRuntime().exec(toInvoke + " "+ ID.pos_i+"-"+ID.pos_j +"-tmp.graph " + nPart);
		int status=part.waitFor();

		if(status!=0) {
			throw new DMasonException("UKWay Exception!!! "+status);
		}
		

		//Removing useless files
		Runtime.getRuntime().exec(remove + ID.pos_i+"-"+ID.pos_j + "-tmp.gml");
		Runtime.getRuntime().exec(remove + ID.pos_i+"-"+ID.pos_j + "-tmp.graph");
		Runtime.getRuntime().exec(remove + ID.pos_i+"-"+ID.pos_j + "-tmpGraphExp");
		Runtime.getRuntime().exec(remove + ID.pos_i+"-"+ID.pos_j + "-tmpUKWay");

		//Loading partitioning file to set nodes' labels
		Iterator<E> vertex_i = graph.vertexSet().iterator();
		BufferedReader br = new BufferedReader(new FileReader(ID.pos_i+"-"+ID.pos_j + "-tmp.graph.part." + nPart));
		HashMap<E, Integer> vertexToCluster = new HashMap<E, Integer>();

		while(vertex_i.hasNext()) {
			vertexToCluster.put(vertex_i.next(), (int)Double.parseDouble(br.readLine()));
		}

		br.close();
		//Runtime.getRuntime().exec(remove + ID.pos_i+"-"+ID.pos_j + "-tmp.graph.part." + nPart);

		return vertexToCluster;
	}
	//	public static<E, V> SimpleGraph<E, V> getCommunity(SimpleGraph<E, V> partGraph, Class<V> cl, int label, int aoi) {
	//		SimpleGraph<E, V> toReturn = new SimpleGraph<E, V>(cl);
	//		Iterator<E> vertex_i = partGraph.vertexSet().iterator();
	//		Iterator<E> neighbor_i;
	//		NeighborIndex<E, V> ni = new NeighborIndex<E, V>(partGraph);
	//		E next, next_neighbor;
	//
	//		while(vertex_i.hasNext()) {
	//			next = vertex_i.next();
	//			if(((LabelVertex)next).getLabel() == label) {
	//				toReturn.addVertex(next);
	//				List<E> neighbor = ni.neighborListOf(next);
	//				for(int j = 0; j < neighbor.size(); j++) {
	//					if(((LabelVertex) neighbor.get(j)).getLabel() == label) {
	//						toReturn.addVertex(neighbor.get(j));
	//						toReturn.addEdge(next, neighbor.get(j));
	//						for(int i = 0; i < aoi; i++) {
	//							neighbor_i = ni.neighborListOf(next).iterator();
	//							while(neighbor_i.hasNext()) {
	//								next_neighbor = neighbor_i.next();
	//								if(((LabelVertex) next_neighbor).getLabel() != label) {
	//									toReturn.addVertex(next_neighbor);
	//									toReturn.addEdge(next, next_neighbor);
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}
	//		return toReturn;
	//	}
}
