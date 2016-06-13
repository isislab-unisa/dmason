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
package it.isislab.dmason.sim.app.DCircles;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
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

	@Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

	ContinuousPortrayal2D circlesPortrayal = new ContinuousPortrayal2D();


	public DCirclesWithUI(GeneralParam args,List<EntryParam<String, Object>> simParams,String topicPrefix) 
	{ 
		super(new DCircles(args,simParams,topicPrefix));

		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
	}
	
	public DCirclesWithUI(GeneralParam args,String topicPrefix) 
	{ 
		super(new DCircles(args,topicPrefix));

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
