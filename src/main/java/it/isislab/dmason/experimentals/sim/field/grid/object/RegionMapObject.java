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
package it.isislab.dmason.experimentals.sim.field.grid.object;


import java.io.Serializable;

/**
 *  
 * RegionMap for Object Grid 
 *  
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
*/
public class RegionMapObject implements Serializable {


	public RegionObject WEST_OUT,WEST_MINE,EAST_MINE,EAST_OUT,SOUTH_MINE,SOUTH_OUT,NORTH_MINE,NORTH_OUT=null;//left,mine,right,down

	public RegionObject NORTH_WEST_MINE,NORTH_WEST_OUT=null;  //corner up left


	public RegionObject NORTH_EAST_MINE,NORTH_EAST_OUT=null; //corner up right

	public RegionObject SOUTH_WEST_MINE,SOUTH_WEST_OUT=null; //corner down left

	public RegionObject SOUTH_EAST_MINE,SOUTH_EAST_OUT=null; //corner down right



	//only for loadbl xy
	public RegionObject  corner_out_up_left_diag_up = null; //corner up left up
	public RegionObject corner_out_up_left_diag_left = null; //corner up left left
	public RegionObject corner_out_up_right_diag_up=null; //corner up right up
	public RegionObject corner_out_up_right_diag_right=null; //corner up right right
	public RegionObject corner_out_down_left_diag_left=null; //corner down left left
	public RegionObject corner_out_down_left_diag_down=null; //corner down left down
	public RegionObject corner_out_down_right_diag_right=null; //corner down right right
	public RegionObject corner_out_down_right_diag_down=null; //corner down right down


	/**
	 * Default constructor
	 */
	public RegionMapObject() { super(); }


	/**
	 * The Constructor create a RegionMap with the parameters:
	 *
	 *
	 * @param left_out            
	 * @param left_mine
	 * @param right_mine
	 * @param right_out
	 * @param down_mine
	 * @param down_out
	 * @param up_mine
	 * @param up_out
	 * @param corner_mine_up_left
	 * @param corner_out_up_left_diag_center
	 * @param corner_out_up_left_diag_up
	 * @param corner_out_up_left_diag_left
	 * @param corner_mine_up_right
	 * @param corner_out_up_right_diag_center
	 * @param corner_out_up_right_diag_up
	 * @param corner_out_up_right_diag_right
	 * @param corner_mine_down_left
	 * @param corner_out_down_left_diag_center
	 * @param corner_out_down_left_diag_left
	 * @param corner_out_down_left_diag_down
	 * @param corner_mine_down_right
	 * @param corner_out_down_right_diag_center
	 * @param corner_out_down_right_diag_right
	 * @param corner_out_down_right_diag_down
	 * 
	 * 
	 * @return a region of distribuited field
	 */
	public RegionMapObject(RegionObject  left_out, RegionObject  left_mine,	RegionObject  right_mine, 
			RegionObject  right_out, RegionObject  down_mine, RegionObject  down_out,
			RegionObject  up_mine, RegionObject  up_out, RegionObject  corner_mine_up_left,
			RegionObject  corner_out_up_left_diag_center, RegionObject  corner_out_up_left_diag_up,
			RegionObject  corner_out_up_left_diag_left, RegionObject  corner_mine_up_right,
			RegionObject  corner_out_up_right_diag_center, RegionObject  corner_out_up_right_diag_up,
			RegionObject  corner_out_up_right_diag_right, RegionObject  corner_mine_down_left,
			RegionObject  corner_out_down_left_diag_center,	RegionObject  corner_out_down_left_diag_left,
			RegionObject  corner_out_down_left_diag_down, RegionObject  corner_mine_down_right,
			RegionObject  corner_out_down_right_diag_center, RegionObject  corner_out_down_right_diag_right,
			RegionObject  corner_out_down_right_diag_down) 
	{
		super();
		this.WEST_OUT = left_out;
		this.WEST_MINE = left_mine;
		this.EAST_MINE = right_mine;
		this.EAST_OUT = right_out;
		this.SOUTH_MINE = down_mine;
		this.SOUTH_OUT = down_out;
		this.NORTH_MINE = up_mine;
		this.NORTH_OUT = up_out;
		this.NORTH_WEST_MINE = corner_mine_up_left;
		this.NORTH_WEST_OUT = corner_out_up_left_diag_center;
		this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;
		this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;
		this.NORTH_EAST_MINE = corner_mine_up_right;
		this.NORTH_EAST_OUT = corner_out_up_right_diag_center;
		this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;
		this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;
		this.SOUTH_WEST_MINE = corner_mine_down_left;
		this.SOUTH_WEST_OUT = corner_out_down_left_diag_center;
		this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;
		this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;
		this.SOUTH_EAST_MINE = corner_mine_down_right;
		this.SOUTH_EAST_OUT = corner_out_down_right_diag_center;
		this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;
		this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;
	}

