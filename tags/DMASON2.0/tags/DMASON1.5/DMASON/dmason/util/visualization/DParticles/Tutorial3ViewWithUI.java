package dmason.util.visualization.DParticles;
import sim.engine.*;

import sim.display.*;
import sim.portrayal.grid.*;
import java.awt.*;
import javax.swing.*;

import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class Tutorial3ViewWithUI extends GUIState {
	
    public Display2D display;
    public JFrame displayFrame;

    SparseGridPortrayal2D particlesPortrayal = new SparseGridPortrayal2D();
    FastValueGridPortrayal2D trailsPortrayal = new FastValueGridPortrayal2D("Trail");

    public static void main(String[] args)
    {
    	Tutorial3ViewWithUI t = new Tutorial3ViewWithUI();
    	t.createController();
    }
	
	public Tutorial3ViewWithUI(Object[] args)
	{
		 super(new Tutorial3View(args));
	}
    
    public Tutorial3ViewWithUI() { super(new Tutorial3View(System.currentTimeMillis())); }
    
    public Tutorial3ViewWithUI(SimState state) { super(state); }
    
    public static String getName() { return "Tutorial3: Particles"; }
    
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
    
    public void quit()
    {
    	super.quit();

    	if (displayFrame!=null) displayFrame.dispose();
    	displayFrame = null;  // let gc
    	display = null;       // let gc
    }

    public void start()
    {
    	super.start();
    	// set up our portrayals
    	setupPortrayals();
    }
    
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
    			((Tutorial3View)state).trails);
    	trailsPortrayal.setMap(
    			new sim.util.gui.SimpleColorMap(
    					0.0,1.0,Color.black,Color.white));
    	particlesPortrayal.setField(((Tutorial3View)state).particles);
    	particlesPortrayal.setPortrayalForAll( new sim.portrayal.simple.OvalPortrayal2D(Color.red) );

    	// reschedule the displayer
    	display.reset();

    	// redraw the display
    	display.repaint();
    }
    
    public void init(Controller c)
    {
    	super.init(c);

    	// Make the Display2D.  We'll have it display stuff later.
    	display = new Display2D(700,700,this,1); // at 400x400, we've got 4x4 per array position
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
    
    
    
    
    
