package it.isislab.dmason.experimentals.sim.field.grid.object;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.grid.object.DistributedRegionObject;
import it.isislab.dmason.experimentals.sim.field.grid.object.EntryObject;
import it.isislab.dmason.experimentals.sim.field.grid.object.RegionObject;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import sim.engine.SimState;
import sim.util.Int2D;

public class DObjectGrid2DXY extends DObjectGrid2D {

	private static final long serialVersionUID = 1L;

	/**
	 * It's the name of the specific field
	 */
	private String name;


	/** Number of neighbors of this cell, that is also the number of regions to create and of topics to publish/subscribe */ 
	protected int numNeighbors;


	private String topicPrefix = "";

	private int width,height;



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
	public DObjectGrid2DXY(int width, int height,SimState sm,int max_distance,int i,int j,int rows,int columns, 
			Object initialGridValue, String name, String prefix, boolean isToroidal) 
	{		
		super(width, height, initialGridValue);
		this.width=width;
		this.height=height;
		this.sm=sm;
		this.AOI=max_distance;
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.updates_cache = new ArrayList<RegionObject>();
		this.name = name;
		this.topicPrefix=prefix;		

		setToroidal(isToroidal);		
		createRegions();	

	}
	public DObjectGrid2DXY(int width, int height,SimState sm,int max_distance,int i,int j,int rows,int columns, 
			 String name, String prefix, boolean isToroidal) 
	{		
		super(width, height);
		this.width=width;
		this.height=height;
		this.sm=sm;
		this.AOI=max_distance;
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.updates_cache = new ArrayList<RegionObject>();
		this.name = name;
		this.topicPrefix=prefix;		

		setToroidal(isToroidal);		
		createRegions();	

	}


	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegions()
	{
		//upper left corner's coordinates
		if(cellType.pos_j<(width%columns))
			own_x=(int)Math.floor(width/columns+1)*cellType.pos_j; 
		else
			own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(cellType.pos_j-((width%columns))); 

		if(cellType.pos_i<(height%rows))
			own_y=(int)Math.floor(height/rows+1)*cellType.pos_i; 
		else
			own_y=(int)Math.floor(height/rows+1)*((height%rows))+(int)Math.floor(height/rows)*(cellType.pos_i-((height%rows))); 

		// own width and height
		if(cellType.pos_j<(width%columns))
			my_width=(int) Math.floor(width/columns+1);
		else
			my_width=(int) Math.floor(width/columns);

		if(cellType.pos_i<(height%rows))
			my_height=(int) Math.floor(height/rows+1);
		else
			my_height=(int) Math.floor(height/rows);


		//calculating the neighbors
		for (int k = -1; k <= 1; k++) 
		{
			for (int k2 = -1; k2 <= 1; k2++) 
			{				
				int v1=cellType.pos_i+k;
				int v2=cellType.pos_j+k2;
				if(v1>=0 && v2 >=0 && v1<rows && v2<columns)
					if( v1!=cellType.pos_i || v2!=cellType.pos_j)
					{
						neighborhood.add(v1+""+v2);
					}	
			}
		}


		if(isToroidal()) 
			makeToroidalSections();
		else
			makeNoToroidalSections();

		return true;
	}


