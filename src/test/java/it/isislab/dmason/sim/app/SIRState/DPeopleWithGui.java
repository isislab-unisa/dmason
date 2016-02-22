package it.isislab.dmason.sim.app.SIRState;

import java.awt.Color;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;

public class DPeopleWithGui extends GUIState {


	public Display2D display;
	public JFrame displayFrame;
	public static String name;
	public Object getSimulationInspectedObject() { return state; }  // non-volatile
	ContinuousPortrayal2D portrayal = new ContinuousPortrayal2D();

	public DPeopleWithGui(GeneralParam args) 
	{ 
		super(new DPeople(args));

		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
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
		DPeople people = (DPeople)state;
		
		portrayal.setField(people.environment);
		
		people.p = portrayal;
		
		display.reset();
		display.setBackdrop(Color.white);
	}

	public void init(Controller c)
	{
		super.init(c);

		// make the displayer
		display = new Display2D(600,600,this);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Cooperative Target Observation Display");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		display.setBackdrop(Color.white);
		displayFrame.setVisible(true);

		display.attach( portrayal, "People" );
	}

	public void quit()
	{
		super.quit();

		if (displayFrame!=null) displayFrame.dispose();
		displayFrame = null;
		display = null;
	}
}
