package dmason.sim.field;

import java.io.Serializable;

/**
 * A Wrapper class for all regions of a distributed field.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class RegionMap<E,F> implements Serializable
{
	public Region<E,F>left_out,left_mine,right_mine,right_out,down_mine,down_out,up_mine,up_out=null;
	public Region<E,F>corner_mine_up_left,corner_out_up_left_diag=null;  //corner up left
	public Region<E,F>corner_mine_up_right,corner_out_up_right_diag=null; //corner up right
	public Region<E,F>corner_mine_down_left,corner_out_down_left_diag=null; //corner down left
	public Region<E,F>corner_mine_down_right,corner_out_down_right_diag=null; //corner down right
	
	public RegionMap() { super(); }
	
	public RegionMap(Region<E,F> left_out, Region<E,F>left_mine, Region<E,F>right_mine,
			Region<E,F>right_out, Region<E,F>down_mine, Region<E,F>down_out,
			Region<E,F>up_mine, Region<E,F>up_out, Region<E,F>corner_mine_up_left,
			Region<E,F>corner_out_up_left_diag, Region<E,F>corner_mine_up_right,
			Region<E,F>corner_out_up_right_diag, Region<E,F>corner_mine_down_left,
			Region<E,F>corner_out_down_left_diag, Region<E,F>corner_mine_down_right,
			Region<E,F>corner_out_down_right_diag) 
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
	public Region<E,F>getcorner_out_up_left_diag() {return corner_out_up_left_diag;}
	public void setcorner_out_up_left_diag(Region<E,F>corner_out_up_left_diag) {this.corner_out_up_left_diag = corner_out_up_left_diag;}
	public Region<E,F>getcorner_mine_up_right() {return corner_mine_up_right;}
	public void setcorner_mine_up_right(Region<E,F>corner_mine_up_right) {this.corner_mine_up_right = corner_mine_up_right;}
	public Region<E,F>getcorner_out_up_right_diag() {return corner_out_up_right_diag;}
	public void setcorner_out_up_right_diag(Region<E,F>corner_out_up_right_diag) {this.corner_out_up_right_diag = corner_out_up_right_diag; }
	public Region<E,F>getcorner_mine_down_left() {return corner_mine_down_left;}
	public void setcorner_mine_down_left(Region<E,F>corner_mine_down_left) {this.corner_mine_down_left = corner_mine_down_left; }
	public Region<E,F>getcorner_out_down_left_diag() {return corner_out_down_left_diag;	}
	public void setcorner_out_down_left_diag(Region<E,F>corner_out_down_left_diag) {this.corner_out_down_left_diag = corner_out_down_left_diag;	}
	public Region<E,F>getcorner_mine_down_right() {return corner_mine_down_right;	}
	public void setcorner_mine_down_right(Region<E,F>corner_mine_down_right) {this.corner_mine_down_right = corner_mine_down_right;	}
	public Region<E,F>getcorner_out_down_right_diag() {return corner_out_down_right_diag;}
	public void setcorner_out_down_right_diag(Region<E,F>corner_out_down_right_diag) {this.corner_out_down_right_diag = corner_out_down_right_diag;	}
}