	private void makeNoToroidalSections() {

		myfield=new RegionObject(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);

		//corner up left

		rmap.NORTH_WEST_MINE=new RegionObject(own_x, own_y, own_x+AOI, own_y+AOI);

		//corner up right

		rmap.NORTH_EAST_MINE=new RegionObject(own_x+my_width-AOI, own_y, own_x+my_width, own_y+AOI);

		//corner down left

		rmap.SOUTH_WEST_MINE=new RegionObject(own_x, own_y+my_height-AOI,own_x+AOI, own_y+my_height);

		//corner down right

		rmap.SOUTH_EAST_MINE=new RegionObject(own_x+my_width-AOI, own_y+my_height-AOI,own_x+my_width,own_y+my_height);

		rmap.WEST_MINE=new RegionObject(own_x,own_y,own_x + AOI , own_y+my_height);


		rmap.EAST_MINE=new RegionObject(own_x + my_width - AOI,own_y,own_x +my_width , own_y+my_height);


		rmap.NORTH_MINE=new RegionObject(own_x ,own_y,own_x+my_width, own_y + AOI);


		rmap.SOUTH_MINE=new RegionObject(own_x,own_y+my_height-AOI,own_x+my_width, (own_y+my_height));

		//horizontal partitioning
		//horizontal partitioning
		if(rows==1){
			numNeighbors = 2;
			if(cellType.pos_j>0 && cellType.pos_j<columns-1){

				rmap.WEST_OUT=new RegionObject(own_x-AOI,own_y,own_x, own_y+my_height);

				rmap.EAST_OUT=new RegionObject(own_x+my_width,own_y,own_x+my_width,own_y+my_height);
			}

			else if(cellType.pos_j==0){
				numNeighbors = 1;
				rmap.EAST_OUT=new RegionObject(own_x+my_width,own_y,own_x+my_width+AOI,own_y+my_height);
			}	


			else if(cellType.pos_j==columns-1){
				numNeighbors = 1;
				rmap.WEST_OUT=new RegionObject(own_x-AOI,own_y,own_x, own_y+my_height);
			}
		}else 
			if(rows>1 && columns == 1){ // Horizontal partitionig
				numNeighbors =2;
				rmap.NORTH_OUT=new RegionObject(own_x, own_y - AOI,	own_x+ my_width,own_y);
				rmap.SOUTH_OUT=new RegionObject(own_x,own_y+my_height,own_x+my_width, own_y+my_height+AOI);
				if(cellType.pos_i == 0){
					numNeighbors =1;
					rmap.NORTH_OUT = null;
				}
				if(cellType.pos_i == rows-1){
					numNeighbors =1;
					rmap.SOUTH_OUT= null;
				}
			}else{ //sqare partitioning 

				/*
				 * In this case we use a different approach: Firt we make all ghost sections, after that
				 * we remove the useful ghost section
				 * 
				 * */
				numNeighbors = 8;
				//corner up left
				rmap.NORTH_WEST_OUT=new RegionObject(own_x-AOI, own_y-AOI,own_x, own_y);


				//corner up right
				rmap.NORTH_EAST_OUT = new RegionObject(own_x+my_width,own_y-AOI,own_x+my_width+AOI,own_y);


				//corner down left
				rmap.SOUTH_WEST_OUT=new RegionObject(own_x-AOI, own_y+my_height,own_x,own_y+my_height+AOI);

				rmap.NORTH_OUT=new RegionObject(own_x, own_y - AOI,	own_x+ my_width,own_y);

				//corner down right
				rmap.SOUTH_EAST_OUT=new RegionObject(own_x+my_width, own_y+my_height,own_x+my_width+AOI,own_y+my_height+AOI);

				rmap.SOUTH_OUT=new RegionObject(own_x,own_y+my_height,own_x+my_width, own_y+my_height+AOI);

				rmap.WEST_OUT=new RegionObject(own_x-AOI,own_y,own_x, own_y+my_height);


				rmap.EAST_OUT=new RegionObject(own_x+my_width,own_y,own_x+my_width+AOI,own_y+my_height);

				if(cellType.pos_i==0 ){
					numNeighbors = 5;
					rmap.NORTH_OUT = null;
					rmap.NORTH_WEST_OUT = null;
					rmap.NORTH_EAST_OUT = null;
				}

				if(cellType.pos_j == 0){
					numNeighbors = 5;
					rmap.SOUTH_WEST_OUT = null;
					rmap.NORTH_WEST_OUT=null;
					rmap.WEST_OUT = null;
				}

				if(cellType.pos_i == rows -1){
					numNeighbors = 5;
					rmap.SOUTH_WEST_OUT = null;
					rmap.SOUTH_OUT = null;
					rmap.SOUTH_EAST_OUT = null;
				}

				if(cellType.pos_j == columns -1){
					numNeighbors = 5;
					rmap.NORTH_EAST_OUT = null;
					rmap.EAST_OUT = null;
					rmap.SOUTH_EAST_OUT = null;
				}

				if((cellType.pos_i == 0 && cellType.pos_j == 0) || 
						(cellType.pos_i == rows-1 && cellType.pos_j==0) || 
						(cellType.pos_i == 0 && cellType.pos_j == columns -1) || 
						(cellType.pos_i == rows-1 && cellType.pos_j == columns -1))
					numNeighbors = 3;
			}
	}

