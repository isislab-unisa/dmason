package dmason.sim.app.DParticles;

import dmason.sim.engine.DistributedSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.grid.DSparseGrid2D;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.util.*;

public class DParticles extends DistributedState<Int2D>
{
    public DSparseGrid2D particles;
    public DoubleGrid2D trails;
    public int gridWidth ;
    public int gridHeight ;   
    public int MODE;
   
    public DParticles(Object[] params)
    {    	
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],(Integer)params[8]);
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
    }    

    public void start()
    {
    	super.start();
        trails = new DoubleGrid2D(gridWidth, gridHeight);

        try 
        {
			particles = DSparseGrid2DFactory.createDSparseGrid2d(gridWidth, gridHeight,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.NUMPEERS,MODE);
		}catch (DMasonException e) { e.printStackTrace();}

        DParticle p=new DParticle(this);

        while(particles.size() != super.NUMAGENTS)
        {		
        	particles.setAvailableRandomLocation(p);
           
            p.xdir = random.nextInt(3)-1;
            p.ydir = random.nextInt(3)-1;
            
           	if(particles.setDistributedObjectLocationForPeer(new Int2D(p.pos.getX(),p.pos.getY()), p, this))
           	{		
           		schedule.scheduleOnce(schedule.getTime()+1.0,p);
           			
           		if(particles.size() != super.NUMAGENTS)
           			p=new DParticle(this);
           	}
        }
        // Schedule the decreaser
        Steppable decreaser = new Steppable()
        {
            public void step(SimState state)
            {
            	trails.multiply(0.9);
            }
            static final long serialVersionUID = 6330208160095250478L;
        };
            
        schedule.scheduleRepeating(Schedule.EPOCH,2,decreaser,1);
       ((DistributedSchedule) (this.schedule)).setField(particles);
    }

    public static void main(String[] args)
    {
        doLoop(DParticles.class, args);
        System.exit(0);
    }    

    static final long serialVersionUID = 9115981605874680023L;
	
	public DistributedField getField() 
	{
		return particles;
	}

	public SimState getState() 
	{
		return this;
	}

	public void addToField(RemoteAgent<Int2D> rm,Int2D loc) 
	{		
		particles.setObjectLocation(rm, loc);
	}

	public boolean setPortrayalForObject(Object o) 
	{
		/*
		 * THIS METHOD MUST NOT BE IMPLEMENTED FOR THIS APP.
		 */
		return false;
	}    
}