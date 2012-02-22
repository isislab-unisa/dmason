/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package dmason.util.visualization.DFlockers;
import java.awt.Color;
import java.util.HashMap;

import dmason.sim.app.DFlockers.DFlocker;
import dmason.sim.engine.RemoteAgent;
import dmason.util.visualization.Updater;
import dmason.util.visualization.ZoomArrayList;
import dmason.util.visualization.ZoomViewer;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.*;
import ec.util.*;

public class DFlockerUpdate extends Updater
{

	public DFlockerUpdate(ZoomViewer zoom) {
		super(zoom);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void step(SimState state) {
		
		FlockersView flockers=(FlockersView)state;
		flockers.flockers.clear();
		try {
			HashMap<String, Object> hash=zoom.synchronizedWithSimulation();
			ZoomArrayList continuos = (ZoomArrayList)hash.get("flockers");
			
			for(Object s: continuos)
				{
					DFlocker r=(DFlocker)s;
					
					flockers.flockers.setObjectLocation(r, (Double2D)r.getPos());
					 SimplePortrayal2D p = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
		                        r.color,
		                        OrientedPortrayal2D.SHAPE_COMPASS)));

		            flockers.flockersPortrayal.setPortrayalForObject(r, p);
				}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Problemi in dflockerUpdate");
		}
		
	}
   
}
