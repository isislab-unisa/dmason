package it.isislab.dmason.sim.field.support.field3D.region;

import java.io.Serializable;
import java.util.HashMap;

import it.isislab.dmason.sim.field.support.field2D.EntryAgent;

public abstract class Region3D<E,F> extends HashMap<String,EntryAgent<F>> implements Serializable,Cloneable {

    private static final long serialVersionUID = 1L;
    //upper left corner
    public E upl_xx,upl_yy,upl_zz;
    //lower right corner
    public E down_xx,down_yy,down_zz;


    /**
     * @param upl_xx x coordinate of upper left corner
     * @param upl_yy y coordinate of upper left corner
     * @param upl_zz z coordinate of upper left corner
     * @param down_xx x coordinate of lower down corner
     * @param down_yy y coordinate of lower down corner
     * @param down_zz z coordinate of lower down corner
     */
    public Region3D(E upl_xx,E upl_yy,E upl_zz, E down_xx, E down_yy,E down_zz)
    {
        super();
        this.upl_xx = upl_xx;
        this.upl_yy = upl_yy;
        this.upl_zz =upl_zz;
        this.down_xx = down_xx;
        this.down_yy = down_yy;
        this.down_zz = down_zz;
    }

    //--> abstract methods to be implemented in sub-classes
    @Override
    public abstract Region3D<E,F> clone();

    /**
     * Check if a location is valid for a Region
     * @param x x coordinate of a location
     * @param y y coordinate of a location
     * @return true if is a valid location
     */
    public abstract boolean isMine(E x,E y,E z);

    /**
     * Add an Entry of agent in the Region
     * @param e the Entry with an agent
     * @return true o false
     */
    public abstract boolean addAgents(EntryAgent<F> e);
    //<--

    @Override
    public String toString()
    {
        return "Region3D [upl_xx=" + upl_xx + ", upl_yy=" + upl_yy +" ,upl_zz="+upl_zz+ ", down_xx="
                + down_xx + ", down_yy=" + down_yy +", down_zz="+down_zz+ "]";
    }

    //getters and setters
    public E getUpl_xx() { return upl_xx; }
    public void setUpl_xx(E upl_xx) { this.upl_xx = upl_xx; }
    public E getUpl_yy() { return upl_yy; }
    public void setUpl_yy(E upl_yy) {	this.upl_yy = upl_yy; }
    public E getUpl_zz() { return upl_zz;}
    public void setUpl_zz(E upl_zz){ this.upl_zz=upl_zz;}
    public E getDown_xx() { return down_xx; }
    public void setDown_xx(E down_xx) { this.down_xx = down_xx; }
    public E getDown_yy() { return down_yy; }
    public void setDown_yy(E down_yy) { this.down_yy = down_yy; }
    public E getDown_zz(){return down_zz;}
    public void setDown_zz(E down_zz){this.down_zz=down_zz;}


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Region3D)) {
            return false;
        }
        Region3D other = (Region3D) obj;
        if (down_xx == null) {
            if (other.down_xx != null) {
                return false;
            }
        } else if (!down_xx.equals(other.down_xx)) {
            return false;
        }
        if (down_yy == null) {
            if (other.down_yy != null) {
                return false;
            }
        } else if (!down_yy.equals(other.down_yy)) {
            return false;
        }
        if(down_zz == null){
            if(other.down_zz!=null){
                return false;
            }
        }else if(!down_zz.equals(other.down_zz)){
            return false;
        }
        if (upl_xx == null) {
            if (other.upl_xx != null) {
                return false;
            }
        } else if (!upl_xx.equals(other.upl_xx)) {
            return false;
        }
        if (upl_yy == null) {
            if (other.upl_yy != null) {
                return false;
            }
        } else if (!upl_yy.equals(other.upl_yy)) {
            return false;
        }
        if (upl_zz == null) {
            if (other.upl_zz != null) {
                return false;
            }
        } else if (!upl_zz.equals(other.upl_zz)) {
            return false;
        }
        for(String agent_id : this.keySet())
            if(!other.containsKey(agent_id))
                return false;
            else if(!other.get(agent_id).equals(this.get(agent_id)))
                return false;

        return true;
    }

}
