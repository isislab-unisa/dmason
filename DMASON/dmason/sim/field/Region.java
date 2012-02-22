 package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Abstract superclass of every class that identify a boundary space in a simulated portion of 
 * distributed field.
 * A Region is identified by the upper left corner and the lower right corner (every region is rectangular)
 *
 * @param <E> the specific type of the corner in the field
 * @param <F> the specific type of location of an agent
 */
public abstract class Region<E,F> extends ArrayList<Entry<F>> implements Serializable,Cloneable
{
	private static final long serialVersionUID = 1L;
	//upper left corner
	public E upl_xx,upl_yy;
	//lower right corner
	public E down_xx,down_yy;
	
	/**
	 * @param upl_xx x coordinate of upper left corner
	 * @param upl_yy y coordinate of upper left corner
	 * @param down_xx x coordinate of lower down corner
	 * @param down_yy y coordinate of lower down corner
	 */
	public Region(E upl_xx,E upl_yy, E down_xx, E down_yy) 
	{
		super();
		this.upl_xx = upl_xx;
		this.upl_yy = upl_yy;
		this.down_xx = down_xx;
		this.down_yy = down_yy;
	}
	
	//--> abstract methods to be implemented in sub-classes
	public abstract Region<E,F> clone();
	
	/**
	 * Check if a location is valid for a Region
	 * @param x x coordinate of a location
	 * @param y y coordinate of a location
	 * @return true if is a valid location
	 */
	public abstract boolean isMine(E x,E y);
	
	/**
	 * Add an Entry of agent in the Region
	 * @param e the Entry with an agent
	 * @return true o false
	 */
	public abstract boolean addAgents(Entry<F> e);
	//<--
	
	public String toString() 
	{
		return "Region [upl_xx=" + upl_xx + ", upl_yy=" + upl_yy + ", down_xx="
				+ down_xx + ", down_yy=" + down_yy + "]";
	}
	
	//getters and setters
	public E getUpl_xx() { return upl_xx; }
	public void setUpl_xx(E upl_xx) { this.upl_xx = upl_xx; }
	public E getUpl_yy() { return upl_yy; }
	public void setUpl_yy(E upl_yy) {	this.upl_yy = upl_yy; }
	public E getDown_xx() { return down_xx; }
	public void setDown_xx(E down_xx) { this.down_xx = down_xx; }
	public E getDown_yy() { return down_yy; }
	public void setDown_yy(E down_yy) { this.down_yy = down_yy; }
}