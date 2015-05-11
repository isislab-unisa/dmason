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
package it.isislab.dmason.sim.app.DNetworkSIR2015;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.DistributedAgentFactory;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field.network.DNetworkFactory;
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;
import it.isislab.dmason.sim.field.network.kway.util.NetworkPartition;
import it.isislab.dmason.sim.field.network.kway.util.PartitionManager;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import java.util.HashMap;
import java.util.Random;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

@AuthorAnnotation(
		author = {"Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class Vertexes15 extends DistributedState<Double2D>{

	private static final long serialVersionUID = -7594558050842065632L;
	public static String topicPrefix = "Vertexes";
	public DNetwork network;
	public Continuous2D yard = new Continuous2D(1.0,100,100);
	private GeneralParam params;

	public Vertexes15()
	{
		super();
	}
	public String graph_path;
	public String graph_parts_path;
	public Vertexes15(GeneralParam params,String graph_path,String graph_parts_path)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),topicPrefix,params.getConnectionType());
		this.params=params;
		this.graph_path=graph_path;
		this.graph_parts_path=graph_parts_path;
	}

	@Override
	public void start()
	{
		super.start();
		
		int commID = (TYPE.pos_i*rows)+TYPE.pos_j;
		NetworkPartition parts_data=PartitionManager.getNetworkPartition(graph_path, graph_parts_path, commID);

		network = DNetworkFactory.createDNetworkField(this, super.rows, super.columns, TYPE.pos_i, TYPE.pos_j, parts_data.getEdges_subscriber_lsit(),"mygraph", topicPrefix);
		HashMap<Integer, Vertex15> myVertexes = new HashMap<Integer,Vertex15>();
		// clear the yard
		yard.clear();

		// clear the network
		network.clear();
		int nu_of_vertices=0;
		Random r = new Random(System.currentTimeMillis());
		for (Edge e : parts_data.getOriginal_graph().edgeSet()) {

			Vertex source = (Vertex)  parts_data.getOriginal_graph().getEdgeSource(e);
			Vertex target = (Vertex)  parts_data.getOriginal_graph().getEdgeTarget(e);
			Integer sourceComm =  parts_data.getGraph2parts().get(source);
			Integer targetComm = parts_data.getGraph2parts().get(target);
		
			Vertex15 netSource = null;
			Vertex15 netTarget = null;

			netSource = myVertexes.get(source.getId());
			netTarget = myVertexes.get(target.getId());

			boolean sourceFlag = source.getId()==1;
			boolean targetFlag = target.getId()==1;
			
			if(netSource==null)
			{
				Double2D pos=new Double2D(yard.getWidth() * r.nextDouble(), yard.getHeight() * r.nextDouble());
				netSource = (Vertex15) DistributedAgentFactory.newIstance(
						Vertex15.class,
						new Class[]{SimState.class,Integer.class,Boolean.class,String.class,Double2D.class},
						new Object[]{this,sourceComm, sourceFlag, source.getId()+"", pos},
						DVertexState15.class);
						
				myVertexes.put(source.getId(), netSource);
				if(sourceComm == commID) {
					schedule.scheduleOnce(netSource);
					nu_of_vertices++;
				}
				network.addNode(netSource);
				yard.setObjectLocation(netSource, pos);
			}

			if(netTarget==null)
			{
				Double2D pos=new Double2D(yard.getWidth() * r.nextDouble(), yard.getHeight() * r.nextDouble());
				netTarget = (Vertex15) DistributedAgentFactory.newIstance(
						Vertex15.class,
						new Class[]{SimState.class,Integer.class,Boolean.class,String.class,Double2D.class},
						new Object[]{this,targetComm, targetFlag, target.getId()+"", pos},
						DVertexState15.class);
						
				myVertexes.put(target.getId(), netTarget);
				if(targetComm == commID){
					schedule.scheduleOnce(netTarget);
					nu_of_vertices++;
				}
				network.addNode(netTarget);
				yard.setObjectLocation(netTarget, pos);
			}
			network.addEdge(netSource, netTarget, null);
		}
		System.out.println(nu_of_vertices);
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
