/**
   Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.app.SIRStateReflection;

import java.awt.Color;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

import javax.swing.JFrame;

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

	public DPeopleWithGui(GeneralParam args, String prefix) 
	{ 
		super(new DPeople(args, prefix));

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