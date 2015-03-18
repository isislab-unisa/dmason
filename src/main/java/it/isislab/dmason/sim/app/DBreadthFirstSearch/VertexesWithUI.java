/**
 * Copyright 2012 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.app.DBreadthFirstSearch;
import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.tools.batch.data.GeneralParam;

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
@AuthorAnnotation(
		author = {"Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class VertexesWithUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;

	ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
	NetworkPortrayal2D networkPortrayal = new NetworkPortrayal2D();
	private static String name;

	public VertexesWithUI(GeneralParam args) 
	{ 
		super(new Vertexes(args,"/Users/andreasolda/Desktop/karate.gexf"));

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
		Vertexes vertexes = (Vertexes) state;

		yardPortrayal.setField( vertexes.yard );

		networkPortrayal.setField( new SpatialNetwork2D( vertexes.yard, vertexes.network ) );
		networkPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());

		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);

		// redraw the display
		display.repaint();
	}

	@Override
	public void init(Controller c)
	{
		super.init(c);

		display = new Display2D(600,600,this);
		display.setClipping(false);

		displayFrame = display.createFrame();
		displayFrame.setTitle(getName());
		c.registerFrame(displayFrame);   
		displayFrame.setVisible(true);
		display.attach( networkPortrayal, "Network" );
		display.attach( yardPortrayal, "Yard" );
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
