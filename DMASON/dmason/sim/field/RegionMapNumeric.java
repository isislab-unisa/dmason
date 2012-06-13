package dmason.sim.field;

import java.io.Serializable;

/**
 * A Wrapper class for all regions of a distributed field.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class RegionMapNumeric<E,F> implements Serializable{

	public RegionNumeric<E,F>left_out,left_mine,right_mine,right_out,down_mine,down_out,up_mine,up_out=null;
	public RegionNumeric<E,F>corner_mine_up_left,corner_out_up_left_diag=null;  //corner up left
	public RegionNumeric<E,F>corner_out_up_left_diag_up = null; //corner up left up
	public RegionNumeric<E,F>corner_out_up_left_diag_left = null; //corner up left left
	public RegionNumeric<E,F>corner_mine_up_right,corner_out_up_right_diag=null; //corner up right
	public RegionNumeric<E,F>corner_out_up_right_diag_up=null; //corner up right up
	public RegionNumeric<E,F>corner_out_up_right_diag_right=null; //corner up right right
	public RegionNumeric<E,F>corner_mine_down_left,corner_out_down_left_diag=null; //corner down left
	public RegionNumeric<E,F>corner_out_down_left_diag_left=null; //corner down left left
	public RegionNumeric<E,F>corner_out_down_left_diag_down=null; //corner down left down
	public RegionNumeric<E,F>corner_mine_down_right,corner_out_down_right_diag=null; //corner down right
	public RegionNumeric<E,F>corner_out_down_right_diag_right=null; //corner down right right
	public RegionNumeric<E,F>corner_out_down_right_diag_down=null; //corner down right down
	
	
	public RegionMapNumeric(RegionNumeric<E,F> left_out, RegionNumeric<E,F>left_mine, RegionNumeric<E,F>right_mine,
			RegionNumeric<E,F>right_out, RegionNumeric<E,F>down_mine, RegionNumeric<E,F>down_out,
			RegionNumeric<E,F>up_mine, RegionNumeric<E,F>up_out, RegionNumeric<E,F>corner_mine_up_left,
			RegionNumeric<E,F>corner_out_up_left_diag, RegionNumeric<E,F>corner_mine_up_right,
			RegionNumeric<E,F>corner_out_up_right_diag, RegionNumeric<E,F>corner_mine_down_left,
			RegionNumeric<E,F>corner_out_down_left_diag, RegionNumeric<E,F>corner_mine_down_right,
			RegionNumeric<E,F>corner_out_down_right_diag) 
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
		this.corner_out_up_left_diag = corner_out_up_left_diag;
		this.corner_mine_up_right = corner_mine_up_right;
		this.corner_out_up_right_diag = corner_out_up_right_diag;
		this.corner_mine_down_left = corner_mine_down_left;
		this.corner_out_down_left_diag = corner_out_down_left_diag;
		this.corner_mine_down_right = corner_mine_down_right;
		this.corner_out_down_right_diag = corner_out_down_right_diag;
	}
	
	public RegionMapNumeric() { super(); }
	
	public RegionMapNumeric(RegionNumeric<E, F> left_out, RegionNumeric<E, F> left_mine,	RegionNumeric<E, F> right_mine, 
			RegionNumeric<E, F> right_out, RegionNumeric<E, F> down_mine, RegionNumeric<E, F> down_out,
			RegionNumeric<E, F> up_mine, RegionNumeric<E, F> up_out, RegionNumeric<E, F> corner_mine_up_left,
			RegionNumeric<E, F> corner_out_up_left_diag_center, RegionNumeric<E, F> corner_out_up_left_diag_up,
			RegionNumeric<E, F> corner_out_up_left_diag_left, RegionNumeric<E, F> corner_mine_up_right,
			RegionNumeric<E, F> corner_out_up_right_diag_center, RegionNumeric<E, F> corner_out_up_right_diag_up,
			RegionNumeric<E, F> corner_out_up_right_diag_right, RegionNumeric<E, F> corner_mine_down_left,
			RegionNumeric<E, F> corner_out_down_left_diag_center,	RegionNumeric<E, F> corner_out_down_left_diag_left,
			RegionNumeric<E, F> corner_out_down_left_diag_down, RegionNumeric<E, F> corner_mine_down_right,
			RegionNumeric<E, F> corner_out_down_right_diag_center, RegionNumeric<E, F> corner_out_down_right_diag_right,
			RegionNumeric<E, F> corner_out_down_right_diag_down) 
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
		this.corner_out_up_left_diag = corner_out_up_left_diag_center;
		this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;
		this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;
		this.corner_mine_up_right = corner_mine_up_right;
		this.corner_out_up_right_diag = corner_out_up_right_diag_center;
		this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;
		this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;
		this.corner_mine_down_left = corner_mine_down_left;
		this.corner_out_down_left_diag = corner_out_down_left_diag_center;
		this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;
		this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;
		this.corner_mine_down_right = corner_mine_down_right;
		this.corner_out_down_right_diag = corner_out_down_right_diag_center;
		this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;
		this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;
	}

	//getters and setters
	public RegionNumeric<E,F>getleft_out() { return left_out; }
	public void setleft_out(RegionNumeric<E,F>left_out) { this.left_out = left_out; }
	public RegionNumeric<E,F>getleft_mine() { return left_mine; }
	public void setleft_mine(RegionNumeric<E,F>left_mine) { this.left_mine = left_mine;}
	public RegionNumeric<E,F>getright_mine() {return right_mine;}
	public void setright_mine(RegionNumeric<E,F>right_mine) {this.right_mine = right_mine;}
	public RegionNumeric<E,F>getright_out() { return right_out;}
	public void setright_out(RegionNumeric<E,F>right_out) {this.right_out = right_out;	}
	public RegionNumeric<E,F>getdown_mine() {return down_mine; }
	public void setdown_mine(RegionNumeric<E,F>down_mine) {this.down_mine = down_mine;}
	public RegionNumeric<E,F>getdown_out() {return down_out;}
	public void setdown_out(RegionNumeric<E,F>down_out) {this.down_out = down_out;}
	public RegionNumeric<E,F>getup_mine() {return up_mine;}
	public void setup_mine(RegionNumeric<E,F>up_mine) {this.up_mine = up_mine;}
	public RegionNumeric<E,F>getup_out() {	return up_out; }
	public void setup_out(RegionNumeric<E,F>up_out) {this.up_out = up_out; }

	public RegionNumeric<E,F>getcorner_mine_up_left() {return corner_mine_up_left;	}
	public void setcorner_mine_up_left(RegionNumeric<E,F>corner_mine_up_left) {this.corner_mine_up_left = corner_mine_up_left;	}
	public RegionNumeric<E,F>getcorner_out_up_left_diag() {return corner_out_up_left_diag;}
	public void setcorner_out_up_left_diag(RegionNumeric<E,F>corner_out_up_left_diag_center) {this.corner_out_up_left_diag = corner_out_up_left_diag_center;}
	public RegionNumeric<E,F>getcorner_out_up_left_diag_up() {return corner_out_up_left_diag_up;}
	public void setcorner_out_up_left_diag_up(RegionNumeric<E,F> corner_out_up_left_diag_up) {this.corner_out_up_left_diag_up = corner_out_up_left_diag_up;}
	public RegionNumeric<E,F>getcorner_out_up_left_diag_left() {return corner_out_up_left_diag_left;}
	public void setCorner_out_up_left_diag_left(RegionNumeric<E,F> corner_out_up_left_diag_left) {this.corner_out_up_left_diag_left = corner_out_up_left_diag_left;}	
	public RegionNumeric<E,F>getcorner_mine_up_right() {return corner_mine_up_right;}
	public void setcorner_mine_up_right(RegionNumeric<E,F>corner_mine_up_right) {this.corner_mine_up_right = corner_mine_up_right;}
	public RegionNumeric<E,F>getcorner_out_up_right_diag() {return corner_out_up_right_diag;}
	public void setcorner_out_up_right_diag(RegionNumeric<E,F>corner_out_up_right_diag_center) {this.corner_out_up_right_diag = corner_out_up_right_diag_center;}
	public RegionNumeric<E,F>getcorner_out_up_right_diag_up() {return corner_out_up_right_diag_up;}
	public void setcorner_out_up_right_diag_up(RegionNumeric<E,F> corner_out_up_right_diag_up) {this.corner_out_up_right_diag_up = corner_out_up_right_diag_up;}
	public RegionNumeric<E,F> getcorner_out_up_right_diag_right() {return corner_out_up_right_diag_right;}
	public void setcorner_out_up_right_diag_right(RegionNumeric<E, F> corner_out_up_right_diag_right) {this.corner_out_up_right_diag_right = corner_out_up_right_diag_right;}	
	public RegionNumeric<E,F>getcorner_mine_down_left() {return corner_mine_down_left;}
	public void setcorner_mine_down_left(RegionNumeric<E,F>corner_mine_down_left) {this.corner_mine_down_left = corner_mine_down_left;}
	public RegionNumeric<E,F>getcorner_out_down_left_diag() {return corner_out_down_left_diag;}
	public void setcorner_out_down_left_diag(RegionNumeric<E,F>corner_out_down_left_diag_center) {this.corner_out_down_left_diag = corner_out_down_left_diag_center;}
	public RegionNumeric<E,F>getcorner_out_down_left_diag_left() {return corner_out_down_left_diag_left;}
	public void setcorner_out_down_left_diag_left(RegionNumeric<E,F> corner_out_down_left_diag_left) {this.corner_out_down_left_diag_left = corner_out_down_left_diag_left;}
	public RegionNumeric<E,F> getcorner_out_down_left_diag_down() {return corner_out_down_left_diag_down;}
	public void setcorner_out_down_left_diag_down(RegionNumeric<E,F> corner_out_down_left_diag_down) {this.corner_out_down_left_diag_down = corner_out_down_left_diag_down;}	
	public RegionNumeric<E,F>getcorner_mine_down_right() {return corner_mine_down_right;	}
	public void setcorner_mine_down_right(RegionNumeric<E,F>corner_mine_down_right) {this.corner_mine_down_right = corner_mine_down_right;}
	public RegionNumeric<E,F>getcorner_out_down_right_diag() {return corner_out_down_right_diag;}
	public void setcorner_out_down_right_diag(RegionNumeric<E,F>corner_out_down_right_diag_center) {this.corner_out_down_right_diag = corner_out_down_right_diag_center;}
	public RegionNumeric<E,F> getcorner_out_down_right_diag_right() {return corner_out_down_right_diag_right;}
	public void setcorner_out_down_right_diag_right(RegionNumeric<E,F> corner_out_down_right_diag_right) {this.corner_out_down_right_diag_right = corner_out_down_right_diag_right;}
	public RegionNumeric<E,F> getcorner_out_down_right_diag_down() {return corner_out_down_right_diag_down;}
	public void setcorner_out_down_right_diag_down(RegionNumeric<E,F> corner_out_down_right_diag_down) {this.corner_out_down_right_diag_down = corner_out_down_right_diag_down;}
	
}
