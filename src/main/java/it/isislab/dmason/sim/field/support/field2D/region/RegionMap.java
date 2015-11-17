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

package it.isislab.dmason.sim.field.support.field2D.region;

import java.io.Serializable;

/**
 * A Wrapper class for all regions of a distributed field.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class RegionMap<E,F> implements Serializable
{
	

	public Region<E,F> WEST_OUT,WEST_MINE,EAST_MINE,EAST_OUT,SOUTH_MINE,SOUTH_OUT,NORTH_MINE,NORTH_OUT=null;//left,mine,right,down
	public Region<E,F> NORTH_WEST_MINE,NORTH_WEST_OUT=null;  //corner up left

	
	public Region<E,F> NORTH_EAST_MINE,NORTH_EAST_OUT=null; //corner up right
	
	public Region<E,F> SOUTH_WEST_MINE,SOUTH_WEST_OUT=null; //corner down left

	public Region<E,F> SOUTH_EAST_MINE,SOUTH_EAST_OUT=null; //corner down right

	
	
	
	public Region<E,F> corner_out_up_left_diag_up = null; //corner up left up
	public Region<E,F>corner_out_up_left_diag_left = null; //corner up left left
	
	public Region<E,F>corner_out_up_right_diag_up=null; //corner up right up
	public Region<E,F>corner_out_up_right_diag_right=null; //corner up right right
	
	public Region<E,F>corner_out_down_left_diag_left=null; //corner down left left
	public Region<E,F>corner_out_down_left_diag_down=null; //corner down left down
	
	public Region<E,F>corner_out_down_right_diag_right=null; //corner down right right
	public Region<E,F>corner_out_down_right_diag_down=null; //corner down right down
	
	
	/**
	 * Default constructor
	 */
	public RegionMap() { super(); }
	
	
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
	public RegionMap(Region<E, F> left_out, Region<E, F> left_mine,	Region<E, F> right_mine, 
			Region<E, F> right_out, Region<E, F> down_mine, Region<E, F> down_out,
			Region<E, F> up_mine, Region<E, F> up_out, Region<E, F> corner_mine_up_left,
			Region<E, F> corner_out_up_left_diag_center, Region<E, F> corner_out_up_left_diag_up,
			Region<E, F> corner_out_up_left_diag_left, Region<E, F> corner_mine_up_right,
			Region<E, F> corner_out_up_right_diag_center, Region<E, F> corner_out_up_right_diag_up,
			Region<E, F> corner_out_up_right_diag_right, Region<E, F> corner_mine_down_left,
			Region<E, F> corner_out_down_left_diag_center,	Region<E, F> corner_out_down_left_diag_left,
			Region<E, F> corner_out_down_left_diag_down, Region<E, F> corner_mine_down_right,
			Region<E, F> corner_out_down_right_diag_center, Region<E, F> corner_out_down_right_diag_right,
			Region<E, F> corner_out_down_right_diag_down) 
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
	public Region<E,F>getleft_out() { return WEST_OUT; }
	public void setleft_out(Region<E,F>left_out) { this.WEST_OUT = left_out; }
	public Region<E,F>getleft_mine() { return WEST_MINE; }
	public void setleft_mine(Region<E,F>left_mine) { this.WEST_MINE = left_mine;}
	public Region<E,F>getright_mine() {return EAST_MINE;}
	public void setright_mine(Region<E,F>right_mine) {this.EAST_MINE = right_mine;}
	public Region<E,F>getright_out() { return EAST_OUT;}
	public void setright_out(Region<E,F>right_out) {this.EAST_OUT = right_out;	}
	public Region<E,F>getdown_mine() {return SOUTH_MINE; }
	public void setdown_mine(Region<E,F>down_mine) {this.SOUTH_MINE = down_mine;}
	public Region<E,F>getdown_out() {return SOUTH_OUT;}
	public void setdown_out(Region<E,F>down_out) {this.SOUTH_OUT = down_out;}
	public Region<E,F>getup_mine() {return NORTH_MINE;}
	public void setup_mine(Region<E,F>up_mine) {this.NORTH_MINE = up_mine;}
	public Region<E,F>getup_out() {	return NORTH_OUT; }
	public void setup_out(Region<E,F>up_out) {this.NORTH_OUT = up_out; }
	public Region<E,F>getcorner_mine_up_left() {return NORTH_WEST_MINE;	}
	public void setcorner_mine_up_left(Region<E,F>corner_mine_up_left) {this.NORTH_WEST_MINE = corner_mine_up_left;	}
	public Region<E,F>getcorner_out_up_left_diag_center() {return NORTH_WEST_OUT;}
	public void setcorner_out_up_left_diag_center(Region<E,F>corner_out_up_left_diag_center) {this.NORTH_WEST_OUT = corner_out_up_left_diag_center;}
	public Region<E,F>getcorner_out_up_left_diag_up() {return corner_out_up_left_diag_up;}
	public void setcorner_out_up_left_diag_up(Region<E,F> corner_out_up_left_diag_up) {this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;}
	public Region<E,F>getcorner_out_up_left_diag_left() {return corner_out_up_left_diag_left;}
	public void setCorner_out_up_left_diag_left(Region<E,F> corner_out_up_left_diag_left) {this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;}	
	public Region<E,F>getcorner_mine_up_right() {return NORTH_EAST_MINE;}
	public void setcorner_mine_up_right(Region<E,F>corner_mine_up_right) {this.NORTH_EAST_MINE = corner_mine_up_right;}
	public Region<E,F>getcorner_out_up_right_diag_center() {return NORTH_EAST_OUT;}
	public void setcorner_out_up_right_diag_center(Region<E,F>corner_out_up_right_diag_center) {this.NORTH_EAST_OUT = corner_out_up_right_diag_center;}
	public Region<E,F>getcorner_out_up_right_diag_up() {return corner_out_up_right_diag_up;}
	public void setcorner_out_up_right_diag_up(Region<E,F> corner_out_up_right_diag_up) {this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;}
	public Region<E,F> getcorner_out_up_right_diag_right() {return corner_out_up_right_diag_right;}
	public void setcorner_out_up_right_diag_right(Region<E, F> corner_out_up_right_diag_right) {this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;}	
	public Region<E,F>getcorner_mine_down_left() {return SOUTH_WEST_MINE;}
	public void setcorner_mine_down_left(Region<E,F>corner_mine_down_left) {this.SOUTH_WEST_MINE = corner_mine_down_left;}
	public Region<E,F>getcorner_out_down_left_diag_center() {return SOUTH_WEST_OUT;}
	public void setcorner_out_down_left_diag_center(Region<E,F>corner_out_down_left_diag_center) {this.SOUTH_WEST_OUT = corner_out_down_left_diag_center;}
	public Region<E,F>getcorner_out_down_left_diag_left() {return corner_out_down_left_diag_left;}
	public void setcorner_out_down_left_diag_left(Region<E,F> corner_out_down_left_diag_left) {this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;}
	public Region<E,F> getcorner_out_down_left_diag_down() {return corner_out_down_left_diag_down;}
	public void setcorner_out_down_left_diag_down(Region<E,F> corner_out_down_left_diag_down) {this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;}	
	public Region<E,F>getcorner_mine_down_right() {return SOUTH_EAST_MINE;	}
	public void setcorner_mine_down_right(Region<E,F>corner_mine_down_right) {this.SOUTH_EAST_MINE = corner_mine_down_right;}
	public Region<E,F>getcorner_out_down_right_diag_center() {return SOUTH_EAST_OUT;}
	public void setcorner_out_down_right_diag_center(Region<E,F>corner_out_down_right_diag_center) {this.SOUTH_EAST_OUT = corner_out_down_right_diag_center;}
	public Region<E,F> getcorner_out_down_right_diag_right() {return corner_out_down_right_diag_right;}
	public void setcorner_out_down_right_diag_right(Region<E,F> corner_out_down_right_diag_right) {this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;}
	public Region<E,F> getcorner_out_down_right_diag_down() {return corner_out_down_right_diag_down;}
	public void setcorner_out_down_right_diag_down(Region<E,F> corner_out_down_right_diag_down) {this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;}


	@Override
	public String toString() {
		return "RegionMap [left_out=" + WEST_OUT + ", left_mine=" + WEST_MINE + ", right_mine=" + EAST_MINE
				+ ", right_out=" + EAST_OUT + ", down_mine=" + SOUTH_MINE + ", down_out=" + SOUTH_OUT + ", up_mine="
				+ NORTH_MINE + ", up_out=" + NORTH_OUT + ", corner_mine_up_left=" + NORTH_WEST_MINE
				+ ", corner_out_up_left_diag_center=" + NORTH_WEST_OUT + ", corner_out_up_left_diag_up="
				+ corner_out_up_left_diag_up + ", corner_out_up_left_diag_left=" + corner_out_up_left_diag_left
				+ ", corner_mine_up_right=" + NORTH_EAST_MINE + ", corner_out_up_right_diag_center="
				+ NORTH_EAST_OUT + ", corner_out_up_right_diag_up=" + corner_out_up_right_diag_up
				+ ", corner_out_up_right_diag_right=" + corner_out_up_right_diag_right + ", corner_mine_down_left="
				+ SOUTH_WEST_MINE + ", corner_out_down_left_diag_center=" + SOUTH_WEST_OUT
				+ ", corner_out_down_left_diag_left=" + corner_out_down_left_diag_left
				+ ", corner_out_down_left_diag_down=" + corner_out_down_left_diag_down + ", corner_mine_down_right="
				+ SOUTH_EAST_MINE + ", corner_out_down_right_diag_center=" + SOUTH_EAST_OUT
				+ ", corner_out_down_right_diag_right=" + corner_out_down_right_diag_right
				+ ", corner_out_down_right_diag_down=" + corner_out_down_right_diag_down + "]";
	}
	
	
}