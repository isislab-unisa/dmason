package dmason.sim.app.DFlockers;

import java.awt.Color;
import java.util.ArrayList;

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
    public DContinuous2D flockers;
    private static boolean isToroidal=true;
    public double width = 150;
    public double height = 150;
    public int numFlockers = 20;
    public double cohesion = 1.0;
    public double avoidance = 1.0;
    public double randomness = 1.0;
    public double consistency = 1.0;
    public double momentum = 1.0;
    public double deadFlockerProbability = 0.1;
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
    
    public DFlockers(Object[] params)
    {    	
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],
    			(Integer)params[8],(String)params[0],(String)params[1],(Integer)params[9],
    			isToroidal,new DistributedMultiSchedule<Double2D>());
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
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
    				super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.NUMPEERS,MODE,"flockers");
    		init_connection();
    	} catch (DMasonException e) { e.printStackTrace(); }

    	DFlocker f=new DFlocker(this,new Double2D(0,0));
    	int j=0;
    	while(flockers.size() != super.NUMAGENTS)
    	{
    		f.setPos(flockers.setAvailableRandomLocation(f));
    		if (random.nextBoolean(deadFlockerProbability)) f.dead = true;

    		if(flockers.setDistributedObjectLocationForPeer(new Double2D(f.pos.getX(),f.pos.getY()), f, this))
    			//if(flockers.setDistributedObjectLocation(new Double2D(f.pos.getX(),f.pos.getY()), f, this))
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