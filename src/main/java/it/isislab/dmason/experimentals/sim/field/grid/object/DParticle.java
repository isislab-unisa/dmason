/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.sim.field.grid.object;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;

/** A bouncing particle. */
/**
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
   
	@Override
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
        	//tut.trails.setElement(location.x, location.y, 1.0);
        	//tut.trails.field[location.x][location.y] = 1.0;
        	tut.trails.setDistributedObjectLocation(location,1.0,state);
        }catch(NullPointerException e){
        	e.printStackTrace();
        	//System.out.println("Error"+tut.TYPE+"--POSIZIONE: "+this.pos+" Direction="+xdir+"  "+ydir);
        } catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
        else if (newx >= tut.gridWidth) {newx--; xdir = -xdir; }
        if (newy < 0) { newy++ ; ydir = -ydir; }
        else if (newy >= tut.gridHeight) {newy--; ydir = -ydir; }
             
        // set my new location
        Int2D newloc = new Int2D(newx,newy);        
        try {
        	
			tut.particles.setDistributedObjectLocation(newloc, this, state);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
	}
}