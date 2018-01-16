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

package it.isislab.dmason.experimentals.sim.field.grid.sparse.loadbalanced;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.support.field2D.loadbalanced.UpdatePositionIntegerField;
import it.isislab.dmason.experimentals.sim.field.support.field2D.loadbalanced.UpdatePositionInterface;
import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.LoadBalancingIntegerField;
import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellIntegerField;
import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2DLB;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.grid.region.RegionInteger;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2D;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 *  <h3>This Field extends SparseGrid2D, to be used in a distributed environment. All the necessary informations for 
 *  the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>square mode</i>.
 *  It represents the field managed by a single peer.
 *  This is an example for a square mode distribution with 9 peers (only to distinguish the regions):
 *  (for code down)
 *  <p>
 *
 *	<ul>
 *	<li>MYFIELD : Region to be simulated by peer.</li>
 *
 *	<li>LEFT_MINE, RIGHT_MINE, UP_MINE, DOWN_MINE,CORNER_MINE_LEFT_UP,CORNER_MINE_LEFT_DOWN,
 *		CORNER_MINE_RIGHT_UP,CORNER_MINE_RIGHT_DOWN :Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, RIGHT_OUT, UP_OUT, DOWN_OUT, CORNER_OUT_LEFT_UP_DIAG, CORNER_OUT_LEFT_DOWN_DIAG,
 *		CORNER_OUT_RIGHT_UP_DIAG, CORNER_OUT_RIGHT_DOWN_DIAG : Boundaries Regions those must not be simulated and sent to neighbors to be simulated.</li>
 *   <li>
 *	All peers subscribes to the topic of boundary region which want the information and run a asynchronous thread
 *	to receive the updates, then publish a topic for every their border (or neighbor), that can be :
 *	<ul>
 *	<li> MYTOPIC L (LEFT BORDER)</li>
 *	<li> MYTOPIC R (RIGHT BORDER)</li>
 *	<li> MYTOPIC U (UPPER BORDER)</li>
 *	<li> MYTOPIC D (LOWER BORDER)</li>
 *
 *	<li> MYTOPIC CUDL (Corner Up Diagonal Left)</li>
 *	<li> MYTOPIC CUDR (Corner Up Diagonal Right)</li>
 *	<li> MYTOPIC CDDL (Corner Down Diagonal Left)</li>
 *	<li> MYTOPIC CDDR (Corner Down Diagonal Right)</li>
 *</ul>
 *</li>
 *	</ul></p>
 *
 * <PRE>
  ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   CORNER DIAG           |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______NORTH_OUT_______|__|__|/________________________|
 * |________________________|__|__|______NORTH_MINE______|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        O  O  |                    / |  E  E                         |
 * |                        V  V  |                   /  |  S  S                         |
 * |         10             E  E  |         11   CORNER  |  T  T         12              |
 * |                        S  S  |               MINE   |  |  |                         |
 * |                        T  T  |                      |  M  O                         |
 * |                        O  M  |       MYFIELD        |  |  U                         |
 * |                        U  I  |                      |  N  T                         |
 * |                        T  N  |                      |  E  |                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_MINE_________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_OUT__________|__|__|_________________________|
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |       20               |  |  |          21          |  |  |           22            |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * ---------------------------------------------------------------------------------------
 * </PRE>
 */

public class DSparseGrid2DXYLB extends DSparseGrid2D implements DistributedField2DLB
{	

	private String NAME;
	private RegionInteger outAgents;
	//quando ricevo cellette e ho splittato a mia volta imposto i topic diversamente delle cellette
	private boolean isSplitted;
	private boolean splitDone;
	private boolean prepareForBalance;
	private boolean prepareForUnion;
	private boolean preUnion;
	private boolean unionDone;
	private boolean isUnited;
	private int positionForUnion = -1;
	//Serve per dividere le celle per il load Balancing
	private LoadBalancingIntegerField balance;
	private HashMap<Integer, MyCellInterface> toSendForBalance;
	private HashMap<Integer, MyCellInterface> toSendForUnion;

	private HashMap<Integer, UpdatePositionIntegerField<DistributedRegion<Integer, Int2D>>> hashUpdatesPosition;

	private String topicPrefix = "";

	private int numAgents;
	private int width,height;
	private int NUMPEERS;

	// <--


	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();

	/**
	 * Constructor of class with paramaters:
	 * 
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param AOI maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DSparseGrid2DXYLB(int width, int height,SimState sm,int jumpDist,int i,int j,int rows,int columns, String name, String prefix) 
	{		
		super(width, height);
		this.width=width;
		this.height=height;
		this.NAME = name;
		this.sm=sm;
		this.topicPrefix = prefix;
		cellType = new CellType(i, j);
		AOI=jumpDist;
		NUMPEERS=rows*columns;
		toSendForBalance = new HashMap<Integer, MyCellInterface>();
		toSendForUnion = new HashMap<Integer, MyCellInterface>();
		outAgents = new RegionInteger(0, 0, 0, 0);

		//upper left corner's coordinates
		own_x=(width/((int)Math.sqrt(NUMPEERS)))* cellType.pos_j; //inversione
		own_y=(height/((int)Math.sqrt(NUMPEERS)))* cellType.pos_i;

		// own width and height
		my_width=(int) (width/Math.sqrt(NUMPEERS));
		my_height=(int) (height/Math.sqrt(NUMPEERS));

		//Divide le celle inizialmente
		balance = new LoadBalancingIntegerField();

		//Calcola il CellType dei vicini
		int [] lP = {0,1,2,7,8,3,6,5,4};

		int np = (int) Math.sqrt(NUMPEERS);
		int z = 0;

		for (int k = 0; k < 8; k++) {
			toSendForUnion.put(k, null);
		}
		//contiene le celle divise inizialmente
		ArrayList<MyCellInterface> listOriginalCell = balance.createRegions(this,my_width, my_height, AOI, own_x,own_y,NUMPEERS);

		//struttura in cui vengono inserite le mycell
		listGrid = new HashMap<Integer,HashMap<CellType, MyCellInterface>>();

		//riempie la struttura contenitore di mycell
		for(int k = 0; k < 9; k++){
			listGrid.put(k, new HashMap<CellType, MyCellInterface>());
		}

		for(int k = 0; k < lP.length; k++){
			listGrid.get(lP[k]).put(cellType, listOriginalCell.get(k));
		}

		//Contenitore di UpdatePosition per gli aggiornamenti e le publish
		hashUpdatesPosition = new HashMap<Integer, UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>>();

		this.isSplitted = false;
		this.splitDone = false;
		this.prepareForBalance = false;
		this.prepareForUnion = false;
		this.preUnion = false;
		this.unionDone = false;
		this.isUnited = true;

		updates_cacheLB=new ArrayList<ArrayList<Region<Integer, Int2D>>>();
		numAgents=0;
	}


	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	@Override
	public Int2D getAvailableRandomLocation()
	{
		int x=(((DistributedState)sm).random.nextInt(width)%(my_width-1))+own_x;
		if(x>(width-1)) x--;
		int y=(((DistributedState)sm).random.nextInt(height)%(my_height-1))+own_y;
		if(y>(height-1)) y--;
		//rm.setPos(new Int2D(x, y));

		return (new Int2D(x, y));
	}

	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
	@Override
	//public boolean setDistributedObjectLocation(final Int2D location,RemotePositionedAgent<Int2D> rm,SimState sm)
	//{
	public boolean setDistributedObjectLocation(final Int2D location, Object remoteObject,SimState sm) throws DMasonException
	{

		if(!(remoteObject instanceof RemotePositionedAgent) && !(((RemotePositionedAgent)remoteObject).getPos() instanceof Int2D))
			throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter must be a RemotePositionedAgent<Int2D>"); 

		RemotePositionedAgent<Int2D> rm=(RemotePositionedAgent<Int2D>) remoteObject;

		numAgents++;

		boolean fl = false;

		if(prepareForBalance)
		{
			this.remove(rm);

			boolean b = false;

			for(Integer pos : listGrid.keySet())
			{	
				HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

				for(CellType ct : hm.keySet())
				{
					if((hm.get(ct).getPosition() == MyCellInterface.CENTER))
					{	
						MyCellIntegerField mc = (MyCellIntegerField) hm.get(ct);
						if(mc.getMyField().isMine(location.x,location.y))
							return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));

						Class o=mc.getMyRMap().getClass();

						Field[] fields = o.getDeclaredFields();

						for (int z = 0; z < fields.length; z++)
						{
							fields[z].setAccessible(true);

							try
							{
								String name=fields[z].getName();
								Method method = o.getMethod("get"+name, null);
								Object returnValue = method.invoke(mc.getMyRMap(), null);

								if(returnValue!=null)
								{
									Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

									if(region.isMine(location.x, location.y))
									{
										if(name.contains("mine"))
										{
											region.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
											mc.getMyField().put(rm.getId(),new EntryAgent<Int2D>(rm, location));
										}
										else
											if(name.contains("out"))
											{
												region.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
												outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
											}
									}
								}
							}
							catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
					else
						if((ct.toString().equals(cellType.toString())) 
								&& ((hm.get(ct).getPosition() != MyCellInterface.CENTER)))
						{
							MyCellIntegerField mc = (MyCellIntegerField) hm.get(ct);
							b = balance.addForBalance(location, rm, mc);
							fl = fl || b;
						}	    			
						else
						{
							MyCellIntegerField mc = (MyCellIntegerField) hm.get(ct);

							if(mc.getMyField().isMine(location.x,location.y))
								return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));

							if(setAgents(rm, location, mc))
								fl = fl || true;
						}
				}
			}
		} 
		else
			if(prepareForUnion && !isSplitted)
			{
				this.remove(rm);

				boolean u = false;

				for(Integer pos : listGrid.keySet())
				{	
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType ct : hm.keySet())
					{	/*
		    			MyCellIntegerField mc =  (MyCellIntegerField) hm.get(ct);
		    			u = balance.addForBalance(location, rm, mc);
		    			fl = fl || u;*/
						MyCellIntegerField mc =  (MyCellIntegerField) hm.get(ct);