	//getters and setters 
	public RegionObject getWEST_OUT() { return WEST_OUT; }
	public void setWEST_OUT(RegionObject WEST_OUT) { this.WEST_OUT = WEST_OUT; }
	public RegionObject getWEST_MINE() { return WEST_MINE; }
	public void setWEST_MINE(RegionObject WEST_MINE) { this.WEST_MINE = WEST_MINE;}
	public RegionObject getEAST_MINE() {return EAST_MINE;}
	public void setEAST_MINE(RegionObject EAST_MINE) {this.EAST_MINE = EAST_MINE;}
	public RegionObject getEAST_OUT() { return EAST_OUT;}
	public void setEAST_OUT(RegionObject EAST_OUT) {this.EAST_OUT = EAST_OUT;	}
	public RegionObject getSOUTH_MINE() {return SOUTH_MINE; }
	public void setSOUTH_MINE(RegionObject SOUTH_MINE) {this.SOUTH_MINE = SOUTH_MINE;}
	public RegionObject getSOUTH_OUT() {return SOUTH_OUT;}
	public void setSOUTH_OUT(RegionObject SOUTH_OUT) {this.SOUTH_OUT = SOUTH_OUT;}
	public RegionObject getNORTH_MINE() {return NORTH_MINE;}
	public void setNORTH_MINE(RegionObject NORTH_MINE) {this.NORTH_MINE = NORTH_MINE;}
	public RegionObject getNORTH_OUT() {	return NORTH_OUT; }
	public void setNORTH_OUT(RegionObject NORTH_OUT) {this.NORTH_OUT = NORTH_OUT; }
	public RegionObject getNORTH_WEST_MINE() {return NORTH_WEST_MINE;	}
	public void setNORTH_WEST_MINE(RegionObject NORTH_WEST_MINE) {this.NORTH_WEST_MINE = NORTH_WEST_MINE;	}
	public RegionObject getNORTH_WEST_OUT() {return NORTH_WEST_OUT;}
	public void setNORTH_WEST_OUT(RegionObject NORTH_WEST_OUT) {this.NORTH_WEST_OUT = NORTH_WEST_OUT;}

	public RegionObject getNORTH_EAST_MINE() {return NORTH_EAST_MINE;}
	public void setNORTH_EAST_MINE(RegionObject NORTH_EAST_MINE) {this.NORTH_EAST_MINE = NORTH_EAST_MINE;}
	public RegionObject getNORTH_EAST_OUT() {return NORTH_EAST_OUT;}
	public void setNORTH_EAST_OUT(RegionObject NORTH_EAST_OUT) {this.NORTH_EAST_OUT = NORTH_EAST_OUT;}

	public RegionObject getSOUTH_WEST_MINE() {return SOUTH_WEST_MINE;}
	public void setSOUTH_WEST_MINE(RegionObject SOUTH_WEST_MINE) {this.SOUTH_WEST_MINE = SOUTH_WEST_MINE;}
	public RegionObject getSOUTH_WEST_OUT() {return SOUTH_WEST_OUT;}
	public void setSOUTH_WEST_OUT(RegionObject SOUTH_WEST_OUT) {this.SOUTH_WEST_OUT = SOUTH_WEST_OUT;}

	public RegionObject getSOUTH_EAST_MINE() {return SOUTH_EAST_MINE;	}
	public void setSOUTH_EAST_MINE(RegionObject SOUTH_EAST_MINE) {this.SOUTH_EAST_MINE = SOUTH_EAST_MINE;}
	public RegionObject getSOUTH_EAST_OUT() {return SOUTH_EAST_OUT;}
	public void setSOUTH_EAST_OUT(RegionObject SOUTH_EAST_OUT) {this.SOUTH_EAST_OUT = SOUTH_EAST_OUT;}









	public RegionObject  getcorner_out_up_left_diag_up(){return corner_out_up_left_diag_up;}  //corner up left up
	public RegionObject getcorner_out_up_left_diag_left(){return corner_out_up_left_diag_left;} //corner up left left

	public RegionObject getcorner_out_up_right_diag_up(){return corner_out_up_right_diag_up;}//corner up right up
	public RegionObject getcorner_out_up_right_diag_right(){return corner_out_up_right_diag_right;} //corner up right right

	public RegionObject getcorner_out_down_left_diag_left(){return corner_out_down_left_diag_left;} //corner down left left
	public RegionObject getcorner_out_down_left_diag_down(){return corner_out_down_left_diag_down;} //corner down left down

	public RegionObject getcorner_out_down_right_diag_right(){return corner_out_down_right_diag_right;} //corner down right right
	public RegionObject getcorner_out_down_right_diag_down(){return corner_out_down_right_diag_down;} //corner down right down






	@Override
	public String toString() {
		return "RegionMapNumeric [WEST_OUT=" + WEST_OUT + ", WEST_MINE=" + WEST_MINE + ", EAST_MINE=" + EAST_MINE
				+ ", EAST_OUT=" + EAST_OUT + ", SOUTH_MINE=" + SOUTH_MINE + ", SOUTH_OUT=" + SOUTH_OUT + ", NORTH_MINE="
				+ NORTH_MINE + ", NORTH_OUT=" + NORTH_OUT + ", NORTH_WEST_MINE=" + NORTH_WEST_MINE + ", NORTH_WEST_OUT="
				+ NORTH_WEST_OUT + ", NORTH_EAST_MINE=" + NORTH_EAST_MINE + ", NORTH_EAST_OUT=" + NORTH_EAST_OUT
				+ ", SOUTH_WEST_MINE=" + SOUTH_WEST_MINE + ", SOUTH_WEST_OUT=" + SOUTH_WEST_OUT + ", SOUTH_EAST_MINE="
				+ SOUTH_EAST_MINE + ", SOUTH_EAST_OUT=" + SOUTH_EAST_OUT + "]";
	}


}
