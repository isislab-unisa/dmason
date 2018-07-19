package it.isislab.dmason.sim.app.DWoims3D;

import java.util.List;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field3D.continuous3D.DContinuousGrid3D;
import it.isislab.dmason.sim.field3D.continuous3D.DContinuousGrid3DFactory;
import sim.engine.SimState;
import sim.util.Double3D;

public class DWoims3D extends DistributedState<Double3D> {

    private static final long serialVersionUID = 1L;

    public static final double DIAMETER = 1;


    public static final double[][] obstInfo = { {40, 0, 0, 0}, {60, 135, 135, 135} };


    public static final double TIMESTEP = 30;

    public DContinuousGrid3D environment;
    public static double gridWidth ;
    public static double gridHeight ;
    public static double gridLenght;
    public int MODE;
    public double neighborhood = 10;
    public final static double EXTRA_SPACE = 0;

    public DWoims3D() {
        // TODO Auto-generated constructor stub
        super();
    }


    public DWoims3D(GeneralParam params, String prefix){
        super(params,new DistributedMultiSchedule<Double3D>(),prefix,params.getConnectionType());
        this.topicPrefix=prefix;
        this.MODE=params.getMode();
        this.gridWidth=params.getWidth();
        this.gridHeight=params.getHeight();
        this.gridLenght=1000;

    }

    public DWoims3D (GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix) {
        // TODO Auto-generated method stub
        super(params,new DistributedMultiSchedule<Double3D>(),prefix,params.getConnectionType());
        this.topicPrefix=prefix;
        this.MODE=params.getMode();
        this.gridWidth=params.getWidth();
        this.gridHeight=params.getHeight();
        this.gridLenght=1000;
        for (EntryParam<String, Object> entryParam : simParams) {

            try {
                this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
            } catch (IllegalArgumentException e) {

                e.printStackTrace();
            } catch (SecurityException e) {

                e.printStackTrace();
            } catch (IllegalAccessException e) {

                e.printStackTrace();
            } catch (NoSuchFieldException e) {

                e.printStackTrace();
            }

        }

        for (EntryParam<String, Object> entryParam : simParams) {

            try {
                out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

        }
    }



    public void start() {
        super.start();
        try {
            environment = DContinuousGrid3DFactory.createDContinuous3D(2*DIAMETER,gridWidth, gridHeight,1000,this,
                    super.AOI,TYPE.pos_i,TYPE.pos_j,TYPE.pos_z,super.rows,super.columns,super.lenghts,MODE,"woims", topicPrefix,true);
            init_connection();
        } catch (DMasonException e) {
            // TODO: handle exception
        }

        DWoim3D woim = new DWoim3D(this, new Double3D(0,0,0));

        int agentsToCreate=0;

        System.out.println(super.NUMAGENTS +" - "+super.NUMPEERS);

        int remainder=super.NUMAGENTS%super.NUMPEERS;
        //System.out.println(super.NUMAGENTS +"%"+ super.NUMPEERS +"="+ remainder );

        if(remainder==0){
            agentsToCreate= super.NUMAGENTS / super.NUMPEERS;
        }

        else if(remainder!=0 && TYPE.pos_i==0 && TYPE.pos_j==0 && TYPE.pos_z==0){
            agentsToCreate= (super.NUMAGENTS / super.NUMPEERS)+remainder;
        }

        else{
            agentsToCreate= super.NUMAGENTS / super.NUMPEERS;
        }
        while(environment.size()!= agentsToCreate){
            woim.setPos(environment.getAvailableRandomLocation());
            if(environment.setObjectLocation(woim, woim.pos))
            {
                //System.out.println("add Agent "+particle.getId()+" in position "+particle.getPos());
                schedule.scheduleOnce(woim);
                woim=new DWoim3D(this, new Double3D(0,0,0));
            }

        }
        for( int i = 0 ; i < obstInfo.length ; i++ )
        {
            DObstacle3D obs = new DObstacle3D(this, new Double3D(obstInfo[i][1],obstInfo[i][2],obstInfo[i][3]));
            obs.setDiameter(obstInfo[i][0]);
            environment.setObjectLocation(obs,obs.pos);

        }

        try {
            if(getTrigger()!=null)
                getTrigger().publishToTriggerTopic("Simulation cell "+environment.cellType+" ready...");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public DistributedField getField() {
        // TODO Auto-generated method stub
        return  (DistributedField) environment;
    }

    @Override
    public void addToField(RemotePositionedAgent rm, Double3D loc) {
        // TODO Auto-generated method stub
        //System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        environment.setObjectLocation(rm, loc);
    }

    @Override
    public SimState getState() {
        // TODO Auto-generated method stub
        return this;
    }



    public static void main(String[] args)
    {
        doLoop(DWoims3D.class, args);
        System.exit(0);
    }

}
