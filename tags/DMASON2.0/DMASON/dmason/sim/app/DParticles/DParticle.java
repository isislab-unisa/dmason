package dmason.sim.app.DParticles;

import sim.engine.SimState;
import sim.util.*;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;

/** A bouncing particle. */

public class DParticle extends RemoteParticle<Int2D>
{
	//public boolean randomize = false;
	public int xdir;  // -1, 0, or 1
    public int ydir;  // -1, 0, or 1
    
	public DParticle(){ }
    public DParticle(DistributedState state)
    {
    	super(state);
    }
   
	public void step(SimState state) 
	{
		DParticles tut = (DParticles)state;
      
        // We could just store my location internally, but for purposes of
        // show, let's get my position out of the particles grid
        
        Int2D location = tut.particles.getObjectLocation(this);
        
        Bag p = tut.particles.getObjectsAtLocation(location);
   
        // leave a trail
        //tut.trails.setDistributedObjectLocation(1.0, location,state);

        try{
        	tut.trails.field[location.x][location.y] = 1.0;
        	//tut.trails.setDistributedObjectLocation(1.0, location,state);
        }catch(NullPointerException e){
        	e.printStackTrace();
        	System.out.println("ERRORE QUI"+tut.TYPE+"--POSIZIONE: "+this.pos+" DIREZIONE="+xdir+"  "+ydir);
        }    
        
        
        // Randomize my direction if requested
        if (p.numObjs > 1)
        {     
        	xdir = tut.random.nextInt(3) - 1;
        	ydir = tut.random.nextInt(3) - 1;
        }
       
        // move
        int newx = location.x + xdir;
        int newy = location.y + ydir;

        // reverse course if hitting boundary
        if (newx < 0) { newx++; xdir = -xdir; }
        else if (newx >= tut.trails.getWidth()) {newx--; xdir = -xdir; }
        if (newy < 0) { newy++ ; ydir = -ydir; }
        else if (newy >= tut.trails.getHeight()) {newy--; ydir = -ydir; }
             
        // set my new location
        Int2D newloc = new Int2D(newx,newy);        
        tut.particles.setDistributedObjectLocation(newloc, this, state);      
	}
}