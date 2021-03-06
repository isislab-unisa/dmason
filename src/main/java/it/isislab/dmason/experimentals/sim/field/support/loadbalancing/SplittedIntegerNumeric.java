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

package it.isislab.dmason.experimentals.sim.field.support.loadbalancing;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionIntegerNumeric;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMapNumeric;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 * This Class is for DInt2D value.
 * For many simulation, we see that this type of field is used for to simulate part of the simulation that don't contains agents.
 * It's used for generate the cell, which make up the field.  
 *
 */
public class SplittedIntegerNumeric implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	private final int costantDivide=3;//used to divide everycell in a 3x3
	public int NUMPEERS;
	public int own_x;	//x coordinate of north-west corner
	public int own_y;	//y coordinate of north-west corner
	public int my_width;
	public int my_height;
	public RegionIntegerNumeric myfield;
	public RegionMapNumeric<Integer, EntryNum<Integer,Int2D>> rmap=new RegionMapNumeric<Integer, EntryNum<Integer,Int2D>>();
	public int MAX_DISTANCE;
	public CellType cellType;
	public CellType parentType;
    public SimState sm ;
	public int superOwnX;
	public int superOwnY;
	public int TOTALWIDTH; // grandezza della simulazione
	public int TOTALHEIGHT;// grandezza della simulazione
	private int SUPERWIDTH;
	private int SUPERHEIGHT;
	private String NAME;
	private int POSITION;
	
	private HashMap<Integer, Boolean> topics;
	private HashMap<Integer, Boolean> positionGood;
	
	/**
	 *Constructor of class with parameters: 
	 *
	 * @param superOwnX is the X coordinate of up left corner of cell that must divide
	 * @param superOwnY is the Y coordinate of up left corner of cell that must divide
	 * @param superWidth is the Width of cell that must divide
	 * @param superHeight is the Height of cell that must divide
	 * @param state the distribute state of simulation
	 * @param mAX_DISTANCE is the max distance for the communication between two or more cells.
	 * @param i is the first number that identifies the cell  
	 * @param j is the second number that identifies the cell
	 * @param numPeers number of peers
	 * @param name is the name of field
	 * @param position if the position of cell into piece of simulation that show
	 * @param parentType is the number of the cell, that divides
	 * @param topics is the list of topic that the single cell must have for the communication
	 * @param positionGood is the list of all side that can send the exact updates
	 */
	public SplittedIntegerNumeric(int superOwnX, int superOwnY, int superWidth,
			int superHeight, DistributedState state, int mAX_DISTANCE,int i, int j,
				int numPeers, String name, int position, CellType parentType, 
					HashMap<Integer, Boolean> topics, HashMap<Integer, Boolean> positionGood) 
	{
		this.superOwnX = superOwnX ;
		this.superOwnY = superOwnY;
		this.SUPERWIDTH = superWidth;
		this.SUPERHEIGHT = superHeight;
		this.sm = state;
		this.MAX_DISTANCE = mAX_DISTANCE;
		this.NUMPEERS = numPeers;	
		this.TOTALWIDTH = superWidth * (int)Math.sqrt(NUMPEERS);
		this.TOTALHEIGHT = superHeight* (int)Math.sqrt(NUMPEERS);
		cellType = new CellType(i, j);
		this.parentType = parentType;
		this.NAME = name;
		this.POSITION = position;
		this.topics = topics;
		this.positionGood = positionGood;
		SplitRegionInteger();
	}
	/**
	 * Create region of Integer value
	 * Calculate the right points for any cell
	 */
	private void SplitRegionInteger() 
	{
		// own width and height
		my_width=SUPERWIDTH/costantDivide;
		my_height=SUPERHEIGHT /costantDivide;
		
		if(cellType.pos_i == cellType.pos_j){
			if(cellType.pos_i == 0){
				own_x= superOwnX;
				own_y= superOwnY;
			}else{
				own_x = superOwnX+(my_width*cellType.pos_j);
				own_y = superOwnY+(my_height*cellType.pos_i);
			}
		}
		if(cellType.pos_i > cellType.pos_j){
			own_x = superOwnX+(my_width*cellType.pos_j);
			own_y = superOwnY +(my_height * cellType.pos_i);		
		}
		
		if(cellType.pos_i < cellType.pos_j){
			own_x = superOwnX + (my_width * cellType.pos_j);
			own_y = superOwnY + (my_height*cellType.pos_i);
		}
		
		//my field
	      myfield=	new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
			own_y+my_height-MAX_DISTANCE);

	      
		//corner mine up left
		rmap.NORTH_WEST_MINE=new RegionIntegerNumeric(own_x, own_y,own_x+MAX_DISTANCE, 
				own_y+MAX_DISTANCE);	
		
		
		// nord ovest 		
		rmap.NORTH_WEST_OUT=new RegionIntegerNumeric((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_up_left_diag_up=new RegionIntegerNumeric((own_x + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, ((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_up_left_diag_left=new RegionIntegerNumeric((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT);
		
		
		//corner mine up right		
		rmap.NORTH_EAST_MINE=new RegionIntegerNumeric(own_x+my_width-MAX_DISTANCE, own_y, 
				own_x+my_width, own_y+MAX_DISTANCE);
		
		
		//corner out up right
		rmap.NORTH_EAST_OUT = new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, (own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_up_right_diag_up=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_up_right_diag_right=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT);
		
		
		// corner mine down left
		rmap.SOUTH_WEST_MINE=new RegionIntegerNumeric(own_x, own_y+my_height-MAX_DISTANCE,
				own_x+MAX_DISTANCE, own_y+my_height);
		
		
		//corner out down left
		rmap.SOUTH_WEST_OUT=new RegionIntegerNumeric((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_down_left_diag_down=new RegionIntegerNumeric((own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH+MAX_DISTANCE)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT);
		rmap.corner_out_down_left_diag_left=new RegionIntegerNumeric((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT);

		
		//corner mine down right
		rmap.SOUTH_EAST_MINE=new RegionIntegerNumeric(own_x+my_width-MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,own_x+my_width,own_y+my_height);		
		
		
		//corner out down right
		rmap.SOUTH_EAST_OUT=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT);
		rmap.corner_out_down_right_diag_down=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT); 
		rmap.corner_out_down_right_diag_right=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH
				+MAX_DISTANCE)%TOTALWIDTH,(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT);

		
		//mine left		
		rmap.WEST_MINE=new RegionIntegerNumeric(own_x,own_y+MAX_DISTANCE,own_x + MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE);//,SUPERWIDTH-2*MAX_DISTANCE,SUPERHEIGHT-2*MAX_DISTANCE);
		//out left
		rmap.WEST_OUT=new RegionIntegerNumeric((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(((own_y+my_height)+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT);//,SUPERWIDTH-2*MAX_DISTANCE,	SUPERHEIGHT-2*MAX_DISTANCE);
		
		
		
		//mine right
		rmap.EAST_MINE=new RegionIntegerNumeric(own_x + my_width - MAX_DISTANCE,own_y+MAX_DISTANCE,
				own_x +my_width , own_y+my_height-MAX_DISTANCE);
				//,SUPERWIDTH-2*MAX_DISTANCE,
				//SUPERHEIGHT-2*MAX_DISTANCE);
		//out right
		rmap.EAST_OUT=new RegionIntegerNumeric((own_x+my_width+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, ((own_y+my_height+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT);
				//SUPERWIDTH-2*MAX_DISTANCE,SUPERHEIGHT-2*MAX_DISTANCE);		

		
		//mine up
		rmap.NORTH_MINE=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y,own_x+my_width-MAX_DISTANCE, 
				own_y + MAX_DISTANCE); //,SUPERWIDTH-2*MAX_DISTANCE,SUPERHEIGHT-2*MAX_DISTANCE);
		//out up
		rmap.NORTH_OUT=new RegionIntegerNumeric(((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y - MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,((own_x+ my_width +TOTALWIDTH)-MAX_DISTANCE)
				%TOTALWIDTH,(own_y+TOTALHEIGHT)%TOTALHEIGHT);
				//,SUPERWIDTH-2*MAX_DISTANCE,
				//SUPERHEIGHT-2*MAX_DISTANCE);		
		
		
		//mine down
		rmap.SOUTH_MINE=new RegionIntegerNumeric(own_x+MAX_DISTANCE,own_y+my_height-MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
				(own_y+my_height));//,SUPERWIDTH-2*MAX_DISTANCE),SUPERHEIGHT-2*MAX_DISTANCE);
		//out down
		rmap.SOUTH_OUT=new RegionIntegerNumeric( ((own_x+TOTALWIDTH)+MAX_DISTANCE) %TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,
				((own_x+my_width+TOTALWIDTH)-MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT);
				//,SUPERWIDTH-2*MAX_DISTANCE,SUPERHEIGHT-2*MAX_DISTANCE);		
	}
	
	//only for testing...used for print region created... stipa ca truov
	private void stampa(){	
		
	Class o=this.rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
		
		System.out.println("MYFIELD "+cellType.toString()+": "+myfield.toString());
		
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			
			try
			{
				String name=fields[z].getName();
		    	Method method = o.getMethod("get"+name, null);
		    	Object returnValue = method.invoke(rmap, null);   	
		    	System.out.println("name="+name +" "+ returnValue+"TTH "+TOTALHEIGHT+"TTW"+TOTALWIDTH+"owny+my_heit="+(own_y+my_height));
		    	
		    
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}	
	}

	/**
	 * Return an MyCellForInteger that is a sub cell of cell that divides
	 * 
	 * @return an MyCellInteger
	 */
	public MyCellIntegerNumeric createMyDivision()
	{
		if((topics != null) && (positionGood != null))
			return new MyCellIntegerNumeric(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, topics, positionGood, POSITION);
		else
			return new MyCellIntegerNumeric(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, POSITION);
	}

}