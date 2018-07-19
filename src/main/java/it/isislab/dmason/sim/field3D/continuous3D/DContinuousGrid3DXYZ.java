package it.isislab.dmason.sim.field3D.continuous3D;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field3D.continuous3D.region.RegionDouble3D;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field3D.DistributedRegion3D;
import it.isislab.dmason.sim.field.support.field3D.region.Region3D;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import sim.engine.SimState;
import sim.util.Double3D;

public class DContinuousGrid3DXYZ extends DContinuousGrid3D implements TraceableField{

    private static Logger logger = Logger.getLogger(DContinuousGrid3DXYZ.class.getCanonicalName());
    /** Name of the field. Used to identify fields in simulation using several fields. */
    private String name;

    private String topicPrefix = "";

    /** Number of neighbors of this cell, that is also the number of regions to create and of topics to publish/subscribe */
    protected int numNeighbors;

    // -----------------------------------------------------------------------
    // GLOBAL INSPECTOR ------------------------------------------------------
    // -----------------------------------------------------------------------
    /** List of parameters to trace */
    private ArrayList<String> tracingFields;
    /** The image to send */
    private BufferedImage currentBitmap;
    /** Simulation's time when currentBitmap was generated */
    private double currentTime;
    /** Statistics to send */
    HashMap<String, Object> currentStats;
    /** True if the global inspector requested graphics **/
    boolean isTracingGraphics;

    // -----------------------------------------------------------------------
    // GLOBAL PARAMETERS -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Java class of current simulation */
    protected Class<? extends SimState> simClass;
    /** List of global parameters. These must be synchronized among fields at each step */
    protected ArrayList<String> globalsNames;
    /** List of methods called for global parameters. Used for increased speed */
    protected ArrayList<Method> globalsMethods;
    /** Will contain globals properties */

    // -----------------------------------------------------------------------
    // ZOOM VIEWER -----------------------------------------------------------
    // -----------------------------------------------------------------------
    //private ZoomArrayList<RemotePositionedAgent> tmp_zoom = new ZoomArrayList<RemotePositionedAgent>();

    private File f;
    private FileWriter fw;
    private BufferedWriter bw;

