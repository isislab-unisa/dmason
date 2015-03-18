/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.app.DBreadthFirstSearch;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedAgentFactory;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field.network.DNetworkFactory;
import it.isislab.dmason.sim.field.network.partitioning.Partitioner;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.GraphMLImpoter;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.ImportException;
import it.isislab.dmason.sim.field.network.partitioning.interfaces.GraphVertex;
import it.isislab.dmason.sim.field.network.partitioning.interfaces.LabelVertex;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;
import it.isislab.dmason.tools.batch.data.GeneralParam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

@AuthorAnnotation(
		author = {"Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class Vertexes extends DistributedState<Double2D>{

	private static final long serialVersionUID = -7594558050842065632L;
	public static String topicPrefix = "Vertexes";
	public DNetwork network;
	public Continuous2D yard = new Continuous2D(1.0,100,100);
	private GeneralParam params;
	public Graph<LabelVertex<Integer>, DefaultWeightedEdge> part = null;

	public Vertexes()
	{
		super();
	}
	public String graph_path;
	public Vertexes(GeneralParam params,String graph_path)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),topicPrefix,params.getConnectionType());
		this.params=params;
		this.graph_path=graph_path;
	}

	@Override
	public void start()
	{
		super.start();

//		ImportSerializedSimpleGraph<V,E> issg = new ImportSerializedSimpleGraph<V,E>("karate.data");
//		UKWayPartRuntimeExec uk = new UKWayPartRuntimeExec("Internet.gml");
//		try {
//			uk.UKWayPart(params.getColumns()*params.getRows());
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		int commID = (TYPE.pos_i*rows)+TYPE.pos_j;
//		SimpleGraph<BasicVertex, DefaultWeightedEdge> myCommunity = uk.getCommunity(commID, 1);
		
		
		GraphMLImpoter<Integer,
		DefaultWeightedEdge> gImp = null;
		try {
			gImp = new GraphMLImpoter(graph_path);
		} catch (ImportException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			part = Partitioner.executeUKWayPart(this.TYPE,gImp.getGraph(), params.getColumns()*params.getRows());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) { 
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		int commID = (TYPE.pos_i*rows)+TYPE.pos_j;
		Graph<LabelVertex<Integer>, DefaultWeightedEdge> myCommunity = Partitioner.getCommunity(part, DefaultWeightedEdge.class, commID);
		
		GraphSubscribersEdgeList grpsub = new GraphSubscribersEdgeList();
		Set<DefaultWeightedEdge> edgeSet = myCommunity.edgeSet();
		
		System.out.println("comm: " + commID + "size: " + edgeSet.size());
		for (DefaultWeightedEdge e : edgeSet) {
			LabelVertex source = (LabelVertex) myCommunity.getEdgeSource(e);
			LabelVertex target = (LabelVertex) myCommunity.getEdgeTarget(e);
			int sourceComm = source.getCommunity();
			int targetComm = target.getCommunity();
			if(sourceComm!=targetComm)
			{
				grpsub.addEdge(sourceComm, targetComm, false);
			}
		}
		HashMap<String, Vertex> myVertexes = new HashMap<String,Vertex>();

		network = DNetworkFactory.createDNetworkField(this, super.rows, super.columns, TYPE.pos_i, TYPE.pos_j, grpsub,"mygraph", topicPrefix);

		// clear the yard
		yard.clear();

		// clear the network
		network.clear();
		Random r = new Random(System.currentTimeMillis());
		for (DefaultWeightedEdge e : edgeSet) {

			GraphVertex<String> source = (GraphVertex) myCommunity.getEdgeSource(e);
			GraphVertex<String> target = (GraphVertex) myCommunity.getEdgeTarget(e);
			int sourceComm = source.getCommunity();
			int targetComm = target.getCommunity();
			Vertex netSource = null;
			Vertex netTarget = null;
			boolean sourceFlag = (int)Float.parseFloat(source.getLabel())==1;
			boolean targetFlag = (int)Float.parseFloat(target.getLabel())==1;

			netSource = myVertexes.get(source.getLabel());
			netTarget = myVertexes.get(target.getLabel());

			if(netSource==null)
			{
				Double2D pos=new Double2D(yard.getWidth() * r.nextDouble(), yard.getHeight() * r.nextDouble());
				//Vertex(DistributedState state,int community, boolean isVisited, String label,Double2D pos)
				netSource = (Vertex) DistributedAgentFactory.newIstance(
						Vertex.class,
						new Class[]{SimState.class,Integer.class,Boolean.class,String.class,Double2D.class},
						new Object[]{this,sourceComm, sourceFlag, source.getLabel()+"", pos},
						DVertexState.class);
						
				myVertexes.put(source.getLabel(), netSource);
				//System.out.println("creo nodo "+);
				if(sourceComm == commID) 
					schedule.scheduleOnce(netSource);
				network.addNode(netSource);
				yard.setObjectLocation(netSource, pos);
			}

			if(netTarget==null)
			{
				
				
				Double2D pos=new Double2D(yard.getWidth() * r.nextDouble(), yard.getHeight() * r.nextDouble());
				netTarget = (Vertex) DistributedAgentFactory.newIstance(
						Vertex.class,
						new Class[]{SimState.class,Integer.class,Boolean.class,String.class,Double2D.class},
						new Object[]{this,targetComm, targetFlag, target.getLabel()+"", pos},
						DVertexState.class);
						
						//new Vertex(this, targetComm, targetFlag, target.getLabel()+"", pos);
				myVertexes.put(target.getLabel(), netTarget);
				if(targetComm == commID)
					schedule.scheduleOnce(netTarget);
				network.addNode(netTarget);
				yard.setObjectLocation(netTarget, pos);
			}
			network.addEdge(netSource, netTarget, null);
		}

		init_connection();

		try {
			if(getTrigger()!=null)
				getTrigger().publishToTriggerTopic("Simulation cell "+network.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public DistributedField getField() {
		// TODO Auto-generated method stub
		return network;
	}

	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setPortrayalForObject(Object o) {
		// TODO Auto-generated method stub
		for(Object oo:yard.allObjects)
		{
			RemoteUnpositionedAgent<Double2D> rm=(RemoteUnpositionedAgent)oo;
			if(rm.getLabel().equalsIgnoreCase(
					((RemoteUnpositionedAgent)o).getLabel()))
			{
				yard.remove(oo);
				((RemoteUnpositionedAgent)o).setPos(rm.getPos());
				yard.setObjectLocation(o, rm.getPos());
				break;
			}
		}
		return false;
	}

	@Override
	public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
		// TODO Auto-generated method stub
	}
}
