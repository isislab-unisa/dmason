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

package it.isislab.dmason.sim.field.network;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemoteUnpositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.network.region.RegionNetwork;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.network.DNetworkRegion;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;
import it.isislab.dmason.sim.field.support.network.UpdateNetworkMap;
import it.isislab.dmason.util.connection.Connection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.engine.SimState;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
/**
 * 
 * @author Ada Mancuso
 * @author Francesco Milone
 * @author Carmine Spagnuolo
 *
 *	This class implements the Distributed version of the field Network of Mason.
 *
 */
public class DNetwork extends Network implements DistributedFieldNetwork{

	private int numVertex;
	private String topicPrefix;
	public CellType cellType;
	private SimState sm;
	public String graph_id;
	private int my_community;
	public GraphSubscribersEdgeList grpsub;
	private ArrayList<RemoteUnpositionedAgent> myCommunityNetwork;
	private HashMap<Integer, RegionNetwork> messageOnNetwork;
	private HashMap<String, RemoteUnpositionedAgent> toMigrate;
	private UpdateNetworkMap updates;
	
	/**
	 * Start time for
	 */
	private long startTime;
	/**
	 * 
	 */
	private long endTime;
	

	public DNetwork(SimState sm, int rows, int columns, int i, int j, GraphSubscribersEdgeList gprsub,String graph_id, String prefix) {
		cellType = new CellType(i, j);
		my_community=(cellType.pos_i*rows)+cellType.pos_j;
		topicPrefix=prefix;
		this.graph_id=graph_id;
		this.sm=sm;
		numVertex=0;
		this.grpsub=gprsub;
		messageOnNetwork = new HashMap<Integer, RegionNetwork>();
		myCommunityNetwork = new ArrayList<RemoteUnpositionedAgent>();
		toMigrate = new HashMap<String, RemoteUnpositionedAgent>();
//		System.out.println(my_community+") "+gprsub + " "+ gprsub.getPublisher(my_community));
		for (Integer neight : gprsub.getPublisher(my_community)) {
			messageOnNetwork.put(neight,new RegionNetwork(neight));
		}
		endTime=startTime=0;
	
	}

	@Override
	public boolean updateNode(RemoteUnpositionedAgent rm, SimState sm) {

		RemoteUnpositionedAgent rmn=rm;
		myCommunityNetwork.add(rmn);
		Bag edgesIn = this.getEdgesIn(rmn);
		Bag edgesOut = this.getEdgesOut(rmn);
		for (Object object : edgesOut) {
			Edge e = (Edge) object;
			if(((RemoteUnpositionedAgent)e.to()).getCommunityId()!=my_community)
			{
				RemoteUnpositionedAgent to = (RemoteUnpositionedAgent)e.to();
				int commToInsert = to.getCommunityId();
				if(messageOnNetwork.get(commToInsert)==null)
					messageOnNetwork.put(commToInsert, new RegionNetwork(commToInsert));	
				messageOnNetwork.get(commToInsert).add(rmn);
				toMigrate.put(to.getLabel(), to);
				
			}
		}
		for (Object object : edgesIn) {
			Edge e = (Edge) object;
			if(((RemoteUnpositionedAgent)e.from()).getCommunityId()!=my_community)
			{
				RemoteUnpositionedAgent from = (RemoteUnpositionedAgent)e.from();
				int commToInsert = from.getCommunityId();
				if(messageOnNetwork.get(commToInsert)==null)
					messageOnNetwork.put(commToInsert, new RegionNetwork(commToInsert));
				messageOnNetwork.get(commToInsert).add(rmn);
				toMigrate.put(from.getLabel(), from);
			}
		}	
		return true;
	}

	@Override
	public void setNumberOfUpdatesToSynchro(int number)
	{
		updates = new UpdateNetworkMap(number);
	}

	@Override
	public boolean synchro() {

		startTime=System.currentTimeMillis();
		// Publish the regions to correspondent topics for the neighbors
		publishRegions();

		// Process Updates from neighbors
		processUpdates();

		//Schedule agents
		for(RemoteUnpositionedAgent ra : myCommunityNetwork)
		{
			sm.schedule.scheduleOnce(ra);
		}

		//Reset initial conditions
		myCommunityNetwork = new ArrayList<RemoteUnpositionedAgent>();

		for(RegionNetwork rn : messageOnNetwork.values())
		{
			Integer community = rn.community;
			rn=new RegionNetwork(community);
		}

		toMigrate.clear();
		endTime=System.currentTimeMillis();
		return true;
	}

	protected void publishRegions()
	{	
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();
		//publish region to right topic/community
		for(Integer commId : messageOnNetwork.keySet())
		{
			try 
			{
				RegionNetwork rn = messageOnNetwork.get(commId);
				DNetworkRegion dnr = new DNetworkRegion(rn, (sm.schedule.getSteps()-1),cellType,my_community);
				connWorker.publishToTopic(dnr,topicPrefix+"-Network-"+my_community+"-"+commId,graph_id);
		
			} catch (Exception e1) { e1.printStackTrace();}
		}
	}

	protected void processUpdates()
	{
		// Take from UpdateNetworkMap the updates for current last terminated step
		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1);
			while(!q.isEmpty())
			{
				DNetworkRegion region=(DNetworkRegion)q.poll();
				// Searching the node who need updates
				for(RemoteUnpositionedAgent rnNew : region.out.values())
				{

					boolean nodeFound=false;
					RemoteUnpositionedAgent rnOld = toMigrate.get(rnNew.getLabel());
					if(rnOld!=null)
					{
						//Node Founded!!!
						nodeFound=true;
						Bag edgesIn = (Bag) this.getEdgesIn(rnOld).clone();
						Bag edgesOut = (Bag) this.getEdgesOut(rnOld).clone();

						//Check if the node was in this field then i remove it
						if(this.removeNode(rnOld)==null) throw new Exception("Oh My NODE!!!");
						//and add the updated version
						this.addNode(rnNew);

						((DistributedState)sm).addToField(rnNew,null);

						//Add the edges to the updated node
						for (Object object : edgesOut) {
							Edge e = (Edge) object;
							Edge newEdge = new Edge(rnNew, e.getTo(), e.getInfo());
							addEdge(newEdge);
						}
						for (Object object : edgesIn) {
							Edge e = (Edge) object;
							Edge newEdge = new Edge(e.getFrom(), rnNew, e.getInfo());
							addEdge(newEdge);
						}		
					}
					
					//if the node is not in this field there is something wrong in messages
					if(nodeFound) nodeFound=false;
					else throw new Exception("Oh My Node! "+rnNew+" with community: "+rnNew.getCommunityId()+" on community "+
					(((DistributedState)sm).TYPE.pos_i*((DistributedState)sm).rows)+(((DistributedState)sm).TYPE.pos_j));
				}
			}
		} catch (Exception e1) { e1.printStackTrace(); }
	}


	public UpdateNetworkMap getNetworkUpdates() {
		return updates;
	}

	@Override
	public DistributedState getState() {
		return (DistributedState)sm;
	}


	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDistributedFieldID() {
		return graph_id;
	}

	@Override
	public VisualizationUpdateMap getGlobals() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createRegions(QuadTree... cell) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getCommunicationTime() {
		// TODO Auto-generated method stub
		return endTime-startTime;
	}
}
