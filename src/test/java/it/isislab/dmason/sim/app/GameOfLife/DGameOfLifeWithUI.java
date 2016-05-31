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
package it.isislab.dmason.sim.app.GameOfLife;

import java.awt.Color;
import java.util.List;
import javax.swing.JFrame;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class DGameOfLifeWithUI extends GUIState {
	public Display2D display;
	public JFrame displayFrame;
	public static String name;

	@Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

    FastValueGridPortrayal2D gridPortrayal = new FastValueGridPortrayal2D();


	public DGameOfLifeWithUI(GeneralParam args,List<EntryParam<String, Object>> simParams,String prefix) 
	{ 
		super(new DGameOfLife(args,simParams, prefix));

		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
	}
	
	
	public DGameOfLifeWithUI(GeneralParam args, String prefix) 
	{ 
		super(new DGameOfLife(args, prefix));

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
		DGameOfLife gol = (DGameOfLife)state;

		gridPortrayal.setField(gol.grid);
		gridPortrayal.setMap(
		            new sim.util.gui.SimpleColorMap(
		                new Color[] {new Color(0,0,0,0), Color.blue}));

		gol.p = gridPortrayal;

		display.reset();

		display.repaint();
	}

	@Override
	public void init(Controller c)
	{
		super.init(c);

		// make the displayer
		display = new Display2D(600,600,this);
		display.setBackdrop(Color.WHITE);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Game Of Life");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);

		display.attach( gridPortrayal, "Life" );
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
