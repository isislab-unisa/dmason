package dmason.sim.app.DParticles;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.grid.DSparseGrid2D;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.sim.field.grid.numeric.DDoubleGrid2D;
import dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.util.*;

public class DParticles extends DistributedState<Int2D> {
	
	private static boolean isToroidal=false;
    public DSparseGrid2D particles;
    public DDoubleGrid2D trails;
    public int gridWidth ;
    public int gridHeight ;   
    public int MODE;
   
    public DParticles(Object[] params)
    {    	
    	super((Integer)params[2],(Integer)params[3],(Integer)params[4],(Integer)params[7],
    			(Integer)params[8],(String)params[0],(String)params[1],(Integer)params[9],
    			isToroidal,new DistributedMultiSchedule<Int2D>());
    	ip = params[0]+"";
    	port = params[1]+"";
    	this.MODE=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
    }    

    public void start()
    {
    	super.start();

        try 
        {
        	trails = DDoubleGrid2DFactory.createDDoubleGrid2D(gridWidth, gridHeight,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.NUMPEERS,MODE,0,false,"trails");
			particles = DSparseGrid2DFactory.createDSparseGrid2d(gridWidth, gridHeight,this,super.MAX_DISTANCE,TYPE.pos_i,TYPE.pos_j,super.NUMPEERS,MODE, "particles");
		    init_connection();
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
		return false;
	}    
}