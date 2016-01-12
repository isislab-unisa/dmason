package it.isislab.dmason.sim.app.openAB.Circle;

import java.awt.Color;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;

public class DCirclesWithUI extends GUIState {
	
	public Display2D display;
	public JFrame displayFrame;
	public static String name;

	public static String topicPrefix = "";

	@Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

	ContinuousPortrayal2D circlesPortrayal = new ContinuousPortrayal2D();


	public DCirclesWithUI(GeneralParam args) 
	{ 
		super(new DCircles(args));

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
		DCircles cr = (DCircles)state;

		circlesPortrayal.setField(cr.circles);
		display.setBackdrop(Color.white);

		cr.p = circlesPortrayal;

		display.reset();

		display.repaint();
	}

	@Override
	public void init(Controller c)
	{
		super.init(c);


		// make the displayer
		display = new Display2D(600,600,this);
		display.setBackdrop(Color.white);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Circles");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);

		display.attach( circlesPortrayal, "Agents" );
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