    public DContinuousGrid3DXYZ(double discretization, double width, double height, double length, SimState sm, int max_distance, int i, int j,int z, int rows, int columns, int depths, String name, String prefix,boolean isToroidal) {
        super(discretization, width, height, length);
        this.width = width;
        this.height = height;
        this.length = length;
        this.sm = sm;
        this.AOI=max_distance;
        this.rows=rows;
        this.columns = columns;
        this.depths = depths;
        this.cellType = new CellType(i, j, z);
        this.updates_cache = new ArrayList<Region3D<Double, Double3D>>();
        this.name=name;
        this.topicPrefix=prefix;
        setToroidal(isToroidal);
        createRegions();

//		f = new File("/home/matdar/Scrivania/test/"+cellType+".txt");
//		try {
//			fw = new FileWriter(f);
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		bw = new BufferedWriter(fw);
//		try {
//			f.createNewFile();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

//		tracingFields = new ArrayList<String>();
//		try
//		{
//			currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
//		}
//		catch(Exception e)
//		{
//			System.out.println("Do not use the GlobalViewer, the requirements of the simulation exceed the limits of the BufferedImage.\n");
//		}
        currentTime = sm.schedule.getTime();
        currentStats = new HashMap<String, Object>();
        isTracingGraphics = false;

        // Initialize variables for GlobalParameters
        globals = new VisualizationUpdateMap<String, Object>();
        globalsNames = new ArrayList<String>();
        globalsMethods = new ArrayList<Method>();

    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public boolean setDistributedObjectLocation(final Double3D location, Object remoteObject, SimState sm)
            throws DMasonException {
        // TODO Auto-generated method stub
        RemotePositionedAgent<Double3D> rm = null;
        if (remoteObject instanceof RemotePositionedAgent){
            if(((RemotePositionedAgent) remoteObject).getPos() instanceof Double3D){
                rm = (RemotePositionedAgent<Double3D>) remoteObject;
            }
            else{throw new DMasonException("Cast Exception setDistributedObjectLocation					//, second input parameter RemotePositionedAgent<E>, E must be a Double3D");}
        }
        else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter must be a RemotePositionedAgent<>");}

        if(setAgents(rm, location)){
            return true;
        }else{
            String errorMessage = String.format("Agent %s at step %d tried to set position (%f - %f - %f): out of boundaries on cell %s. (ex OH MY GOD!)",
                    rm.getId(),this.sm.schedule.getSteps(), location.x, location.y,location.z, cellType);
            logger.severe( errorMessage );
        }

        return false;
    }

    private boolean setAgents(RemotePositionedAgent<Double3D> rm, Double3D location) {
        // TODO Auto-generated method stub
        if(rmap.NORTH_WEST_FRONT_MINE!=null && rmap.NORTH_WEST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//				tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.NORTH_WEST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.WEST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_WEST_REAR_MINE!=null && rmap.NORTH_WEST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//					tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.NORTH_WEST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.WEST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.NORTH_EAST_FRONT_MINE!=null && rmap.NORTH_EAST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//						tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.NORTH_EAST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.EAST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_EAST_REAR_MINE!=null && rmap.NORTH_EAST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//							tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.NORTH_EAST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.EAST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.SOUTH_WEST_FRONT_MINE!=null && rmap.SOUTH_WEST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//								tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.SOUTH_WEST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.WEST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_WEST_REAR_MINE!=null && rmap.SOUTH_WEST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//									tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.SOUTH_WEST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.WEST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_EAST_FRONT_MINE!=null && rmap.SOUTH_EAST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//										tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.SOUTH_EAST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.EAST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_EAST_REAR_MINE!=null && rmap.SOUTH_EAST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//											tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
            rmap.SOUTH_EAST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.EAST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_FRONT_MINE!=null && rmap.NORTH_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//												tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.NORTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
//
//												if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//													tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.NORTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_REAR_MINE!=null && rmap.NORTH_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//													if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//														tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.NORTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//														if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//															tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.NORTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.NORTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.SOUTH_FRONT_MINE!=null && rmap.SOUTH_FRONT_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//															if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.SOUTH_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																	tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.SOUTH_WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_REAR_MINE!=null && rmap.SOUTH_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																	if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																		tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.SOUTH_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																			tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.SOUTH_EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.SOUTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.WEST_FRONT_MINE!=null && rmap.WEST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																				tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.WEST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.WEST_REAR_MINE!=null && rmap.WEST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																					tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.WEST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.EAST_FRONT_MINE!=null && rmap.EAST_FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																						tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.EAST_FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.EAST_REAR_MINE!=null && rmap.EAST_REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																							tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            rmap.EAST_REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.NORTH_MINE!=null && rmap.NORTH_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																								tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.NORTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_MINE!=null && rmap.SOUTH_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
//
//																								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																									tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.WEST_MINE!=null && rmap.WEST_MINE.isMine(location.x, location.y, location.z) ){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																										tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.WEST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.EAST_MINE!=null && rmap.EAST_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																											tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.EAST_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.FRONT_MINE!=null && rmap.FRONT_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																												tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.FRONT_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.REAR_MINE!=null && rmap.REAR_MINE.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																												if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																													tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            myfield.addAgents(new EntryAgent<Double3D>(rm, location));
            return rmap.REAR_MINE.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (myfield.isMine(location.x, location.y, location.z)){
            //if(isTracingGraphics)/*system management viewer*/
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

//																													if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
//																														tmp_zoom.add(rm);
            //if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
            //GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

            return myfield.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.NORTH_WEST_FRONT_OUT != null && rmap.NORTH_WEST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_WEST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.NORTH_WEST_REAR_OUT!=null && rmap.NORTH_WEST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_WEST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.NORTH_EAST_FRONT_OUT != null && rmap.NORTH_EAST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_EAST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.NORTH_EAST_REAR_OUT!= null && rmap.NORTH_EAST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_EAST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.SOUTH_WEST_FRONT_OUT!=null && rmap.SOUTH_WEST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_WEST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.SOUTH_WEST_REAR_OUT!=null && rmap.SOUTH_WEST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_WEST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.SOUTH_EAST_FRONT_OUT!=null && rmap.SOUTH_EAST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_EAST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.SOUTH_EAST_REAR_OUT!=null && rmap.SOUTH_EAST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_EAST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.NORTH_FRONT_OUT!=null && rmap.NORTH_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_WEST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_EAST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_REAR_OUT!=null && rmap.NORTH_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.SOUTH_FRONT_OUT!=null && rmap.SOUTH_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_WEST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_EAST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if (rmap.SOUTH_REAR_OUT!=null && rmap.SOUTH_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if (rmap.WEST_FRONT_OUT!=null && rmap.WEST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.WEST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.WEST_REAR_OUT!=null && rmap.WEST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.WEST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.EAST_FRONT_OUT!= null && rmap.EAST_FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.EAST_FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if (rmap.EAST_REAR_OUT!=null && rmap.EAST_REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.EAST_REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.NORTH_OUT!=null && rmap.NORTH_OUT.isMine(location.x, location.y, location.z)){
            return rmap.NORTH_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.SOUTH_OUT!=null && rmap.SOUTH_OUT.isMine(location.x, location.y, location.z)){
            return rmap.SOUTH_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.WEST_OUT!=null && rmap.WEST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.WEST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }else
        if(rmap.EAST_OUT!=null && rmap.EAST_OUT.isMine(location.x, location.y, location.z)){
            return rmap.EAST_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if(rmap.FRONT_OUT!=null && rmap.FRONT_OUT.isMine(location.x, location.y, location.z)){
            return rmap.FRONT_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        } else
        if (rmap.REAR_OUT!=null && rmap.REAR_OUT.isMine(location.x, location.y, location.z)){
            return rmap.REAR_OUT.addAgents(new EntryAgent<Double3D>(rm, location));
        }




        return false;
    }

    @Override
    public Double3D getAvailableRandomLocation() {
        // TODO Auto-generated method stub
        double shiftx = ((DistributedState)sm).random.nextDouble();
        double shifty = ((DistributedState)sm).random.nextDouble();
        double shiftz = ((DistributedState)sm).random.nextDouble();

        double x = own_x+my_width*shiftx;
        double y = own_y+my_height*shifty;
        double z = own_z+my_length*shiftz;
        return new Double3D(x,y,z);
    }


    @Override
    public synchronized boolean synchro() {
        // TODO Auto-generated method stub
        ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
        Connection connWorker = (Connection)((DistributedState<?>)sm).getCommunicationWorkerConnection();
        //long startMem = 0,startPub=0,startUpd=0,endUpd=0;
        if(this.getState().schedule.getSteps()!=0){
            //startMem= System.currentTimeMillis();
            clear_ghost_regions();
            //SAVE AGENTS IN THE GHOST SECTION
            memorizeRegionOut();

            for(EntryAgent<Double3D> e : myfield.values()){
                RemotePositionedAgent<Double3D> rm = e.r;
                Double3D loc = e.l;
                rm.setPos(loc);
                sm.schedule.scheduleOnce(rm);
                ((DistributedState<Double3D>)sm).addToField(rm, loc);
            }
        }
        //long endMem=System.currentTimeMillis();
        //startPub=System.currentTimeMillis();
        // Publish the regions to correspondent topics for the neighbors
        publishRegions(connWorker);
        //long endPub=System.currentTimeMillis();
        // Process information received from neighbor
        //startUpd = System.currentTimeMillis();
        processUpdates();
        //endUpd = System.currentTimeMillis();
//		if(this.getState().schedule.getSteps() !=0){
//			// Update ZoomViewer (if any)
////			if(conn!=null &&
////					((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
////			{
////				try {
////					tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
////					conn.publishToTopic(tmp_zoom,topicPrefix+"GRAPHICS"+cellType,name);
////					tmp_zoom=new ZoomArrayList<RemotePositionedAgent>();
////				} catch (Exception e1) {
////					// TODO Auto-generated catch block
////					e1.printStackTrace();
////				}
////			}
//		}
//		if(this.getState().schedule.getSteps()!=0){
//			try {
//				StringBuilder sb = new StringBuilder();
//				sb.append(this.getState().schedule.getSteps());
//				sb.append(",");
//				sb.append((endMem-startMem));
//				sb.append(",");
//				sb.append((endPub-startPub));
//				sb.append(",");
//				sb.append((endUpd-startUpd));
//				sb.append("\n");
//				bw.write(sb.toString());
//				bw.flush();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
        return true;
    }

    protected void processUpdates() {
        // Take from UpdateMap the updates for current last terminated step and use
        // verifyUpdates() to elaborate informations
        PriorityQueue<Object> q;
        try
        {
            q = updates.getUpdates(sm.schedule.getSteps()-1, numNeighbors);
            while(!q.isEmpty())
            {
                DistributedRegion3D<Double, Double3D> region=(DistributedRegion3D<Double,Double3D>)q.poll();
                verifyUpdates(region);
            }
        } catch (InterruptedException e1) {e1.printStackTrace(); System.out.println("ciao");} catch (DMasonException e1) {e1.printStackTrace(); }

        for(Region3D<Double, Double3D> region : updates_cache)
            for(EntryAgent<Double3D> e_m:region.values())
                ((DistributedState<Double3D>)sm).addToField(e_m.r,e_m.l);


        this.reset();

    }

    private boolean reset() {
        myfield.clear();

        Class o=rmap.getClass();

        Field[] fields = o.getDeclaredFields();

        for (int z = 0; z < fields.length; z++)
        {
            fields[z].setAccessible(true);
            try
            {
                String name=fields[z].getName();
                Method method = o.getMethod("get"+name, null);
                Object returnValue = method.invoke(rmap, null);
                if(returnValue!=null)
                {
                    //System.out.println("clear region "+name);
                    Region3D<Double,Double3D> region=((Region3D<Double,Double3D>)returnValue);
                    region.clear();
                }
            }
            catch (IllegalArgumentException e){e.printStackTrace(); return false;}
            catch (IllegalAccessException e) {e.printStackTrace();return false;}
            catch (SecurityException e) {e.printStackTrace();return false;}
            catch (NoSuchMethodException e) {e.printStackTrace();return false;}
            catch (InvocationTargetException e) {e.printStackTrace();return false;}
        }
        return true;

    }

    private void verifyUpdates(DistributedRegion3D<Double, Double3D> box) {
        Region3D<Double,Double3D> r_mine=box.out;
        Region3D<Double,Double3D> r_out=box.mine;

        for(String agent_id : r_mine.keySet())
        {
            EntryAgent<Double3D> e_m = r_mine.get(agent_id);
            RemotePositionedAgent<Double3D> rm=e_m.r;
            ((DistributedState<Double3D>)sm).addToField(rm,e_m.l);
            rm.setPos(e_m.l);
            sm.schedule.scheduleOnce(rm);
        }

        updates_cache.add(r_out);

    }

    protected void publishRegions(Connection connWorker) {
        // TODO Auto-generated method stub
        if(rmap.NORTH_WEST_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_WEST_FRONT_MINE,rmap.NORTH_WEST_FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType,DistributedRegion3D.NORTH_WEST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NWF", name);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_WEST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_WEST_REAR_MINE, rmap.NORTH_WEST_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.NORTH_WEST_REAR);

                connWorker.publishToTopic(dr, topicPrefix+cellType+"NWR", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.SOUTH_WEST_FRONT_OUT!=null){
            try{
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_WEST_FRONT_MINE, rmap.SOUTH_WEST_FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType, DistributedRegion3D.SOUTH_WEST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SWF", name);
            }catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        if(rmap.SOUTH_WEST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_WEST_REAR_MINE, rmap.SOUTH_WEST_REAR_OUT,
                        sm.schedule.getSteps()-1,cellType,DistributedRegion3D.SOUTH_WEST_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SWR", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_EAST_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_EAST_FRONT_MINE,rmap.NORTH_EAST_FRONT_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.NORTH_EAST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NEF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_EAST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_EAST_REAR_MINE, rmap.NORTH_EAST_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.NORTH_EAST_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NER", name);

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.SOUTH_EAST_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_EAST_FRONT_MINE, rmap.SOUTH_EAST_FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType,DistributedRegion3D.SOUTH_EAST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SEF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if (rmap.SOUTH_EAST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double,Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_EAST_REAR_MINE, rmap.SOUTH_EAST_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.SOUTH_EAST_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SER", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_FRONT_MINE, rmap.NORTH_FRONT_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.NORTH_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        if(rmap.NORTH_WEST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_WEST_MINE, rmap.NORTH_WEST_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.NORTH_WEST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NW", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_REAR_MINE, rmap.NORTH_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.NORTH_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NR", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.NORTH_EAST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_EAST_MINE, rmap.NORTH_EAST_OUT,
                        sm.schedule.getSteps()-1,cellType, DistributedRegion3D.NORTH_EAST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"NE", name);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        if(rmap.SOUTH_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double, Double3D>(rmap.SOUTH_FRONT_MINE, rmap.SOUTH_FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType, DistributedRegion3D.SOUTH_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.SOUTH_WEST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_WEST_MINE, rmap.SOUTH_WEST_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.SOUTH_WEST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SW", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.SOUTH_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_REAR_MINE, rmap.SOUTH_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.SOUTH_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SR", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.SOUTH_EAST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_EAST_MINE, rmap.SOUTH_EAST_OUT,
                        sm.schedule.getSteps()-1,cellType,DistributedRegion3D.SOUTH_EAST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"SE", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        if(rmap.WEST_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.WEST_FRONT_MINE, rmap.WEST_FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType,DistributedRegion3D.WEST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"WF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.WEST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.WEST_REAR_MINE, rmap.WEST_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.WEST_REAR);
                connWorker.publishToTopic(dr,topicPrefix+cellType+"WR", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.EAST_FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double, Double3D>(rmap.EAST_FRONT_MINE, rmap.EAST_FRONT_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.EAST_FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"EF", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        if(rmap.EAST_REAR_OUT!=null){
            try {
                DistributedRegion3D<Double,Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.EAST_REAR_MINE, rmap.EAST_REAR_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.EAST_REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"ER", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        if(rmap.NORTH_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.NORTH_MINE, rmap.NORTH_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.NORTH);
                connWorker.publishToTopic(dr,topicPrefix+cellType+"N", name);

//				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//			    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
//
//			    objectOutputStream.writeObject(dr);
//			    objectOutputStream.flush();
//			    objectOutputStream.close();
//			    ByteArrayOutputStream byteOutputStream2 = new ByteArrayOutputStream();
//			    ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(byteOutputStream2);
//
//			    objectOutputStream2.writeObject(dr.mine);
//			    objectOutputStream2.flush();
//			    objectOutputStream2.close();
//			    ByteArrayOutputStream byteOutputStream3 = new ByteArrayOutputStream();
//			    ObjectOutputStream objectOutputStream3 = new ObjectOutputStream(byteOutputStream3);
//
//			    objectOutputStream3.writeObject(dr.out);
//			    objectOutputStream3.flush();
//			    objectOutputStream3.close();
//
//			    ByteArrayOutputStream byteOutputStream4 = new ByteArrayOutputStream();
//			    ObjectOutputStream objectOutputStream4 = new ObjectOutputStream(byteOutputStream4);
//
//			    objectOutputStream4.writeObject(rmap.NORTH_MINE);
//			    objectOutputStream4.flush();
//			    objectOutputStream4.close();
//
//			    ByteArrayOutputStream byteOutputStream5 = new ByteArrayOutputStream();
//			    ObjectOutputStream objectOutputStream5 = new ObjectOutputStream(byteOutputStream5);
//
//			    objectOutputStream5.writeObject(rmap.NORTH_OUT);
//			    objectOutputStream5.flush();
//			    objectOutputStream5.close();
//			    if(cellType.pos_i==0 && cellType.pos_j==0 && cellType.pos_z==0)
//			    	System.out.println("cella: "+cellType +"	step: "+this.sm.schedule.getSteps()+";	dr size:"+byteOutputStream.size()+";	# agent in mine: "+dr.mine.values().size()+";	# agent in out: "+dr.out.size()+";	dr.mine size: "+byteOutputStream2.size()+ ";	dr.out size: "+byteOutputStream3.size() +";	rmap mine: "+byteOutputStream4.size()+";	rmap out: "+byteOutputStream5.size());
//


            } catch (Exception e) {
                // TODO: handle exception
                //System.out.println(topicPrefix+cellType+"N");
                e.printStackTrace();
            }

        }

        if(rmap.SOUTH_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.SOUTH_MINE, rmap.SOUTH_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.SOUTH);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"S", name);
            } catch (Exception e) {
                //System.out.println(topicPrefix+cellType+"S");
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.WEST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.WEST_MINE, rmap.WEST_OUT,
                        sm.schedule.getSteps()-1, cellType,DistributedRegion3D.WEST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"W", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.EAST_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double, Double3D>(rmap.EAST_MINE, rmap.EAST_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.EAST);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"E", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if(rmap.FRONT_OUT!=null){
            try {
                DistributedRegion3D<Double,Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.FRONT_MINE, rmap.FRONT_OUT,
                        sm.schedule.getSteps()-1,cellType, DistributedRegion3D.FRONT);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"F", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        if (rmap.REAR_OUT!=null){
            try {
                DistributedRegion3D<Double, Double3D> dr = new DistributedRegion3D<Double,Double3D>(rmap.REAR_MINE, rmap.REAR_OUT,
                        sm.schedule.getSteps()-1, cellType, DistributedRegion3D.REAR);
                connWorker.publishToTopic(dr, topicPrefix+cellType+"R", name);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

    /**
     * Method written with Java Reflect that memorizes the agent of out regions in the update cache
     */
    private void memorizeRegionOut()
    {
        Class o=rmap.getClass();

        Field[] fields = o.getDeclaredFields();
        for (int z = 0; z < fields.length; z++)
        {
            fields[z].setAccessible(true);
            try
            {
                String name=fields[z].getName();
                Method method = o.getMethod("get"+name, null);
                Object returnValue = method.invoke(rmap, null);
                if(returnValue!=null)
                {
                    Region3D<Double,Double3D> region=((Region3D<Double,Double3D>)returnValue);
                    if(name.contains("OUT"))
                    {
                        //System.out.println("memorizeRegionOut");
                        updates_cache.add(region.clone());


                    }
                }
            }
            catch (IllegalArgumentException e){e.printStackTrace();}
            catch (IllegalAccessException e) {e.printStackTrace();}
            catch (SecurityException e) {e.printStackTrace();}
            catch (NoSuchMethodException e) {e.printStackTrace();}
            catch (InvocationTargetException e) {e.printStackTrace();}
        }
    }

    private void clear_ghost_regions() {
        // TODO Auto-generated method stu
        for(Region3D<Double, Double3D> region:updates_cache )
            for (EntryAgent<Double3D> rm : region.values())
                this.remove(rm.r);

        updateFields();
        updates_cache=new ArrayList<Region3D<Double,Double3D>>();
    }


    /**
     * This method, written with Java Reflect, follows two logical ways for all the regions:
     * - if a region is an out one, the agent's location is updated and it's insert a new Entry
     * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
     * 		for the next step)
     * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
     */
    private void updateFields()
    {
        Class o=rmap.getClass();

        Field[] fields = o.getDeclaredFields();
        for (int z = 0; z < fields.length; z++)
        {
            fields[z].setAccessible(true);
            try
            {
                String name=fields[z].getName();
                Method method = o.getMethod("get"+name, null);
                Object returnValue = method.invoke(rmap, null);
                if(returnValue!=null)
                {
                    Region3D<Double,Double3D> region=((Region3D<Double,Double3D>)returnValue);
                    if(name.contains("OUT"))
                    {

                        for(String agent_id : region.keySet())
                        {
                            //System.out.println("UpdateFields");
                            EntryAgent<Double3D> e = region.get(agent_id);
                            RemotePositionedAgent<Double3D> rm=e.r;
                            rm.setPos(e.l);
                            this.remove(rm);
                        }
                    }
                }
            }
            catch (IllegalArgumentException e){e.printStackTrace();}
            catch (IllegalAccessException e) {e.printStackTrace();}
            catch (SecurityException e) {e.printStackTrace();}
            catch (NoSuchMethodException e) {e.printStackTrace();}
            catch (InvocationTargetException e) {e.printStackTrace();}
        }
    }



    @Override
    public DistributedState<Double3D> getState() {
        // TODO Auto-generated method stub
        return (DistributedState) sm;
    }

    @Override
    public void setTable(HashMap table) {
        ConnectionJMS conn = (ConnectionJMS) ((DistributedState<?>)sm).getCommunicationManagementConnection();
        if(conn!=null)
            conn.setTable(table);
    }

    @Override
    public String getDistributedFieldID() {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public UpdateMap getUpdates() {
        // TODO Auto-generated method stub
        return updates;
    }

    @Override
    public VisualizationUpdateMap<String, Object> getGlobals() {
        // TODO Auto-generated method stub
        return globals;
    }

    @Override
    public boolean createRegions(QuadTree... cell) {
        // TODO Auto-generated method stub
        if(cell.length > 1) return false;

        if(cellType.pos_j<(width%columns))
            own_x=(int)Math.floor(width/columns+1)*cellType.pos_j;
        else
            own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(cellType.pos_j-((width%columns)));

        if(cellType.pos_i<(height%rows))
            own_y=(int)Math.floor(height/rows+1)*cellType.pos_i;
        else
            own_y=(int)Math.floor(height/rows+1)*((height%rows))+(int)Math.floor(height/rows)*(cellType.pos_i-((height%rows)));

        if(cellType.pos_z<(length%depths))
            own_z = (int) Math.floor(length/depths+1)*cellType.pos_z;
        else
            own_z = (int) Math.floor(length/depths+1)*((length%depths))+(int) Math.floor(length/depths)*(cellType.pos_z-((length%depths)));

        // own width, height and lenght
        if(cellType.pos_j<(width%columns))
            my_width=(int) Math.floor(width/columns+1);
        else
            my_width=(int) Math.floor(width/columns);

        if(cellType.pos_i<(height%rows))
            my_height=(int) Math.floor(height/rows+1);
        else
            my_height=(int) Math.floor(height/rows);

        if(cellType.pos_z < (length%depths))
            my_length = (int) Math.floor((length/depths+1));
        else
            my_length = (int) Math.floor(length/depths);

        //		System.out.println("<"+cellType.pos_i+cellType.pos_j+cellType.pos_z+"> "+my_width +" - "+my_height+" - "+my_length);
        //		System.out.println("<"+cellType.pos_i+cellType.pos_j+cellType.pos_z+"> "+own_x +" - "+own_y+" - "+own_z);
        //calculating the neighbors
        for (int k = -1; k <= 1; k++)
        {
            for (int k2 = -1; k2 <= 1; k2++)
            {
                for(int k3 = -1; k3 <= 1; k3++){
                    int v1=cellType.pos_i+k;
                    int v2=cellType.pos_j+k2;
                    int v3=cellType.pos_z+k3;
                    if(v1>=0 && v2 >=0 && v1<rows && v2<columns && v3>=0 && v3<depths)
                        if( v1!=cellType.pos_i || v2!=cellType.pos_j || v3!=cellType.pos_z)
                        {
                            neighborhood.add(v1+""+v2+""+v3);
                        }
                }
            }
        }
        //System.out.println("<"+cellType.pos_i+cellType.pos_j+cellType.pos_z+">"+neighborhood);
        //System.out.println(isToroidal());
        if(isToroidal())
            makeToroidalSections();
        else
            makeNoToroidalSections();

        return true;

    }

    private void makeNoToroidalSections() {
        // TODO Auto-generated method stub
        numNeighbors=26;

        myfield = new RegionDouble3D(own_x+AOI, own_y+AOI, own_z+AOI, own_x+my_width-AOI, own_y+my_height-AOI, own_z+my_length-AOI);

        //corner
        rmap.NORTH_WEST_FRONT_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z+my_length-AOI, own_x+AOI, own_y+my_height, own_z+my_length);
        rmap.NORTH_WEST_FRONT_OUT = new RegionDouble3D(own_x-AOI, own_y+my_height, own_z+my_length,own_x, own_y+my_height,own_z+my_length+AOI);

        rmap.NORTH_EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI, own_y+my_height-AOI, own_z+my_length-AOI, own_x+my_width, own_y+my_height, own_z+my_length);
        rmap.NORTH_EAST_FRONT_OUT = new RegionDouble3D(own_x+my_width,own_y+my_height,own_z+my_length,own_x+my_width+AOI,own_y+my_height+AOI,own_z+my_length+AOI);

        rmap.NORTH_WEST_REAR_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z, own_x+AOI, own_y+my_height, own_z+AOI);
        rmap.NORTH_WEST_REAR_OUT = new RegionDouble3D(own_x-AOI,own_y+my_height,own_z-AOI,own_x,own_y+my_height+AOI,own_z);

        rmap.NORTH_EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.NORTH_EAST_REAR_OUT = new RegionDouble3D(own_x+my_width,own_y+my_height,own_z-AOI,own_x+my_width+AOI,own_y+my_height+AOI,own_z);

        rmap.SOUTH_WEST_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+AOI,own_y+AOI,own_z+my_length);
        rmap.SOUTH_WEST_FRONT_OUT = new RegionDouble3D(own_x-AOI,own_y-AOI,own_z+my_length,own_x,own_y,own_z+my_length+AOI);

        rmap.SOUTH_EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z+my_length-AOI,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_EAST_FRONT_OUT = new RegionDouble3D(own_x+my_width,own_y-AOI,own_z+my_length,own_x+my_width+AOI,own_y,own_z+my_length+AOI);

        rmap.SOUTH_WEST_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+AOI,own_z+AOI);
        rmap.SOUTH_WEST_REAR_OUT = new RegionDouble3D(own_x-AOI,own_y-AOI,own_z-AOI,own_x,own_y,own_z);

        rmap.SOUTH_EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+AOI,own_z+AOI);
        rmap.SOUTH_EAST_REAR_OUT= new RegionDouble3D(own_x+my_width,own_y-AOI,own_z-AOI,own_x+my_width+AOI,own_y,own_z);

        //edges
        rmap.NORTH_FRONT_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z+my_length-AOI, own_x+my_width, own_y+my_height, own_z+my_length);
        rmap.NORTH_FRONT_OUT = new RegionDouble3D(own_x, own_y, own_z+my_length,own_x+my_width,own_y+my_height+AOI,own_z+my_length+AOI);

        rmap.NORTH_WEST_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.NORTH_WEST_OUT = new RegionDouble3D(own_x-AOI,own_y+my_height,own_z,own_x,own_y+my_height+AOI,own_z+my_length);

        rmap.NORTH_REAR_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.NORTH_REAR_OUT = new RegionDouble3D(own_x,own_y+my_height,own_z-AOI,own_x+my_width,own_y+my_height+AOI,own_z);

        rmap.NORTH_EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.NORTH_EAST_OUT = new RegionDouble3D(own_x+my_width,own_y+my_height,own_z,own_x+my_width+AOI,own_y+my_height+AOI,own_z+my_length);

        rmap.SOUTH_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_FRONT_OUT = new RegionDouble3D(own_x,own_y-AOI,own_z+my_length,own_x+my_width,own_y,own_z+my_length+AOI);

        rmap.SOUTH_WEST_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+AOI,own_z+my_length);
        rmap.SOUTH_WEST_OUT = new RegionDouble3D(own_x-AOI,own_y-AOI,own_z,own_x,own_y,own_z+my_length);

        rmap.SOUTH_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+AOI,own_z+AOI);
        rmap.SOUTH_REAR_OUT = new RegionDouble3D(own_x,own_y-AOI,own_z-AOI,own_x+my_width,own_y,own_z);

        rmap.SOUTH_EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_EAST_OUT = new RegionDouble3D(own_x+my_width,own_y-AOI,own_z,own_x+my_width+AOI,own_y,own_z+my_length);

        rmap.WEST_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.WEST_FRONT_OUT = new RegionDouble3D(own_x-AOI,own_y,own_z+my_length,own_x,own_y+my_height,own_z+my_length+AOI);

        rmap.WEST_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+my_height,own_z+AOI);
        rmap.WEST_REAR_OUT = new RegionDouble3D(own_x-AOI,own_y,own_z-AOI,own_x,own_y+my_height,own_z);

        rmap.EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z+my_length-AOI,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.EAST_FRONT_OUT = new RegionDouble3D(own_x+my_width,own_y,own_z+my_length,own_x+my_width+AOI,own_y+my_height,own_z+my_length+AOI);

        rmap.EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.EAST_REAR_OUT = new RegionDouble3D(own_x+my_width,own_y,own_z-AOI,own_x+my_width+AOI,own_y+my_height,own_z);

        //faces
        rmap.NORTH_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.NORTH_OUT = new RegionDouble3D(own_x,own_y+my_height,own_z,own_x+my_width,own_y+my_height+AOI,own_z+my_length);

        rmap.SOUTH_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_OUT = new RegionDouble3D(own_x,own_y-AOI,own_z,own_x+my_width,own_y,own_z+my_length);

        rmap.WEST_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.WEST_OUT = new RegionDouble3D(own_x-AOI,own_y,own_z,own_x,own_y+my_height,own_z+my_length);

        rmap.EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.EAST_OUT = new RegionDouble3D(own_x+my_width,own_y,own_z,own_x+my_width+AOI,own_y+my_height,own_z+my_length);

        rmap.FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.FRONT_OUT = new RegionDouble3D(own_x,own_y,own_z+my_length,own_x+my_width,own_y+my_height,own_z+my_length+AOI);

        rmap.REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.REAR_OUT = new RegionDouble3D(own_x,own_y,own_z-AOI,own_x+my_width,own_y+my_height,own_z);

        if(depths==1){
            //set front and rear to null
            rmap.FRONT_OUT = null;
            rmap.REAR_OUT=null;
            rmap.NORTH_FRONT_OUT=null;
            rmap.NORTH_REAR_OUT=null;
            rmap.SOUTH_FRONT_OUT=null;
            rmap.SOUTH_REAR_OUT=null;
            rmap.WEST_FRONT_OUT=null;
            rmap.WEST_REAR_OUT=null;
            rmap.EAST_FRONT_OUT=null;
            rmap.EAST_REAR_OUT=null;
            rmap.NORTH_WEST_FRONT_OUT=null;
            rmap.NORTH_WEST_REAR_OUT=null;
            rmap.SOUTH_WEST_FRONT_OUT=null;
            rmap.SOUTH_WEST_REAR_OUT = null;
            rmap.NORTH_EAST_FRONT_OUT=null;
            rmap.NORTH_EAST_REAR_OUT=null;
            rmap.SOUTH_EAST_FRONT_OUT=null;
            rmap.SOUTH_EAST_REAR_OUT=null;
            if(rows==1){
                //set north and south to null
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
                rmap.NORTH_WEST_OUT=null;
                rmap.NORTH_EAST_OUT=null;
                rmap.SOUTH_WEST_OUT=null;
                rmap.SOUTH_EAST_OUT=null;
                numNeighbors=2;
                if(cellType.pos_j==0){
                    numNeighbors=1;
                    //set west to null
                    rmap.WEST_OUT=null;
                }else if(cellType.pos_j==(columns-1)){
                    numNeighbors=1;
                    //set east to null
                    rmap.EAST_OUT=null;
                }
            } else if(rows >1 && columns==1){
                //set east and west to null
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;
                rmap.NORTH_WEST_OUT=null;
                rmap.NORTH_EAST_OUT=null;
                rmap.SOUTH_WEST_OUT=null;
                rmap.SOUTH_EAST_OUT=null;
                numNeighbors=2;
                if(cellType.pos_i==0){
                    numNeighbors=1;
                    //set north to null
                    rmap.NORTH_OUT=null;
                }else if(cellType.pos_i==(rows-1)){
                    numNeighbors=1;
                    //set south to null
                    rmap.SOUTH_OUT=null;
                }
            } else {
                numNeighbors=8;

                if(cellType.pos_i==0 ){
                    numNeighbors = 5;
                    //set north to null
                    rmap.NORTH_OUT=null;
                    rmap.NORTH_WEST_OUT=null;
                    rmap.NORTH_EAST_OUT=null;
                }

                if(cellType.pos_j == 0){
                    numNeighbors = 5;
                    //set west to null
                    rmap.WEST_OUT=null;
                    rmap.NORTH_WEST_OUT=null;
                    rmap.SOUTH_WEST_OUT=null;
                }

                if(cellType.pos_i == rows -1){
                    numNeighbors = 5;
                    //set south to null
                    rmap.SOUTH_OUT=null;
                    rmap.SOUTH_WEST_OUT=null;
                    rmap.SOUTH_EAST_OUT=null;
                }

                if(cellType.pos_j == columns -1){
                    numNeighbors = 5;
                    //set east to null
                    rmap.EAST_OUT=null;
                    rmap.NORTH_EAST_OUT=null;
                    rmap.SOUTH_EAST_OUT=null;
                }

                if((cellType.pos_i == 0 && cellType.pos_j == 0) ||
                        (cellType.pos_i == rows-1 && cellType.pos_j==0) ||
                        (cellType.pos_i == 0 && cellType.pos_j == columns -1) ||
                        (cellType.pos_i == rows-1 && cellType.pos_j == columns -1))
                    numNeighbors = 3;

            }
        }else{

            if(rows==1 && columns==1){
                //set east and west to null
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;
                rmap.WEST_FRONT_OUT=null;
                rmap.WEST_REAR_OUT=null;
                rmap.EAST_FRONT_OUT=null;
                rmap.EAST_REAR_OUT=null;
                //set north and south to null
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
                rmap.NORTH_FRONT_OUT=null;
                rmap.NORTH_WEST_OUT=null;
                rmap.NORTH_REAR_OUT=null;
                rmap.NORTH_EAST_OUT=null;
                rmap.SOUTH_FRONT_OUT=null;
                rmap.SOUTH_WEST_OUT=null;
                rmap.SOUTH_REAR_OUT=null;
                rmap.SOUTH_EAST_OUT=null;
                rmap.NORTH_WEST_FRONT_OUT=null;
                rmap.NORTH_WEST_REAR_OUT=null;
                rmap.SOUTH_WEST_FRONT_OUT=null;
                rmap.SOUTH_WEST_REAR_OUT=null;
                rmap.NORTH_EAST_FRONT_OUT=null;
                rmap.NORTH_EAST_REAR_OUT=null;
                rmap.SOUTH_EAST_FRONT_OUT=null;
                rmap.SOUTH_EAST_REAR_OUT=null;

                numNeighbors=2;
                if(cellType.pos_z==0){
                    numNeighbors=1;
                    rmap.FRONT_OUT=null;
                }else if(cellType.pos_z==(depths-1)){
                    numNeighbors=1;
                    rmap.REAR_OUT=null;
                }
            }else if(rows==1 && columns >1){
                //set north and south to null
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
                rmap.NORTH_FRONT_OUT=null;
                rmap.NORTH_WEST_OUT=null;
                rmap.NORTH_REAR_OUT=null;
                rmap.NORTH_EAST_OUT=null;
                rmap.SOUTH_FRONT_OUT=null;
                rmap.SOUTH_WEST_OUT=null;
                rmap.SOUTH_REAR_OUT=null;
                rmap.SOUTH_EAST_OUT=null;
                rmap.NORTH_WEST_FRONT_OUT=null;
                rmap.NORTH_WEST_REAR_OUT=null;
                rmap.SOUTH_WEST_FRONT_OUT=null;
                rmap.SOUTH_WEST_REAR_OUT=null;
                rmap.NORTH_EAST_FRONT_OUT=null;
                rmap.NORTH_EAST_REAR_OUT=null;
                rmap.SOUTH_EAST_FRONT_OUT=null;
                rmap.SOUTH_EAST_REAR_OUT=null;

                numNeighbors=8;
                if(cellType.pos_j==0){
                    numNeighbors=5;
                    rmap.WEST_FRONT_OUT=null;
                    rmap.WEST_REAR_OUT=null;
                    rmap.WEST_OUT=null;
                }
                if(cellType.pos_j==(columns-1)){
                    numNeighbors=5;
                    rmap.EAST_FRONT_OUT=null;
                    rmap.EAST_REAR_OUT=null;
                    rmap.EAST_OUT=null;
                }
                if(cellType.pos_z==0){
                    numNeighbors=5;
                    rmap.WEST_FRONT_OUT=null;
                    rmap.FRONT_OUT=null;
                    rmap.EAST_FRONT_OUT=null;
                }
                if(cellType.pos_z==(depths-1)){
                    numNeighbors=5;
                    rmap.WEST_REAR_OUT=null;
                    rmap.REAR_OUT=null;
                    rmap.EAST_REAR_OUT=null;
                }
                if((cellType.pos_j==0 && cellType.pos_z==0)
                        || (cellType.pos_j==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_j==(columns-1) && cellType.pos_z==0)
                        || (cellType.pos_j==(columns-1) && cellType.pos_z==(depths-1)))
                    numNeighbors=3;
            }else if (rows>1 && columns==1){
                //set west and east to null
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;
                rmap.NORTH_WEST_OUT=null;
                rmap.NORTH_EAST_OUT=null;
                rmap.SOUTH_WEST_OUT=null;
                rmap.SOUTH_EAST_OUT=null;
                rmap.WEST_FRONT_OUT=null;
                rmap.WEST_REAR_OUT=null;
                rmap.EAST_FRONT_OUT=null;
                rmap.EAST_REAR_OUT=null;
                rmap.NORTH_WEST_FRONT_OUT=null;
                rmap.NORTH_WEST_REAR_OUT=null;
                rmap.SOUTH_WEST_FRONT_OUT=null;
                rmap.SOUTH_WEST_REAR_OUT=null;
                rmap.NORTH_EAST_FRONT_OUT=null;
                rmap.NORTH_EAST_REAR_OUT=null;
                rmap.SOUTH_EAST_FRONT_OUT=null;
                rmap.SOUTH_EAST_REAR_OUT=null;

                numNeighbors=8;
                if(cellType.pos_i==0){
                    rmap.NORTH_OUT=null;
                    rmap.NORTH_FRONT_OUT=null;
                    rmap.NORTH_REAR_OUT=null;
                    numNeighbors=5;
                }
                if(cellType.pos_i==(rows-1)){
                    rmap.SOUTH_OUT=null;
                    rmap.SOUTH_FRONT_OUT=null;
                    rmap.SOUTH_REAR_OUT=null;
                    numNeighbors=5;
                }
                if(cellType.pos_z==0){
                    rmap.NORTH_FRONT_OUT=null;
                    rmap.FRONT_OUT=null;
                    rmap.SOUTH_FRONT_OUT=null;
                    numNeighbors=5;
                }
                if (cellType.pos_z==(depths-1)){
                    rmap.NORTH_REAR_OUT=null;
                    rmap.REAR_OUT=null;
                    rmap.SOUTH_REAR_OUT=null;
                    numNeighbors=5;
                }

                if((cellType.pos_i==0 && cellType.pos_z==0)
                        || (cellType.pos_i==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_i==(rows-1) && cellType.pos_z==0)
                        || (cellType.pos_i==(rows-1) && cellType.pos_z==(depths-1))){
                    numNeighbors=3;
                }
            } else{

                numNeighbors=26;

                //faces
                if (cellType.pos_i==0){
                    numNeighbors=17;
                    rmap.NORTH_OUT=null;
                    rmap.NORTH_FRONT_OUT=null;
                    rmap.NORTH_WEST_OUT=null;
                    rmap.NORTH_REAR_OUT=null;
                    rmap.NORTH_EAST_OUT=null;
                    rmap.NORTH_WEST_FRONT_OUT=null;
                    rmap.NORTH_WEST_REAR_OUT=null;
                    rmap.NORTH_EAST_FRONT_OUT=null;
                    rmap.NORTH_EAST_REAR_OUT=null;

                }
                if (cellType.pos_i==(rows-1)){
                    numNeighbors=17;
                    rmap.SOUTH_OUT=null;
                    rmap.SOUTH_FRONT_OUT=null;
                    rmap.SOUTH_WEST_OUT=null;
                    rmap.SOUTH_REAR_OUT=null;
                    rmap.SOUTH_EAST_OUT=null;
                    rmap.SOUTH_WEST_FRONT_OUT=null;
                    rmap.SOUTH_WEST_REAR_OUT=null;
                    rmap.SOUTH_EAST_FRONT_OUT=null;
                    rmap.SOUTH_EAST_REAR_OUT=null;
                }
                if(cellType.pos_j==0){
                    numNeighbors=17;
                    rmap.WEST_OUT=null;
                    rmap.NORTH_WEST_OUT=null;
                    rmap.SOUTH_WEST_OUT=null;
                    rmap.WEST_FRONT_OUT=null;
                    rmap.WEST_REAR_OUT=null;
                    rmap.NORTH_WEST_FRONT_OUT=null;
                    rmap.NORTH_WEST_REAR_OUT=null;
                    rmap.SOUTH_WEST_FRONT_OUT=null;
                    rmap.SOUTH_WEST_REAR_OUT=null;

                }
                if(cellType.pos_j==(columns-1)){
                    numNeighbors=17;
                    rmap.EAST_OUT=null;
                    rmap.NORTH_EAST_OUT=null;
                    rmap.SOUTH_EAST_OUT=null;
                    rmap.EAST_FRONT_OUT=null;
                    rmap.EAST_REAR_OUT=null;
                    rmap.NORTH_EAST_FRONT_OUT=null;
                    rmap.NORTH_EAST_REAR_OUT=null;
                    rmap.SOUTH_EAST_FRONT_OUT=null;
                    rmap.SOUTH_EAST_REAR_OUT=null;
                }
                if(cellType.pos_z==0){
                    numNeighbors=17;
                    rmap.FRONT_OUT=null;
                    rmap.NORTH_FRONT_OUT=null;
                    rmap.SOUTH_FRONT_OUT=null;
                    rmap.WEST_FRONT_OUT=null;
                    rmap.EAST_FRONT_OUT=null;
                    rmap.NORTH_WEST_FRONT_OUT=null;
                    rmap.SOUTH_WEST_FRONT_OUT=null;
                    rmap.NORTH_EAST_FRONT_OUT=null;
                    rmap.SOUTH_EAST_FRONT_OUT=null;
                }
                if(cellType.pos_z==(depths-1)){
                    numNeighbors=17;
                    rmap.REAR_OUT=null;
                    rmap.NORTH_REAR_OUT=null;
                    rmap.SOUTH_REAR_OUT=null;
                    rmap.WEST_REAR_OUT=null;
                    rmap.EAST_REAR_OUT=null;
                    rmap.NORTH_WEST_REAR_OUT=null;
                    rmap.SOUTH_WEST_REAR_OUT=null;
                    rmap.NORTH_EAST_REAR_OUT=null;
                    rmap.SOUTH_EAST_REAR_OUT=null;
                }

                //edges
                if((cellType.pos_i==0 && cellType.pos_j==0) || (cellType.pos_i==0 && cellType.pos_j==(columns-1))
                        || (cellType.pos_i==0 && cellType.pos_z==0) || (cellType.pos_i==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_j==0 && cellType.pos_z==0) || (cellType.pos_j==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_j==(columns-1) && cellType.pos_z==0) || (cellType.pos_j==(columns-1) && cellType.pos_z==(depths-1))
                        || (cellType.pos_i==(rows-1) && cellType.pos_j==0) || (cellType.pos_i==(rows-1) && cellType.pos_j==(columns-1))
                        || (cellType.pos_i==(rows-1) && cellType.pos_z==0) || (cellType.pos_i==(rows-1) && cellType.pos_z==(depths-1))
                        )
                    numNeighbors=11;

                //vertices
                if((cellType.pos_i==0 && cellType.pos_j==0 && cellType.pos_z==0)
                        || (cellType.pos_i==0 && cellType.pos_j==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_i==0 && cellType.pos_j==(columns-1) && cellType.pos_z==0)
                        || (cellType.pos_i==0 && cellType.pos_j==(columns-1) && cellType.pos_z==(depths-1))
                        || (cellType.pos_i==(rows-1) && cellType.pos_j==0 && cellType.pos_z==0)
                        || (cellType.pos_i==(rows-1) && cellType.pos_j==0 && cellType.pos_z==(depths-1))
                        || (cellType.pos_i==(rows-1) && cellType.pos_j==(columns-1) && cellType.pos_z==0)
                        || (cellType.pos_i==(rows-1) && cellType.pos_j==(columns-1) && cellType.pos_z==(depths-1)))
                    numNeighbors=7;
            }

        }
    }

    private void makeToroidalSections() {

        myfield = new RegionDouble3D(own_x+AOI, own_y+AOI, own_z+AOI, own_x+my_width-AOI, own_y+my_height-AOI, own_z+my_length-AOI);

        //corner
        rmap.NORTH_WEST_FRONT_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z+my_length-AOI, own_x+AOI, own_y+my_height, own_z+my_length);
        rmap.NORTH_WEST_FRONT_OUT = new RegionDouble3D((own_x-AOI+width)%width, (own_y+my_height+height)%height, (own_z+my_length+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width, (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.NORTH_EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI, own_y+my_height-AOI, own_z+my_length-AOI, own_x+my_width, own_y+my_height, own_z+my_length);
        rmap.NORTH_EAST_FRONT_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+my_height+height)%height,(own_z+my_length+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.NORTH_WEST_REAR_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z, own_x+AOI, own_y+my_height, own_z+AOI);
        rmap.NORTH_WEST_REAR_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y+my_height+height)%height,(own_z-AOI+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.NORTH_EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.NORTH_EAST_REAR_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+my_height+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.SOUTH_WEST_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+AOI,own_y+AOI,own_z+my_length);
        rmap.SOUTH_WEST_FRONT_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y-AOI+height)%height,(own_z+my_length+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.SOUTH_EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z+my_length-AOI,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_EAST_FRONT_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y-AOI+height)%height,(own_z+my_length+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.SOUTH_WEST_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+AOI,own_z+AOI);
        rmap.SOUTH_WEST_REAR_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y-AOI+height)%height,(own_z-AOI+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.SOUTH_EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+AOI,own_z+AOI);
        rmap.SOUTH_EAST_REAR_OUT= new RegionDouble3D((own_x+my_width+width)%width,(own_y-AOI+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        //edges
        rmap.NORTH_FRONT_MINE = new RegionDouble3D(own_x, own_y+my_height-AOI, own_z+my_length-AOI, own_x+my_width, own_y+my_height, own_z+my_length);
        rmap.NORTH_FRONT_OUT = new RegionDouble3D((own_x+width)%width, (own_y+my_height+height)%height, (own_z+my_length+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width, (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.NORTH_WEST_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.NORTH_WEST_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y+my_height+height)%height,(own_z+length)%length,(own_x+width)%width==0?width:(own_x+width)%width,
                (own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,(own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.NORTH_REAR_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.NORTH_REAR_OUT = new RegionDouble3D((own_x+width)%width,(own_y+my_height+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.NORTH_EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.NORTH_EAST_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+my_height+height)%height,(own_z+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.SOUTH_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_FRONT_OUT = new RegionDouble3D((own_x+width)%width,(own_y-AOI+height)%height,(own_z+my_length+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.SOUTH_WEST_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+AOI,own_z+my_length);
        rmap.SOUTH_WEST_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y-AOI+height)%height,(own_z+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.SOUTH_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+AOI,own_z+AOI);
        rmap.SOUTH_REAR_OUT = new RegionDouble3D((own_x+width)%width,(own_y-AOI+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.SOUTH_EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_EAST_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y-AOI+height)%height,(own_z+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.WEST_FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.WEST_FRONT_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y+height)%height,(own_z+my_length+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.WEST_REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+my_height,own_z+AOI);
        rmap.WEST_REAR_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y+height)%height,(own_z-AOI+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        rmap.EAST_FRONT_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z+my_length-AOI,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.EAST_FRONT_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+height)%height,(own_z+my_length+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.EAST_REAR_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.EAST_REAR_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        //faces
        rmap.NORTH_MINE = new RegionDouble3D(own_x,own_y+my_height-AOI,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.NORTH_OUT = new RegionDouble3D((own_x+width)%width,(own_y+my_height+height)%height,(own_z+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+my_height+AOI+height)%height==0?height:(own_y+my_height+AOI+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.SOUTH_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+AOI,own_z+my_length);
        rmap.SOUTH_OUT = new RegionDouble3D((own_x+width)%width,(own_y-AOI+height)%height,(own_z+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+height)%height==0?height:(own_y+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.WEST_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+AOI,own_y+my_height,own_z+my_length);
        rmap.WEST_OUT = new RegionDouble3D((own_x-AOI+width)%width,(own_y+height)%height,(own_z+length)%length,
                (own_x+width)%width==0?width:(own_x+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.EAST_MINE = new RegionDouble3D(own_x+my_width-AOI,own_y,own_z,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.EAST_OUT = new RegionDouble3D((own_x+my_width+width)%width,(own_y+height)%height,(own_z+length)%length,
                (own_x+my_width+AOI+width)%width==0?width:(own_x+my_width+AOI+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+my_length+length)%length==0?length:(own_z+my_length+length)%length);

        rmap.FRONT_MINE = new RegionDouble3D(own_x,own_y,own_z+my_length-AOI,own_x+my_width,own_y+my_height,own_z+my_length);
        rmap.FRONT_OUT = new RegionDouble3D((own_x+width)%width,(own_y+height)%height,(own_z+my_length+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+my_length+AOI+length)%length==0?length:(own_z+my_length+AOI+length)%length);

        rmap.REAR_MINE = new RegionDouble3D(own_x,own_y,own_z,own_x+my_width,own_y+my_height,own_z+AOI);
        rmap.REAR_OUT = new RegionDouble3D((own_x+width)%width,(own_y+height)%height,(own_z-AOI+length)%length,
                (own_x+my_width+width)%width==0?width:(own_x+my_width+width)%width,(own_y+my_height+height)%height==0?height:(own_y+my_height+height)%height,
                (own_z+length)%length==0?length:(own_z+length)%length);

        if(depths==1){
            rmap.FRONT_OUT=null;
            rmap.REAR_OUT=null;
            if(rows>1 && columns==1){
                numNeighbors=22;
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;
            }else if (rows==1 && columns>1){
                numNeighbors=22;
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
            }else{
                numNeighbors=24;
            }
        }else{
            if(rows==1 && columns==1){
                numNeighbors=22;
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;

            }else if(rows>1 && columns==1){
                numNeighbors=24;
                rmap.WEST_OUT=null;
                rmap.EAST_OUT=null;
            }else if (rows==1 && columns>1){
                numNeighbors=24;
                rmap.NORTH_OUT=null;
                rmap.SOUTH_OUT=null;
            }else{
                numNeighbors=26;
            }
        }

    }

	/*@Override
	public void trace(String param) {
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = true;
		else
			tracingFields.add(param);

	}

	@Override
	public void untrace(String param) {
		if (param.equals("-GRAPHICS"))
			isTracingGraphics = false;
		else
		{
			tracingFields.remove(param);
			currentStats.remove(param);
		}
	}*/

    @Override
    public boolean verifyPosition(Double3D pos) {
        // TODO Auto-generated method stub
        return (rmap.EAST_FRONT_MINE !=null && rmap.EAST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.EAST_MINE!=null && rmap.EAST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.EAST_REAR_MINE !=null && rmap.EAST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.FRONT_MINE != null && rmap.FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_EAST_FRONT_MINE != null && rmap.NORTH_EAST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_EAST_MINE != null && rmap.NORTH_EAST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_EAST_REAR_MINE != null && rmap.NORTH_EAST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_FRONT_MINE != null && rmap.NORTH_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_REAR_MINE != null && rmap.NORTH_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_WEST_FRONT_MINE != null && rmap.NORTH_WEST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_WEST_MINE != null && rmap.NORTH_WEST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.NORTH_WEST_REAR_MINE != null && rmap.NORTH_WEST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.REAR_MINE != null && rmap.REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_EAST_FRONT_MINE != null && rmap.SOUTH_EAST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_EAST_MINE != null && rmap.SOUTH_EAST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_EAST_REAR_MINE !=null && rmap.SOUTH_EAST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_FRONT_MINE != null && rmap.SOUTH_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_REAR_MINE !=null && rmap.SOUTH_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_WEST_FRONT_MINE != null && rmap.SOUTH_WEST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_WEST_MINE != null && rmap.SOUTH_WEST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.SOUTH_WEST_REAR_MINE != null && rmap.SOUTH_WEST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.WEST_FRONT_MINE != null && rmap.WEST_FRONT_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(pos.x, pos.y, pos.z))
                || (rmap.WEST_REAR_MINE != null && rmap.WEST_REAR_MINE.isMine(pos.x, pos.y, pos.z))
                || (myfield.isMine(pos.x, pos.y, pos.z));



    }

    @Override
    public void trace(String param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void untrace(String param) {
        // TODO Auto-generated method stub

    }

	@Override
	public long getCommunicationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
