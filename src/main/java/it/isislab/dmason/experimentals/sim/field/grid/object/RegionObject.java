package it.isislab.dmason.experimentals.sim.field.grid.object;

import java.io.Serializable;
import java.util.HashMap;
import sim.util.Int2D;


public class RegionObject extends HashMap<String, EntryObject<Int2D>> implements Cloneable, Serializable {


	private static final long serialVersionUID = 1L;
	//upper left corner
	public Integer upl_xx,upl_yy;
	//lower right corner
	public Integer down_xx,down_yy;

	/**
	 * @param upl_xx x coordinate of upper left corner
	 * @param upl_yy y coordinate of upper left corner
	 * @param down_xx x coordinate of lower down corner
	 * @param down_yy y coordinate of lower down corner
	 */
	public RegionObject(Integer upl_xx,Integer upl_yy, Integer down_xx, Integer down_yy) 
	{
		super();
		this.upl_xx = upl_xx;
		this.upl_yy = upl_yy;
		this.down_xx = down_xx;
		this.down_yy = down_yy;
	}



	
	@Override
	public RegionObject clone() 
	{
		RegionObject r=new RegionObject(upl_xx, upl_yy, down_xx, down_yy);

		for(EntryObject<Int2D> e: this.values())
		{
			Object d=e.r;
			r.put(e.l.toString(), new EntryObject<Int2D>(d, new Int2D(e.l.getX(),e.l.getY())));
		}
		return r;
	}
	
	/**
	 * Add an Entry of a Numeric in the Region
	 * @param e the Entry with an value and a location
	 * @return true o false
	 */
	public boolean addEntryObject(EntryObject<Int2D> e){
		if(e == null || e.l == null || e.r == null) return false;
		if(this.containsKey(e.l.toString()) && this.get(e.l.toString()).equals(e) )
			return true;
		this.put(e.l.toString(),e);
		return true;


	}

	/**
	 * Check if a location is valid for a Region
	 * @param x x coordinate of a location
	 * @param y y coordinate of a location
	 * @return true if is a valid location
	 */
	public boolean isMine(Integer x,Integer y){
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegionObject other = (RegionObject) obj;
		if (down_xx == null) {
			if (other.down_xx != null)
				return false;
		} else if (!down_xx.equals(other.down_xx))
			return false;
		if (down_yy == null) {
			if (other.down_yy != null)
				return false;
		} else if (!down_yy.equals(other.down_yy))
			return false;
		if (upl_xx == null) {
			if (other.upl_xx != null)
				return false;
		} else if (!upl_xx.equals(other.upl_xx))
			return false;
		if (upl_yy == null) {
			if (other.upl_yy != null)
				return false;
		} else if (!upl_yy.equals(other.upl_yy))
			return false;

		for(String location: this.keySet())
			if(!other.containsKey(location) || !other.get(location).equals(this.get(location)))
				return false;

		return true;
	}


	@Override
	public String toString() 
	{
		return "Region [upl_xx=" + upl_xx + ", upl_yy=" + upl_yy + ", down_xx="
				+ down_xx + ", down_yy=" + down_yy + "]";
	}



	public Integer getUpl_xx() {
		return upl_xx;
	}
	public void setUpl_xx(Integer upl_xx) {
		this.upl_xx = upl_xx;
	}
	public Integer getUpl_yy() {
		return upl_yy;
	}public void setUpl_yy(Integer upl_yy) {
		this.upl_yy = upl_yy;
	}
	public Integer getDown_xx() {
		return down_xx;
	}

	public void setDown_xx(Integer down_xx) {
		this.down_xx = down_xx;
	}
	public Integer getDown_yy() {
		return down_yy;
	}
	public void setDown_yy(Integer down_yy) {
		this.down_yy = down_yy;
	}
}
