package dmason.sim.app.DFlockers;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;

import dmason.util.inspection.InspectableState;
import dmason.annotation.Thin;
import dmason.annotation.batch;
import dmason.batch.data.EntryParam;
import dmason.batch.data.GeneralParam;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.*;

public class DFlockers extends DistributedState<Double2D>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DContinuous2D flockers;
    private static boolean isToroidal=true;
     
    @batch(
    	domain = "100-300",
    	suggestedValue = "250"
    )
    public double width = 150;
    @batch
    public double height = 150;
    @batch
    public int numFlockers = 20;
    @batch
    public double cohesion = 1.0;
    @batch
    public double avoidance = 1.0;
    @batch
    public double randomness = 1.0;
    @batch
    public double consistency = 1.0;
    @batch
    public double momentum = 1.0;
    @batch
    public double deadFlockerProbability = 0.1;
    @batch
    public double neighborhood = 10;
    
    public double jump = 0.7;  // how far do we move in a timestep?
    
    public double getCohesion() { return cohesion; }
    public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
    public double getAvoidance() { return avoidance; }
    public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
    public double getRandomness() { return randomness; }
    public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
    public double getConsistency() { return consistency; }
    public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
    public double getMomentum() { return momentum; }
    public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
    public int getNumFlockers() { return numFlockers; }
    public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
    public double getWidth() { return width; }
    public void setWidth(double val) { if (val > 0) width = val; }
    public double getHeight() { return height; }
    public void setHeight(double val) { if (val > 0) height = val; }
    public double getNeighborhood() { return neighborhood; }
    public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
    public double getDeadFlockerProbability() { return deadFlockerProbability; }
    public void setDeadFlockerProbability(double val) { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }
    
    public double gridWidth ;
    public double gridHeight ;   
    public int MODE;
    
    public static String topicPrefix = "";
    
    
//    int localTest = 1; int globalTest = -1;
//    public int getTest() { return localTest; }
//    public void setTest(int value) { localTest = value; }
//    public boolean globalTest() { return true; }
//    public int getGlobalTest() { return globalTest; }
//    public void setGlobalTest(Object value) { globalTest = (Integer)value; }
//    public Integer reduceTest(Object[] shard) {
//    	int globalTest = 0;
//    	for (int i = 0; i < shard.length; i++) globalTest +=  ((Integer)shard[i]).intValue();
//    	return globalTest;
//    } 
    
    
   /* public DFlockers(Object[] params)
    {    	
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],
    			(Integer)params[8],(String)params[0],(String)params[1],(Integer)params[9],
    			isToroidal,new DistributedMultiSchedule<Double2D>());
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
    }*/
    
    public DFlockers(GeneralParam params)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Double2D>(),topicPrefix);
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    }
    
    public DFlockers(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
    {    	
    	super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
    			params.getJ(),params.getIp(),params.getPort(),params.getMode(),
    			isToroidal,new DistributedMultiSchedule<Double2D>(), prefix);
    	ip = params.getIp();
    	port = params.getPort();
    	this.MODE=params.getMode();
    	gridWidth=params.getWidth();
    	gridHeight=params.getHeight();
    	topicPrefix = prefix; 
    	
    	//System.out.println(simParams.size());
    	for (EntryParam<String, Object> entryParam : simParams) {
    		
    		try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    	for (EntryParam<String, Object> entryParam : simParams) {
    		
    		try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		
    	
    }
    public DFlockers()
    {
    	super();
    }
    
    public void start()
    {
    	super.start();

    	// set up the flockers field.  It looks like a discretization
    	// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
    	// that's 16 hash lookups! I would have guessed that 
    	// neighborhood * 2 (which is about 4 lookups on average)
    	// would be optimal.  Go figure.
    	try 
    	{
    		flockers = DContinuous2DFactory.createDContinuous2D(neighborhood/1.5,gridWidth, gridHeight,this,
    				super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"flockers", topicPrefix);
    		init_connection();
    	} catch (DMasonException e) { e.printStackTrace(); }

    	
    	
    	if ( (TYPE.pos_i == 0 && TYPE.pos_j == 0) )
    	{
    		
    		DFlocker f=new DFlocker(this,new Double2D(0,0));
        	int j=0;
        	
    	while(flockers.size() != super.NUMAGENTS)
    	{
    		f.setPos(flockers.setAvailableRandomLocation(f));
    		if (random.nextBoolean(deadFlockerProbability)) f.dead = true;

    		//if(flockers.setDistributedObjectLocationForPeer(new Double2D(f.pos.getX(),f.pos.getY()), f, this))
    			//if(flockers.setDistributedObjectLocation(new Double2D(f.pos.getX(),f.pos.getY()), f, this))
    		if(flockers.setObjectLocation(f, f.loc))
    		{
    			Color c=new Color(
    					128 + this.random.nextInt(128),
    					128 + this.random.nextInt(128),
    					128 + this.random.nextInt(128));
    			f.setColor(c);
    			schedule.scheduleOnce(f);
    			f=new DFlocker(this,new Double2D(0,0));
    		}

    		j++;

    	}
    	}

    	try {
			getTrigger().publishToTriggerTopic("Simulation cell "+flockers.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void main(String[] args)
    {
    	doLoop(DFlockers.class, args);
    	System.exit(0);
    }

    public DistributedField getField() 
    {
    	return flockers;
    }
	
    public void addToField(RemoteAgent rm, Double2D loc) 
    {
    	flockers.setObjectLocation(rm,loc);
        setPortrayalForObject(rm);

    }

    public SimState getState() 
    {
    	return this;
    }

    public boolean setPortrayalForObject(Object o) 
    {
    	if(flockers.p!=null)
    	{
    		DFlocker f=(DFlocker)o;
    		SimplePortrayal2D pp = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
    				f.getColor(),
    				OrientedPortrayal2D.SHAPE_COMPASS)));
    		flockers.p.setPortrayalForObject(o, pp);
    		return true;
    	}
    	return false;
    }    
}