						if(mc.getMyField().isMine(location.x,location.y))
							return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));

						Class o=mc.getMyRMap().getClass();

						Field[] fields = o.getDeclaredFields();

						for (int z = 0; z < fields.length; z++)
						{
							fields[z].setAccessible(true);

							try
							{
								String name=fields[z].getName();
								Method method = o.getMethod("get"+name, null);
								Object returnValue = method.invoke(mc.getMyRMap(), null);

								if(returnValue!=null)
								{
									Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

									if(region.isMine(location.x, location.y))
									{
										if(name.contains("mine"))
										{
											fl = u || region.put(rm.getId(),new EntryAgent<Int2D>(rm, location))!=null?true:false;
											mc.getMyField().put(rm.getId(),new EntryAgent<Int2D>(rm, location));
										}
										else
											if(name.contains("out"))
											{
												fl = u || region.put(rm.getId(),new EntryAgent<Int2D>(rm, location))!=null?true:false;
												outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
											}
									}
								}
							}
							catch (Exception e) {
								// TODO: handle exception
							}
						}
					}
				}
			}    	
			else
				if(preUnion)
				{
					this.remove(rm);

					boolean f = false;

					for(Integer pos : listGrid.keySet())
					{	
						HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
						for(CellType ct : hm.keySet())
						{	
							MyCellIntegerField mc =(MyCellIntegerField) hm.get(ct);
							if(mc.isUnion())
							{
								f = balance.addForBalance(location, rm, mc);
								fl = fl || f;
							}
							else
							{
								if(mc.getMyField().isMine(location.x,location.y)) 
									return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));
								if(setAgents(rm, location, mc))
									fl = fl || true;
							}
						}
					}
				}
				else
				{		    		
					for(Integer pos : listGrid.keySet())
					{	
						HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

						for(CellType ct : hm.keySet())
						{	
							MyCellIntegerField mc = (MyCellIntegerField)hm.get(ct);

							if(mc.isMine(location.x, location.y) && getState().schedule.getSteps()>1)
							{
								if(mc.getMyField().isMine(location.x,location.y)) 
									return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));
								if(setAgents(rm, location, mc)){
									fl = fl || true;
								}
							}
							else{
								if(mc.getMyField().isMine(location.x,location.y))  
									return mc.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));
								if(setAgents(rm, location, mc))
									fl = fl || true;
							}
						}
					}
				}    	
		if(fl) 
			return true;
		else
		{
			System.out.println(cellType+")OH MY GOD!"+"  location: "+location+" ID= "+rm.getId()); // it should never happen (don't tell it to anyone shhhhhhhh! ;P) 
			return false;
		}
	}

	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	@Override
	public synchronized boolean synchro() 
	{	

		//startStep=System.currentTimeMillis();

		for(ArrayList<Region<Integer, Int2D>> arr : updates_cacheLB)
		{
			for(Region<Integer,Int2D> region : arr)
			{
				for(EntryAgent<Int2D> remote_agent : region.values())
				{
					this.remove(remote_agent.r);
				}
			}
		}

		if(prepareForUnion && !isSplitted)
		{
			for (int j2 = 0; j2 <= 16; j2++) {
				listGrid.get(MyCellInterface.CENTER).get(cellType).getPositionGood().put(j2, false);
				listGrid.get(MyCellInterface.CENTER).get(cellType).getPositionPublish().put((j2%8), false);
			}
		}

		if(preUnion){
			CellType c = null;
			int p=-1;
			for(Integer pos : listGrid.keySet())
			{	
				HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
				for(CellType ct : hm.keySet())
				{	
					if(!hm.get(ct).getParentCellType().toString().equals(cellType.toString()) && 
							hm.get(ct).isUnion()){
						c = ct;
						p = pos;
						positionForUnion = pos;
						if(!isSplitted)	
							clearReturnedOut((MyCellIntegerField)hm.get(ct));
					}
				}
			}
			if(p!=-1)
				toSendForUnion.put(p, listGrid.get(p).remove(c));
		}

		resetPublishList(sm.schedule.getSteps()-1);

		if(preUnion)
		{
			if(!isSplitted)
				for(Integer position : toSendForUnion.keySet())
				{
					if(toSendForUnion.get(position)!= null){

						if(position%2!=0) 	// topic sui lati
						{
							listGrid.get((position-1+8)%8).get(cellType).getPositionPublish().put(position, true);
							listGrid.get((position+1+8)%8).get(cellType).getPositionPublish().put(position, true);
						}
					}
				}

		}
		/*
		beforePrepare=System.currentTimeMillis();
		timeStep.print(beforePrepare-startStep+";");
		 */
		//--> publishing the regions to correspondent topics for the neighbors
		preparePublishUnion();
		preparePublishCenter();
		preparePublishMicroCell();
		positionForUnion = -1;
		unionDone = false;

		if(preUnion)
		{
			if(!isSplitted)
				for(Integer position : toSendForUnion.keySet())
				{
					if(toSendForUnion.get(position)!= null){

						if(position%2==0) 	// topic sugli angoli
						{
							listGrid.get(position).get(cellType).getPositionPublish().put(position, true);
						}
						else // topic sui lati
						{	
							listGrid.get((position-1+8)%8).get(cellType).getPositionPublish().put(position, true);
							listGrid.get(position).get(cellType).getPositionPublish().put(position, true);
							listGrid.get((position+1+8)%8).get(cellType).getPositionPublish().put(position, true);
						}
					}
				}

			preUnion = false;
		}

		if(prepareForBalance)
		{
			for (int k = 0; k < 8; k++) {

				toSendForBalance.put(k, listGrid.get(k).remove(cellType));
			}
		}

		for(Integer pos : listGrid.keySet())
		{
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
			for(CellType ct : hm.keySet())
			{
				removeOut(hm.get(ct));
			}
		}

		//every agent in the myfield region is scheduled
		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

			for(CellType ct : hm.keySet())
			{	
				if(ct.equals(cellType)){
					MyCellIntegerField md = (MyCellIntegerField) hm.get(ct);

					for(EntryAgent<Int2D> e: md.getMyField().values())
					{
						RemotePositionedAgent<Int2D> rm=e.r;
						Int2D loc=e.l;
						rm.setPos(loc);
						this.remove(rm);
						sm.schedule.scheduleOnce(rm);
						setObjectLocation(rm,loc);
					}
				}
			}
		}

		//every agent in the myfield region is scheduled
		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

			for(CellType ct : hm.keySet())
			{	
				if(!ct.equals(cellType)){
					MyCellIntegerField md = (MyCellIntegerField) hm.get(ct);

					for(EntryAgent<Int2D> e: md.getMyField().values())
					{
						RemotePositionedAgent<Int2D> rm=e.r;
						Int2D loc=e.l;
						rm.setPos(loc);
						this.remove(rm);
						sm.schedule.scheduleOnce(rm);
						setObjectLocation(rm,loc);
					}
				}
			}
		}

		updates_cacheLB=new ArrayList<ArrayList<Region<Integer, Int2D>>>();

		((DistributedMultiSchedule)(sm.schedule)).manageMerge(hashUpdatesPosition,this,cellType);
		((DistributedMultiSchedule)(sm.schedule)).manageBalance(hashUpdatesPosition,this,cellType,balance);

		/*
		beforePublish=System.currentTimeMillis();
		timeStep.print(beforePublish-beforePrepare+";");
		 */
		//PUBLISH SUI TOPIC
		try {
			Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();

			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.WEST),topicPrefix+cellType.toString()+"L", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.EAST),topicPrefix+cellType.toString()+"R", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.NORTH),topicPrefix+cellType.toString()+"U", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.SOUTH),topicPrefix+cellType.toString()+"D", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.NORTH_WEST),topicPrefix+cellType.toString()+"CUDL", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.NORTH_EAST),topicPrefix+cellType.toString()+"CUDR", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST),topicPrefix+cellType.toString()+"CDDL", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST),topicPrefix+cellType.toString()+"CDDR", NAME);	

		} catch (Exception e1) { e1.printStackTrace();}
		//<--
		/*
		afterPublish=System.currentTimeMillis();
		timeStep.print(afterPublish-beforePublish+";");
		 */
		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations
		PriorityQueue<Object> q;
		//long sumUpdate=0;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, 8);			

			while(!q.isEmpty())
			{
				UpdatePositionIntegerField<DistributedRegion<Integer, Int2D>> region=(UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>)q.poll();

				((DistributedMultiSchedule)(sm.schedule)).externalAgents+=region.getNumAgentExternalCell();
				//sumUpdate-=System.currentTimeMillis();

				if(region.isPreBalance())
				{
					if(region.getMyCell()!=null)
					{
						MyCellIntegerField mc = region.getMyCell();

						resetArrivedCellPositions(mc);

						listGrid.get(mc.getPosition()).put(mc.getParentCellType(), mc);

						for(EntryAgent<Int2D> e: mc.getMyField().values())
						{							
							RemotePositionedAgent<Int2D> rm=e.r;
							Int2D loc=e.l;
							rm.setPos(loc);
							this.remove(rm);
							sm.schedule.scheduleOnce(rm);
							setObjectLocation(rm,loc);
						}

						if(!isSplitted){
							clearArrivedOut(mc);
							updateExternalOutFromAdiacentCell(mc);
						}
						updateFields(mc);
						updateInternalMine(mc);
						memorizeRegionOut(mc);
						if(!isSplitted)
						{
							if(mc.getPosition()%2==0) 	// position sugli angoli
							{
								listGrid.get(mc.getPosition()).get(cellType).getPositionGood().put(mc.getPosition(), false);
							}
							else // position sui lati
							{
								int h=10;

								{	//centrale
									listGrid.get(mc.getPosition()).get(cellType).getPositionGood().put(mc.getPosition(), false);

									for (int k = 0; k < 7; k+=2) 
									{	
										if(!listGrid.get(mc.getPosition()).get(cellType).getPositionPublish().get((k+1+8)%8))
										{
											listGrid.get(mc.getPosition()).get(cellType).getPositionGood().put(h+k, false);
										}
										if(!listGrid.get(mc.getPosition()).get(cellType).getPositionPublish().get((k-1+8)%8))
										{
											listGrid.get(mc.getPosition()).get(cellType).getPositionGood().put(h+k, false);
										}
									}
								}
								{	//laterali
									listGrid.get((mc.getPosition()-1+8)%8).get(cellType).getPositionGood().
									put(((mc.getPosition()+1+8)%8),false);
									listGrid.get((mc.getPosition()+1+8)%8).get(cellType).getPositionGood().
									put(((mc.getPosition()-1+8)%8),false);
								}
							}
						}
					}
					else
					{
						resetLocalPositionPublish(region.getPosition());
					}
				}
				//sumUpdate+=System.currentTimeMillis();

				//the owner send the cell requested by a splitted neighbours
				if(region.isPreUnion())
				{
					preUnion = true;
					int k = balance.calculatePositionForBalance(region.getPosition());
					HashMap<CellType, MyCellInterface> hm = listGrid.get(k);

					for(CellType ct : hm.keySet()){

						if(hm.get(ct).getParentCellType().toString().equals(region.getCellType().toString()))
						{
							hm.get(ct).setUnion(true);
							resetUnionLocalPositionForShipping(hm.get(ct).getPosition());
						}
					}
				}

				//cell received for union
				if(region.isUnion())
				{
					MyCellIntegerField mc = region.getMyCell();
					region.setMyCell(null);
					int rightPosition = balance.calculatePositionForBalance(mc.getPosition());
					mc.setPosition(rightPosition);
					mc.setParentCellType(cellType);
					mc.setUnion(false);
					listGrid.get(mc.getPosition()).put(cellType, mc);
					region.setUnion(false);
				}

				verifyUpdates(region);	
			}			

			if(prepareForBalance && !isSplitted)
			{
				for (int j2 = 0; j2 <= 16; j2++) {
					listGrid.get(MyCellInterface.CENTER).get(cellType).getPositionGood().put(j2, true);
				}

				for(Integer pos : listGrid.keySet())
				{
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType ct : hm.keySet()){

						if(!ct.equals(cellType))
						{

							for (int j2 = 0; j2 <= 16; j2++) {

								hm.get(ct).getPositionGood().put(j2, true);
							}
						}
					}
				}
			}

			makeUnion();

		}catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(isSplitted && splitDone){
			for (int j2 = 0; j2 <= 8; j2++) {
				listGrid.get(MyCellInterface.CENTER).get(cellType).getPositionPublish().put(j2, true);
			}

			for(Integer pos : listGrid.keySet())
			{
				if(pos != MyCellInterface.CENTER)
					listGrid.get(pos).remove(cellType);
			}

			splitDone = false;
		}
		ArrayList<Region<Integer,Int2D>> tmp = new ArrayList<Region<Integer,Int2D>>();
		tmp.add(outAgents.clone());
		updates_cacheLB.add(tmp);
		for(ArrayList<Region<Integer,Int2D>> regions : updates_cacheLB)
			for(Region<Integer,Int2D> r : regions)
				for(EntryAgent<Int2D> e_m: r.values())
				{
					RemotePositionedAgent<Int2D> rm=e_m.r;
					((DistributedState<Int2D>)sm).addToField(rm,e_m.l);
				}

		outAgents = new RegionInteger(0, 0, 0, 0);

		this.reset();
		/*
		timeStep.print(sumUpdate+";");

		afterUpdateCell=System.currentTimeMillis();
		timeStep.print(afterUpdateCell-afterPublish+";\n");
		timeStep.flush();
		 */
		//numAgents=0;
		return true;
	}

	//Resetta le strutture contenitore delle publish
	private void resetPublishList(long step){

		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_UP_LEFT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.CORNER_DIAG_UP_LEFT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.UP, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.UP,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_UP_RIGHT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.CORNER_DIAG_UP_RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.RIGHT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_DOWN_RIGHT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.CORNER_DIAG_DOWN_RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.DOWN, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.DOWN,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_DOWN_LEFT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.CORNER_DIAG_DOWN_LEFT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.LEFT, 
				new UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>>(step,UpdatePositionInterface.LEFT,cellType,null));
	}

	private void makeUnion() {

		if(prepareForUnion && !isSplitted)
		{
			for(Integer pos : listGrid.keySet())
			{
				if(pos != MyCellInterface.CENTER)
				{
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType c : hm.keySet()){

						if(c.equals(cellType))
						{
							MyCellIntegerField mc = (MyCellIntegerField)hm.get(c);
							int antiPosition = balance.calculatePositionForBalance(mc.getPosition());
							if(mc.getPosition()%2==0)//angoli
							{
								for (int j2 = 0; j2 < 8; j2++) {

									if(j2 == ((antiPosition-1+8)%8))	
										mc.getPositionPublish().put(j2, false);
									else
										if(j2 == antiPosition)
											mc.getPositionPublish().put(j2, false);
										else
											if(j2 == ((antiPosition+1+8)%8))
												mc.getPositionPublish().put(j2, false);
											else
												mc.getPositionPublish().put(j2, true);	
								}
							}
							else
							{
								for (int j2 = 0; j2 < 8; j2++) 
								{
									if(j2 == ((mc.getPosition()-1+8)%8))	
										mc.getPositionPublish().put(j2, true);
									else
										if(j2 == mc.getPosition())
											mc.getPositionPublish().put(j2, true);
										else
											if(j2 == ((mc.getPosition()+1+8)%8))
												mc.getPositionPublish().put(j2, true);
											else
												mc.getPositionPublish().put(j2, false);
								}
							}
						}
					}
				}
			}

			for(Integer pos : listGrid.keySet())
			{
				if(pos != MyCellInterface.CENTER)
				{
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType ct : hm.keySet())
					{
						if(!ct.equals(cellType) && hm.get(ct)!=null)
						{
							MyCellIntegerField mc = (MyCellIntegerField)hm.get(ct);
							int topicPosition = mc.getPosition();

							if(topicPosition%2==0) 	// topic sugli angoli
							{
								listGrid.get(topicPosition).get(cellType).getPositionPublish().put(topicPosition, false);
							}
							else // topic sui lati
							{
								listGrid.get((topicPosition-1+8)%8).get(cellType).getPositionPublish().put(((topicPosition+1+8)%8), false);
								listGrid.get((topicPosition-1+8)%8).get(cellType).getPositionPublish().put(topicPosition, false);
								listGrid.get(topicPosition).get(cellType).getPositionPublish().put(topicPosition, false);
								listGrid.get((topicPosition+1+8)%8).get(cellType).getPositionPublish().put(((topicPosition-1+8)%8), false);
								listGrid.get((topicPosition+1+8)%8).get(cellType).getPositionPublish().put(topicPosition, false);
							}
						}
					}
				}
			}

			for(Integer pos : listGrid.keySet())
			{
				if(pos != MyCellInterface.CENTER)
				{
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType c : hm.keySet()){

						if(c.equals(cellType))
						{
							MyCellIntegerField mc = (MyCellIntegerField)hm.get(c);
							int antiPosition = balance.calculatePositionForBalance(mc.getPosition());
							int position = mc.getPosition();

							if(position%2==0) //angoli
							{
								for (int l = 0; l <= 16; l++) {

									mc.getPositionGood().put(l, true);
								}

								mc.getPositionGood().put(((antiPosition+2+8)%8)+9, false);
								mc.getPositionGood().put(((antiPosition+1+8)%8), false);
								mc.getPositionGood().put((antiPosition+10), false);
								mc.getPositionGood().put(antiPosition, false);
								mc.getPositionGood().put((antiPosition+9), false);
								mc.getPositionGood().put(((antiPosition-1+8)%8), false);
								mc.getPositionGood().put(((antiPosition-2+8)%8)+10, false);


								if(!mc.getPositionPublish().get(((position-2+8)%8)))
								{
									mc.getPositionGood().put(((position-2+8)%8), false);
								}
								if(!mc.getPositionPublish().get(position))
								{
									mc.getPositionGood().put(position, false);
								}
								if(!mc.getPositionPublish().get(((position+2+8)%8)))
								{
									mc.getPositionGood().put(((position+2+8)%8), false);
								}
							}
							else //lati
							{
								for (int l = 0; l <= 16; l++) {

									mc.getPositionGood().put(l, false);
								}

								if(mc.getPositionPublish().get(position))
								{
									mc.getPositionGood().put(((position+1+8)%8), true);
									mc.getPositionGood().put(((position+1+8)%8)+9, true);
									mc.getPositionGood().put(position, true);
									mc.getPositionGood().put(((position-1+8)%8), true);
									mc.getPositionGood().put(((position-1+8)%8)+10, true);
								}
								else
								{
									mc.getPositionGood().put(((position+1+8)%8), true);
									mc.getPositionGood().put(((position+1+8)%8)+9, false);
									mc.getPositionGood().put(position, false);
									mc.getPositionGood().put(((position-1+8)%8), true);
									mc.getPositionGood().put(((position-1+8)%8)+10, false);

								}
							}
						}
					}
				}
			}

			for(Integer pos : listGrid.keySet())
			{	
				if(pos != MyCellInterface.CENTER)
				{
					HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

					for(CellType ct : hm.keySet())
					{
						if(ct.equals(cellType)){
							MyCellIntegerField md = (MyCellIntegerField)hm.get(ct);

							for(EntryAgent<Int2D> e: md.getMyField().values())
							{
								RemotePositionedAgent<Int2D> rm=e.r;
								Int2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);
							}

							updateFields(md);
							updateInternalMine(md);
							updateInternalOut(md);
							memorizeRegionOut(md);
						}
					}
				}
			}

			updateInternalOut(listGrid.get(MyCellInterface.CENTER).get(cellType));

			for(Integer pos : listGrid.keySet())
			{	
				HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
				for(CellType ct : hm.keySet())
				{
					if(!ct.equals(cellType))
					{
						updateInternalOut(hm.get(ct));
					}
				}
			}

			prepareForUnion = false;
			isUnited = true;
		}

		if(prepareForUnion && isSplitted)
		{ 
			for(Integer pos : listGrid.keySet())
			{
				HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

				for(CellType c : hm.keySet()){

					if(!c.equals(cellType))
					{
						MyCellIntegerField mc = (MyCellIntegerField)hm.get(c);

						int position = mc.getPosition();
						int antiPosition = balance.calculatePositionForBalance(position);

						int h = 10; 

						if(position%2==0) //angoli
						{
							//INIZIALIZZIAMO LE POSITION GOOD
							for (int k = 0; k <= 16; k++) 
							{
								if(k == antiPosition)
									mc.getPositionGood().put(antiPosition, false);
								else
									mc.getPositionGood().put(k, true);
							}

							for (int k = 0; k < 8; k++) 
							{
								if(k==(antiPosition))
									mc.getPositionPublish().put(k, false);
								else
									mc.getPositionPublish().put(k, true);
							}

							HashMap<CellType, MyCellInterface> hm1 = listGrid.get(((mc.getPosition()-1+8)%8));
							HashMap<CellType, MyCellInterface> hm2 = listGrid.get(((mc.getPosition()+1+8)%8));

							for(CellType ct : hm1.keySet())
							{
								if(!ct.equals(cellType) && hm1.get(ct)!=null)
								{
									mc.getPositionPublish().put(((mc.getPosition()-2+8)%8), false);
									mc.getPositionPublish().put(((mc.getPosition()-3+8)%8), false);
								}
							}
							for(CellType ct : hm2.keySet())
							{
								if(!ct.equals(cellType) && hm2.get(ct)!=null)
								{
									mc.getPositionPublish().put(((mc.getPosition()+2+8)%8), false);
									mc.getPositionPublish().put(((mc.getPosition()+3+8)%8), false);
								}
							}
						}
						else //laterali
						{								
							//INIZIALIZZIAMO LE POSITION GOOD
							for (int k = 0; k <= 16; k++) 
							{
								mc.getPositionGood().put(k, true);
							}

							mc.getPositionGood().put(((antiPosition+1+8)%8), false);
							mc.getPositionGood().put(((antiPosition+1+8)%8)+9, false);
							mc.getPositionGood().put(antiPosition, false);
							mc.getPositionGood().put(((antiPosition-1+8)%8), false);
							mc.getPositionGood().put(((antiPosition-1+8)%8)+10, false);
						}
					}

				}
			}

			isSplitted = false;
			unionDone = true;
		}
	}

	private void resetUnionLocalPositionForShipping(int position) {

		if(!isSplitted)
		{	
			if(position%2==0) 	// topic sugli angoli
			{
				listGrid.get(position).get(cellType).getPositionPublish().put(position, false);
			}
			else // topic sui lati
			{	
				listGrid.get((position-1+8)%8).get(cellType).getPositionPublish().put(position, false);
				listGrid.get(position).get(cellType).getPositionPublish().put(position, false);
				listGrid.get((position+1+8)%8).get(cellType).getPositionPublish().put(position, false);
			}
		}

		if(position%2!=0)
		{
			HashMap<CellType, MyCellInterface> hm1 = listGrid.get((position-1+8)%8);
			for(CellType ct : hm1.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerField mc1 =(MyCellIntegerField) hm1.get(ct);
					mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), true);
					mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), true);
				}
			}

			HashMap<CellType, MyCellInterface> hm2 = listGrid.get((position+1+8)%8);
			for(CellType ct : hm2.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerField mc2 = (MyCellIntegerField) hm2.get(ct);
					mc2.getPositionPublish().put(((mc2.getPosition()-2+8)%8), true);
					mc2.getPositionPublish().put(((mc2.getPosition()-3+8)%8), true);
				}
			}
		}

		if(!isSplitted)
		{
			if(position%2==0) 	// position sugli angoli
			{
				listGrid.get(position).get(cellType).getPositionGood().put(position, true);
			}
			else // position sui lati
			{			
				{	//centrale

					for (int l = 0; l <= 16; l++) {

						listGrid.get(position).get(cellType).getPositionGood().put(l, false);
					}

					listGrid.get(position).get(cellType).getPositionGood().put(((position+1+8)%8), true);
					listGrid.get(position).get(cellType).getPositionGood().put(((position+1+8)%8)+9, true);
					listGrid.get(position).get(cellType).getPositionGood().put(position, true);
					listGrid.get(position).get(cellType).getPositionGood().put(((position-1+8)%8), true);
					listGrid.get(position).get(cellType).getPositionGood().put(((position-1+8)%8)+10, true);
				}
				{	//laterali
					listGrid.get((position-1+8)%8).get(cellType).getPositionGood().
					put(((position+1+8)%8),true);
					listGrid.get((position+1+8)%8).get(cellType).getPositionGood().
					put(((position-1+8)%8),true);
				}
			}
		}
	}

	private void clearReturnedOut(MyCellIntegerField mc) {

		int position =mc.getPosition();

		if(position == MyCellInterface.NORTH)
		{
			mc.getMyRMap().SOUTH_WEST_OUT.clear();
			mc.getMyRMap().SOUTH_WEST_MINE.clear();
			mc.getMyRMap().SOUTH_EAST_MINE.clear();
			mc.getMyRMap().SOUTH_EAST_OUT.clear();
		}
		else
			if(position == MyCellInterface.EAST)
			{
				mc.getMyRMap().NORTH_WEST_OUT.clear();
				mc.getMyRMap().NORTH_WEST_MINE.clear();
				mc.getMyRMap().SOUTH_WEST_MINE.clear();
				mc.getMyRMap().SOUTH_WEST_OUT.clear();
			}
			else
				if(position == MyCellInterface.SOUTH)
				{
					mc.getMyRMap().NORTH_WEST_OUT.clear();
					mc.getMyRMap().NORTH_WEST_MINE.clear();
					mc.getMyRMap().NORTH_EAST_MINE.clear();
					mc.getMyRMap().NORTH_EAST_OUT.clear();
				}
				else
					if(position == MyCellInterface.WEST)
					{
						mc.getMyRMap().NORTH_EAST_OUT.clear();
						mc.getMyRMap().NORTH_EAST_MINE.clear();
						mc.getMyRMap().SOUTH_EAST_MINE.clear();
						mc.getMyRMap().SOUTH_EAST_OUT.clear();
					}
	}


	private void resetLocalPositionPublish(int position) {

		int topicPosition = balance.calculatePositionForBalance(position);

		if(!isSplitted)
		{	
			if(topicPosition%2==0) 	// topic sugli angoli
			{
				listGrid.get(topicPosition).get(cellType).getPositionPublish().put(topicPosition, false);
			}
			else // topic sui lati
			{
				listGrid.get((topicPosition-1+8)%8).get(cellType).getPositionPublish().put(topicPosition, false);
				listGrid.get(topicPosition).get(cellType).getPositionPublish().put(topicPosition, false);
				listGrid.get((topicPosition+1+8)%8).get(cellType).getPositionPublish().put(topicPosition, false);
			}
		}

		if(topicPosition%2!=0)
		{
			HashMap<CellType, MyCellInterface> hm1 = listGrid.get((topicPosition-1+8)%8);
			for(CellType ct : hm1.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerField mc = (MyCellIntegerField) hm1.get(ct);
					mc.getPositionPublish().put(((topicPosition+1+8)%8), false);
					mc.getPositionPublish().put(((topicPosition+2+8)%8), false);
				}
			}

			HashMap<CellType, MyCellInterface> hm2 = listGrid.get((topicPosition+1+8)%8);
			for(CellType ct : hm2.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerField mc = (MyCellIntegerField) hm2.get(ct);
					mc.getPositionPublish().put(((topicPosition-1+8)%8), false);
					mc.getPositionPublish().put(((topicPosition-2+8)%8), false);
				}
			}
		}
	}

	private void resetArrivedCellPositions(MyCellIntegerField mc) {

		int position = mc.getPosition();
		int antiPosition = balance.calculatePositionForBalance(position);

		int h = 10; 

		if(!isSplitted){
			if(position%2==0) //angoli
			{
				//INIZIALIZZIAMO LE POSITION GOOD
				for (int k = 0; k <= 16; k++) 
				{
					if(k == antiPosition)
						mc.getPositionGood().put(antiPosition, false);
					else
						mc.getPositionGood().put(k, true);
				}

				for (int k = 0; k < 8; k++) 
				{
					if(k==(antiPosition))
						mc.getPositionPublish().put(k, false);
					else
						mc.getPositionPublish().put(k, true);
				}

				HashMap<CellType, MyCellInterface> hm1 = listGrid.get(((mc.getPosition()-1+8)%8));
				HashMap<CellType, MyCellInterface> hm2 = listGrid.get(((mc.getPosition()+1+8)%8));

				for(CellType ct : hm1.keySet())
				{
					if(!ct.equals(cellType) && hm1.get(ct)!=null)
					{
						mc.getPositionPublish().put(((mc.getPosition()-2+8)%8), false);
						mc.getPositionPublish().put(((mc.getPosition()-3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						mc.getPositionPublish().put(((mc.getPosition()+2+8)%8), false);
						mc.getPositionPublish().put(((mc.getPosition()+3+8)%8), false);
					}
				}
			}
			else //laterali
			{								
				//INIZIALIZZIAMO LE POSITION GOOD
				for (int k = 0; k <= 16; k++) 
				{
					mc.getPositionGood().put(k, true);
				}

				for (int k = 0; k < 8; k++) 
				{	
					if(k==((position-2+8)%8))
						mc.getPositionPublish().put(k, true);
					else
						if(k==((position-1+8)%8))
							mc.getPositionPublish().put(k, true);
						else
							if(k == position)
								mc.getPositionPublish().put(k, true);
							else
								if(k == ((position+1+8)%8))
									mc.getPositionPublish().put(k, true);
								else
									if(k == ((position+2+8)%8))
										mc.getPositionPublish().put(k, true);
									else
										mc.getPositionPublish().put(k, false);
				}


				mc.getPositionGood().put(((antiPosition+1+8)%8), false);
				mc.getPositionGood().put(((antiPosition+1+8)%8)+9, false);
				mc.getPositionGood().put(antiPosition, false);
				mc.getPositionGood().put(((antiPosition-1+8)%8), false);
				mc.getPositionGood().put(((antiPosition-1+8)%8)+10, false);


				HashMap<CellType, MyCellInterface> hm1 = listGrid.get(((mc.getPosition()-1+8)%8));
				HashMap<CellType, MyCellInterface> hm2 = listGrid.get(((mc.getPosition()+1+8)%8));

				for(CellType ct : hm1.keySet())
				{
					if(!ct.equals(cellType) && hm1.get(ct)!=null)
					{
						MyCellIntegerField mc1 = (MyCellIntegerField)hm1.get(ct);
						mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), false);
						mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						MyCellIntegerField mc2 = (MyCellIntegerField)hm2.get(ct);
						mc2.getPositionPublish().put(((mc2.getPosition()-2+8)%8), false);
						mc2.getPositionPublish().put(((mc2.getPosition()-3+8)%8), false);
					}
				}
			}
		}
		else
		{

			if(mc.getPosition()%2==0)
			{
				for (int k = 0; k <= 16; k++) 
				{
					mc.getPositionGood().put(k, true);
				}

				for (int k = 0; k < 8; k++) 
				{
					mc.getPositionPublish().put(k, true);
				}

				HashMap<CellType, MyCellInterface> hm1 = listGrid.get(((mc.getPosition()-1+8)%8));
				HashMap<CellType, MyCellInterface> hm2 = listGrid.get(((mc.getPosition()+1+8)%8));

				for(CellType ct : hm1.keySet())
				{
					if(!ct.equals(cellType) && hm1.get(ct)!=null)
					{
						mc.getPositionPublish().put(((mc.getPosition()-2+8)%8), false);
						mc.getPositionPublish().put(((mc.getPosition()-3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						mc.getPositionPublish().put(((mc.getPosition()+2+8)%8), false);
						mc.getPositionPublish().put(((mc.getPosition()+3+8)%8), false);
					}
				}
			}
			else
			{
				for (int k = 0; k <= 16; k++) 
				{
					mc.getPositionGood().put(k, true);
				}

				for (int k = 0; k < 8; k++) 
				{
					mc.getPositionPublish().put(k, true);
				}
				HashMap<CellType, MyCellInterface> hm1 = listGrid.get(((mc.getPosition()-1+8)%8));
				HashMap<CellType, MyCellInterface> hm2 = listGrid.get(((mc.getPosition()+1+8)%8));

				for(CellType ct : hm1.keySet())
				{
					if(!ct.equals(cellType) && hm1.get(ct)!=null)
					{
						MyCellIntegerField mc1 =(MyCellIntegerField) hm1.get(ct);
						mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), false);
						mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						MyCellIntegerField mc2 = (MyCellIntegerField) hm2.get(ct);
						mc2.getPositionPublish().put(((mc2.getPosition()-2+8)%8), false);
						mc2.getPositionPublish().put(((mc2.getPosition()-3+8)%8), false);
					}
				}
			}
		}

	}

	private void updateExternalOutFromAdiacentCell(MyCellInterface mc) {

		int position = mc.getPosition();

		if(position == MyCellInterface.NORTH_WEST)
		{
			for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>) ((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_WEST).get(cellType)).getMyRMap().getNORTH_WEST_OUT().values())
			{			    	
				RemotePositionedAgent<Int2D> rm=e.r;
				Int2D loc=e.l;
				rm.setPos(loc);
				this.remove(rm);
				sm.schedule.scheduleOnce(rm);
				setObjectLocation(rm,loc);
			}
		}
		else
			if(position == MyCellInterface.NORTH)
			{
				for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_WEST).get(cellType)).getMyRMap().getNORTH_EAST_OUT().values())
				{			    	
					RemotePositionedAgent<Int2D> rm=e.r;
					Int2D loc=e.l;
					rm.setPos(loc);
					this.remove(rm);
					sm.schedule.scheduleOnce(rm);
					setObjectLocation(rm,loc);
				}
				for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH).get(cellType)).getMyRMap().getNORTH_OUT().values())
				{			    	
					RemotePositionedAgent<Int2D> rm=e.r;
					Int2D loc=e.l;
					rm.setPos(loc);
					this.remove(rm);
					sm.schedule.scheduleOnce(rm);
					setObjectLocation(rm,loc);
				}
				for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_EAST).get(cellType)).getMyRMap().getNORTH_WEST_OUT().values())
				{			    	
					RemotePositionedAgent<Int2D> rm=e.r;
					Int2D loc=e.l;
					rm.setPos(loc);
					this.remove(rm);
					sm.schedule.scheduleOnce(rm);
					setObjectLocation(rm,loc);
				}
			}
			else
				if(position == MyCellInterface.NORTH_EAST)
				{
					for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_EAST).get(cellType)).getMyRMap().getNORTH_EAST_OUT().values())
					{			    	
						RemotePositionedAgent<Int2D> rm=e.r;
						Int2D loc=e.l;
						rm.setPos(loc);
						this.remove(rm);
						sm.schedule.scheduleOnce(rm);
						setObjectLocation(rm,loc);
					}
				}
				else
					if(position == MyCellInterface.EAST)
					{
						for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_EAST).get(cellType)).getMyRMap().getSOUTH_EAST_OUT().values())
						{			    	
							RemotePositionedAgent<Int2D> rm=e.r;
							Int2D loc=e.l;
							rm.setPos(loc);
							this.remove(rm);
							sm.schedule.scheduleOnce(rm);
							setObjectLocation(rm,loc);
						}
						for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.EAST).get(cellType)).getMyRMap().getEAST_OUT().values())
						{			    	
							RemotePositionedAgent<Int2D> rm=e.r;
							Int2D loc=e.l;
							rm.setPos(loc);
							this.remove(rm);
							sm.schedule.scheduleOnce(rm);
							setObjectLocation(rm,loc);
						}
						for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_EAST).get(cellType)).getMyRMap().getNORTH_EAST_OUT().values())
						{			    	
							RemotePositionedAgent<Int2D> rm=e.r;
							Int2D loc=e.l;
							rm.setPos(loc);
							this.remove(rm);
							sm.schedule.scheduleOnce(rm);
							setObjectLocation(rm,loc);
						}
					}
					else
						if(position == MyCellInterface.SOUTH_EAST)
						{
							for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_EAST).get(cellType)).getMyRMap().getSOUTH_EAST_OUT().values())
							{			    	
								RemotePositionedAgent<Int2D> rm=e.r;
								Int2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);
							}
						}
						else
							if(position == MyCellInterface.SOUTH)
							{
								for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_EAST).get(cellType)).getMyRMap().getSOUTH_WEST_OUT().values())
								{			    	
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
								for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH).get(cellType)).getMyRMap().getSOUTH_OUT().values())
								{			    	
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
								for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_WEST).get(cellType)).getMyRMap().getSOUTH_EAST_OUT().values())
								{			    	
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
							}
							else
								if(position == MyCellInterface.SOUTH_WEST)
								{
									for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_WEST).get(cellType)).getMyRMap().getSOUTH_WEST_OUT().values())
									{			    	
										RemotePositionedAgent<Int2D> rm=e.r;
										Int2D loc=e.l;
										rm.setPos(loc);
										this.remove(rm);
										sm.schedule.scheduleOnce(rm);
										setObjectLocation(rm,loc);
									}
								}
								else
									if(position == MyCellInterface.WEST)
									{
										for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.SOUTH_WEST).get(cellType)).getMyRMap().getNORTH_WEST_OUT().values())
										{			    	
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
										for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.WEST).get(cellType)).getMyRMap().getWEST_OUT().values())
										{			    	
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
										for(EntryAgent<Int2D> e: (Collection<EntryAgent<Int2D>>)((MyCellIntegerField)listGrid.get(MyCellInterface.NORTH_WEST).get(cellType)).getMyRMap().getSOUTH_WEST_OUT().values())
										{			    	
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
									}		
	}

	private void clearArrivedOut(MyCellIntegerField mc) {

		int position = mc.getPosition();
		if(position == MyCellInterface.NORTH_WEST)
		{
			mc.getMyRMap().SOUTH_EAST_OUT.clear();
		}
		else
			if(position == MyCellInterface.NORTH)
			{
				mc.getMyRMap().SOUTH_WEST_OUT.clear();
				mc.getMyRMap().corner_out_down_left_diag_down.clear();
				mc.getMyRMap().SOUTH_OUT.clear();
				mc.getMyRMap().corner_out_down_right_diag_down.clear();
				mc.getMyRMap().SOUTH_EAST_OUT.clear();
			}
			else
				if(position == MyCellInterface.NORTH_EAST)
				{
					mc.getMyRMap().SOUTH_WEST_OUT.clear();
				}
				else
					if(position == MyCellInterface.EAST)
					{
						mc.getMyRMap().NORTH_WEST_OUT.clear();
						mc.getMyRMap().corner_out_up_left_diag_left.clear();
						mc.getMyRMap().WEST_OUT.clear();
						mc.getMyRMap().corner_out_down_left_diag_left.clear();
						mc.getMyRMap().SOUTH_WEST_OUT.clear();
					}
					else
						if(position == MyCellInterface.SOUTH_EAST)
						{
							mc.getMyRMap().NORTH_WEST_OUT.clear();
						}
						else
							if(position == MyCellInterface.SOUTH)
							{
								mc.getMyRMap().NORTH_WEST_OUT.clear();
								mc.getMyRMap().corner_out_up_left_diag_up.clear();
								mc.getMyRMap().NORTH_OUT.clear();
								mc.getMyRMap().corner_out_up_right_diag_up.clear();
								mc.getMyRMap().NORTH_EAST_OUT.clear();
							}
							else
								if(position == MyCellInterface.SOUTH_WEST)
								{
									mc.getMyRMap().NORTH_EAST_OUT.clear();
								}
								else
									if(position == MyCellInterface.WEST)
									{
										mc.getMyRMap().NORTH_EAST_OUT.clear();
										mc.getMyRMap().corner_out_up_right_diag_right.clear();
										mc.getMyRMap().EAST_OUT.clear();
										mc.getMyRMap().corner_out_down_right_diag_right.clear();
										mc.getMyRMap().SOUTH_EAST_OUT.clear();
									}
	}

	private void preparePublishUnion() {

		for(Integer pos : toSendForUnion.keySet())
		{	
			if(toSendForUnion.get(pos)!=null)
			{
				MyCellIntegerField md = (MyCellIntegerField)toSendForUnion.get(pos);

				if(md.getPosition() == MyCellInterface.NORTH_WEST)
				{
					/*
	    			if(isSplitted)
	    			{
	    				DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
	    						new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_right_diag_center);    					
	    			}*/

					if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_right_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.EAST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_right);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_right);
						}
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right);    	 

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
						}
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH))
					{

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_down);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_down);
						}

						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_down);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
						}
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_center);
					}
					else{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_down_left_center);
					}
				}

				if(md.getPosition() == MyCellInterface.NORTH)
				{

					if(isSplitted)
					{
						/*
						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_diag_left);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_down_left_diag_center);
						/*
						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_diag_down);

						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().down_mine,
										md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);

						DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_diag_down);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_down_right_diag_center);	
						/*
						DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_diag_right);
						 */
					}
				}

				if(md.getPosition() == MyCellInterface.NORTH_EAST)
				{
					/*
	    			if(isSplitted)
	    			{
	    				DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
	    						new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_left_diag_center);
	    			}*/

					if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_left_center);
					}

					if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_down_right_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_down);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_down);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_down);
						}
					}	
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_down);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);
						}
					}
					if(md.getPositionPublish().get(MyCellInterface.WEST))
					{
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_left);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_left);
						}
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_left);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left);

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
						}
					}
				}

				if(md.getPosition() == MyCellInterface.EAST)
				{

					if(isSplitted)
					{

						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_left_diag_center);
						/*
						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_diag_up);

						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_diag_left);

						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().left_mine,
										md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);

						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_diag_left);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_left_diag_center);
						/*
						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_diag_down);
						 */
					}
				}

				if(md.getPosition() == MyCellInterface.SOUTH_EAST)
				{
					/*
	    			if(isSplitted)
	    			{		
	    				DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
	    						new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_left_diag_center);
	    			}*/

					if(md.getPositionPublish().get(MyCellInterface.NORTH))
					{

						if(isSplitted)
						{	
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_up);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_up);
						}
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_up);
						DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_up);
					}
					else
					{

						if(isSplitted)
						{	

							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
						}

						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_up);
						DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
					}

					if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_up_right_center);
					}

					if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_left_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.WEST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_left);

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_left);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_left);
						}
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
						}
					}
				}

				if(md.getPosition() == MyCellInterface.SOUTH)
				{
					if(isSplitted)
					{
						/*
						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_diag_left);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_up_left_diag_center);
						/*
						DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_diag_up);

						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().up_mine,
										md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);

						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_diag_up);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_up_right_diag_center);
						/*
						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_diag_right);
						 */
					}
				}

				if(md.getPosition() == MyCellInterface.SOUTH_WEST)
				{
					/*
	    			if(isSplitted)
	    			{
	    				DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
	    						new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_right_diag_center);
	    			}*/

					if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_up_left_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.NORTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_up);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_up);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_up);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_up);
						}
					}
					else{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_up);
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
						}
					}
					if(md.getPositionPublish().get(MyCellInterface.EAST))
					{

						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_right);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_right);
						}
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_right);
					}
					else
					{
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
						}
						else
						{
							RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
							empty.clear();
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											empty, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
						}
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_center);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_right_center);
					}
				}

				if(md.getPosition() == MyCellInterface.WEST)
				{
					if(isSplitted)
					{
						/*
						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_up = 
    							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
    									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_diag_up);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_right_diag_center);
						/*
    					DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_right = 
    							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
    									md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_diag_right);

    					DistributedRegion<Integer,Int2D> dr_right = 
    							new DistributedRegion<Integer,Int2D>(md.getMyRMap().right_mine,
    									md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);

    					DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_right = 
    							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
    									md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_diag_right);
						 */
						DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_right_diag_center);
						/*
    					DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_down = 
    							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
    									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_diag_down);
						 */
					}
				}
			}
		}
	}

	private void preparePublishMicroCell() {

		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

			for(CellType ct : hm.keySet())
			{	
				if(!ct.equals(cellType))
				{
					MyCellIntegerField md = (MyCellIntegerField) hm.get(ct);

					if(md.getPosition() == MyCellInterface.NORTH_WEST)
					{
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_down_right_diag_center);    					
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_right);
							}
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right);    	    				
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_down);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_down);
							}

							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_down);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
						}
					}

					if(md.getPosition() == MyCellInterface.NORTH)
					{
						hashUpdatesPosition.get(MyCellInterface.NORTH).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							/*
    						DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_left = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
    										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_diag_left);*/
							DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_down_left_diag_center);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_diag_down);

							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_down);

							DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_diag_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_down_right_diag_center);
							/*
    						DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_right = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
    										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_diag_right);*/
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_diag_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_diag_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
							}
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
						}
					}

					if(md.getPosition() == MyCellInterface.NORTH_EAST)
					{
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_down_left_diag_center);
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_down);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_down);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_down);
							}
						}	
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_down);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_down);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_left);
							}
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_left);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left);

							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
							}
						}
					}

					if(md.getPosition() == MyCellInterface.EAST)
					{
						hashUpdatesPosition.get(MyCellInterface.EAST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{

							DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_left_diag_center);
							/*DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_up = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
    										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_diag_up);*/
							DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_diag_left);

							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left);

							DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_diag_left);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_left_diag_center);
							/*DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_down = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_left,
    										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_diag_down);*/

						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_diag_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
							}
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_left_diag_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_diag_down);
							}
							else{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
							}
						}
					}

					if(md.getPosition() == MyCellInterface.SOUTH_EAST)
					{
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{		
							DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_up_left_diag_center);
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							if(isSplitted || unionDone)
							{	

								DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_up);
							}
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_up);
						}
						else
						{
							if(isSplitted || unionDone)
							{	

								DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
							}

							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_left);

							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_left);
							}
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_left_left);
							}
						}
					}

					if(md.getPosition() == MyCellInterface.SOUTH)
					{
						hashUpdatesPosition.get(MyCellInterface.SOUTH).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							/*DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_left = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_left,
    										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_diag_left);*/
							DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_up_left_diag_center);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_diag_up);

							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_up);

							DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_diag_up);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_up_right_diag_center);
							/*
    						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_right = 
    								new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
    										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_diag_right);*/
						}

						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							if(isSplitted || unionDone){
								DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_diag_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
							}
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_left_diag_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_diag_left);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
							}
						}
					}

					if(md.getPosition() == MyCellInterface.SOUTH_WEST)
					{
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_up_right_diag_center);
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_up);
							}
						}
						else{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_up);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.EAST))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_up_right_right);
							}
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_right);
						}
						else
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_right);
							}
							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_center);
						}
						else
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
						}
					}

					if(md.getPosition() == MyCellInterface.WEST)
					{
						hashUpdatesPosition.get(MyCellInterface.WEST).setNumAgentExternalCell(md.getMyField().size());
						if(isSplitted)
						{
							/*
    						DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_up = 
        							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_up_right,
        									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
        					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_diag_up);*/
							DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_right_diag_center);
							DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_diag_right);

							DistributedRegion<Integer,Int2D> dr_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
											md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right);

							DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_right = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_right_diag_right);
							DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_right_diag_center);
							/*DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_down = 
        							new DistributedRegion<Integer,Int2D>(md.getMyRMap().corner_mine_down_right,
        									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
        					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_diag_down);*/
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_up_left_center);
						}

						if(md.getPositionPublish().get(MyCellInterface.NORTH))
						{
							DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
							DistributedRegion<Integer,Int2D> dr_up = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
											md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_up_right_diag_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_diag_up);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegion<Integer,Int2D> dr_corner_down_right_diag_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_diag_down);
							}
							else
							{
								RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
								empty.clear();
								DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
										new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);	
							}
							DistributedRegion<Integer,Int2D> dr_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
											md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
							DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_center = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_left_center);

						}
						if(md.getPositionPublish().get(MyCellInterface.WEST))
						{
							DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
							DistributedRegion<Integer,Int2D> dr_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
											md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
							DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
						}
					}
				}
			}
		}
	}

	private void preparePublishCenter(){

		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

			MyCellIntegerField md = (MyCellIntegerField)listGrid.get(pos).get(cellType);

			if(md != null)
			{
				//PUBLISH POSITION CORNER UP LEFT
				if(md.getPosition()==MyCellInterface.NORTH_WEST){

					//LEFT MINE UP
					if(md.getPositionPublish().get(MyCellInterface.WEST))
					{
						DistributedRegion<Integer,Int2D> dr_left_corner_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left_corner_down_diag);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_left);
					}
					else
					{
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_left);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_down_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_corner_up_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_left);
					}

					if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST))
					{						
						DistributedRegion<Integer,Int2D> dr_corner_left_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_corner_left_up);
					}

					if(md.getPositionPublish().get(MyCellInterface.NORTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_up,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_left_up);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
						DistributedRegion<Integer,Int2D> dr_corner_right_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_right_up_diag);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_left_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_up,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_left_up);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_up);
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_up);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										empty,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_up_right_up);
					}
				}

				//PUBLISH POSITION UP
				if(md.getPosition()==MyCellInterface.NORTH){

					if(md.getPositionPublish().get(MyCellInterface.NORTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_left_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_left_up_diag);

						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);

						DistributedRegion<Integer,Int2D> dr_corner_right_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_right_up_diag);
					}
					else
					{
						if(positionForUnion != MyCellInterface.NORTH){
							DistributedRegion<Integer,Int2D> dr_corner_left_up_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT,	(sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_left_up_diag);

							DistributedRegion<Integer,Int2D> dr_corner_right_up_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_right_up_diag);
						}
					}
				}

				//PUBLISH POSITION CORNER UP RIGHT
				if(md.getPosition()==MyCellInterface.NORTH_EAST){

					if(md.getPositionPublish().get(MyCellInterface.NORTH))
					{
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left_corner_up_diag);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);
						DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_up);
					}
					else
					{
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_up);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left_corner_up_diag_up);
						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_up);
						DistributedRegion<Integer,Int2D> dr_corner_up_right_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_up);
					}

					if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST))
					{	
						DistributedRegion<Integer,Int2D> dr_corner_up_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_corner_up_right);
					}

					if(md.getPositionPublish().get(MyCellInterface.EAST)){
						DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_up_right_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_right_down_diag);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_up_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_corner_up_right_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right);
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_right);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_right_corner_down_diag_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right_corner_down_diag_right);
					}
				}

				//PUBLISH POSITION RIGHT
				if(md.getPosition()==MyCellInterface.EAST){

					if(md.getPositionPublish().get(MyCellInterface.EAST)){
						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right_corner_up_diag);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_right_down_diag);
					}
					else
					{
						if(positionForUnion != MyCellInterface.EAST){
							DistributedRegion<Integer,Int2D> dr_right_corner_up_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
											md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right_corner_up_diag);

							DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_right_down_diag);
						}
					}
				}

				//PUBLISH POSITION CORNER DOWN RIGHT
				if(md.getPosition()==MyCellInterface.SOUTH_EAST){

					if(md.getPositionPublish().get(MyCellInterface.EAST)){
						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right_corner_up_diag);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
					}
					else
					{
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_right_diag_right);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_right_corner_up_diag_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_right);
					}

					if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST))
					{	
						DistributedRegion<Integer,Int2D> dr_corner_down_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_corner_down_right);
					}

					if(md.getPositionPublish().get(MyCellInterface.SOUTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_right_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_left_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_left_down_diag);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_right_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_down);
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_left_diag_down);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_left_corner_down_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_left_corner_down_diag_down);
					}
				}

				//PUBLISH POSITION DOWN
				if(md.getPosition()==MyCellInterface.SOUTH){

					if(md.getPositionPublish().get(MyCellInterface.SOUTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_right_down_diag);

						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);

						DistributedRegion<Integer,Int2D> dr_corner_left_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_left_down_diag);
					}
					else
					{
						if(positionForUnion != MyCellInterface.SOUTH){
							DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
											md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_right_down_diag);		    			
							DistributedRegion<Integer,Int2D> dr_corner_left_down_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_left_down_diag);
						}
					}
				}

				//PUBLISH POSITION CORNER DOWN LEFT
				if(md.getPosition()==MyCellInterface.SOUTH_WEST){

					if(md.getPositionPublish().get(MyCellInterface.SOUTH))
					{
						DistributedRegion<Integer,Int2D> dr_corner_right_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_right_down_diag);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
					}
					else
					{
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_down_right_diag_down);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_right_corner_down_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_right_corner_down_diag_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_down);
					}

					if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST))
					{	
						DistributedRegion<Integer,Int2D> dr_corner_down_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_corner_down_left);
					}

					if(md.getPositionPublish().get(MyCellInterface.WEST)){

						DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_down_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left_corner_up_diag);
					}
					else
					{
						DistributedRegion<Integer,Int2D> dr_corner_down_left_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_left);
						RegionInteger empty = ((RegionInteger)md.getMyRMap().corner_out_up_left_diag_left);
						empty.clear();
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										empty, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_left_corner_up_diag_left);
					}
				}

				//PUBLISH POSITION LEFT
				if(md.getPosition()==MyCellInterface.WEST){

					if(md.getPositionPublish().get(MyCellInterface.WEST)){
						DistributedRegion<Integer,Int2D> dr_corner_left_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_left_down_diag);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left_corner_up_diag);
					}
					else
					{
						if(positionForUnion != MyCellInterface.WEST){
							DistributedRegion<Integer,Int2D> dr_corner_left_down_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
											md.getMyRMap().SOUTH_WEST_OUT,	(sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_left_down_diag);
							DistributedRegion<Integer,Int2D> dr_left_corner_up_diag = 
									new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
											md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left_corner_up_diag);
						}
					}
				}

				if(md.getPosition() == MyCellInterface.CENTER){

					if(md.getPositionPublish().get(MyCellInterface.NORTH_WEST)){
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().NORTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_WEST).add(dr_left_corner_up_diag_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.NORTH)){
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_left_corner_up_diag_up);

						DistributedRegion<Integer,Int2D> dr_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_MINE,
										md.getMyRMap().NORTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_up);

						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag_up = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH).add(dr_right_corner_up_diag_up);

					}
					if(md.getPositionPublish().get(MyCellInterface.NORTH_EAST)){
						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().NORTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.NORTH_EAST).add(dr_right_corner_up_diag_center);
					}

					if(md.getPositionPublish().get(MyCellInterface.EAST)){
						DistributedRegion<Integer,Int2D> dr_right_corner_up_diag_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_EAST_MINE,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right_corner_up_diag_right);
						DistributedRegion<Integer,Int2D> dr_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().EAST_MINE,
										md.getMyRMap().EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_right);
						DistributedRegion<Integer,Int2D> dr_corner_down_right_right = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.EAST).add(dr_corner_down_right_right);
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH_EAST)){
						DistributedRegion<Integer,Int2D> dr_right_corner_down_diag = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().SOUTH_EAST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_EAST).add(dr_right_corner_down_diag);
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH)){
						DistributedRegion<Integer,Int2D> dr_corner_right_down_diag_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_EAST_MINE,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_right_down_diag_down);
						DistributedRegion<Integer,Int2D> dr_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_MINE,
										md.getMyRMap().SOUTH_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_down);
						DistributedRegion<Integer,Int2D> dr_corner_down_left_down = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH).add(dr_corner_down_left_down);
					}
					if(md.getPositionPublish().get(MyCellInterface.SOUTH_WEST)){
						DistributedRegion<Integer,Int2D> dr_left_corner_down_diag_center = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().SOUTH_WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.SOUTH_WEST).add(dr_left_corner_down_diag_center);
					}
					if(md.getPositionPublish().get(MyCellInterface.WEST)){
						DistributedRegion<Integer,Int2D> dr_corner_left_down_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().SOUTH_WEST_MINE,
										md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_corner_left_down_diag_left);
						DistributedRegion<Integer,Int2D> dr_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().WEST_MINE,
										md.getMyRMap().WEST_OUT, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left);
						DistributedRegion<Integer,Int2D> dr_left_corner_up_diag_left = 
								new DistributedRegion<Integer,Int2D>(md.getMyRMap().NORTH_WEST_MINE,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.WEST).add(dr_left_corner_up_diag_left);
					}
				}
			}
		}
	}


	private void updateInternalMine(MyCellIntegerField md) {

		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);
				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

					if(name.contains("mine"))
					{
						for(EntryAgent<Int2D> e: region.values())
						{

							if(name.contains("left_mine") && !md.getPositionGood().get(MyCellInterface.WEST))
							{	

								RemotePositionedAgent<Int2D> rm=e.r;
								Int2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);
							}
							else
								if(name.contains("right_mine") && !md.getPositionGood().get(MyCellInterface.EAST))
								{
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
								else
									if(name.contains("up_mine") && !md.getPositionGood().get(MyCellInterface.NORTH))
									{
										RemotePositionedAgent<Int2D> rm=e.r;
										Int2D loc=e.l;
										rm.setPos(loc);
										this.remove(rm);
										sm.schedule.scheduleOnce(rm);
										setObjectLocation(rm,loc);
									}
									else
										if(name.contains("down_mine") && !md.getPositionGood().get(MyCellInterface.SOUTH))
										{
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
										else
											if(name.contains("corner_mine_down_left") && !md.getPositionGood().get(MyCellInterface.SOUTH_WEST))
											{
												RemotePositionedAgent<Int2D> rm=e.r;
												Int2D loc=e.l;
												rm.setPos(loc);
												this.remove(rm);
												sm.schedule.scheduleOnce(rm);
												setObjectLocation(rm,loc);
											}
											else
												if(name.contains("corner_mine_down_right") && !md.getPositionGood().get(MyCellInterface.SOUTH_EAST))
												{
													RemotePositionedAgent<Int2D> rm=e.r;
													Int2D loc=e.l;

													rm.setPos(loc);
													this.remove(rm);
													sm.schedule.scheduleOnce(rm);
													setObjectLocation(rm,loc);
												}
												else
													if(name.contains("corner_mine_up_left") && !md.getPositionGood().get(MyCellInterface.NORTH_WEST))
													{
														RemotePositionedAgent<Int2D> rm=e.r;
														Int2D loc=e.l;
														rm.setPos(loc);
														this.remove(rm);
														sm.schedule.scheduleOnce(rm);
														setObjectLocation(rm,loc);
													}
													else
														if(name.contains("corner_mine_up_right") && !md.getPositionGood().get(MyCellInterface.NORTH_EAST))
														{
															RemotePositionedAgent<Int2D> rm=e.r;
															Int2D loc=e.l;
															rm.setPos(loc);
															this.remove(rm);
															sm.schedule.scheduleOnce(rm);
															setObjectLocation(rm,loc);
														}
						}
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}
	}

	private void updateInternalOut(MyCellInterface md) {

		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);
				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

					if(name.contains("out"))
					{
						for(EntryAgent<Int2D> e: region.values())
						{

							if(name.contains("left_out") && !md.getPositionGood().get(MyCellInterface.WEST))
							{	

								RemotePositionedAgent<Int2D> rm=e.r;
								Int2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);
							}
							else
								if(name.contains("right_out") && !md.getPositionGood().get(MyCellInterface.EAST))
								{
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
								else
									if(name.contains("up_out") && !md.getPositionGood().get(MyCellInterface.NORTH))
									{
										RemotePositionedAgent<Int2D> rm=e.r;
										Int2D loc=e.l;
										rm.setPos(loc);
										this.remove(rm);
										sm.schedule.scheduleOnce(rm);
										setObjectLocation(rm,loc);
									}
									else
										if(name.contains("down_out") && !md.getPositionGood().get(MyCellInterface.SOUTH))
										{
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
										else
											if(name.contains("corner_out_down_left_diag_center") && !md.getPositionGood().get(MyCellInterface.SOUTH_WEST))
											{
												RemotePositionedAgent<Int2D> rm=e.r;
												Int2D loc=e.l;
												rm.setPos(loc);
												this.remove(rm);
												sm.schedule.scheduleOnce(rm);
												setObjectLocation(rm,loc);
											}
											else
												if(name.contains("corner_out_down_left_diag_down") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
												{
													RemotePositionedAgent<Int2D> rm=e.r;
													Int2D loc=e.l;
													rm.setPos(loc);
													this.remove(rm);
													sm.schedule.scheduleOnce(rm);
													setObjectLocation(rm,loc);
												}
												else
													if(name.contains("corner_out_down_left_diag_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
													{
														RemotePositionedAgent<Int2D> rm=e.r;
														Int2D loc=e.l;
														rm.setPos(loc);
														this.remove(rm);
														sm.schedule.scheduleOnce(rm);
														setObjectLocation(rm,loc);
													}
													else
														if(name.contains("corner_out_down_right_diag_center") && !md.getPositionGood().get(MyCellInterface.SOUTH_EAST))
														{
															RemotePositionedAgent<Int2D> rm=e.r;
															Int2D loc=e.l;

															rm.setPos(loc);
															this.remove(rm);
															sm.schedule.scheduleOnce(rm);
															setObjectLocation(rm,loc);
														}
														else 
															if(name.contains("corner_out_down_right_diag_down") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
															{
																RemotePositionedAgent<Int2D> rm=e.r;
																Int2D loc=e.l;

																rm.setPos(loc);
																this.remove(rm);
																sm.schedule.scheduleOnce(rm);
																setObjectLocation(rm,loc);
															}
															else
																if(name.contains("corner_out_down_right_diag_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
																{
																	RemotePositionedAgent<Int2D> rm=e.r;
																	Int2D loc=e.l;

																	rm.setPos(loc);
																	this.remove(rm);
																	sm.schedule.scheduleOnce(rm);
																	setObjectLocation(rm,loc);
																}
																else
																	if(name.contains("corner_out_up_left_diag_center") && !md.getPositionGood().get(MyCellInterface.NORTH_WEST))
																	{
																		RemotePositionedAgent<Int2D> rm=e.r;
																		Int2D loc=e.l;
																		rm.setPos(loc);
																		this.remove(rm);
																		sm.schedule.scheduleOnce(rm);
																		setObjectLocation(rm,loc);
																	}
																	else
																		if(name.contains("corner_out_up_left_diag_up") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
																		{
																			RemotePositionedAgent<Int2D> rm=e.r;
																			Int2D loc=e.l;
																			rm.setPos(loc);
																			this.remove(rm);
																			sm.schedule.scheduleOnce(rm);
																			setObjectLocation(rm,loc);
																		}
																		else
																			if(name.contains("corner_out_up_left_diag_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
																			{
																				RemotePositionedAgent<Int2D> rm=e.r;
																				Int2D loc=e.l;
																				rm.setPos(loc);
																				this.remove(rm);
																				sm.schedule.scheduleOnce(rm);
																				setObjectLocation(rm,loc);
																			}
																			else
																				if(name.contains("corner_out_up_right_diag_center") && !md.getPositionGood().get(MyCellInterface.NORTH_EAST))
																				{
																					RemotePositionedAgent<Int2D> rm=e.r;
																					Int2D loc=e.l;
																					rm.setPos(loc);
																					this.remove(rm);
																					sm.schedule.scheduleOnce(rm);
																					setObjectLocation(rm,loc);
																				}
																				else
																					if(name.contains("corner_out_up_right_diag_up") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
																					{
																						RemotePositionedAgent<Int2D> rm=e.r;
																						Int2D loc=e.l;
																						rm.setPos(loc);
																						this.remove(rm);
																						sm.schedule.scheduleOnce(rm);
																						setObjectLocation(rm,loc);
																					}
																					else
																						if(name.contains("corner_out_up_right_diag_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
																						{
																							RemotePositionedAgent<Int2D> rm=e.r;
																							Int2D loc=e.l;
																							rm.setPos(loc);
																							this.remove(rm);
																							sm.schedule.scheduleOnce(rm);
																							setObjectLocation(rm,loc);
																						}
						}
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}
	}

	/**
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cacheLB:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	private void verifyUpdates(UpdatePositionIntegerField<DistributedRegion<Integer,Int2D>> super_box)
	{
		ArrayList<Region<Integer, Int2D>> updates_out = new ArrayList<Region<Integer,Int2D>>();

		for(DistributedRegion<Integer, Int2D> sb : super_box){

			Region<Integer,Int2D> r_mine=sb.out;
			Region<Integer,Int2D> r_out=sb.mine;

			for(EntryAgent<Int2D> e_m: r_mine.values())
			{
				RemotePositionedAgent<Int2D> rm=e_m.r;
				rm.setPos(e_m.l);
				sm.schedule.scheduleOnce(rm);

				((DistributedState<Int2D>)sm).addToField(rm,e_m.l);
			}

			updates_out.add(r_out);
		}

		updates_cacheLB.add(updates_out);
	}

	private void memorizeRegionOut(MyCellIntegerField md)
	{
		ArrayList<Region<Integer, Int2D>> updates_out = new ArrayList<Region<Integer,Int2D>>();

		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);
				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

					if(name.contains("out"))
					{		
						if(name.contains("left_out") && md.getPositionGood().get(MyCellInterface.WEST))
						{
							updates_out.add(region.clone());
						}
						else
							if(name.contains("right_out") && md.getPositionGood().get(MyCellInterface.EAST))
							{
								updates_out.add(region.clone());
							}
							else
								if(name.contains("up_out") && md.getPositionGood().get(MyCellInterface.NORTH))
								{
									updates_out.add(region.clone());
								}
								else
									if(name.contains("down_out") && md.getPositionGood().get(MyCellInterface.SOUTH))
									{
										updates_out.add(region.clone());
									}
									else
										if(name.contains("corner_out_down_left_diag_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
										{							
											updates_out.add(region.clone());
										}
										else
											if(name.contains("corner_out_down_left_diag_down") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
											{
												updates_out.add(region.clone());
											}
											else
												if(name.contains("corner_out_down_left_diag_center") && md.getPositionGood().get(MyCellInterface.SOUTH_WEST))
												{
													updates_out.add(region.clone());
												}
												else
													if(name.contains("corner_out_down_right_diag_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
													{
														updates_out.add(region.clone());
													}
													else
														if(name.contains("corner_out_down_right_diag_down") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
														{
															updates_out.add(region.clone());
														}
														else
															if(name.contains("corner_out_down_right_diag_center") && md.getPositionGood().get(MyCellInterface.SOUTH_EAST))
															{
																updates_out.add(region.clone());
															}
															else
																if(name.contains("corner_out_up_left_diag_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
																{
																	updates_out.add(region.clone());
																}
																else
																	if(name.contains("corner_out_up_left_diag_up") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
																	{
																		updates_out.add(region.clone());
																	}
																	else
																		if(name.contains("corner_out_up_left_diag_center") && md.getPositionGood().get(MyCellInterface.NORTH_WEST))
																		{
																			updates_out.add(region.clone());
																		}
																		else
																			if(name.contains("corner_out_up_right_diag_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
																			{
																				updates_out.add(region.clone());
																			}
																			else
																				if(name.contains("corner_out_up_right_diag_up") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
																				{
																					updates_out.add(region.clone());
																				}
																				else
																					if(name.contains("corner_out_up_right_diag_center") && md.getPositionGood().get(MyCellInterface.NORTH_EAST))
																					{
																						updates_out.add(region.clone());
																					}
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}
		updates_cacheLB.add(updates_out);
	}

	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cacheLB (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	private void removeOut(MyCellInterface md)
	{
		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);
				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
					if(name.contains("out"))
					{
						for(EntryAgent<Int2D> e: region.values())
						{
							RemotePositionedAgent<Int2D> rm=e.r;
							rm.setPos(e.l);
							this.remove(rm);
						} 
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}

	}

	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cacheLB (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	private void updateFields(MyCellInterface md)
	{
		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);
				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
					/*		   			if(name.contains("out"))
		   			{
		   				for(Entry<Int2D> e: region)
		   			 	{
		   					RemoteAgent<Int2D> rm=e.r;
		   					rm.setPos(e.l);
		   					this.remove(rm);
		   			 	} 
		   			}
		   			else*/
					if(name.contains("mine"))
					{
						for(EntryAgent<Int2D> e: region.values())
						{			    	
							if(name.contains("left_mine") && md.getPositionGood().get(MyCellInterface.WEST))
							{	
								RemotePositionedAgent<Int2D> rm=e.r;
								Int2D loc=e.l;
								rm.setPos(loc);
								this.remove(rm);
								sm.schedule.scheduleOnce(rm);
								setObjectLocation(rm,loc);
							}
							else
								if(name.contains("right_mine") && md.getPositionGood().get(MyCellInterface.EAST))
								{
									RemotePositionedAgent<Int2D> rm=e.r;
									Int2D loc=e.l;
									rm.setPos(loc);
									this.remove(rm);
									sm.schedule.scheduleOnce(rm);
									setObjectLocation(rm,loc);
								}
								else
									if(name.contains("up_mine") && md.getPositionGood().get(MyCellInterface.NORTH))
									{
										RemotePositionedAgent<Int2D> rm=e.r;
										Int2D loc=e.l;
										rm.setPos(loc);
										this.remove(rm);
										sm.schedule.scheduleOnce(rm);
										setObjectLocation(rm,loc);
									}
									else
										if(name.contains("down_mine") && md.getPositionGood().get(MyCellInterface.SOUTH))
										{
											RemotePositionedAgent<Int2D> rm=e.r;
											Int2D loc=e.l;
											rm.setPos(loc);
											this.remove(rm);
											sm.schedule.scheduleOnce(rm);
											setObjectLocation(rm,loc);
										}
										else
											if(name.contains("corner_mine_down_left") && md.getPositionGood().get(MyCellInterface.SOUTH_WEST))
											{
												RemotePositionedAgent<Int2D> rm=e.r;
												Int2D loc=e.l;
												rm.setPos(loc);
												this.remove(rm);
												sm.schedule.scheduleOnce(rm);
												setObjectLocation(rm,loc);
											}
											else
												if(name.contains("corner_mine_down_right") && md.getPositionGood().get(MyCellInterface.SOUTH_EAST))
												{
													RemotePositionedAgent<Int2D> rm=e.r;									
													Int2D loc=e.l;
													rm.setPos(loc);
													this.remove(rm);
													sm.schedule.scheduleOnce(rm);
													setObjectLocation(rm,loc);
												}
												else
													if(name.contains("corner_mine_up_left") && md.getPositionGood().get(MyCellInterface.NORTH_WEST))
													{
														RemotePositionedAgent<Int2D> rm=e.r;
														Int2D loc=e.l;
														rm.setPos(loc);
														this.remove(rm);
														sm.schedule.scheduleOnce(rm);
														setObjectLocation(rm,loc);
													}
													else
														if(name.contains("corner_mine_up_right") && md.getPositionGood().get(MyCellInterface.NORTH_EAST))
														{
															RemotePositionedAgent<Int2D> rm=e.r;
															Int2D loc=e.l;
															rm.setPos(loc);
															this.remove(rm);
															sm.schedule.scheduleOnce(rm);
															setObjectLocation(rm,loc);
														}
						}
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}

	}

	/**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 **/
	private boolean setAgents(RemotePositionedAgent<Int2D> rm,Int2D location, MyCellInterface mc)
	{
		MyCellIntegerField md = (MyCellIntegerField) mc;

		Class o=md.getMyRMap().getClass();

		Field[] fields = o.getDeclaredFields();

		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);

			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(md.getMyRMap(), null);

				if(returnValue!=null)
				{
					Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);

					if(name.contains("out"))
					{
						if(name.contains("left_out") && md.getPositionGood().get(MyCellInterface.WEST))
						{
							if(region.isMine(location.x,location.y))
							{   	 
								outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
								return  region.addAgents(new EntryAgent<Int2D>(rm, location));
							}
						}
						else
							if(name.contains("right_out") && md.getPositionGood().get(MyCellInterface.EAST))
							{
								if(region.isMine(location.x,location.y))
								{
									outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
									return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
								}
							}
							else
								if(name.contains("up_out") && md.getPositionGood().get(MyCellInterface.NORTH))
								{
									if(region.isMine(location.x,location.y))
									{   
										outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
										return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
									}
								}
								else
									if(name.contains("down_out") && md.getPositionGood().get(MyCellInterface.SOUTH))
									{
										if(region.isMine(location.x,location.y))
										{
											outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
											return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
										}
									}
									else
										if(name.contains("corner_out_down_left_diag_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
										{		
											if(region.isMine(location.x,location.y))
											{   
												outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
												return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
											}
										}
										else
											if(name.contains("corner_out_down_left_diag_down") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
											{
												if(region.isMine(location.x,location.y))
												{   
													outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
													return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
												}
											}
											else
												if(name.contains("corner_out_down_left_diag_center") && md.getPositionGood().get(MyCellInterface.SOUTH_WEST))
												{
													if(region.isMine(location.x,location.y))
													{   
														outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
														return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
													}
												}
												else
													if(name.contains("corner_out_down_right_diag_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
													{
														if(region.isMine(location.x,location.y))
														{   
															outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
															return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
														}
													}
													else
														if(name.contains("corner_out_down_right_diag_down") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
														{
															if(region.isMine(location.x,location.y))
															{   
																outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
															}
														}
														else
															if(name.contains("corner_out_down_right_diag_center") && md.getPositionGood().get(MyCellInterface.SOUTH_EAST))
															{
																if(region.isMine(location.x,location.y))
																{   
																	outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																	return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																}
															}
															else
																if(name.contains("corner_out_up_left_diag_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
																{
																	if(region.isMine(location.x,location.y))
																	{   
																		outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																		return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																	}
																}
																else
																	if(name.contains("corner_out_up_left_diag_up") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
																	{
																		if(region.isMine(location.x,location.y))
																		{   
																			outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																			return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																		}
																	}
																	else
																		if(name.contains("corner_out_up_left_diag_center") && md.getPositionGood().get(MyCellInterface.NORTH_WEST))
																		{
																			if(region.isMine(location.x,location.y))
																			{  
																				outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																				return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																			}
																		}
																		else
																			if(name.contains("corner_out_up_right_diag_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
																			{
																				if(region.isMine(location.x,location.y))
																				{   
																					outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																					return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																				}
																			}
																			else
																				if(name.contains("corner_out_up_right_diag_up") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
																				{
																					if(region.isMine(location.x,location.y))
																					{   
																						outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																						return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																					}
																				}
																				else
																					if(name.contains("corner_out_up_right_diag_center") && md.getPositionGood().get(MyCellInterface.NORTH_EAST))
																					{
																						if(region.isMine(location.x,location.y))
																						{   
																							outAgents.put(rm.getId(),new EntryAgent<Int2D>(rm, location));
																							return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
																						}
																					}
					}
					else
						if(name.contains("mine"))
						{		
							if(region.isMine(location.x,location.y))
							{   	 
								md.getMyField().addAgents(new EntryAgent<Int2D>(rm, location));
								return  region.addAgents(new EntryAgent<Int2D>(rm, location));	
							}
						}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}

		}

		return false;
	}

	/**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 */
	//	private boolean setAgents(RemotePositionedAgent<Int2D> rm,Int2D location, MyCellInterface md)
	//	{
	//		RegionMap<Integer, Int2D> rmap =(RegionMap<Integer, Int2D>) md.getMyRMap();
	//
	//		MyCellIntegerField ms = (MyCellIntegerField) md;
	//		
	//		if(md.getPositionGood().get(MyCellInterface.LEFT))
	//		{
	//			Region<Integer,Int2D> region = rmap.left_out;
	//			if(region.isMine(location.x,location.y))
	//			{   	 
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.RIGHT))
	//		{
	//			Region<Integer,Int2D> region = rmap.right_out;
	//			if(region.isMine(location.x,location.y))
	//			{   	 
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.UP))
	//		{
	//			Region<Integer,Int2D> region = rmap.up_out;
	//			if(region.isMine(location.x,location.y))
	//			{   	 
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.DOWN))
	//		{
	//			Region<Integer,Int2D> region = rmap.down_out;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
	//		{		
	//			Region<Integer,Int2D> region = rmap.corner_out_down_left_diag_left;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_down_left_diag_down;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_down_left_diag_center;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_down_right_diag_right;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_down_right_diag_down;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_down_right_diag_center;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_left_diag_left;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_left_diag_up;
	//			if(region.isMine(location.x,location.y))
	//			{   	
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_left_diag_center;
	//			if(region.isMine(location.x,location.y))
	//			{   
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_right_diag_right;
	//			if(region.isMine(location.x,location.y))
	//			{   	
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_right_diag_up;
	//			if(region.isMine(location.x,location.y))
	//			{   	
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
	//		{
	//			Region<Integer,Int2D> region = rmap.corner_out_up_right_diag_center;
	//			if(region.isMine(location.x,location.y))
	//			{   	
	//				outAgents.add(new Entry<Int2D>(rm, location));
	//				return  region.addAgents(new Entry<Int2D>(rm, location));	
	//			}
	//		}
	//
	//
	//		if(rmap.corner_mine_up_left.isMine(location.x,location.y))
	//		{   	 
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.corner_mine_up_left.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.up_mine.isMine(location.x,location.y))
	//		{   	 
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.up_mine.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.corner_mine_up_right.isMine(location.x,location.y))
	//		{   	
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.corner_mine_up_right.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.right_mine.isMine(location.x,location.y))
	//		{   	 
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.right_mine.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.corner_mine_down_right.isMine(location.x,location.y))
	//		{   	
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.corner_mine_down_right.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.down_mine.isMine(location.x,location.y))
	//		{   	
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.down_mine.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.corner_mine_down_left.isMine(location.x,location.y))
	//		{   	
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.corner_mine_down_left.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		if(rmap.left_mine.isMine(location.x,location.y))
	//		{   	
	//			ms.getMyField().addAgents(new Entry<Int2D>(rm, location));
	//			return  rmap.left_mine.addAgents(new Entry<Int2D>(rm, location));	
	//		}
	//
	//		return false;
	//	}


	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	private boolean reset()
	{
		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);

			for(CellType ct : hm.keySet())
			{	
				MyCellIntegerField md = (MyCellIntegerField) hm.get(ct);


				md.getMyField().clear();	

				Class o=md.getMyRMap().getClass();

				Field[] fields = o.getDeclaredFields();
				for (int z = 0; z < fields.length; z++)
				{
					fields[z].setAccessible(true);
					try
					{
						String name=fields[z].getName();
						Method method = o.getMethod("get"+name, null);
						Object returnValue = method.invoke(md.getMyRMap(), null);
						if(returnValue!=null)
						{
							Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
							region.clear();    
						}
					}
					catch (IllegalArgumentException e){e.printStackTrace(); return false;} 
					catch (IllegalAccessException e) {e.printStackTrace();return false;} 
					catch (SecurityException e) {e.printStackTrace();return false;} 
					catch (NoSuchMethodException e) {e.printStackTrace();return false;} 
					catch (InvocationTargetException e) {e.printStackTrace();return false;}
				}
			}
		}
		return true;
	}

	/**
	 * Implemented method from the abstract class.
	 */
	@Override
	public DistributedState getState() { return (DistributedState)sm; }

	//getters and setters
	public int getOwn_x() { return own_x; }
	public void setOwn_x(int own_x) { this.own_x = own_x; }
	public int getOwn_y() {	return own_y; }
	public void setOwn_y(int own_y) { this.own_y = own_y; }

	@Override
	public void setTable(HashMap table) {
		ConnectionJMS conn = (ConnectionJMS) ((DistributedState<?>)sm).getCommunicationManagementConnection();
		if(conn!=null)
			conn.setTable(table);
	}


	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	@Override
	public HashMap<Integer, MyCellInterface> getToSendForBalance() {
		// TODO Auto-generated method stub
		return toSendForBalance;
	}

	@Override
	public HashMap<Integer, MyCellInterface> getToSendForUnion() {
		// TODO Auto-generated method stub
		return toSendForUnion;
	}

	@Override
	public void setIsSplitted(boolean isSplitted) {
		// TODO Auto-generated method stub
		if(isSplitted){
			isUnited = false;
		}
		this.isSplitted = isSplitted;
		this.splitDone = isSplitted;
	}

	@Override
	public void prepareForBalance(boolean prepareForBalance){

		this.prepareForBalance = prepareForBalance;
	}

	@Override
	public void prepareForUnion(boolean prepareForUnion){

		this.prepareForUnion = prepareForUnion;
	}

	@Override
	public boolean isSplitted() {
		// TODO Auto-generated method stub
		return isSplitted;
	}

	@Override
	public boolean isUnited() {
		// TODO Auto-generated method stub
		return isUnited;
	}

	@Override
	public boolean isPrepareForBalance() {
		// TODO Auto-generated method stub
		return prepareForBalance;
	}

	@Override
	public int getNumAgents() {
		// TODO Auto-generated method stub
		return numAgents;
	}



	@Override
	public int getLeftMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getRightMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		// TODO Auto-generated method stub
		return globals;
	}
	@Override
	public boolean verifyPosition(Int2D pos) {

		//we have to implement this
		return false;

	}


	@Override
	public String getDistributedFieldID() {
		// TODO Auto-generated method stub
		return NAME;
	}


	@Override
	public boolean createRegions(QuadTree... cell) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public long getCommunicationTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}