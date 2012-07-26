package dmason.sim.field;

import java.io.Serializable;

/**
 * A Wrapper class for all regions of a distributed field.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class RegionMap<E,F> implements Serializable
{
	

	public Region<E,F>left_out,left_mine,right_mine,right_out,down_mine,down_out,up_mine,up_out=null;//left,mine,right,down
	public Region<E,F>corner_mine_up_left,corner_out_up_left_diag_center=null;  //corner up left
	public Region<E,F>corner_out_up_left_diag_up = null; //corner up left up
	public Region<E,F>corner_out_up_left_diag_left = null; //corner up left left
	public Region<E,F>corner_mine_up_right,corner_out_up_right_diag_center=null; //corner up right
	public Region<E,F>corner_out_up_right_diag_up=null; //corner up right up
	public Region<E,F>corner_out_up_right_diag_right=null; //corner up right right
	public Region<E,F>corner_mine_down_left,corner_out_down_left_diag_center=null; //corner down left
	public Region<E,F>corner_out_down_left_diag_left=null; //corner down left left
	public Region<E,F>corner_out_down_left_diag_down=null; //corner down left down
	public Region<E,F>corner_mine_down_right,corner_out_down_right_diag_center=null; //corner down right
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
		this.left_out = left_out;
		this.left_mine = left_mine;
		this.right_mine = right_mine;
		this.right_out = right_out;
		this.down_mine = down_mine;
		this.down_out = down_out;
		this.up_mine = up_mine;
		this.up_out = up_out;
		this.corner_mine_up_left = corner_mine_up_left;
		this.corner_out_up_left_diag_center = corner_out_up_left_diag_center;
		this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;
		this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;
		this.corner_mine_up_right = corner_mine_up_right;
		this.corner_out_up_right_diag_center = corner_out_up_right_diag_center;
		this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;
		this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;
		this.corner_mine_down_left = corner_mine_down_left;
		this.corner_out_down_left_diag_center = corner_out_down_left_diag_center;
		this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;
		this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;
		this.corner_mine_down_right = corner_mine_down_right;
		this.corner_out_down_right_diag_center = corner_out_down_right_diag_center;
		this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;
		this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;
	}

	//getters and setters 
	public Region<E,F>getleft_out() { return left_out; }
	public void setleft_out(Region<E,F>left_out) { this.left_out = left_out; }
	public Region<E,F>getleft_mine() { return left_mine; }
	public void setleft_mine(Region<E,F>left_mine) { this.left_mine = left_mine;}
	public Region<E,F>getright_mine() {return right_mine;}
	public void setright_mine(Region<E,F>right_mine) {this.right_mine = right_mine;}
	public Region<E,F>getright_out() { return right_out;}
	public void setright_out(Region<E,F>right_out) {this.right_out = right_out;	}
	public Region<E,F>getdown_mine() {return down_mine; }
	public void setdown_mine(Region<E,F>down_mine) {this.down_mine = down_mine;}
	public Region<E,F>getdown_out() {return down_out;}
	public void setdown_out(Region<E,F>down_out) {this.down_out = down_out;}
	public Region<E,F>getup_mine() {return up_mine;}
	public void setup_mine(Region<E,F>up_mine) {this.up_mine = up_mine;}
	public Region<E,F>getup_out() {	return up_out; }
	public void setup_out(Region<E,F>up_out) {this.up_out = up_out; }
	public Region<E,F>getcorner_mine_up_left() {return corner_mine_up_left;	}
	public void setcorner_mine_up_left(Region<E,F>corner_mine_up_left) {this.corner_mine_up_left = corner_mine_up_left;	}
	public Region<E,F>getcorner_out_up_left_diag_center() {return corner_out_up_left_diag_center;}
	public void setcorner_out_up_left_diag_center(Region<E,F>corner_out_up_left_diag_center) {this.corner_out_up_left_diag_center = corner_out_up_left_diag_center;}
	public Region<E,F>getcorner_out_up_left_diag_up() {return corner_out_up_left_diag_up;}
	public void setcorner_out_up_left_diag_up(Region<E,F> corner_out_up_left_diag_up) {this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;}
	public Region<E,F>getcorner_out_up_left_diag_left() {return corner_out_up_left_diag_left;}
	public void setCorner_out_up_left_diag_left(Region<E,F> corner_out_up_left_diag_left) {this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;}	
	public Region<E,F>getcorner_mine_up_right() {return corner_mine_up_right;}
	public void setcorner_mine_up_right(Region<E,F>corner_mine_up_right) {this.corner_mine_up_right = corner_mine_up_right;}
	public Region<E,F>getcorner_out_up_right_diag_center() {return corner_out_up_right_diag_center;}
	public void setcorner_out_up_right_diag_center(Region<E,F>corner_out_up_right_diag_center) {this.corner_out_up_right_diag_center = corner_out_up_right_diag_center;}
	public Region<E,F>getcorner_out_up_right_diag_up() {return corner_out_up_right_diag_up;}
	public void setcorner_out_up_right_diag_up(Region<E,F> corner_out_up_right_diag_up) {this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;}
	public Region<E,F> getcorner_out_up_right_diag_right() {return corner_out_up_right_diag_right;}
	public void setcorner_out_up_right_diag_right(Region<E, F> corner_out_up_right_diag_right) {this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;}	
	public Region<E,F>getcorner_mine_down_left() {return corner_mine_down_left;}
	public void setcorner_mine_down_left(Region<E,F>corner_mine_down_left) {this.corner_mine_down_left = corner_mine_down_left;}
	public Region<E,F>getcorner_out_down_left_diag_center() {return corner_out_down_left_diag_center;}
	public void setcorner_out_down_left_diag_center(Region<E,F>corner_out_down_left_diag_center) {this.corner_out_down_left_diag_center = corner_out_down_left_diag_center;}
	public Region<E,F>getcorner_out_down_left_diag_left() {return corner_out_down_left_diag_left;}
	public void setcorner_out_down_left_diag_left(Region<E,F> corner_out_down_left_diag_left) {this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;}
	public Region<E,F> getcorner_out_down_left_diag_down() {return corner_out_down_left_diag_down;}
	public void setcorner_out_down_left_diag_down(Region<E,F> corner_out_down_left_diag_down) {this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;}	
	public Region<E,F>getcorner_mine_down_right() {return corner_mine_down_right;	}
	public void setcorner_mine_down_right(Region<E,F>corner_mine_down_right) {this.corner_mine_down_right = corner_mine_down_right;}
	public Region<E,F>getcorner_out_down_right_diag_center() {return corner_out_down_right_diag_center;}
	public void setcorner_out_down_right_diag_center(Region<E,F>corner_out_down_right_diag_center) {this.corner_out_down_right_diag_center = corner_out_down_right_diag_center;}
	public Region<E,F> getcorner_out_down_right_diag_right() {return corner_out_down_right_diag_right;}
	public void setcorner_out_down_right_diag_right(Region<E,F> corner_out_down_right_diag_right) {this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;}
	public Region<E,F> getcorner_out_down_right_diag_down() {return corner_out_down_right_diag_down;}
	public void setcorner_out_down_right_diag_down(Region<E,F> corner_out_down_right_diag_down) {this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;}
	
}