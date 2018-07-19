package it.isislab.dmason.sim.field3D.continuous3D;

import java.util.ArrayList;

import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field3D.continuous3D.region.RegionDouble3D;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field3D.region.Region3D;
import it.isislab.dmason.sim.field.support.field3D.region.RegionMap3D;
import it.isislab.dmason.sim.field3D.DistributedField3D;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.util.Double3D;

public abstract class DContinuousGrid3D extends Continuous3D implements DistributedField3D<Double3D> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CellType cellType;

    public int rows,columns,depths;


    public double own_x, own_y, own_z;

    public double my_width, my_height, my_length;

    /** Will contain globals parameters */
    public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();
    RegionDouble3D myfield;
    RegionMap3D<Double, Double3D> rmap = new RegionMap3D<Double,Double3D>();
    ArrayList<Region3D<Double, Double3D>> updates_cache;
    public int AOI;
    UpdateMap updates = new UpdateMap<Double,Double3D>();
    public SimState sm;
    public ArrayList<String> neighborhood=new ArrayList<String>();
    public boolean gui=true;
    private static boolean isToroidal;


    public DContinuousGrid3D(double discretization, double width, double height, double length) {
        super(discretization, width, height, length);
        // TODO Auto-generated constructor stub
    }


    public boolean isToroidal()
    {
        return isToroidal;
    }

    public void setToroidal(boolean isToroidal)
    {
        this.isToroidal=isToroidal;
    }

}
