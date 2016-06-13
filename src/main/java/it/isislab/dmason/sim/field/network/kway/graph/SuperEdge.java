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
package it.isislab.dmason.sim.field.network.kway.graph;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.util.HashSet;

/**
 * this class represents a superedge.
 * It stores a collection of edges, 
 * and its weight is the sum of the weights of the edges it stores
 */
@AuthorAnnotation(author = { "Alessia Antelmi", "Gennaro Cordasco", "Carmine Spagnuolo" }, date = "20/7/2015")
public class SuperEdge extends Edge{

	private static final long serialVersionUID = -73145959729902958L;
	
	private HashSet<Edge> original_edges;
	private int weight_superEdge;		
	
	public SuperEdge() {
		original_edges= new HashSet<Edge>();
		weight_superEdge = 0;
	}
	
	public SuperEdge(HashSet<Edge> original_edges) {
		this.original_edges = original_edges;
		weight_superEdge = 0;
	}
	
	public HashSet<Edge> getOriginal_edges() {
		return original_edges;
	}

	public void setOriginal_edges(HashSet<Edge> original_edges) {
		this.original_edges = original_edges;
	}
	
	public String toString() {
		return super.toString()+ " ets: " + original_edges + " edge weight " + weight_superEdge;
	}

	public int getWeight_Egde() {
		return weight_superEdge;
	}

	/**
	 * adds an edge to superEdge
	 * and increments superEdge weight 
	 * @param e - new edge to add
	 */
	public void addEdge(Edge e){
		original_edges.add(e);
		this.setOriginal_edges(original_edges);
		weight_superEdge += e.getWeight();
	}
	
	/**
	 * remove the edge to superEdge
	 * and decrements superEdge weight 
	 * @param e - edge to remove
	 */
	public void removeEdge(Edge e){
		original_edges.remove(e);
		this.setOriginal_edges(original_edges);
		weight_superEdge -= e.getWeight();
	}
	
}







