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
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import sim.engine.SimState;
import sim.util.Double2D;

public class SplittedDoubleField implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final int costantDivide=3;//used to divide everycell in a 3x3
	public int NUMPEERS;
	public double own_x;	//x coordinate of north-west corner
	public double own_y;	//y coordinate of north-west corner
	public double my_width;
	public double my_height;
	public RegionDouble myfield;
	public RegionMap<Double, Double2D> rmap=new RegionMap<Double, Double2D>();
	public int AOI;
	public CellType cellType;
	public CellType parentType;
    public SimState sm ;
	public double superOwnX;
	public double superOwnY;
	public double TOTALWIDTH; // grandezza della simulazione
	public double TOTALHEIGHT;// grandezza della simulazione
	private double SUPERWIDTH;
	private double SUPERHEIGHT;
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
	public SplittedDoubleField(double superOwnX, double superOwnY, double superWidth,
			double superHeight, DistributedState state, int mAX_DISTANCE,int i, int j,
				int numPeers, String name, int position, CellType parentType, 
					HashMap<Integer, Boolean> topics, HashMap<Integer, Boolean> positionGood) 
	{
		this.superOwnX = superOwnX ;
		this.superOwnY = superOwnY;
		this.SUPERWIDTH = superWidth ;
		this.SUPERHEIGHT = superHeight;
		this.sm = state;
		this.AOI = mAX_DISTANCE;
		this.NUMPEERS = numPeers;
		this.TOTALWIDTH = superWidth * Math.sqrt(NUMPEERS);
		this.TOTALHEIGHT = superHeight*Math.sqrt(NUMPEERS);
		cellType = new CellType(i, j);
		this.parentType = parentType;
		this.NAME = name;
		this.POSITION = position;
		this.topics = topics;
		this.positionGood = positionGood;
		SplitRegion();
	}

	
	/**
	 * Create region of Double field
	 * Calculate the right points for any cell
	 */
	private void SplitRegion(){
		
		
		my_width=(SUPERWIDTH/costantDivide);
		my_height= (SUPERHEIGHT/costantDivide);
		
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
		myfield=new RegionDouble(own_x+AOI,own_y+AOI,own_x+my_width-AOI, 
				own_y+my_height-AOI);
		
		//corner mine up left
		rmap.NORTH_WEST_MINE=new RegionDouble(own_x, own_y,own_x+AOI, 
				own_y+AOI);
		//corner out up left		
		rmap.NORTH_WEST_OUT=new RegionDouble((own_x-AOI + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-AOI+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_up_left_diag_up=new RegionDouble((own_x + TOTALWIDTH)%TOTALWIDTH, 
				(own_y-AOI+TOTALHEIGHT)%TOTALHEIGHT, ((own_x+AOI)+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_up_left_diag_left=new RegionDouble((own_x-AOI + TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+AOI)%TOTALHEIGHT);
		
		//corner mine up right		
		rmap.NORTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y, 
				own_x+my_width, own_y+AOI);
		
		//corner out up right
		rmap.NORTH_EAST_OUT = new RegionDouble((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y-AOI+TOTALHEIGHT)%TOTALHEIGHT, (own_x+my_width+AOI+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_up_right_diag_up=new RegionDouble((own_x+my_width+TOTALWIDTH-AOI)
				%TOTALWIDTH, (own_y-AOI+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH, (own_y+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_up_right_diag_right=new RegionDouble((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+AOI+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+TOTALHEIGHT+AOI)%TOTALHEIGHT);
		
		// corner mine down left
		rmap.SOUTH_WEST_MINE=new RegionDouble(own_x, own_y+my_height-AOI,
				own_x+AOI, own_y+my_height);
		//corner out down left
		rmap.SOUTH_WEST_OUT=new RegionDouble((own_x-AOI+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+AOI+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_down_left_diag_down=new RegionDouble((own_x+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH+AOI)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT+AOI)%TOTALHEIGHT);
		
		rmap.corner_out_down_left_diag_left=new RegionDouble((own_x-AOI+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-AOI)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT);

		//corner mine down right
		rmap.SOUTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, 
				own_y+my_height-AOI,own_x+my_width,own_y+my_height);		
		//corner out down right
		
		rmap.SOUTH_EAST_OUT=new RegionDouble((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+AOI+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+AOI+TOTALHEIGHT)%TOTALHEIGHT);
		
		rmap.corner_out_down_right_diag_down=new RegionDouble((own_x+my_width+TOTALWIDTH-AOI)
				%TOTALWIDTH, (own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH)
				%TOTALWIDTH,(own_y+my_height+AOI+TOTALHEIGHT)%TOTALHEIGHT); 
		
		rmap.corner_out_down_right_diag_right=new RegionDouble((own_x+my_width+TOTALWIDTH)%TOTALWIDTH, 
				(own_y+my_height+TOTALHEIGHT-AOI)%TOTALHEIGHT,(own_x+my_width+TOTALWIDTH
				+AOI)%TOTALWIDTH,(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT);

		//mine left		
		rmap.WEST_MINE=new RegionDouble(own_x,own_y+AOI,own_x + AOI, 
				own_y+my_height-AOI);
		//out left
		rmap.WEST_OUT=new RegionDouble((own_x-AOI+TOTALWIDTH)%TOTALWIDTH,
				((own_y+AOI)+TOTALHEIGHT)%TOTALHEIGHT,(own_x+TOTALWIDTH)%TOTALWIDTH, 
				(((own_y+my_height)+TOTALHEIGHT)-AOI)%TOTALHEIGHT);
		
		//mine right
		rmap.EAST_MINE=new RegionDouble(own_x + my_width - AOI,own_y+AOI,
				own_x +my_width , own_y+my_height-AOI);
		
		//out right
		rmap.EAST_OUT=new RegionDouble((own_x+my_width+TOTALWIDTH)%TOTALWIDTH,
				((own_y+AOI)+TOTALHEIGHT)%TOTALHEIGHT,(own_x+my_width+AOI+TOTALWIDTH)
				%TOTALWIDTH, ((own_y+my_height+TOTALHEIGHT)-AOI)%TOTALHEIGHT);		

		//mine up
		rmap.NORTH_MINE=new RegionDouble(own_x+AOI,own_y,own_x+my_width-AOI, 
				own_y + AOI );
		//out up
		rmap.NORTH_OUT=new RegionDouble(((own_x+AOI)+TOTALWIDTH)%TOTALWIDTH, 
				(own_y - AOI+TOTALHEIGHT)%TOTALHEIGHT,((own_x+ my_width +TOTALWIDTH)-AOI)
				%TOTALWIDTH,(own_y+TOTALHEIGHT)%TOTALHEIGHT
				);		
		
		//mine down
		rmap.SOUTH_MINE=new RegionDouble(own_x+AOI,own_y+my_height-AOI,own_x+my_width-AOI, 
				(own_y+my_height));
		//out down
		rmap.SOUTH_OUT=new RegionDouble( ((own_x+AOI)+TOTALWIDTH) %TOTALWIDTH,
				(own_y+my_height+TOTALHEIGHT)%TOTALHEIGHT,
				((own_x+my_width+TOTALWIDTH)-AOI)%TOTALWIDTH, 
				(own_y+my_height+AOI+TOTALHEIGHT)%TOTALHEIGHT
				);		
	}
	
	//only for testing...used for print region created... stipa ca truov
	public void stampa()
	{	
		Class o=this.rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
		
		System.out.println("MYFIELD DI "+cellType.toString()+": "+myfield.toString());
		
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
	
	public MyCellDoubleField createMyDivision()
	{
		if((topics != null) && (positionGood != null))
			return new MyCellDoubleField(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, topics, positionGood, POSITION);
		else
			return new MyCellDoubleField(rmap, myfield, NAME, own_x, own_y, my_width, my_height, ((DistributedState)sm).schedule.getSteps(), parentType, POSITION);
	}

}