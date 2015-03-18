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

package it.isislab.dmason.sim.field.support.loadbalancing;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.grid.numeric.region.RegionDoubleNumericLB;
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
 * This Class is for Double value.
 * For many simulation, we see that this type of field is used for to simulate part of the simulation that don't contains agents.
 * It's used for generate the cell, which make up the field.  
 *
 */
public class SplittedDoubleNumeric implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int costantDivide=3;//used to divide everycell in a 3x3
	public int NUMPEERS;
	public int own_x;	//x coordinate of north-west corner
	public int own_y;	//y coordinate of north-west corner
	public int my_width;
	public int my_height;
	public RegionDoubleNumericLB myfield;
	public RegionMapNumeric<Integer, EntryNum<Double,Int2D>> rmap=new RegionMapNumeric<Integer, EntryNum<Double,Int2D>>();
	private RegionMapNumeric<Integer, EntryNum<Double,Int2D>> superRmap = null;
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
	 * Create region of double value
	 * Calculate the right points for any cell
	 */
	public SplittedDoubleNumeric(int superOwnX, int superOwnY, int superWidth,
			int superHeight, DistributedState state, int mAX_DISTANCE,int i, int j,
				int numPeers, String name, int position, CellType parentType, 
					HashMap<Integer, Boolean> topics, HashMap<Integer, Boolean> positionGood) 
	{
		this.superOwnX = superOwnX ;
		this.superOwnY = superOwnY;
		this.SUPERWIDTH = superWidth ;
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
		myfield=new RegionDoubleNumericLB(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,TOTALWIDTH,TOTALHEIGHT);

		//corner mine up left
		rmap.corner_mine_up_left=new RegionDoubleNumericLB(own_x, own_y,own_x+MAX_DISTANCE, 
				own_y+MAX_DISTANCE,TOTALWIDTH, TOTALHEIGHT);	
		//corner out up left		
		rmap.corner_out_up_left_diag_center=new RegionDoubleNumericLB((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_left_diag_up=new RegionDoubleNumericLB((own_x + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, ((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_left_diag_left=new RegionDoubleNumericLB((own_x-MAX_DISTANCE + TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		
		//corner mine up right		
		rmap.corner_mine_up_right=new RegionDoubleNumericLB(own_x+my_width-MAX_DISTANCE, own_y, 
				own_x+my_width, own_y+MAX_DISTANCE,TOTALWIDTH, TOTALHEIGHT);
		//corner out up right
		rmap.corner_out_up_right_diag_center = new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT, (own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_right_diag_up=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y-MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_up_right_diag_right=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		
		// corner mine down left
		rmap.corner_mine_down_left=new RegionDoubleNumericLB(own_x, own_y+my_height-MAX_DISTANCE,
				own_x+MAX_DISTANCE, own_y+my_height,TOTALWIDTH, TOTALHEIGHT);
		//corner out down left
		rmap.corner_out_down_left_diag_center=new RegionDoubleNumericLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_left_diag_down=new RegionDoubleNumericLB((own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH+MAX_DISTANCE)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT+MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_left_diag_left=new RegionDoubleNumericLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);

		//corner mine down right
		rmap.corner_mine_down_right=new RegionDoubleNumericLB(own_x+my_width-MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,own_x+my_width,own_y+my_height,TOTALWIDTH,TOTALHEIGHT);		
		//corner out down right
		rmap.corner_out_down_right_diag_center=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT);
		rmap.corner_out_down_right_diag_down=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH-MAX_DISTANCE)
				%TOTALWIDTH, (own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, TOTALHEIGHT); 
		rmap.corner_out_down_right_diag_right=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH
				+MAX_DISTANCE)%TOTALWIDTH,(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH, 
				TOTALHEIGHT);

		//mine left		
		rmap.left_mine=new RegionDoubleNumericLB(own_x,own_y+MAX_DISTANCE,own_x + MAX_DISTANCE, 
				own_y+my_height-MAX_DISTANCE,TOTALWIDTH,TOTALHEIGHT);
		//out left
		rmap.left_out=new RegionDoubleNumericLB((own_x-MAX_DISTANCE+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(((own_y+my_height)+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT,TOTALWIDTH,
				TOTALHEIGHT);
		
		//mine right
		rmap.right_mine=new RegionDoubleNumericLB(own_x + my_width - MAX_DISTANCE,own_y+MAX_DISTANCE,
				own_x +my_width , own_y+my_height-MAX_DISTANCE,TOTALWIDTH,
				TOTALHEIGHT);
		//out right
		rmap.right_out=new RegionDoubleNumericLB((own_x+my_width+TOTALWIDTH)%TOTALWIDTH,
				((own_y+TOTALHEIGHT)+MAX_DISTANCE)%TOTALHEIGHT,(own_x+my_width+MAX_DISTANCE+TOTALWIDTH)
				%TOTALWIDTH, ((own_y+my_height+TOTALHEIGHT)-MAX_DISTANCE)%TOTALHEIGHT,
				TOTALWIDTH,TOTALHEIGHT);		

		//mine up
		rmap.up_mine=new RegionDoubleNumericLB(own_x+MAX_DISTANCE,own_y,own_x+my_width-MAX_DISTANCE, 
				own_y + MAX_DISTANCE ,TOTALWIDTH,TOTALHEIGHT);
		//out up
		rmap.up_out=new RegionDoubleNumericLB(((own_x+TOTALWIDTH)+MAX_DISTANCE)%TOTALWIDTH, 
				(own_y - MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,((own_x+ my_width +TOTALWIDTH)-MAX_DISTANCE)
				%TOTALWIDTH,(own_y+TOTALHEIGHT)%TOTALHEIGHT,TOTALWIDTH,
				TOTALHEIGHT);		
		
		//mine down
		rmap.down_mine=new RegionDoubleNumericLB(own_x+MAX_DISTANCE,own_y+my_height-MAX_DISTANCE,own_x+my_width-MAX_DISTANCE, 
				(own_y+my_height),TOTALWIDTH,TOTALHEIGHT);
		//out down
		rmap.down_out=new RegionDoubleNumericLB( ((own_x+TOTALWIDTH)+MAX_DISTANCE) %TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,
				((own_x+my_width+TOTALWIDTH)-MAX_DISTANCE)%TOTALWIDTH, 
				(own_y+my_height+MAX_DISTANCE+TOTALHEIGHT)%TOTALHEIGHT,
				TOTALWIDTH,TOTALHEIGHT);		
	}
	//only for testing...used for print region created... stipa ca truov
	public void stampa()
	{	
		
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
	
	public MyCellDoubleNumeric createMyDivision()
	{
		if((topics != null) && (positionGood != null))
			return new MyCellDoubleNumeric(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, topics, positionGood, POSITION);
		else
			return new MyCellDoubleNumeric(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, POSITION);
	}

}