	private void makeToroidalSections() {

		numNeighbors = 8;
		myfield=new RegionObject(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);


		//corner up left
		rmap.NORTH_WEST_OUT=new RegionObject((own_x-AOI + width)%width, (own_y-AOI+height)%height, 
				(own_x+width)%width==0?width:(own_x+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_WEST_MINE=new RegionObject(own_x, own_y, own_x+AOI, own_y+AOI);

		//corner up right
		rmap.NORTH_EAST_OUT = new RegionObject((own_x+my_width+width)%width, (own_y-AOI+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+height)%height==0?height:(own_y+height)%height);
		rmap.NORTH_EAST_MINE=new RegionObject(own_x+my_width-AOI, own_y, own_x+my_width, own_y+AOI);

		//corner down left
		rmap.SOUTH_WEST_OUT=new RegionObject((own_x-AOI+width)%width, (own_y+my_height+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_WEST_MINE=new RegionObject(own_x, own_y+my_height-AOI,own_x+AOI, own_y+my_height);

		//corner down right
		rmap.SOUTH_EAST_OUT=new RegionObject((own_x+my_width+width)%width, (own_y+my_height+height)%height, 
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_EAST_MINE=new RegionObject(own_x+my_width-AOI, own_y+my_height-AOI,own_x+my_width,own_y+my_height);

		rmap.WEST_OUT=new RegionObject((own_x-AOI+width)%width,(own_y+height)%height,
				(own_x+width)%width==0?width:(own_x+width)%width, ((own_y+my_height)+height)%height==0?height:((own_y+my_height)+height)%height);
		rmap.WEST_MINE=new RegionObject(own_x,own_y,own_x + AOI , own_y+my_height);

		rmap.EAST_OUT=new RegionObject((own_x+my_width+width)%width,(own_y+height)%height,
				(own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width, (own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height);
		rmap.EAST_MINE=new RegionObject(own_x + my_width - AOI,own_y,own_x +my_width , own_y+my_height);


		rmap.NORTH_MINE=new RegionObject(own_x ,own_y,own_x+my_width, own_y + AOI);


		rmap.SOUTH_MINE=new RegionObject(own_x,own_y+my_height-AOI,own_x+my_width, (own_y+my_height));

		rmap.NORTH_OUT=new RegionObject((own_x+width)%width, (own_y - AOI+height)%height,
				(own_x+ my_width +width)%width==0?width:(own_x+ my_width +width)%width,(own_y+height)%height==0?height:(own_y+height)%height);

		rmap.SOUTH_OUT=new RegionObject((own_x+width)%width,(own_y+my_height+height)%height,
				(own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width, (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height);

		//if square partitioning
		if(rows==1 && columns >1){
			numNeighbors = 6;
			rmap.NORTH_OUT = null;
			rmap.SOUTH_OUT = null;
		}
		else if(rows > 1 && columns == 1){
			numNeighbors = 6;
			rmap.EAST_OUT = null;
			rmap.WEST_OUT = null;
		}
	}

	@Override
	public synchronized boolean synchro() {

		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();

		
		if(this.getState().schedule.getSteps() !=0){

			clear_ghost_regions();
			memorizeRegionOut();


			//every value in the myfield region is setted
			for(EntryObject<Int2D> e: myfield.values())
			{			
				Int2D loc=e.l;
				Object i = e.r;
				this.field[loc.getX()][loc.getY()]=i;	
				
			}     
			
		}


		//--> publishing the regions to correspondent topics for the neighbors	
		publishRegions(connWorker);

		processUpdates();
		
		return true;
	}


	private void processUpdates() {
		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, numNeighbors);
			while(!q.isEmpty())
			{
				DistributedRegionObject region =
						(DistributedRegionObject)q.poll();
				verifyUpdates(region);	
			}			

		}catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(RegionObject region : updates_cache){
			for(EntryObject<Int2D> e_m: region.values())
			{
				Int2D i=new Int2D(e_m.l.getX(), e_m.l.getY());
				field[i.getX()][i.getY()]=e_m.r;	
			}
		}	

		this.reset();
	}


	private void publishRegions(Connection connWorker) {
		//--> publishing the regions to correspondent topics for the neighbors
		if(rmap.WEST_OUT!=null)
		{
			DistributedRegionObject dr =
					new DistributedRegionObject
			(rmap.WEST_MINE,rmap.WEST_OUT, (sm.schedule.getSteps()-1),
					cellType,DistributedRegionObject.WEST);
			try 
			{				
				connWorker.publishToTopic(dr,topicPrefix+cellType+"L", name);

			} catch (Exception e1) { e1.printStackTrace();}
		}

		if(rmap.EAST_OUT!=null)
		{
			DistributedRegionObject dr = 
					new DistributedRegionObject
			(rmap.EAST_MINE,rmap.EAST_OUT,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionObject.EAST);	
			try 
			{				
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"R", name);

			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.NORTH_OUT!=null )
		{
			DistributedRegionObject dr = 
					new  DistributedRegionObject
			(rmap.NORTH_MINE,rmap.NORTH_OUT,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionObject.NORTH);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"U", name);

			} catch (Exception e1) {e1.printStackTrace();}
		}

		if(rmap.SOUTH_OUT!=null )
		{
			DistributedRegionObject dr =
					new DistributedRegionObject
			(rmap.SOUTH_MINE,rmap.SOUTH_OUT,(sm.schedule.getSteps()-1),
					cellType,DistributedRegionObject.SOUTH);

			try 
			{				
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"D", name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}

		if(rmap.NORTH_WEST_OUT!=null)
		{
			DistributedRegionObject dr = 
					new DistributedRegionObject
			(rmap.NORTH_WEST_MINE,rmap.NORTH_WEST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionObject.NORTH_WEST);

			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CUDL", name);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.NORTH_EAST_OUT!=null)
		{
			DistributedRegionObject dr = 
					new DistributedRegionObject	
			(rmap.NORTH_EAST_MINE,rmap.NORTH_EAST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionObject.NORTH_EAST);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CUDR", name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.SOUTH_WEST_OUT!=null)
		{
			DistributedRegionObject dr = 
					new DistributedRegionObject
			(rmap.SOUTH_WEST_MINE, rmap.SOUTH_WEST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionObject.SOUTH_WEST);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CDDL", name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.SOUTH_EAST_OUT!=null)
		{
			DistributedRegionObject dr = 
					new DistributedRegionObject
			(rmap.SOUTH_EAST_MINE,rmap.SOUTH_EAST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegionObject.SOUTH_EAST);
			try 
			{
				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"CDDR", name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}	
		//<--
	}


	private void clear_ghost_regions() {
		updateFields(); //update fields with java reflect
		updates_cache= new ArrayList<RegionObject>();
	}



	/**
	 * Provide the int value shift logic among the peers
	 * @param d
	 * @param l
	 * @param sm
	 * @return
	 */
	public boolean setDistributedObjectLocation( Int2D l, Object remoteValue ,SimState sm) throws DMasonException{


		//if(!(remoteValue instanceof Integer))
			//throw new DMasonException("Cast Exception setDistributedObjectLocation, second parameter must be a int");

		//int d = (Integer) remoteValue;

		if(setValue(remoteValue, l)) return true;
		else{
			String errorMessage = String.format("Unable to set value on position (%d, %d): out of boundaries on cell %s. (ex OH MY GOD!)",
					l.x, l.y, cellType);

			System.err.println( errorMessage ); // it should never happen (don't tell it to anyone shhhhhhhh! ;P) // it should never happen (don't tell it to anyone shhhhhhhh! ;P)
		}
		return false;
	}



	@Override
	public DistributedState getState() {

		return (DistributedState)sm;
	}

	/**
	 * This method, written with Java Reflect, provides to add the value
	 * in the right Region.
	 * @param value The value to add
	 * @param l The new location of the value
	 * @return true if the value is added in right way
	 */
	private boolean setValue(Object value, Int2D l){


		if(rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(l.x,l.y))
		{
			
			rmap.NORTH_WEST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
			rmap.WEST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
			myfield.addEntryObject(new EntryObject<Int2D>(value, l));
			return rmap.NORTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
		}
		else
			if(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(l.x,l.y))
			{
				
				rmap.NORTH_EAST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
				rmap.EAST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
				myfield.addEntryObject(new EntryObject<Int2D>(value, l));
				return rmap.NORTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
			}
			else
				if(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(l.x,l.y))
				{
					
					rmap.SOUTH_WEST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
					rmap.WEST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
					myfield.addEntryObject(new EntryObject<Int2D>(value, l));
					return rmap.SOUTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
				}
				else
					if(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(l.x,l.y))
					{
						
						rmap.SOUTH_EAST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
						rmap.EAST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
						myfield.addEntryObject(new EntryObject<Int2D>(value, l));
						return rmap.SOUTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
					}
					else
						if(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(l.x,l.y))
						{
							
							myfield.addEntryObject(new EntryObject<Int2D>(value, l));
							return rmap.WEST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
						}
						else
							if(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(l.x,l.y))
							{
								
								myfield.addEntryObject(new EntryObject<Int2D>(value, l));
								return rmap.EAST_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
							}
							else
								if(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(l.x,l.y))
								{
									
									myfield.addEntryObject(new EntryObject<Int2D>(value, l));
									return rmap.NORTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
								}
								else
									if(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(l.x,l.y))
									{
										
										myfield.addEntryObject(new EntryObject<Int2D>(value, l));
										return rmap.SOUTH_MINE.addEntryObject(new EntryObject<Int2D>(value, l));
									}
									else
										if(myfield.isMine(l.x,l.y))
										{
											
											return myfield.addEntryObject(new EntryObject<Int2D>(value, l));
										}
										else
											if(rmap.WEST_OUT!=null && rmap.WEST_OUT.isMine(l.x,l.y)) 
												return rmap.WEST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
											else
												if(rmap.EAST_OUT!=null && rmap.EAST_OUT.isMine(l.x,l.y)) 
													return rmap.EAST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
												else
													if(rmap.NORTH_OUT!=null && rmap.NORTH_OUT.isMine(l.x,l.y))
														return rmap.NORTH_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
													else
														if(rmap.SOUTH_OUT!=null && rmap.SOUTH_OUT.isMine(l.x,l.y))
															return rmap.SOUTH_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
														else
															if(rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(l.x,l.y)) 
																return rmap.NORTH_WEST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
															else 
																if(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(l.x,l.y)) 
																	return rmap.SOUTH_WEST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
																else
																	if(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(l.x,l.y)) 
																		return rmap.NORTH_EAST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));
																	else
																		if(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(l.x,l.y))
																			return rmap.SOUTH_EAST_OUT.addEntryObject(new EntryObject<Int2D>(value, l));


		return false;	       			       			
	}


	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the value's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the value's location is updated and the value is setted.
	 */
	public void updateFields()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					RegionObject region=((RegionObject)returnValue);

					if(name.contains("OUT"))
					{
						for(EntryObject<Int2D> e : region.values()){

							Int2D pos = new Int2D(e.l.getX(), e.l.getY());
							Object i = e.r;
							this.field[pos.getX()][pos.getY()]=i;
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


	public void memorizeRegionOut()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					RegionObject region=((RegionObject)returnValue);
					if(name.contains("OUT"))
					{

						updates_cache.add(region.clone());
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
	 * This method takes updates from box and set every value in the regions out.
	 * Every value in the regions mine is compared with every value in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegionObject box)
	{
		RegionObject r_mine=box.out;
		RegionObject r_out=box.mine;

		for(EntryObject<Int2D> e_m: r_mine.values())
		{
			Int2D i=new Int2D(e_m.l.getX(),e_m.l.getY());

			field[i.getX()][i.getY()]=e_m.r;		  		
		}		
		updates_cache.add(r_out);
	}

	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	public  boolean reset()
	{
		myfield.clear();

		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);

				if(returnValue!=null)
				{
					RegionObject region=((RegionObject)returnValue);
					region.clear();    
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace(); return false;} 
			catch (IllegalAccessException e) {e.printStackTrace();return false;} 
			catch (SecurityException e) {e.printStackTrace();return false;} 
			catch (NoSuchMethodException e) {e.printStackTrace();return false;} 
			catch (InvocationTargetException e) {e.printStackTrace();return false;}
		}
		return true;
	}

	@Override
	public Int2D getAvailableRandomLocation() {
	
		
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();

		int x= (int)(own_x+my_width*shiftx);	
		int y= (int)(own_y+my_height*shifty);  

		return (new Int2D(x, y));
	}



	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}




	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}





	@Override
	public VisualizationUpdateMap<String, Object> getGlobals(){	return null;}
	
	@Override
	public boolean verifyPosition(Int2D pos) {

		return (rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(pos.x,pos.y))||

				(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(pos.x,pos.y))
				||
				(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(pos.x,pos.y))
				||(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(pos.x,pos.y))
				||(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(pos.x,pos.y))
				||(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(pos.x,pos.y))
				||(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(pos.x,pos.y))
				||(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(pos.x,pos.y))
				||(myfield.isMine(pos.x,pos.y));

	}


	@Override
	public String getDistributedFieldID() {

		return name;
	}


	@Override
	public boolean createRegions(QuadTree... cell) {
		// TODO Auto-generated method stub
		return false;
	}

}
