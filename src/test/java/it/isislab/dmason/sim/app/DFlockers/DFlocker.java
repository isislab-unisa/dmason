/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.sim.app.DFlockers;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import java.awt.Color;
import java.util.Date;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Orientable2D;
import sim.util.Bag;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
/**
 * Distributed version of Flocker-Mason simulation    
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class DFlocker extends RemoteFlock<Double2D> implements Orientable2D
{
    public Double2D lastd = new Double2D(0,0);
    public Color color;
    public boolean dead = false;
    long startTime=0;
    long globalTime=System.currentTimeMillis();
    public boolean stampa=false;
    public DFlocker(){}
    public DFlocker(DistributedState<Double2D> sm,Double2D location) 
    {   
    	super(sm);
    	startTime=System.currentTimeMillis();
    	pos = location;
    	
    }
    
    public Bag getNeighbors(DistributedState<Double2D> sm)
    {
        //return ((DContinuousGrid2D)sm.getField()).getObjectsExactlyWithinDistance(pos, ((DFlockers)sm).neighborhood, true);
    	return ((DContinuousGrid2D)sm.getField()).getNeighborsExactlyWithinDistance(pos, ((DFlockers)sm).neighborhood, true);
    }
    
    public double getOrientation() { return orientation2D(); }
    public boolean isDead() { return dead; }
    public void setDead(boolean val) { dead = val; }
    
    @Override
	public void setOrientation2D(double val)
    {
        lastd = new Double2D(Math.cos(val),Math.sin(val));
    }
    
    @Override
	public double orientation2D()
    {
        if (lastd.x == 0 && lastd.y == 0) return 0;
        return Math.atan2(lastd.y, lastd.x);
    }
    
    public Double2D momentum()
    {
        return lastd;
    }

    public Double2D consistency(Bag b, Continuous2D flockers)
    {
        if (b==null || b.numObjs == 0) return new Double2D(0,0);
        
        double x = 0; 
        double y= 0;
        int i =0;
        int count = 0;
        for(i=0;i<b.numObjs;i++)
        {
            DFlocker other = (DFlocker)(b.objs[i]);
            if (!other.dead)
            {
                double dx = flockers.tdx(pos.x,other.pos.x);
                double dy = flockers.tdy(pos.y,other.pos.y);
                Double2D m = ((DFlocker)b.objs[i]).momentum();
                count++;
                x += m.x;
                y += m.y;
            }
        }
        if (count > 0) { x /= count; y /= count; }
        return new Double2D(x,y);
    }
    
    public Double2D cohesion(Bag b, Continuous2D flockers)
    {
        if (b==null || b.numObjs == 0) return new Double2D(0,0);
        
        double x = 0; 
        double y= 0;        

        int count = 0;
        int i =0;
        for(i=0;i<b.numObjs;i++)
        {
            DFlocker other = (DFlocker)(b.objs[i]);
            if (!other.dead)
            {
                double dx = flockers.tdx(pos.x,other.pos.x);
                double dy = flockers.tdy(pos.y,other.pos.y);
                count++;
                x += dx;
                y += dy;
            }
        }
        if (count > 0) { x /= count; y /= count; }
        return new Double2D(-x/10,-y/10);
    }
 
    public Double2D avoidance(Bag b, Continuous2D flockers)
    {
        if (b==null || b.numObjs == 0) return new Double2D(0,0);
        double x = 0;
        double y = 0;
        
        int i=0;
        int count = 0;

        for(i=0;i<b.numObjs;i++)
        {
            DFlocker other = (DFlocker)(b.objs[i]);
            if (other != this )
            {
                double dx = flockers.tdx(pos.x,other.pos.x);
                double dy = flockers.tdy(pos.y,other.pos.y);
                double lensquared = dx*dx+dy*dy;
                count++;
                x += dx/(lensquared*lensquared + 1);
                y += dy/(lensquared*lensquared + 1);
            }
        }
        if (count > 0) { x /= count; y /= count; }
        return new Double2D(400*x,400*y);      
    }
        
    public Double2D randomness(MersenneTwisterFast r)
    {
        double x = r.nextDouble() * 2 - 1.0;
        double y = r.nextDouble() * 2 - 1.0;
        double l = Math.sqrt(x * x + y * y);
        return new Double2D(0.05*x/l,0.05*y/l);
    }
    
	@Override
	public void step(SimState state)
	{     	
		
		final DFlockers flock = (DFlockers)state;
		pos = flock.flockers.getObjectLocation(this);
		
		if (dead) return;
    	        
		Bag b = getNeighbors((DistributedState)state);
		Double2D avoid = avoidance(b,flock.flockers);
		Double2D cohe = cohesion(b,flock.flockers);
		Double2D rand = randomness(flock.random);
		Double2D cons = consistency(b,flock.flockers);
		Double2D mome = momentum();

		double dx = flock.cohesion * cohe.x + flock.avoidance * avoid.x + flock.consistency* cons.x + flock.randomness * rand.x + flock.momentum * mome.x;
		double dy = flock.cohesion * cohe.y + flock.avoidance * avoid.y + flock.consistency* cons.y + flock.randomness * rand.y + flock.momentum * mome.y;
    	                
		// renormalize to the given step size
		double dis = Math.sqrt(dx*dx+dy*dy);
		if (dis>0)
		{
			dx = dx / dis * flock.jump;
			dy = dy / dis * flock.jump;
		}
    	        
		lastd = new Double2D(dx,dy);
		pos = new Double2D(flock.flockers.stx(pos.x + dx), flock.flockers.sty(pos.y + dy));
    	
		/**
		 * TESTING CLUSTER
		 */
//		long time = System.currentTimeMillis()- startTime ;	
//		if( ( time>=(1*30*1000) ) && stampa==true){
//			
//			//System.out.println(( (System.currentTimeMillis()-globalTime)  /1000)/60+";"+ flock.schedule.getSteps()/*+" "+new Date(System.currentTimeMillis())*/); 
//			flock.out.println(( (System.currentTimeMillis()-globalTime)  /1000)/60+";"+ flock.schedule.getSteps());
//			startTime=System.currentTimeMillis();
//		} 

		
		try {
			flock.flockers.setDistributedObjectLocation(pos, this, state);
		} catch (DMasonException e) {
			e.printStackTrace();
		}	
			}
	
	public Color getColor() 
	{
		return color;
	}
	
	public void setColor(Color color) 
	{
		this.color = color;
	}
}