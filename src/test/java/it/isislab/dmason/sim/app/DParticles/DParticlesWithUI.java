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
package it.isislab.dmason.sim.app.DParticles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
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
public class DParticlesWithUI extends GUIState
{
    public Display2D display;
    public JFrame displayFrame;
    public static String name;
    SparseGridPortrayal2D particlesPortrayal = new SparseGridPortrayal2D();
    FastValueGridPortrayal2D trailsPortrayal = new FastValueGridPortrayal2D("Trail");

    /*public DParticlesWithUI(Object[] args) 
    { 
    	super(new DParticles(args));
    	name=String.valueOf(args[7])+""+(String.valueOf(args[8]));
    }*/
    
    public DParticlesWithUI(GeneralParam args,List<EntryParam<String, Object>>list,String prefix) 
    { 
    	super(new DParticles(args,list,prefix));
    	name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
    }
    
    public static String getName() { return "Peer: <"+name+">"; }
    
// We comment this out of the example, which will cause MASON to look
// for a file called "index.html" in the same directory -- which we've
// included for consistency with the other applications in the demo 
// apps directory.

/*
  public static Object getInfoByClass(Class theClass)
  {
  return "<H2>Tutorial3</H2><p>An odd little particle-interaction example.";
  }
*/
    
    @Override
	public void quit()
    {
    	super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;  // let gc
        display = null;       // let gc
    }

    @Override
	public void start()
    {
        super.start();
        // set up our portrayals
        setupPortrayals();
    }
    
    @Override
	public void load(SimState state)
    {
        super.load(state);
        // we now have new grids.  Set up the portrayals to reflect that
        setupPortrayals();
    }
        
    // This is called by start() and by load() because they both had this code
    // so I didn't have to type it twice :-)
    public void setupPortrayals()
    {
        // tell the portrayals what to
        // portray and how to portray them
        trailsPortrayal.setField(
            ((DParticles)state).trails);
        trailsPortrayal.setMap(
            new sim.util.gui.SimpleColorMap(
               0.0,1.0,Color.black,Color.white));
        particlesPortrayal.setField(((DParticles)state).particles);
        particlesPortrayal.setPortrayalForAll( new sim.portrayal.simple.OvalPortrayal2D(Color.red) );
        


        ((DParticles)state).p=particlesPortrayal;
       

        // reschedule the displayer
        display.reset();
                
        // redraw the display
        display.repaint();
        }
    
    @Override
	public void init(Controller c)
    {
        super.init(c);
        
        // Make the Display2D.  We'll have it display stuff later.
        display = new Display2D(600,600,this,1); // at 400x400, we've got 4x4 per array position
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.black);

        // attach the portrayals
        display.attach(trailsPortrayal,"Trails");
        display.attach(particlesPortrayal,"Particles");
    }
}