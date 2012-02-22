/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package dmason.util.visualization.DFlockers;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;
import java.awt.*;
import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;

public class FlockersWithUIView extends GUIState
    {
    public Display2D display;
    public JFrame displayFrame;
    public FlockersWithUIView(Object[] args) { 
    	super(new FlockersView(args)); 
    	
    }
    
    public static void main(String[] args)
        {
        new FlockersWithUIView().createController();  // randomizes by currentTimeMillis
        }

    public Object getSimulationInspectedObject() { return state; }  // non-volatile

    ContinuousPortrayal2D flockersPortrayal = new ContinuousPortrayal2D();
        
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
/*    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D(); */
    
    public FlockersWithUIView()
        {
        super(new FlockersView(0));
        }
    
    public FlockersWithUIView(SimState state) 
        {
        super(state);
        }

    public static String getName() { return "Flockers"; }

    public void start()
        {
        super.start();
        setupPortrayals();
        }

    public void load(SimState state)
        {
        super.load(state);
        setupPortrayals();
        }
        
    public void setupPortrayals()
        {
        FlockersView flock = (FlockersView)state;
        flock.setPortrayal(flockersPortrayal);
        flockersPortrayal.setField(flock.flockers);
        // uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
/*       trailsPortrayal.setField(flock.flockers);  */
        
        // make the flockers random colors and four times their normal size (prettier)
        for(int x=0;x<flock.flockers.allObjects.numObjs;x++)
            {
            SimplePortrayal2D p = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
                        new Color(128 + state.random.nextInt(128),
                            128 + state.random.nextInt(128),
                            128 + state.random.nextInt(128)),
                        OrientedPortrayal2D.SHAPE_COMPASS)));

            flockersPortrayal.setPortrayalForObject(flock.flockers.allObjects.objs[x], p);
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
            /* 
               trailsPortrayal.setPortrayalForObject(flock.flockers.allObjects.objs[x], 
               new TrailedPortrayal2D(this, p, trailsPortrayal, 100));
            */
            }
        
        // update the size of the display appropriately.
        double w = flock.flockers.getWidth();
        double h = flock.flockers.getHeight();
        if (w == h)
            { display.insideDisplay.width = display.insideDisplay.height = 750; }
        else if (w > h)
            { display.insideDisplay.width = 750; display.insideDisplay.height = 750 * (h/w); }
        else if (w < h)
            { display.insideDisplay.height = 750; display.insideDisplay.width = 750 * (w/h); }
            
        // reschedule the displayer
        display.reset();
                
        // redraw the display
        display.repaint();
        }

    public void init(Controller c)
        {
        super.init(c);

        // make the displayer
        display = new Display2D(750,750,this,1);
        display.setBackdrop(Color.black);


        displayFrame = display.createFrame();
        displayFrame.setTitle("Flockers");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
        /* display.attach( trailsPortrayal, "Trails" ); */
                
        display.attach( flockersPortrayal, "Behold the Flock!" );
        }
        
    public void quit()
        {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
        }
  

    }
