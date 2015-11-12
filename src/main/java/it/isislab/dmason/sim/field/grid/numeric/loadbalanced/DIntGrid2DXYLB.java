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

package it.isislab.dmason.sim.field.grid.numeric.loadbalanced;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumericLB;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.loadbalanced.UpdatePositionIntegerNumeric;
import it.isislab.dmason.sim.field.support.field2D.loadbalanced.UpdatePositionInterface;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMapNumeric;
import it.isislab.dmason.sim.field.support.field2D.region.RegionNumeric;
import it.isislab.dmason.sim.field.support.loadbalancing.LoadBalancingIntegerNumeric;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellIntegerNumeric;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.util.RemoteParam;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.engine.SimState;
import sim.util.Int2D;


/**
 *  <h3>This Field extends IntGrid2D, to be used in a distributed environment. All the necessary informations for 
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
 * ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   CORNER DIAG           |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______UP_OUT__________|__|__|/________________________|
 * |________________________|__|__|______UP_MINE_________|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        L  L  |                    / |  R  R                         |
 * |                        E  E  |                   /  |  I  I                         |
 * |         10             F  F  |         11   CORNER  |  G  G         12              |
 * |                        T  T  |               MINE   |  H  H                         |
 * |                        |  |  |                      |  T  T                         |
 * |                        O  M  |       MYFIELD        |  |  |                         |
 * |                        U  I  |                      |  M  O                         |
 * |                        T  N  |                      |  I  U                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___DOWN_MINE__________|__|__|_________________________|
 * |________________________|__|__|___DOWN_OUT___________|__|__|_________________________|
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

public class DIntGrid2DXYLB extends DIntGrid2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String NAME;
	//quando ricevo cellette e ho splittato a mia volta imposto i topic diversamente delle cellette
	private boolean isSplitted;
	private boolean splitDone;
	private boolean prepareForBalance;
	private boolean prepareForUnion;
	private int positionForUnion = -1;
	private boolean preUnion;
	private boolean unionDone;
	private boolean isUnited;
	//Serve per dividere le celle per il load Balancing
	private LoadBalancingIntegerNumeric balance;

	private HashMap<Integer, UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>> hashUpdatesPosition;
	private HashMap<Integer, MyCellInterface> toSendForBalance;
	private HashMap<Integer, MyCellInterface> toSendForUnion;
	RegionIntegerNumericLB outAgents;
	private int width,height;

	// codice profiling
	private long totPub;
	private long startPub;
	private long endPub;
	private int numStep;
	private FileOutputStream f;
	private PrintStream ps;
	private int numAgents;
	// fine codice profiling
	
	private String topicPrefix = "";
	private int NUMPEERS;
	
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
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param initialGridValue the initial value that we want to set at grid at begin simulation 
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DIntGrid2DXYLB(int width, int height,SimState sm,int max_distance,int i,int j,int rows,int columns, 
			Integer initialGridValue, String name, String prefix) 
	{		
		super(width, height, initialGridValue);
		this.width=width;
		this.height=height;
		this.NAME = name;
		this.sm=sm;
		this.topicPrefix = prefix;
		cellType = new CellType(i, j);
		MAX_DISTANCE=max_distance;
		NUMPEERS=rows*columns;
		toSendForBalance = new HashMap<Integer, MyCellInterface>();
		toSendForUnion = new HashMap<Integer, MyCellInterface>();
		
		//upper left corner's coordinates
		own_x=(width/((int)Math.sqrt(NUMPEERS)))* cellType.pos_j; //inversione
		own_y=(height/((int)Math.sqrt(NUMPEERS)))* cellType.pos_i;

		// own width and height
		my_width=(int) (width/Math.sqrt(NUMPEERS));
		my_height=(int) (height/Math.sqrt(NUMPEERS));

		//Divide le celle inizialmente
		balance = new LoadBalancingIntegerNumeric();

		//contiene le celle divise inizialmente
		ArrayList<MyCellIntegerNumeric> listOriginalCell = balance.createRegions(this,my_width,my_height,MAX_DISTANCE,own_x,own_y,NUMPEERS);

		//struttura in cui vengono inserite le MyCellForInteger
		listGrid = new HashMap<Integer,HashMap<CellType, MyCellInterface>>();

		//riempie la struttura contenitore di MyCellForInteger
		for(int k = 0; k < 9; k++){
			listGrid.put(k, new HashMap<CellType, MyCellInterface>());
		}

		int [] lP = {0,1,2,7,8,3,6,5,4};
		for(int k = 0; k < lP.length; k++){
			listGrid.get(lP[k]).put(cellType, listOriginalCell.get(k));
		}

		//Contenitore di UpdatePositionForInteger per gli aggiornamenti e le publish
		hashUpdatesPosition = new HashMap<Integer, UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>>();

		this.isSplitted = false;
		this.splitDone = false;
		this.prepareForBalance = false;
		this.prepareForUnion = false;
		this.preUnion = false;
		this.unionDone = false;
		this.isUnited = true;

		outAgents = new RegionIntegerNumericLB(0, 0, 0, 0, 0, 0);
		
		updates_cacheLB=new ArrayList<ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>>();	

		// codice profiling
		totPub = 0;
		startPub = 0;
		endPub = 0;
		f = null;
		ps = null;
		numStep = 1001;
		// fine codice profiling
	}

	@Override
	public Int2D getAvailableRandomLocation() {
		int x=(((DistributedState)sm).random.nextInt(width)%(my_width-1))+own_x;
		if(x>(width-1)) x--;
		int y=(((DistributedState)sm).random.nextInt(height)%(my_height-1))+own_y;
		if(y>(height-1)) y--;

		//rm.setPos(new Int2D(x, y));

		return (new Int2D(x, y));
	}


	@Override
	public DistributedState getState() {
	
		return (DistributedState)sm;
	}

	@Override
	public ArrayList<MessageListener> getLocalListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return NAME;
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
	public void setIsSplitted(boolean isSplitted) {
		// TODO Auto-generated method stub
		if(isSplitted)
			isUnited = false;
		this.isSplitted = isSplitted;
		this.splitDone = isSplitted;
	}

	@Override
	public void prepareForBalance(boolean setForBalance){
		
		this.prepareForBalance = setForBalance;
	}

	@Override
	public HashMap<Integer, MyCellInterface> getToSendForUnion() {
		return toSendForUnion;
	}

	@Override
	public void prepareForUnion(boolean prepareForUnion) {
		this.prepareForUnion = prepareForUnion;
	}

	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
	//public boolean setDistributedObjectLocation(int d, Int2D location, SimState sm)
	//{
	public boolean setDistributedObjectLocation( Int2D location, Object ob ,SimState sm) throws DMasonException{

		
		if(!(ob instanceof Integer)) throw new DMasonException("Cast Exception setDistributedObjectLocation, second parameter must be a int");
	
		
		int d=(Integer) ob;
		numAgents++;
		boolean fl = false;
		
		if(prepareForBalance)
		{		
			boolean b = false;
			
	    	for(Integer pos : listGrid.keySet())
	    	{	
	    		HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
	    		
	    		for(CellType ct : hm.keySet()){
	    			if((hm.get(ct).getPosition() == MyCellInterface.CENTER))
	    			{	
	    				MyCellIntegerNumeric mc = (MyCellIntegerNumeric) hm.get(ct);
	    				if(mc.getMyField().isMine(location.x,location.y))
	    					return mc.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(d, location));
	    					
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
	    				    		RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
	    				    		
	    				    		if(region.isMine(location.x, location.y))
	    				    		{
	    				    			if(name.contains("mine"))
	    				    			{
	    				    				region.put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
	    				    				mc.getMyField().put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
	    				    			}
	    				    			else
	    				    				if(name.contains("out"))
	    				    				{
	    				    					region.put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
	    				    					outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
	    				    				}
	    				    		}
	    				    	}
	    					}
	    					catch (Exception e) {
								e.printStackTrace();
							}
	    				}
	    			}
	    			if(ct.toString().equals(cellType.toString()))
	    			{
			    		MyCellIntegerNumeric mc = (MyCellIntegerNumeric) hm.get(ct);
			    		b = balance.addForBalance(location, d, mc);
		    			fl = fl || b;
	    			}
	    			else
	    			{
	    				MyCellIntegerNumeric md = (MyCellIntegerNumeric) hm.get(ct);
	    				
				    	if(md.getMyField().isMine(location.x,location.y))  
				    		return md.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(d, location));
				        
				    	if(setValue(d, location, md))
				    		fl = fl || true;
	    			}
	    		}
	    	}
		} 
		else
	    	if(prepareForUnion && !isSplitted)
	    	{
	    	
	    		boolean u = false;
		    	for(Integer pos : listGrid.keySet())
		    	{	
		    		HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
		    		
		    		for(CellType ct : hm.keySet()){
		    			
		    			/*MyCellIntegerNumeric mc =  (MyCellIntegerNumeric) hm.get(ct);
		    			u = balance.addForBalance(location, d, mc);
		    			fl = fl || u;*/
		    			MyCellIntegerNumeric mc =  (MyCellIntegerNumeric) hm.get(ct);
		    			
		    			if(mc.getMyField().isMine(location.x,location.y))
	    					return mc.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(d, location));
		    			
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
	    				    		RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
	    				    		
	    				    		if(region.isMine(location.x, location.y))
	    				    		{
	    				    			if(name.contains("mine"))
	    				    			{
	    				    				fl = u || region.put(location.toString(),new EntryNum<Integer,Int2D>(d, location))!=null;
	    				    				mc.getMyField().put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
	    				    			}
	    				    			else
	    				    				if(name.contains("out"))
	    				    				{
	    				    					fl = u || region.put(location.toString(),new EntryNum<Integer,Int2D>(d, location))!=null;
	    				    					outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(d, location));
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
		    	{		    		
			    	for(Integer pos : listGrid.keySet())
			    	{	
			    		HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
			    		
			    		for(CellType ct : hm.keySet())
			    		{	
			    			
			    			MyCellIntegerNumeric md = (MyCellIntegerNumeric)hm.get(ct);
			    			
			    			
			    			if(md.isMine(location.x, location.y) && getState().schedule.getSteps()>1){
						    	if(md.getMyField().isMine(location.x,location.y))  
						    		return md.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(d, location));
						        
						    	if(setValue(d, location, md))
						    		fl = fl || true;
			    			}
			    			else{
			    				if(md.getMyField().isMine(location.x,location.y))  
			    					return md.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(d, location));
						        
			    				if(setValue(d, location, md))
						    		fl = fl || true;
			    			}
			    		}
			    	}
		    	}
		
		if(fl) 
			return true;
		else
		{
	    	System.out.println(cellType+")OH MY GOD!"+"  location: "+location+" value= "+d); // it should never happen (don't tell it to anyone shhhhhhhh! ;P) 
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
	    					clearReturnedOut((MyCellIntegerNumeric)hm.get(ct));
	    			}
	    		}
	    	}
	    	if(p!=-1){
	    		MyCellInterface mc = listGrid.get(p).remove(c);
	    		for (int j = (Integer)mc.getOwn_x(); j < ((Integer)mc.getOwn_x()+(Integer)mc.getMy_width()); j++)
					for (int i =(Integer)mc.getOwn_y(); i <((Integer)mc.getOwn_y()+(Integer)mc.getMy_height()); i++)
						if(setValue(field[j][i],new Int2D(j, i), mc)) continue;
						else
							((RegionIntegerNumericLB)mc.getMyField()).addEntryNum(new EntryNum<Integer,Int2D>(field[j][i], new Int2D(j, i)));
	    		toSendForUnion.put(p, mc);
	    		removeValue(mc);
	    	}
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
				
				MyCellInterface mc = listGrid.get(k).remove(cellType);
				for (int j = (Integer)mc.getOwn_x(); j < ((Integer)mc.getOwn_x()+(Integer)mc.getMy_width()); j++)
					for (int i =(Integer)mc.getOwn_y(); i <((Integer)mc.getOwn_y()+(Integer)mc.getMy_height()); i++)
						if(setValue(field[j][i],new Int2D(j, i), mc)) continue;
						else
							((RegionIntegerNumericLB)mc.getMyField()).addEntryNum(new EntryNum<Integer,Int2D>(field[j][i], new Int2D(j, i)));
				toSendForBalance.put(k, mc);
				removeValue(mc);
			}
		}
	
		//every agent in the myfield region is scheduled
		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
	    		
			for(CellType ct : hm.keySet())
			{	
				if(ct.equals(cellType)){
					MyCellIntegerNumeric md = (MyCellIntegerNumeric) hm.get(ct);
	
					for(EntryNum<Integer, Int2D> e: md.getMyField().values())
					{
						Int2D loc=e.l;
						int i = e.r;
						field[loc.getX()][loc.getY()]=i;
					}
					//updateFields(hm.get(ct)); //update fields with java reflect
				}
			}
		}
	
		for(Integer pos : listGrid.keySet())
		{	
			HashMap<CellType, MyCellInterface> hm = listGrid.get(pos);
	
			for(CellType ct : hm.keySet())
			{	
				if(!ct.equals(cellType)){
					MyCellIntegerNumeric md =(MyCellIntegerNumeric) hm.get(ct);
	
					for(EntryNum<Integer, Int2D> e: md.getMyField().values())
					{
						Int2D loc=e.l;
						int i = e.r;
						field[loc.getX()][loc.getY()]=i;
					}
				}
			}
		}
	
		updates_cacheLB=new ArrayList<ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>>();
	
		((DistributedMultiSchedule)(sm.schedule)).manageMerge(hashUpdatesPosition,this,cellType);
		((DistributedMultiSchedule)(sm.schedule)).manageBalance(hashUpdatesPosition,this,cellType,balance);
		
		//PUBLISH SUI TOPIC
		try {
			Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();
			
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.LEFT),topicPrefix+cellType.toString()+"L", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.RIGHT),topicPrefix+cellType.toString()+"R", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.UP),topicPrefix+cellType.toString()+"U", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.DOWN),topicPrefix+cellType.toString()+"D", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT),topicPrefix+cellType.toString()+"CUDL", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT),topicPrefix+cellType.toString()+"CUDR", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT),topicPrefix+cellType.toString()+"CDDL", NAME);
			connWorker.publishToTopic(hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT),topicPrefix+cellType.toString()+"CDDR", NAME);	
			
		} catch (Exception e1) { e1.printStackTrace();}
		//<--
	
		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations
		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, 8);			
	
			while(!q.isEmpty())
			{
				UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>> region=(UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>)q.poll();
				
				if(region.isPreBalance())
				{
					if(region.getMyCell()!=null)
					{
						MyCellIntegerNumeric mc = region.getMyCell();
	
						resetArrivedCellPositions(mc);
						
						listGrid.get(mc.getPosition()).put(mc.getParentCellType(), mc);
						
						for(EntryNum<Integer, Int2D> e: mc.getMyField().values())
						{							
							Int2D loc=e.l;
							int i = e.r;
							field[loc.getX()][loc.getY()]=i;
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
					MyCellIntegerNumeric mc = region.getMyCell();
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
			
			for(Integer pos : toSendForBalance.keySet())
			{
					MyCellInterface mc = toSendForBalance.get(pos);
					removeValue(mc);
			}
			
			splitDone = false;
		}
		ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>> tmp = new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
		tmp.add(outAgents.clone());
		updates_cacheLB.add(tmp);
		for(ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>> regions : updates_cacheLB)
			for(RegionNumeric<Integer,EntryNum<Integer,Int2D>> r : regions)
				for(EntryNum<Integer, Int2D> e_m: r.values())
				{
					Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
					field[i.getX()][i.getY()]=e_m.r;	
				}
		outAgents = new RegionIntegerNumericLB(0,0,0,0,0,0);
		this.reset();
	
		return true;
	}

	//Resetta le strutture contenitore delle publish
	private void resetPublishList(long step){
		
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_UP_LEFT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.CORNER_DIAG_UP_LEFT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.UP, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.UP,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_UP_RIGHT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.CORNER_DIAG_UP_RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.RIGHT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_DOWN_RIGHT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.CORNER_DIAG_DOWN_RIGHT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.DOWN, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.DOWN,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.CORNER_DIAG_DOWN_LEFT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.CORNER_DIAG_DOWN_LEFT,cellType,null));
		hashUpdatesPosition.put(UpdatePositionInterface.LEFT, 
				new UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>>(step,UpdatePositionInterface.LEFT,cellType,null));
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
							MyCellIntegerNumeric mc = (MyCellIntegerNumeric)hm.get(c);
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
							MyCellIntegerNumeric mc = (MyCellIntegerNumeric)hm.get(ct);
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
							MyCellIntegerNumeric mc = (MyCellIntegerNumeric)hm.get(c);
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
			    			MyCellIntegerNumeric md = (MyCellIntegerNumeric)hm.get(ct);
			    			
							for(EntryNum<Integer, Int2D> e: md.getMyField().values())
							{
								Int2D loc=e.l;
								int i = e.r;
								field[loc.getX()][loc.getY()]=i;
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
						MyCellIntegerNumeric mc = (MyCellIntegerNumeric)hm.get(c);
						
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
					MyCellIntegerNumeric mc1 =(MyCellIntegerNumeric) hm1.get(ct);
					mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), true);
					mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), true);
				}
			}
			
			HashMap<CellType, MyCellInterface> hm2 = listGrid.get((position+1+8)%8);
			for(CellType ct : hm2.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerNumeric mc2 = (MyCellIntegerNumeric) hm2.get(ct);
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

	private void clearReturnedOut(MyCellIntegerNumeric mc) {
		
		int position =mc.getPosition();
		
		if(position == MyCellInterface.UP)
		{
			mc.getMyRMap().corner_out_down_left_diag_center.clear();
			mc.getMyRMap().corner_mine_down_left.clear();
			mc.getMyRMap().corner_mine_down_right.clear();
			mc.getMyRMap().corner_out_down_right_diag_center.clear();
		}
		else
			if(position == MyCellInterface.RIGHT)
			{
				mc.getMyRMap().corner_out_up_left_diag_center.clear();
				mc.getMyRMap().corner_mine_up_left.clear();
				mc.getMyRMap().corner_mine_down_left.clear();
				mc.getMyRMap().corner_out_down_left_diag_center.clear();
			}
			else
				if(position == MyCellInterface.DOWN)
				{
					mc.getMyRMap().corner_out_up_left_diag_center.clear();
					mc.getMyRMap().corner_mine_up_left.clear();
					mc.getMyRMap().corner_mine_up_right.clear();
					mc.getMyRMap().corner_out_up_right_diag_center.clear();
				}
				else
					if(position == MyCellInterface.LEFT)
					{
						mc.getMyRMap().corner_out_up_right_diag_center.clear();
						mc.getMyRMap().corner_mine_up_right.clear();
						mc.getMyRMap().corner_mine_down_right.clear();
						mc.getMyRMap().corner_out_down_right_diag_center.clear();
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
					MyCellIntegerNumeric mc = (MyCellIntegerNumeric) hm1.get(ct);
					mc.getPositionPublish().put(((topicPosition+1+8)%8), false);
					mc.getPositionPublish().put(((topicPosition+2+8)%8), false);
				}
			}
			
			HashMap<CellType, MyCellInterface> hm2 = listGrid.get((topicPosition+1+8)%8);
			for(CellType ct : hm2.keySet())
			{
				if(!ct.equals(cellType))
				{
					MyCellIntegerNumeric mc = (MyCellIntegerNumeric) hm2.get(ct);
					mc.getPositionPublish().put(((topicPosition-1+8)%8), false);
					mc.getPositionPublish().put(((topicPosition-2+8)%8), false);
				}
			}
		}
	}

	private void resetArrivedCellPositions(MyCellIntegerNumeric mc) {
	
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
						MyCellIntegerNumeric mc1 = (MyCellIntegerNumeric)hm1.get(ct);
						mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), false);
						mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						MyCellIntegerNumeric mc2 = (MyCellIntegerNumeric)hm2.get(ct);
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
						MyCellIntegerNumeric mc1 =(MyCellIntegerNumeric) hm1.get(ct);
						mc1.getPositionPublish().put(((mc1.getPosition()+2+8)%8), false);
						mc1.getPositionPublish().put(((mc1.getPosition()+3+8)%8), false);
					}
				}
				for(CellType ct : hm2.keySet())
				{
					if(!ct.equals(cellType) && hm2.get(ct)!=null)
					{
						MyCellIntegerNumeric mc2 = (MyCellIntegerNumeric) hm2.get(ct);
						mc2.getPositionPublish().put(((mc2.getPosition()-2+8)%8), false);
						mc2.getPositionPublish().put(((mc2.getPosition()-3+8)%8), false);
					}
				}
			}
		}
		
	}

	private void updateExternalOutFromAdiacentCell(MyCellInterface mc) {
	
		int position = mc.getPosition();
		
		if(position == MyCellInterface.CORNER_DIAG_UP_LEFT)
		{
			for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB) ((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_LEFT).get(cellType)).getMyRMap().getcorner_out_up_left_diag_center()).values())
			{			    	
				Int2D loc=e.l;
				int i = e.r;
				field[loc.getX()][loc.getY()]=i;
			}
		}
		else
			if(position == MyCellInterface.UP)
			{
				for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_LEFT).get(cellType)).getMyRMap().getcorner_out_up_right_diag_center()).values())
				{			    	
					Int2D loc=e.l;
					int i = e.r;
					field[loc.getX()][loc.getY()]=i;
				}
				for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.UP).get(cellType)).getMyRMap().getup_out()).values())
				{			    	
					Int2D loc=e.l;
					int i = e.r;
					field[loc.getX()][loc.getY()]=i;
				}
				for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).get(cellType)).getMyRMap().getcorner_out_up_left_diag_center()).values())
				{			    	
					Int2D loc=e.l;
					int i = e.r;
					field[loc.getX()][loc.getY()]=i;
				}
			}
			else
				if(position == MyCellInterface.CORNER_DIAG_UP_RIGHT)
				{
					for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).get(cellType)).getMyRMap().getcorner_out_up_right_diag_center()).values())
					{			    	
						Int2D loc=e.l;
						int i = e.r;
						field[loc.getX()][loc.getY()]=i;
					}
				}
				else
					if(position == MyCellInterface.RIGHT)
					{
						for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).get(cellType)).getMyRMap().getcorner_out_down_right_diag_center()).values())
						{			    	
							Int2D loc=e.l;
							int i = e.r;
							field[loc.getX()][loc.getY()]=i;
						}
						for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.RIGHT).get(cellType)).getMyRMap().getright_out()).values())
						{			    	
							Int2D loc=e.l;
							int i = e.r;
							field[loc.getX()][loc.getY()]=i;
						}
						for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).get(cellType)).getMyRMap().getcorner_out_up_right_diag_center()).values())
						{			    	
							Int2D loc=e.l;
							int i = e.r;
							field[loc.getX()][loc.getY()]=i;
						}
					}
					else
						if(position == MyCellInterface.CORNER_DIAG_DOWN_RIGHT)
						{
							for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).get(cellType)).getMyRMap().getcorner_out_down_right_diag_center()).values())
							{			    	
								Int2D loc=e.l;
								int i = e.r;
								field[loc.getX()][loc.getY()]=i;
							}
						}
						else
							if(position == MyCellInterface.DOWN)
							{
								for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).get(cellType)).getMyRMap().getcorner_out_down_left_diag_center()).values())
								{			    	
									Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
								}
								for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.DOWN).get(cellType)).getMyRMap().getdown_out()).values())
								{			    	
									Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
								}
								for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).get(cellType)).getMyRMap().getcorner_out_down_right_diag_center()).values())
								{			    	
									Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
								}
							}
							else
								if(position == MyCellInterface.CORNER_DIAG_DOWN_LEFT)
								{
									for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).get(cellType)).getMyRMap().getcorner_out_down_left_diag_center()).values())
									{			    	
										Int2D loc=e.l;
										int i = e.r;
										field[loc.getX()][loc.getY()]=i;
									}
								}
								else
									if(position == MyCellInterface.LEFT)
									{
										for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).get(cellType)).getMyRMap().getcorner_out_up_left_diag_center()).values())
										{			    	
											Int2D loc=e.l;
											int i = e.r;
											field[loc.getX()][loc.getY()]=i;
										}
										for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.LEFT).get(cellType)).getMyRMap().getleft_out()).values())
										{			    	
											Int2D loc=e.l;
											int i = e.r;
											field[loc.getX()][loc.getY()]=i;
										}
										for(EntryNum<Integer, Int2D> e: ((RegionIntegerNumericLB)((MyCellIntegerNumeric)listGrid.get(MyCellInterface.CORNER_DIAG_UP_LEFT).get(cellType)).getMyRMap().getcorner_out_down_left_diag_center()).values())
										{			    	
											Int2D loc=e.l;
											int i = e.r;
											field[loc.getX()][loc.getY()]=i;
										}
									}		
	}

	private void clearArrivedOut(MyCellIntegerNumeric mc) {
	
		int position = mc.getPosition();
		if(position == MyCellInterface.CORNER_DIAG_UP_LEFT)
		{
			mc.getMyRMap().corner_out_down_right_diag_center.clear();
		}
		else
			if(position == MyCellInterface.UP)
			{
				mc.getMyRMap().corner_out_down_left_diag_center.clear();
				mc.getMyRMap().corner_out_down_left_diag_down.clear();
				mc.getMyRMap().down_out.clear();
				mc.getMyRMap().corner_out_down_right_diag_down.clear();
				mc.getMyRMap().corner_out_down_right_diag_center.clear();
			}
			else
				if(position == MyCellInterface.CORNER_DIAG_UP_RIGHT)
				{
					mc.getMyRMap().corner_out_down_left_diag_center.clear();
				}
				else
					if(position == MyCellInterface.RIGHT)
					{
						mc.getMyRMap().corner_out_up_left_diag_center.clear();
						mc.getMyRMap().corner_out_up_left_diag_left.clear();
						mc.getMyRMap().left_out.clear();
						mc.getMyRMap().corner_out_down_left_diag_left.clear();
						mc.getMyRMap().corner_out_down_left_diag_center.clear();
					}
					else
						if(position == MyCellInterface.CORNER_DIAG_DOWN_RIGHT)
						{
							mc.getMyRMap().corner_out_up_left_diag_center.clear();
						}
						else
							if(position == MyCellInterface.DOWN)
							{
								mc.getMyRMap().corner_out_up_left_diag_center.clear();
								mc.getMyRMap().corner_out_up_left_diag_up.clear();
								mc.getMyRMap().up_out.clear();
								mc.getMyRMap().corner_out_up_right_diag_up.clear();
								mc.getMyRMap().corner_out_up_right_diag_center.clear();
							}
							else
								if(position == MyCellInterface.CORNER_DIAG_DOWN_LEFT)
								{
									mc.getMyRMap().corner_out_up_right_diag_center.clear();
								}
								else
									if(position == MyCellInterface.LEFT)
									{
										mc.getMyRMap().corner_out_up_right_diag_center.clear();
										mc.getMyRMap().corner_out_up_right_diag_right.clear();
										mc.getMyRMap().right_out.clear();
										mc.getMyRMap().corner_out_down_right_diag_right.clear();
										mc.getMyRMap().corner_out_down_right_diag_center.clear();
									}
	}

	private void preparePublishUnion() {
	
		for(Integer pos : toSendForUnion.keySet())
		{	
			if(toSendForUnion.get(pos)!=null)
			{
	    		MyCellIntegerNumeric md = (MyCellIntegerNumeric)toSendForUnion.get(pos);
	    		
	    		if(md.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT)
	    		{
	    			/*
	    			if(isSplitted)
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_right_diag_center);    					
	    			}*/
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_right_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_right);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_right);
	    				}
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);    	 
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
	    				}
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN))
	    			{
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_down);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_down);
	    				}
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_down);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
	    				}
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_center);
	    			}
	    			else{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_left_center);
	    			}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.UP)
	    		{
	    			
	    			if(isSplitted)
					{
	    				/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_left = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_diag_left);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_left_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_down = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_diag_down);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
										md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_down = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_diag_down);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_right_diag_center);	
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_right = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_diag_right);
						*/
					}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT)
	    		{
	    			/*
	    			if(isSplitted)
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_left_diag_center);
	    			}*/
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_left_center);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_right_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_down);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_down);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_down);
	    				}
	    			}	
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
	    				}
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT))
	    			{
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_left);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_left);
	    				}
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_left);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
	    				}
	    			}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.RIGHT)
	    		{
	    			
	    			if(isSplitted)
					{
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_left_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_up = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_diag_up);
						
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_left = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_diag_left);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
										md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_left = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_diag_left);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_left_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_down = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_diag_down);
						*/
					}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT)
	    		{
	    			/*
	    			if(isSplitted)
	    			{		
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_left_diag_center);
	    			}*/
	
	    			if(md.getPositionPublish().get(MyCellInterface.UP))
	    			{
	    				
	    				if(isSplitted)
	    				{	
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_up);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_up);
	    				}
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_up);
	    			}
	    			else
	    			{
	    				
	    				if(isSplitted)
	    				{	
	
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
	    				}
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_right_center);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_left_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_left);
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_left);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_left);
	    				}
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
	    				}
	    			}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.DOWN)
	    		{
	    			if(isSplitted)
					{
	    				/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_left = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_diag_left);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_left_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_up = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_diag_up);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
										md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
	
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_up = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_diag_up);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_right_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_right = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_diag_right);
						*/
					}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT)
	    		{
	    			/*
	    			if(isSplitted)
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_right_diag_center);
	    			}*/
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_left_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.UP))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_up);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_up);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_up);
	    				}
	    			}
	    			else{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
	    				}
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT))
	    			{
	    				
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_right);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_right);
	    				}
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_right);
	    			}
	    			else
	    			{
	    				if(isSplitted)
	    				{
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
	    				}
	    				else
	    				{
	    					RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
	    					empty.clear();
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									empty, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
	    				}
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_center);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_right_center);
	    			}
	    		}
	
	    		if(md.getPosition() == MyCellInterface.LEFT)
	    		{
	    			if(isSplitted)
					{
	    				/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_up = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_diag_up);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_right_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_right = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_diag_right);
						
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
										md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);
						
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_right = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_diag_right);
						*/
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
										md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
						hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_right_diag_center);
						/*
						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_down = 
								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
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
					MyCellIntegerNumeric md = (MyCellIntegerNumeric) hm.get(ct);
	
					if(md.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT)
					{
						if(isSplitted)
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_right_diag_center);    					
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right);
							if(isSplitted || unionDone)
							{
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_right);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_right);
							}
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);    	    				
							if(isSplitted || unionDone)
							{
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
												md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_down);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_down);
							}
							
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_down);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
												md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
						}
					}
					
					if(md.getPosition() == MyCellInterface.UP)
					{
						if(isSplitted)
						{
							/*
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_diag_left);*/
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_down_left_diag_center);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_diag_down);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_diag_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_right_diag_center);
							/*
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_diag_right);*/
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_diag_right);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_diag_left);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
						}
					}
					
					if(md.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT)
					{
						if(isSplitted)
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_left_diag_center);
						}
						
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_down);
							if(isSplitted || unionDone)
							{
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_down);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_down);
							}
						}	
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_down);
							if(isSplitted || unionDone)
							{
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_down);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
												md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_left);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_left);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_left);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
							
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
												md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
							}
						}
					}
					
					if(md.getPosition() == MyCellInterface.RIGHT)
					{
						if(isSplitted)
						{
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_left_diag_center);
							/*DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_diag_up);*/
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_diag_left);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_diag_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_left_diag_center);
							/*DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_diag_down);*/
	
						}
						
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_diag_up);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
							if(isSplitted || unionDone)
							{
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_diag_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_diag_down);
							}
							else{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    	    				empty.clear();
	    	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
							}
						}
					}
					
					if(md.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT)
					{
						if(isSplitted)
						{		
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_left_diag_center);
						}
	
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							if(isSplitted || unionDone)
	    					{	
				
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
												md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_up);
	    					}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
								empty.clear();
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_up);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_up);
						}
						else
						{
							if(isSplitted || unionDone)
	    					{	
				
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
												md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
	    					}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
								empty.clear();
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
							}
							
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
						}
						
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_left);
							
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_left);
							}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_left);
							}
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
							}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_left_left);
							}
						}
					}
					
					if(md.getPosition() == MyCellInterface.DOWN)
					{
						if(isSplitted)
						{
							/*DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_diag_left);*/
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_left_diag_center);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_diag_up);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
	
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_diag_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_up_right_diag_center);
							/*
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_diag_right);*/
						}
	
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							if(isSplitted || unionDone){
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_diag_right);
							}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
								empty.clear();
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
										new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
												empty, (sm.schedule.getSteps()-1),cellType);
								hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_diag_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_diag_left);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    	    				empty.clear();
	    	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
							}
						}
					}
					
					if(md.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT)
					{
						if(isSplitted)
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
											md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_right_diag_center);
	    				}
						
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_up_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_up);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_up);
							}
						}
						else{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_up);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.RIGHT))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_right);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_up_right_right);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_right);
						}
						else
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
							}
							else
							{
	    	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
	    	    				empty.clear();
	    						DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_right);
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
											md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_right);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_center);
						}
						else
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_right_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
											md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_left_center);
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
						}
					}
	
					if(md.getPosition() == MyCellInterface.LEFT)
					{
						if(isSplitted)
						{
							/*
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_up = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_diag_up);*/
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_center = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_right_diag_center);
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    									md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_diag_right);
	    					
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    									md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right);
	    					
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_right = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_right_diag_right);
	    					DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_center = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_right_diag_center);
	    					/*DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_down = 
	    							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_diag_down);*/
						}
	
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_up_left_center);
						}
	
						if(md.getPositionPublish().get(MyCellInterface.UP))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
											md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_diag_up = 
	        							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	        									md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	        					hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_diag_up);
							}
							else
							{
	    						RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    	    				empty.clear();
	    	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
							}
						}
						if(md.getPositionPublish().get(MyCellInterface.DOWN))
						{
							if(isSplitted || unionDone)
							{
								DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_diag_down = 
	        							new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	        									md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	        					hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_diag_down);
							}
							else
							{
								RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    	    				empty.clear();
	    	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    								new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    										empty, (sm.schedule.getSteps()-1),cellType);
	    						hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);	
							}
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
											md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_down, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
						}
						if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_center = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_left_center);
	
						}
						if(md.getPositionPublish().get(MyCellInterface.LEFT))
						{
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
											md.getMyRMap().corner_out_down_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
											md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
							DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
									new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
											md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
							hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
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
	
			MyCellIntegerNumeric md = (MyCellIntegerNumeric)listGrid.get(pos).get(cellType);
			
			if(md != null)
			{
	    		//PUBLISH POSITION CORNER UP LEFT
	    		if(md.getPosition()==MyCellInterface.CORNER_DIAG_UP_LEFT){
	
	    			//LEFT MINE UP
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left_corner_down_diag);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_left);
	    			}
	    			else
	    			{
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_left);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_down_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_left);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
	    			{						
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_corner_left_up);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.UP))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_up,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_left_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_right_up_diag);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_left_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_up,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_left_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_up);
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_up);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								empty,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_up_right_up);
	    			}
	    		}
	
	    		//PUBLISH POSITION UP
	    		if(md.getPosition()==MyCellInterface.UP){
	
	    			if(md.getPositionPublish().get(MyCellInterface.UP))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_left_up_diag);
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_right_up_diag);
	    			}
	    			else
	    			{
	    				if(positionForUnion != MyCellInterface.UP){
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_up_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
		    								md.getMyRMap().corner_out_up_left_diag_center,	(sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_left_up_diag);
		
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_up_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
		    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_right_up_diag);
	    				}
	    			}
	    		}
	
	    		//PUBLISH POSITION CORNER UP RIGHT
	    		if(md.getPosition()==MyCellInterface.CORNER_DIAG_UP_RIGHT){
	
	    			if(md.getPositionPublish().get(MyCellInterface.UP))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left_corner_up_diag);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_up);
	    			}
	    			else
	    			{
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_up);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left_corner_up_diag_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_up);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_up);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
	    			{	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_corner_up_right);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_up_right_right);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_right_down_diag);
	    				
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_up_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_corner_up_right_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right);
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_right);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_down_diag_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right_corner_down_diag_right);
	    			}
	    		}
	
	    		//PUBLISH POSITION RIGHT
	    		if(md.getPosition()==MyCellInterface.RIGHT){
	
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right_corner_up_diag);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_right_down_diag);
	    				
	    			}
	    			else
	    			{
	    				if(positionForUnion != MyCellInterface.RIGHT){
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
		    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right_corner_up_diag);
		
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
		    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_right_down_diag);
	    				}
	    			}
	    		}
	
	    		//PUBLISH POSITION CORNER DOWN RIGHT
	    		if(md.getPosition()==MyCellInterface.CORNER_DIAG_DOWN_RIGHT){
	
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right_corner_up_diag);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
	
	    			}
	    			else
	    			{
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_right_diag_right);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_right_corner_up_diag_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_right);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
	    			{	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_corner_down_right);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_right_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_left_down_diag);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_down);
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_left_diag_down);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_down_diag_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_left_corner_down_diag_down);
	    			}
	    		}
	
	    		//PUBLISH POSITION DOWN
	    		if(md.getPosition()==MyCellInterface.DOWN){
	
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_right_down_diag);
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_left_down_diag);
	    			}
	    			else
	    			{
	    				if(positionForUnion != MyCellInterface.DOWN){
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
		    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_right_down_diag);		    			
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
		    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_left_down_diag);
	    				}
	    			}
	    		}
	
	    		//PUBLISH POSITION CORNER DOWN LEFT
	    		if(md.getPosition()==MyCellInterface.CORNER_DIAG_DOWN_LEFT){
	
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN))
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_right_down_diag);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
	    			}
	    			else
	    			{
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_down_right_diag_down);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_down_diag_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_right_corner_down_diag_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_down);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
	    			{	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_corner_down_left);
	    			}
	
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT)){
	
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_down_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left_corner_up_diag);
	    			}
	    			else
	    			{
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_left);
	    				RegionIntegerNumericLB empty = ((RegionIntegerNumericLB)md.getMyRMap().corner_out_up_left_diag_left);
	    				empty.clear();
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								empty, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_left_corner_up_diag_left);
	    			}
	    		}
	
	    		//PUBLISH POSITION LEFT
	    		if(md.getPosition()==MyCellInterface.LEFT){
	
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_left_down_diag);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left_corner_up_diag);
	    			}
	    			else
	    			{
	    				if(positionForUnion != MyCellInterface.LEFT){
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
		    								md.getMyRMap().corner_out_down_left_diag_center,	(sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_left_down_diag);
		    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag = 
		    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
		    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
		    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left_corner_up_diag);
	    				}
	    			}
	    		}
	    		
	    		if(md.getPosition() == MyCellInterface.CENTER){
	    			
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_LEFT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).add(dr_left_corner_up_diag_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.UP)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_left_corner_up_diag_up);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().up_mine,
	    								md.getMyRMap().up_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_up);
	    				
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag_up = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_up, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.UP).add(dr_right_corner_up_diag_up);
	    				    				
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_UP_RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).add(dr_right_corner_up_diag_center);
	    			}
	    			
	    			if(md.getPositionPublish().get(MyCellInterface.RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_up_diag_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_right,
	    								md.getMyRMap().corner_out_up_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right_corner_up_diag_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().right_mine,
	    								md.getMyRMap().right_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_right);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_right_right = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_right, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.RIGHT).add(dr_corner_down_right_right);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_right_corner_down_diag = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).add(dr_right_corner_down_diag);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.DOWN)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_right_down_diag_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_right,
	    								md.getMyRMap().corner_out_down_right_diag_down, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_right_down_diag_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().down_mine,
	    								md.getMyRMap().down_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_down);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_down_left_down = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_down,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.DOWN).add(dr_corner_down_left_down);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_down_diag_center = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_center, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).add(dr_left_corner_down_diag_center);
	    			}
	    			if(md.getPositionPublish().get(MyCellInterface.LEFT)){
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_corner_left_down_diag_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_down_left,
	    								md.getMyRMap().corner_out_down_left_diag_left,	(sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_corner_left_down_diag_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().left_mine,
	    								md.getMyRMap().left_out, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left);
	    				DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> dr_left_corner_up_diag_left = 
	    						new DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>(md.getMyRMap().corner_mine_up_left,
	    								md.getMyRMap().corner_out_up_left_diag_left, (sm.schedule.getSteps()-1),cellType);
	    				hashUpdatesPosition.get(MyCellInterface.LEFT).add(dr_left_corner_up_diag_left);
	    			}
	    		}
			}
		}
	}

	private void updateInternalMine(MyCellInterface md) {
		
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
					RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
	
					if(name.contains("mine"))
					{
						for(EntryNum<Integer, Int2D> e: region.values())
						{
	
							if(name.contains("left_mine") && !md.getPositionGood().get(MyCellInterface.LEFT))
							{	
	
								Int2D loc=e.l;
								int i = e.r;
								field[loc.getX()][loc.getY()]=i;
							}
							else
								if(name.contains("right_mine") && !md.getPositionGood().get(MyCellInterface.RIGHT))
								{
									Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
								}
								else
									if(name.contains("up_mine") && !md.getPositionGood().get(MyCellInterface.UP))
									{
										Int2D loc=e.l;
										int i = e.r;
										field[loc.getX()][loc.getY()]=i;
									}
									else
										if(name.contains("down_mine") && !md.getPositionGood().get(MyCellInterface.DOWN))
										{
											Int2D loc=e.l;
											int i = e.r;
											field[loc.getX()][loc.getY()]=i;
										}
										else
											if(name.contains("corner_mine_down_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
											{
												Int2D loc=e.l;
												int i = e.r;
												field[loc.getX()][loc.getY()]=i;
											}
											else
												if(name.contains("corner_mine_down_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
												{
													Int2D loc=e.l;
													int i = e.r;
													field[loc.getX()][loc.getY()]=i;
												}
												else
													if(name.contains("corner_mine_up_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
													{
														Int2D loc=e.l;
														int i = e.r;
														field[loc.getX()][loc.getY()]=i;
													}
													else
														if(name.contains("corner_mine_up_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
														{
															Int2D loc=e.l;
															int i = e.r;
															field[loc.getX()][loc.getY()]=i;
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
					RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
	
					if(name.contains("out"))
					{
						for(EntryNum<Integer, Int2D> e: region.values())
						{
	
							if(name.contains("left_out") && !md.getPositionGood().get(MyCellInterface.LEFT))
							{	
								Int2D loc=e.l;
								int i = e.r;
								field[loc.getX()][loc.getY()]=i;
							}
							else
								if(name.contains("right_out") && !md.getPositionGood().get(MyCellInterface.RIGHT))
								{
									Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
								}
								else
									if(name.contains("up_out") && !md.getPositionGood().get(MyCellInterface.UP))
									{
										Int2D loc=e.l;
										int i = e.r;
										field[loc.getX()][loc.getY()]=i;
									}
									else
										if(name.contains("down_out") && !md.getPositionGood().get(MyCellInterface.DOWN))
										{
											Int2D loc=e.l;
											int i = e.r;
											field[loc.getX()][loc.getY()]=i;
										}
										else
											if(name.contains("corner_out_down_left_diag_center") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
											{
												Int2D loc=e.l;
												int i = e.r;
												field[loc.getX()][loc.getY()]=i;
											}
											else
												if(name.contains("corner_out_down_left_diag_down") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
												{
													Int2D loc=e.l;
													int i = e.r;
													field[loc.getX()][loc.getY()]=i;
												}
												else
													if(name.contains("corner_out_down_left_diag_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
													{
														Int2D loc=e.l;
														int i = e.r;
														field[loc.getX()][loc.getY()]=i;
													}
													else
														if(name.contains("corner_out_down_right_diag_center") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
														{
															Int2D loc=e.l;
															int i = e.r;
															field[loc.getX()][loc.getY()]=i;
														}
														else 
															if(name.contains("corner_out_down_right_diag_down") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
															{
																Int2D loc=e.l;
																int i = e.r;
																field[loc.getX()][loc.getY()]=i;
															}
															else
																if(name.contains("corner_out_down_right_diag_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
																{
																	Int2D loc=e.l;
																	int i = e.r;
																	field[loc.getX()][loc.getY()]=i;
																}
																else
																	if(name.contains("corner_out_up_left_diag_center") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
																	{
																		Int2D loc=e.l;
																		int i = e.r;
																		field[loc.getX()][loc.getY()]=i;
																	}
																	else
																		if(name.contains("corner_out_up_left_diag_up") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
																		{
																			Int2D loc=e.l;
																			int i = e.r;
																			field[loc.getX()][loc.getY()]=i;
																		}
																		else
																			if(name.contains("corner_out_up_left_diag_left") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
																			{
																				Int2D loc=e.l;
																				int i = e.r;
																				field[loc.getX()][loc.getY()]=i;
																			}
																			else
																				if(name.contains("corner_out_up_right_diag_center") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
																				{
																					Int2D loc=e.l;
																					int i = e.r;
																					field[loc.getX()][loc.getY()]=i;
																				}
																				else
																					if(name.contains("corner_out_up_right_diag_up") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
																					{
																						Int2D loc=e.l;
																						int i = e.r;
																						field[loc.getX()][loc.getY()]=i;
																					}
																					else
																						if(name.contains("corner_out_up_right_diag_right") && !md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
																						{
																							Int2D loc=e.l;
																							int i = e.r;
																							field[loc.getX()][loc.getY()]=i;
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
	private void verifyUpdates(UpdatePositionIntegerNumeric<DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>>> super_box)
	{
		ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>> updates_out = new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
		
		for(DistributedRegionNumeric<Integer,EntryNum<Integer,Int2D>> sb : super_box){
	
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_mine=sb.out;
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_out=sb.mine;
			
			for(EntryNum<Integer, Int2D> e_m: r_mine.values())
			{
				Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;	
			}
			
			updates_out.add(r_out);
		}

		updates_cacheLB.add(updates_out);
	}

	private void memorizeRegionOut(MyCellIntegerNumeric md)
	{
		ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>> updates_out = new ArrayList<RegionNumeric<Integer,EntryNum<Integer,Int2D>>>();
		    	
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
	    			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
	
	    			if(name.contains("out"))
	    			{		
		    			if(name.contains("left_out") && md.getPositionGood().get(MyCellInterface.LEFT))
		    			{
		    				updates_out.add(region.clone());
		    			}
		    			else
		    				if(name.contains("right_out") && md.getPositionGood().get(MyCellInterface.RIGHT))
		    				{
		    					updates_out.add(region.clone());
							}
		    				else
		    					if(name.contains("up_out") && md.getPositionGood().get(MyCellInterface.UP))
		    					{
		    						updates_out.add(region.clone());
		    					}
		    					else
		    						if(name.contains("down_out") && md.getPositionGood().get(MyCellInterface.DOWN))
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
		    									if(name.contains("corner_out_down_left_diag_center") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
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
		    												if(name.contains("corner_out_down_right_diag_center") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
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
		    															if(name.contains("corner_out_up_left_diag_center") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
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
		    																		if(name.contains("corner_out_up_right_diag_center") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
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
	    			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
		   			if(name.contains("out"))
		   			{
		   				for(EntryNum<Integer, Int2D> e: region.values())
		   			 	{
		   					Int2D loc=e.l;
							int i = e.r;
							field[loc.getX()][loc.getY()]=i;
		   			 	} 
		   			}
		   			else
		   				if(name.contains("mine"))
		   				{
		   					for(EntryNum<Integer, Int2D> e: region.values())
		   					{			    	
			    				if(name.contains("left_mine") && md.getPositionGood().get(MyCellInterface.LEFT))
			    				{	
			    					Int2D loc=e.l;
									int i = e.r;
									field[loc.getX()][loc.getY()]=i;
			    				}
			    				else
			    					if(name.contains("right_mine") && md.getPositionGood().get(MyCellInterface.RIGHT))
			    					{
			    						Int2D loc=e.l;
										int i = e.r;
										field[loc.getX()][loc.getY()]=i;
			    					}
			    					else
			    						if(name.contains("up_mine") && md.getPositionGood().get(MyCellInterface.UP))
			    						{
			    							Int2D loc=e.l;
			    							int i = e.r;
			    							field[loc.getX()][loc.getY()]=i;
			    						}
			    						else
			    							if(name.contains("down_mine") && md.getPositionGood().get(MyCellInterface.DOWN))
			    							{
			    								Int2D loc=e.l;
			    								int i = e.r;
			    								field[loc.getX()][loc.getY()]=i;
			    							}
			    							else
			    								if(name.contains("corner_mine_down_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
			    								{
			    									Int2D loc=e.l;
			    									int i = e.r;
			    									field[loc.getX()][loc.getY()]=i;
			    								}
			    								else
			    									if(name.contains("corner_mine_down_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
			    									{
			    										Int2D loc=e.l;
			    										int i = e.r;
			    										field[loc.getX()][loc.getY()]=i;
			    									}
			    									else
			    										if(name.contains("corner_mine_up_left") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
			    										{
			    											Int2D loc=e.l;
			    											int i = e.r;
			    											field[loc.getX()][loc.getY()]=i;
			    										}
			    										else
			    											if(name.contains("corner_mine_up_right") && md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
			    											{
			    												Int2D loc=e.l;
			    												int i = e.r;
			    												field[loc.getX()][loc.getY()]=i;
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
	 */
	private boolean setValue(int value, Int2D location,MyCellInterface md)
	{
	
		RegionMapNumeric<Integer, EntryNum<Integer,Int2D>> rmap =(RegionMapNumeric<Integer, EntryNum<Integer,Int2D>>) md.getMyRMap();
		MyCellIntegerNumeric ms = (MyCellIntegerNumeric) md;
		
		if(md.getPositionGood().get(MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.left_out;
			if(region.isMine(location.x,location.y))
			{   	 
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			}
		}

		if(md.getPositionGood().get(MyCellInterface.RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.right_out;
			if(region.isMine(location.x,location.y))
			{   	 
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.UP))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.up_out;
			if(region.isMine(location.x,location.y))
			{   	 
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.DOWN))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.down_out;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_LEFT))
		{		
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_left_diag_left;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT_DOWN))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_left_diag_down;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_left_diag_center;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_right_diag_right;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT_DOWN))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_right_diag_down;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_down_right_diag_center;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_left_diag_left;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT_UP))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_left_diag_up;
			if(region.isMine(location.x,location.y))
			{   	
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_left_diag_center;
			if(region.isMine(location.x,location.y))
			{   
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_right_diag_right;
			if(region.isMine(location.x,location.y))
			{   	
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT_UP))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_right_diag_up;
			if(region.isMine(location.x,location.y))
			{   	
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}

		if(md.getPositionGood().get(MyCellInterface.CORNER_DIAG_UP_RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> region = rmap.corner_out_up_right_diag_center;
			if(region.isMine(location.x,location.y))
			{   	
				outAgents.put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
				return  region.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
			}
		}


		if(rmap.corner_mine_up_left.isMine(location.x,location.y))
		{   	 
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.corner_mine_up_left.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.up_mine.isMine(location.x,location.y))
		{   	 
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.up_mine.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.corner_mine_up_right.isMine(location.x,location.y))
		{   	
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.corner_mine_up_right.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.right_mine.isMine(location.x,location.y))
		{   	 
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.right_mine.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.corner_mine_down_right.isMine(location.x,location.y))
		{   	
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.corner_mine_down_right.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.down_mine.isMine(location.x,location.y))
		{   	
			ms.getMyField().addEntryNum(new EntryNum<Integer,Int2D>(value, location));
			return  rmap.down_mine.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.corner_mine_down_left.isMine(location.x,location.y))
		{   	
			ms.getMyField().put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
			return  rmap.corner_mine_down_left.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		if(rmap.left_mine.isMine(location.x,location.y))
		{   	
			ms.getMyField().put(location.toString(),new EntryNum<Integer,Int2D>(value, location));
			return  rmap.left_mine.addEntryNum(new EntryNum<Integer,Int2D>(value, location));	
		}

		return false;
	}

	private void removeValue(MyCellInterface md)
	{
		MyCellIntegerNumeric mc = (MyCellIntegerNumeric)md;
		RegionMapNumeric<Integer, EntryNum<Integer,Int2D>> rmap =(RegionMapNumeric<Integer, EntryNum<Integer,Int2D>>) md.getMyRMap();

		for(EntryNum<Integer, Int2D> e: ((MyCellIntegerNumeric)md).getMyField().values())
		{
			Int2D loc=e.l;
			field[loc.getX()][loc.getY()]=0;
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.UP) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT) ||
				(mc.getPosition() == MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o1 = rmap.corner_out_up_left_diag_center;
			for (int i = r_o1.getUpl_yy(); i < r_o1.getDown_yy(); i++) {
				for (int j = r_o1.upl_xx; j < r_o1.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o2 = rmap.corner_out_up_left_diag_left;
			for (int i = r_o2.getUpl_yy(); i < r_o2.getDown_yy(); i++) {
				for (int j = r_o2.upl_xx; j < r_o2.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o3 = rmap.corner_out_up_left_diag_up;
			for (int i = r_o3.getUpl_yy(); i < r_o3.getDown_yy(); i++) {
				for (int j = r_o3.upl_xx; j < r_o3.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.corner_mine_up_left;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.UP) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT) ||
				(mc.getPosition() == MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o = rmap.up_out;
			for (int i = r_o.getUpl_yy(); i < r_o.getDown_yy(); i++) {
				for (int j = r_o.upl_xx; j < r_o.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.up_mine;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.UP) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o1 = rmap.corner_out_up_right_diag_center;
			for (int i = r_o1.getUpl_yy(); i < r_o1.getDown_yy(); i++) {
				for (int j = r_o1.upl_xx; j < r_o1.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o2 = rmap.corner_out_up_right_diag_right;
			for (int i = r_o2.getUpl_yy(); i < r_o2.getDown_yy(); i++) {
				for (int j = r_o2.upl_xx; j < r_o2.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o3 = rmap.corner_out_up_right_diag_up;
			for (int i = r_o3.getUpl_yy(); i < r_o3.getDown_yy(); i++) {
				for (int j = r_o3.upl_xx; j < r_o3.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.corner_mine_up_right;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.UP) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.DOWN) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o = rmap.right_out;
			for (int i = r_o.getUpl_yy(); i < r_o.getDown_yy(); i++) {
				for (int j = r_o.upl_xx; j < r_o.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.right_mine;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.DOWN) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o1 = rmap.corner_out_down_right_diag_center;
			for (int i = r_o1.getUpl_yy(); i < r_o1.getDown_yy(); i++) {
				for (int j = r_o1.upl_xx; j < r_o1.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o2 = rmap.corner_out_down_right_diag_right;
			for (int i = r_o2.getUpl_yy(); i < r_o2.getDown_yy(); i++) {
				for (int j = r_o2.upl_xx; j < r_o2.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o3 = rmap.corner_out_down_right_diag_down;
			for (int i = r_o3.getUpl_yy(); i < r_o3.getDown_yy(); i++) {
				for (int j = r_o3.upl_xx; j < r_o3.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.corner_mine_down_right;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.DOWN) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT) ||
				(mc.getPosition() == MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o = rmap.down_out;
			for (int i = r_o.getUpl_yy(); i < r_o.getDown_yy(); i++) {
				for (int j = r_o.upl_xx; j < r_o.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.down_mine;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.DOWN) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT) ||
				(mc.getPosition() == MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o1 = rmap.corner_out_down_left_diag_center;
			for (int i = r_o1.getUpl_yy(); i < r_o1.getDown_yy(); i++) {
				for (int j = r_o1.upl_xx; j < r_o1.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o2 = rmap.corner_out_down_left_diag_left;
			for (int i = r_o2.getUpl_yy(); i < r_o2.getDown_yy(); i++) {
				for (int j = r_o2.upl_xx; j < r_o2.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o3 = rmap.corner_out_down_left_diag_down;
			for (int i = r_o3.getUpl_yy(); i < r_o3.getDown_yy(); i++) {
				for (int j = r_o3.upl_xx; j < r_o3.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.corner_mine_down_left;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
		
		if((mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_LEFT) ||
				(mc.getPosition() == MyCellInterface.UP) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_UP_RIGHT) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_RIGHT) ||
				(mc.getPosition() == MyCellInterface.DOWN) ||
				(mc.getPosition() == MyCellInterface.CORNER_DIAG_DOWN_LEFT) ||
				(mc.getPosition() == MyCellInterface.LEFT))
		{
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_o = rmap.left_out;
			for (int i = r_o.getUpl_yy(); i < r_o.getDown_yy(); i++) {
				for (int j = r_o.upl_xx; j < r_o.down_xx; j++) {
					field[j][i]=0;
				}
			}
			RegionNumeric<Integer,EntryNum<Integer,Int2D>> r_m = rmap.left_mine;
			for (int i = r_m.getUpl_yy(); i < r_m.getDown_yy(); i++) {
				for (int j = r_m.upl_xx; j < r_m.down_xx; j++) {
					field[j][i]=0;
				}
			}
		}
	}
	
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
				MyCellIntegerNumeric md = (MyCellIntegerNumeric) hm.get(ct);
				
	    		
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
				    		RegionNumeric<Integer,EntryNum<Integer,Int2D>> region=((RegionNumeric<Integer,EntryNum<Integer,Int2D>>)returnValue);
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
		return numAgents;
	}

	@Override
	public void resetParameters() {
		numAgents=0;
		
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
		return globals;
	}
	@Override
	public boolean verifyPosition(Int2D pos) {
		
		//we have to implement this
		return false;

	}
}
