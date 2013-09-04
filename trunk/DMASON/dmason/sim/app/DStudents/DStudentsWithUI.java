package dmason.sim.app.DStudents;

import java.awt.Color;

import javax.swing.JFrame;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.OvalPortrayal2D;
import dmason.batch.data.GeneralParam;


public class DStudentsWithUI extends GUIState {

	public static String name;
	public static String topicPrefix = "";
	public Display2D display;
	public JFrame displayFrame;
	
	ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
	/*------------------------------------------------------------------*/
	NetworkPortrayal2D buddiesPortrayal = new NetworkPortrayal2D();

	public DStudentsWithUI(GeneralParam args) {

		super(new DStudents(args));
		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
	}

	public static String getName() { return "Peer: <"+name+">"; }

	public Object getSimulationInspectedObject() { return state; }

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

	public void init(Controller c)
	{
		super.init(c);

		// make the displayer
		display = new Display2D(600,600,this);
		// turn off clipping
		display.setClipping(false);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Schoolyard Display");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);
		/*---------------------------------*/
		display.attach(buddiesPortrayal, "Buddies");
		/*---------------------------------*/
		display.attach( yardPortrayal, "Yard" );
	}

	public void quit()
	{
		super.quit();

		if (displayFrame!=null) displayFrame.dispose();
		displayFrame = null;
		display = null;
	}

	public void setupPortrayals()
	{
		DStudents students = (DStudents) state;

		// tell the portrayals what to portray and how to portray them
		yardPortrayal.setField( students.yard );
		Color paint = new Color(0,0,255);
		yardPortrayal.setPortrayalForAll(/*new OvalPortrayal2D()*/new OvalPortrayal2D(paint,4));
		/*-------------------------*/
		buddiesPortrayal.setField(new SpatialNetwork2D(students.yard, students.network));
		buddiesPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());
		/*-------------------------*/

		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);

		// redraw the display
		display.repaint();
	}

}
