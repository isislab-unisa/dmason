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
package it.isislab.dmason.test.sim.app.DFlockers;

import it.isislab.dmason.test.sim.app.DFlockers.DFlocker;
import it.isislab.dmason.test.sim.app.DFlockers.DFlockers;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.tools.batch.data.GeneralParam;

import java.awt.Color;

import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.Double2D;
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
public class DFlockersWithUI extends GUIState
{
	
    public Display2D display;
    public JFrame displayFrame;
    public static String name;
    
    public static String topicPrefix = "";

    @Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

    ContinuousPortrayal2D flockersPortrayal = new ContinuousPortrayal2D();
        
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
/*    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D(); */
    
   /* public DFlockersWithUI(Object[] args) 
    { 
    	super(new DFlockers(args));
    
    	name=String.valueOf(args[7])+""+(String.valueOf(args[8]));
    }*/
    
    public DFlockersWithUI(GeneralParam args) 
    { 
    	super(new DFlockers(args));
    
    	name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
    }
  
    public static String getName() { return "Peer: <"+name+">"; }

    @Override
	public void start()
    {
        super.start();
        setupPortrayals();
    }

    @Override
	public void load(SimState state)
    {
        super.load(state);
        setupPortrayals();
    }
        
    public void setupPortrayals()
    {
        DFlockers flock = (DFlockers)state;

        flockersPortrayal.setField(flock.flockers);
        // uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
/*       trailsPortrayal.setField(flock.flockers);  */
        
        // make the flockers random colors and four times their normal size (prettier)
        for(int x=0;x<flock.flockers.allObjects.numObjs;x++)
        {
        	DistributedState<Double2D> dState=(DistributedState<Double2D>)state;
        	 DFlocker f=(DFlocker)flock.flockers.allObjects.objs[x];
            SimplePortrayal2D p = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
                        f.getColor(),
                        OrientedPortrayal2D.SHAPE_COMPASS)));
           ;
            
            flockersPortrayal.setPortrayalForObject(flock.flockers.allObjects.objs[x], p);
            
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
            /* 
               trailsPortrayal.setPortrayalForObject(flock.flockers.allObjects.objs[x], 
               new TrailedPortrayal2D(this, p, trailsPortrayal, 100));
            */
            }
        //flock.flockers.attachPortrayal(flockersPortrayal);
        flock.p=flockersPortrayal;
        
        // update the size of the display appropriately.
      /*  double w = flock.flockers.getWidth();
        double h = flock.flockers.getHeight();
        if (w == h)
            { display.insideDisplay.width = display.insideDisplay.height = 750; }
        else if (w > h)
            { display.insideDisplay.width = 750; display.insideDisplay.height = 750 * (h/w); }
        else if (w < h)
            { display.insideDisplay.height = 750; display.insideDisplay.width = 750 * (w/h); }
           */ 
        // reschedule the displayer
        display.reset();
                
        // redraw the display
        display.repaint();
    }

    @Override
	public void init(Controller c)
    {
        super.init(c);

        // make the displayer
        display = new Display2D(600,600,this,1);
        display.setBackdrop(Color.black);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Flockers");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
        /* display.attach( trailsPortrayal, "Trails" ); */
                
        display.attach( flockersPortrayal, "Behold the Flock!" );
    }
        
    @Override
	public void quit()
    {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
}