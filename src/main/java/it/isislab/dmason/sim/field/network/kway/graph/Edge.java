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

import java.io.Serializable;

import org.jgrapht.graph.DefaultEdge;
@AuthorAnnotation(author = { "Alessia Antelmi", "Gennaro Cordasco", "Carmine Spagnuolo" }, date = "20/7/2015")
public class Edge extends DefaultEdge implements Serializable{

	private static final long serialVersionUID = 3449952080817016948L;
	private int weight;

	@Override
	public Vertex getSource(){
		return  (Vertex) super.getSource();
	}

	@Override
	public Vertex getTarget(){
		return (Vertex) super.getTarget();
	}
	
	public Vertex getNeighbor(Vertex v){
		 return (getSource() == v)? getTarget() : getSource();
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
