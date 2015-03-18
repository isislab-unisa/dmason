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

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * only mine
 *  
 *                                                                 CORNER DIAG 
 *                                                                 /    
 *  															  /
 *  ________________________|__|__|______UP_OUT__________ _|__|__|/________________________|
 * |__|___________________|_|__|__|10|___UP_MINE_______|11 |__|__|__|___________________|__|
 * |__|___________________|_|__|9_|0_|_______1_________| 2_|12 |__|__|___________________|__|
 * |                        |__|__|                     /  |__|__|                         |
 * |                        L  L  |                    /   |  R  R                         |
 * |                        E  E  |                   /    |  I  I                         |
 * |                        F  F  |              CORNER    |  G  G                         |
 * |                        T  T 7|         8     MINE     |3 H  H                         |
 * |                        |  |  |                        |  T  T                         |
 * |                        O  M  |       MYFIELD          |  |  |                         |
 * |                        U  I  |                        |  M  O                         |
 * |                        T__N__|                        |__I__U                         |
 * |________________________|__|__|______________________ _|__|__|_________________________|
 * |__|__________________|__|__|16|6_|DOWN_MINE_5______|4_ |13|__|__|____________________|_|
 * |__|__________________|__|__|__|15|DOWN_OUT_________|14 |__|__|__|____________________|_|
    |
 * 
 *The class create a cell that is one of the sub-cells that constitute part of the simulation.   
 *@see MyCellInterface
 */
public class MyCellDoubleField implements MyCellInterface,Serializable{

	private static final long serialVersionUID = 1L;
	private RegionMap MYRMAP;
	private RegionDouble MYFIELD;
	private String NAME;
	private CellType PARENTTYPE;
	private double own_x; 
	private double own_y;
	private double upl_xx, upl_yy, down_xx, down_yy;
	private double my_width;
	private double my_height;
	private HashMap<Integer, Boolean> positionPublish;
	private HashMap<Integer, Boolean> positionGood;
	private boolean makeUnion;

	private int POSITION;
	private long step;
	
	
	/**
	 * Constructor of class with parameters:
	 *  
	 * @param MYRMAP RegionMap with all region of this cell
	 * @param MYFIELD  central region of Cell
	 * @param name identifier of field
	 * @param own_x x cordinate of corner up left, origin of cell
	 * @param own_y y cordinate of corner up left, origin of cell
	 * @param my_width width of cell
	 * @param my_height height of cell
	 * @param step number of step
	 * @param parentType CellType of Cell which is divided 
	 * @param positionPublish a HashMap<Integer,Boolean> where the key is the topic and the associated value indicates if is possible to send on this topic  
	 * @param positionGood a HashMap<Integer,Boolean> where the key is the region and the associated value indicates if is possible to send the update
	 * @param position is the position of cell into field
	 */

	public MyCellDoubleField(RegionMap MYRMAP, RegionDouble MYFIELD, String name, double own_x, double own_y, 
			double my_width, double my_height, long step, CellType parentType, HashMap<Integer, 
				Boolean> positionPublish,HashMap<Integer, Boolean> positionGood, int position) {
	
		this.NAME = name;
		this.MYRMAP = MYRMAP;
		this.MYFIELD = MYFIELD;
		this.own_x = own_x;
		this.own_y = own_y;
		this.my_width = my_width;
		this.my_height = my_height;
		this.step = step;
		this.PARENTTYPE = parentType;
		this.POSITION = position;
		this.positionPublish = positionPublish;
		this.positionGood = positionGood;
		this.makeUnion = false;
		this.upl_xx = (Double) MYRMAP.corner_out_up_left_diag_center.upl_xx; 
		this.upl_yy = (Double) MYRMAP.corner_out_up_left_diag_center.upl_yy;
		this.down_xx = (Double) MYRMAP.corner_out_down_right_diag_center.down_xx; 
		this.down_yy = (Double) MYRMAP.corner_out_down_right_diag_center.down_xx;
	}

	public MyCellDoubleField(RegionMap MYRMAP, RegionDouble MYFIELD, String name, double own_x, double own_y, 
			double my_width, double my_height, long step, CellType parentType, int position) {
		
		this.NAME = name;
		this.MYRMAP = MYRMAP;
		this.MYFIELD = MYFIELD;
		this.own_x = own_x;
		this.own_y = own_y;
		this.my_width = my_width;
		this.my_height = my_height;
		this.step = step;
		this.PARENTTYPE = parentType;
		this.POSITION = position;
		positionPublish = new HashMap<Integer, Boolean>();
		positionGood = new HashMap<Integer, Boolean>();
		this.makeUnion = false;
		
		this.upl_xx = (Double) MYRMAP.corner_out_up_left_diag_center.upl_xx; 
		this.upl_yy = (Double) MYRMAP.corner_out_up_left_diag_center.upl_yy;
		this.down_xx = (Double) MYRMAP.corner_out_down_right_diag_center.down_xx; 
		this.down_yy = (Double) MYRMAP.corner_out_down_right_diag_center.down_xx;
		
		for (int j2 = 0; j2 <= 8; j2++) {
			positionPublish.put(j2, false);
		}
		for (int j2 = 0; j2 <= 16; j2++) {
			positionGood.put(j2, false);
		}
	}
	
	@Override
	public HashMap<Integer, Boolean> getPositionGood() {return positionGood;}
	@Override
	public void setPositionGood(HashMap<Integer, Boolean> positionGood) {this.positionGood = positionGood;}
	@Override
	public HashMap<Integer, Boolean> getPositionPublish() {return positionPublish;}
	@Override
	public void setPositionPublish(HashMap<Integer, Boolean> PositionPublish) {this.positionPublish = PositionPublish;}
	@Override
	public CellType getParentCellType() {return PARENTTYPE;}
	@Override
	public void setParentCellType(CellType parentType) {PARENTTYPE = parentType;}
	@Override
	public String getId() {return NAME;}
	@Override
	public void seId(String name){this.NAME = name;}
	@Override
	public RegionMap getMyRMap() {return MYRMAP;}
	@Override
	public void setMYRMAP(Object myRMap) {MYRMAP = (RegionMap)myRMap;}
	@Override
	public RegionDouble getMyField() {return MYFIELD;}
	@Override
	public void setMyField(Object myField) {MYFIELD = (RegionDouble)myField;}
	@Override
	public Object getOwn_x(){return own_x;}
	@Override
	public void setOwn_x(Object own_x){this.own_x = (Double) own_x;}
	@Override
	public Object getOwn_y() {return own_y;}
	@Override
	public void setOwn_y(Object own_y) {this.own_y = (Double)own_y;}
	@Override
	public Object getMy_width() {return my_width;}
	@Override
	public void setMy_width(Object my_width) {this.my_width = (Double)my_width;}
	@Override
	public Object getMy_height() {return my_height;}
	@Override
	public void setMy_height(Object my_height) {this.my_height = (Double)my_height;}
	@Override
	public int getPosition() {return POSITION;}
	@Override
	public void setPosition(int position) {this.POSITION = position;}
	@Override
	public long getStep() {return step;}
	@Override
	public void setStep(long step) {this.step = step;}
	@Override
	public boolean isUnion() {return this.makeUnion;}
	@Override
	public void setUnion(boolean makeUnion) {this.makeUnion = makeUnion;}
	@Override
	public boolean isMine(Object xx, Object yy) {
		double x = (Double) xx;
		double y = (Double) yy;
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
	}
}
