package dmason.sim.field.network;

/* Grafo di supporto per il Network*/

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import dmason.sim.field.CellType;

public class AuxiliaryGraph {

		private UndirectedGraph<Node, DefaultEdge> grafo;
		
		public AuxiliaryGraph(){
			grafo=new SimpleGraph<Node, DefaultEdge>(DefaultEdge.class);
			
			//--------------------------
			
			Node v1=new Node(100, 100,1);
			Node v4=new Node(300, 100,4);
			Node v3=new Node(300, 300,3);
			Node v2=new Node(100, 300,2);
			Node v5=new Node(350,350,5);
			Node v6=new Node(50, 330, 6);
			Node v7=new Node(80, 50, 7);
			Node v8=new Node(300, 80, 8);
			
			grafo.addVertex(v1);
			grafo.addVertex(v2);
			grafo.addVertex(v3);
			grafo.addVertex(v4);
			grafo.addVertex(v5);
			grafo.addVertex(v6);
			grafo.addVertex(v7);
			grafo.addVertex(v8);
			
			grafo.addEdge(v1, v2);
			grafo.addEdge(v2, v3);
			grafo.addEdge(v3, v4);
			
			grafo.addEdge(v3, v5);
			grafo.addEdge(v2, v6);
			grafo.addEdge(v6, v3);
			grafo.addEdge(v1, v7);
			grafo.addEdge(v4, v7);
			grafo.addEdge(v1, v8);
			grafo.addEdge(v1, v6);
			grafo.addEdge(v4, v8);
			
			
			
			//--------------------------------------*/
			
			
			
			
			//-----------------------------------------------*/
			
		}
		
		public UndirectedGraph<Node, DefaultEdge> getGraph(){
			return grafo;
		
		}
		
		/* Controlla se esiste un arco tra due vertici forniti i rispetivi id*/
		public boolean edgeExist(int id1,int id2){
			Set<Node> set=grafo.vertexSet();
			Iterator<Node> i=set.iterator();
			Node node,a=null,b=null;
			while(i.hasNext()){
				node=i.next();
				if (node.getId()==id1)
					a=node;
				else if (node.getId()==id2)
					b=node;
			}
			if (grafo.containsEdge(a, b))
				return true;
			return false;
		}

}
