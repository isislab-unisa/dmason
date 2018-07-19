package it.isislab.dmason.sim.field.support.field3D;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.sim.field.support.field3D.region.Region3D;

import java.io.Serializable;

public class DistributedRegion3D<E,F> implements Serializable, DistributedRegionInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // Valid values for <code>position</code>
    public static int NORTH = 1;
    public static int SOUTH = 2;
    public static int WEST = 3;
    public static int EAST = 4;
    public static int FRONT = 5;
    public static int REAR = 6;

    public static int NORTH_FRONT = 7;
    public static int NORTH_WEST = 8;
    public static int NORTH_REAR = 9;
    public static int NORTH_EAST = 10;
    public static int SOUTH_FRONT = 11;
    public static int SOUTH_WEST = 12;
    public static int SOUTH_REAR = 13;
    public static int SOUTH_EAST = 14;
    public static int WEST_FRONT = 15;
    public static int WEST_REAR = 16;
    public static int EAST_FRONT = 17;
    public static int EAST_REAR = 18;

    public static int NORTH_WEST_FRONT = 19;
    public static int NORTH_WEST_REAR = 20;
    public static int SOUTH_WEST_FRONT = 21;
    public static int SOUTH_WEST_REAR = 22;
    public static int NORTH_EAST_FRONT = 23;
    public static int NORTH_EAST_REAR = 24;
    public static int SOUTH_EAST_FRONT = 25;
    public static int SOUTH_EAST_REAR = 26;

    public int position;

    public long step;
    public CellType type;

    public Region3D<E, F> mine;
    public Region3D<E,F> out;

    /**
     * Constructor of class with parameters:
     *
     * @param mine RegionNumeric into field that send the updates
     * @param out RegionNumeric external field that send the updates
     * @param step the number of step in which send the updates
     * @param type the celltype of cell that send the updates
     */
    public DistributedRegion3D(Region3D<E,F> mine, Region3D<E,F> out,long step,CellType type)
    {
        super();
        this.mine = mine.clone();
        this.out = out.clone();
        this.step = step;
        this.type = type;
    }

    /**
     * Creates a new region used for swapping.
     * @param mine The <i>mine</i> region relative to the cell <code>type</code>.
     * @param out The <i>out</i> region relative to the cell <code>type</code>.
     * @param step Simulation step at which this region is referring.
     * @param type Identifies the cell that created this region.
     * @param position Specify the position of this cell relative to the cell <code>type</code>.
     */
    public DistributedRegion3D(Region3D<E,F> mine, Region3D<E,F> out, long step, CellType type, int position)
    {
        super();
        this.mine = mine.clone();
        this.out = out.clone();
        this.step = step;
        this.type = type;
        this.position = position;
    }

    public Region3D<E,F> getmine() { return mine; }
    public void setmine(Region3D<E,F> mine) { this.mine = mine; }
    public Region3D<E,F> getout() { return out; }
    public void setout(Region3D<E,F> out) { this.out = out;}

    @Override
    public long getStep() {	return step;}
    public void setstep(long step) {this.step = step;}
    public CellType gettype() { return type; }
    public void settype(CellType type) { this.type = type; }

    @Override
    public int getPosition() { return position; }


}
