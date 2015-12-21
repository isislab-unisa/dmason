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
package it.isislab.dmason.experimentals.util.visualization.sim.app.DFlockers;

import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomViewer;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class FlockersView extends SimState
{
	
	public ConnectionNFieldsWithActiveMQAPI con;
	public String id_Cell;
	public int numCell;
	public int mode;
	
	public FlockersView(Object[] args)
	{
		super(1);
		con=(ConnectionNFieldsWithActiveMQAPI)args[0];
		id_Cell=(String)args[1];
		isSynchro = (Boolean)args[2];
		this.numCell = (Integer)args[3];
		int wh = (Integer)args[4];//width
		int ht = (Integer)args[5];//height
		this.mode = (Integer)args[6];
		
		width = ZoomViewer.getCellWidth(mode, wh, numCell);
		height = ZoomViewer.getCellHeight(mode, ht, numCell);
	}
	
	public boolean isSynchro;
    public Continuous2D flockers;
    public double width;
    public double height;
    public int numFlockers = 0;
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
    
    /** Creates a Flockers simulation with the given random number seed. */
    public FlockersView(long seed)
    {
    	super(seed);
    }
    
    @Override
	public void start()
    {
        super.start();
        
        // set up the flockers field.  It looks like a discretization
        // of about neighborhood / 1.5 is close to optimal for us.  Hmph,
        // that's 16 hash lookups! I would have guessed that 
        // neighborhood * 2 (which is about 4 lookups on average)
        // would be optimal.  Go figure.
        flockers = new Continuous2D(neighborhood/1.5,width,height);
        
      //View Zoom in Central GUI
    	
    	ZoomViewer zoom;
		try {
			zoom = new ZoomViewer(con,id_Cell,isSynchro,numCell,(int)width,(int)height,mode);
	       	//in according order
        	zoom.registerField("flockers",flockers);
        	
        	
        	schedule.scheduleRepeating(new DFlockerUpdate(zoom));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    ContinuousPortrayal2D flockersPortrayal ;
    public void setPortrayal(ContinuousPortrayal2D p)
    {
    	flockersPortrayal=p;
    }

    public static void main(String[] args)
    {
    	doLoop(FlockersView.class, args);
    	System.exit(0);
    }    
}
