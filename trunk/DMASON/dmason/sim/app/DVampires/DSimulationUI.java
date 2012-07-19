package dmason.sim.app.DVampires;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;
import dmason.sim.engine.DistributedState;
import java.awt.*;
import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

public class DSimulationUI extends GUIState
{
	
    public Display2D display;
    public JFrame displayFrame;
    public static String name;

    public Object getSimulationInspectedObject() { return state; }  // non-volatile

    ContinuousPortrayal2D worldPortrayal = new ContinuousPortrayal2D();
        
    public DSimulationUI(Object[] args) 
    { 
    	super(new DSimulation(args));
    
    	name = String.valueOf(args[7]) + "" + String.valueOf(args[8]);
    }
  
    public static String getName() { return "Peer: <"+name+">"; }

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
        DSimulation sim = (DSimulation)state;

        worldPortrayal.setField(sim.world);
        
        // make the flockers random colors and four times their normal size (prettier)
        for(int x=0; x < sim.world.allObjects.numObjs; x++)
        {
        	DistributedState<Double2D> dState=(DistributedState<Double2D>)state;
        	DRoost f = (DRoost)sim.world.allObjects.objs[x];
            SimplePortrayal2D p = new AdjustablePortrayal2D(
            		new MovablePortrayal2D(
            				new OrientedPortrayal2D(
            						new SimplePortrayal2D(),
            						0,
            						4.0,
            						new Color(139, 69, 19), // Brown
            						OrientedPortrayal2D.SHAPE_COMPASS)
            				)
            		);
            
            worldPortrayal.setPortrayalForObject(sim.world.allObjects.objs[x], p);
        }
        sim.world.attachPortrayal(worldPortrayal);

        // update the size of the display appropriately.
        double w = sim.world.getWidth();
        double h = sim.world.getHeight();
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
                
        display.attach( worldPortrayal, "Behold the Flock!" );
    }
        
    public void quit()
    {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
}