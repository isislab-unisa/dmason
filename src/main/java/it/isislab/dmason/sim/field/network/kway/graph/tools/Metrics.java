/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.sim.field.network.kway.graph.tools;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.SuperEdge;
import it.isislab.dmason.sim.field.network.kway.graph.SuperVertex;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
/**
 * Stores information about a superGraph:
 * 		- number of super edges
 * 		- weight of super edges
 * 		- variance of super vertex dimension
 * 		- imbalance
 */
public class Metrics {

public Metrics(Graph g){
		this.numSuperEdges = calculateNumberSuperEdges(g);
		this.weightSuperEdges = calculateWeightSuperEdges(g);
		this.variance = calculateVarianceSuperVertex(g);
		this.imbalance = calculateImbalanceSupervertex(g);
	}
	
	private long calculateNumberSuperEdges(Graph g){
		return g.edgeSet().size();
	}
		
	private long calculateWeightSuperEdges(Graph g){
		long weight = 0;
		
		Iterator<Edge> iterE = g.edgeSet().iterator();
		
		/* get the weight of edges composing a superedge */
		while(iterE.hasNext()){
			SuperEdge e = (SuperEdge) iterE.next();
			
			weight += e.getWeight_Egde();
		}
		
		return weight;
	}
	
	private double calculateVarianceSuperVertex(Graph g){
		double variance = 0;
		double averageValue = calculateAverageValue(g);
		float sum = 0;
		int numSuperVertex = g.vertexSet().size();
		Iterator<Vertex> iter = g.vertexSet().iterator();
		
		/* calculates variance^2  */
		iter = g.vertexSet().iterator();
		
		while(iter.hasNext()){
			SuperVertex v = (SuperVertex) iter.next();
			sum += Math.pow(v.getOriginal_vertex().size() - averageValue, 2);
		}
		
		variance = Math.sqrt(sum/numSuperVertex);
		
		return variance;
	}
	
	private double calculateImbalanceSupervertex(Graph g){
		double averageValue = calculateAverageValue(g);
		long maxSize = 0;
		long size = 0;
		
		Iterator<Vertex> iter = g.vertexSet().iterator();
		
		/* calculates max size of a super vertex */
		iter = g.vertexSet().iterator();
		
		while(iter.hasNext()){
			SuperVertex v = (SuperVertex) iter.next();
			size = v.getOriginal_vertex().size();
			
			if(size > maxSize)
				maxSize = size;
		}
		
		imbalance = maxSize/averageValue;
		
		return imbalance;
	}
	
	private double calculateAverageValue(Graph g){
		double averageValue = 0;
		int numSuperVertex = g.vertexSet().size();
		Iterator<Vertex> iter = g.vertexSet().iterator();
		
		while(iter.hasNext()){
			SuperVertex v = (SuperVertex) iter.next();
			averageValue += v.getOriginal_vertex().size();
		}
		
		averageValue = averageValue / numSuperVertex;
		
		return averageValue;
	}
	
	/* numberEdges;weightSuperEdges;variance*/
	public void createCSVmetrics(String filename){
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + ".metrics.csv"));
			
			String metrics = numSuperEdges + ";" + weightSuperEdges + ";" + variance + imbalance;
			
			out.write(metrics.getBytes());
			out.close();
			
		} catch (IOException e) {		
			e.printStackTrace();
		}
	}
		
	public String toCSVString(){
		return numSuperEdges + ";" + weightSuperEdges + ";" + variance + ";" + imbalance;
	}
	
	
	/* getters*/
	public long getNumEdges(){	return numSuperEdges;	}
	
	public long getWeightSuperEdges() {return weightSuperEdges;	}

	public double getVariance() {	return variance;	}
	
	public double getImbalance(){	return imbalance;	}
	
	
	
 private long numSuperEdges;
 private long weightSuperEdges;
 private double variance;	//variance of super vertex dimension
 private double imbalance;  //partitions imbalance
}



























