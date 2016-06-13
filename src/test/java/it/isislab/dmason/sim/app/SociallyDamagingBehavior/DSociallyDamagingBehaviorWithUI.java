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
package it.isislab.dmason.sim.app.SociallyDamagingBehavior;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.TrailedPortrayal2D;

public class DSociallyDamagingBehaviorWithUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;
	public static String name;


	@Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

	ContinuousPortrayal2D DSDBPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();


	public DSociallyDamagingBehaviorWithUI(GeneralParam args,String prefix) 
	{ 
		super(new DSociallyDamagingBehavior(args,prefix));

		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
	}


	public DSociallyDamagingBehaviorWithUI(GeneralParam args,List<EntryParam<String, Object>> simParams,String prefix) 
	{ 
		super(new DSociallyDamagingBehavior(args,simParams,prefix));

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
		DSociallyDamagingBehavior dsdb = (DSociallyDamagingBehavior)state;

		DSDBPortrayal.setField(dsdb.human_being);
		// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
		/*       trailsPortrayal.setField(flock.flockers);  */

		// make the flockers random colors and four times their normal size (prettier)
		for(int x=0;x<dsdb.human_being.allObjects.numObjs;x++)
		{
			SimplePortrayal2D basic =       new TrailedPortrayal2D(
					this,
					new OrientedPortrayal2D(
							new SimplePortrayal2D(), 0, 4.0,
							(dsdb.human_being.allObjects.objs[x] instanceof Honest)?
									(Color.green): (Color.red)
									,
									OrientedPortrayal2D.SHAPE_COMPASS),
									trailsPortrayal, 100);

			// note that the basic portrayal includes the TrailedPortrayal.  We'll add that to BOTH 
			// trails so it's sure to be selected even when moving.  The issue here is that MovablePortrayal2D
			// bypasses the selection mechanism, but then sends selection to just its own child portrayal.
			// but we need selection sent to both simple portrayals in in both field portrayals, even after
			// moving.  So we do this by simply having the TrailedPortrayal wrapped in both field portrayals.
			// It's okay because the TrailedPortrayal will only draw itself in the trailsPortrayal, which
			// we passed into its constructor.

			DSDBPortrayal.setPortrayalForObject(dsdb.human_being.allObjects.objs[x], 
					new AdjustablePortrayal2D(new MovablePortrayal2D(basic)));
			trailsPortrayal.setPortrayalForObject(dsdb.human_being.allObjects.objs[x], basic );
		}
		dsdb.p=DSDBPortrayal;

		// update the size of the display appropriately.
		double w = dsdb.human_being.getWidth();
		double h = dsdb.human_being.getHeight();
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

	@Override
	public void init(Controller c)
	{
		super.init(c);

		// make the displayer
		display = new Display2D(750,750,this,1);
		display.setBackdrop(Color.black);

		displayFrame = display.createFrame();
		displayFrame.setTitle("Socially Damaging Behaviors");
		c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
		displayFrame.setVisible(true);
		// uncomment this to try out trails  (also need to uncomment out some others in this file, look around)
		/* display.attach( trailsPortrayal, "Trails" ); */

		display.attach( DSDBPortrayal, "Behold the Human!" );
	}

	@Override
	public void quit()
	{
		super.quit();

		if (displayFrame!=null) displayFrame.dispose();
		displayFrame = null;
		display = null;
	}
	public static Class<?> getSimClass(){return DSociallyDamagingBehavior.class;}
}
