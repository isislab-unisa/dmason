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
package it.isislab.dmason.util.visualization.sim.app.DFlockers;


import it.isislab.dmason.sim.app.DFlockers.DFlocker;
import it.isislab.dmason.util.visualization.zoomviewerapp.Updater;
import it.isislab.dmason.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.util.visualization.zoomviewerapp.ZoomViewer;

import java.util.HashMap;

import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.Double2D;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
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
					
					flockers.flockers.setObjectLocation(r, (Double2D)zoom.getZoomAgentLocation(r.getPos()));
					SimplePortrayal2D p = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
		                        r.color,
		                        OrientedPortrayal2D.SHAPE_COMPASS)));

		            flockers.flockersPortrayal.setPortrayalForObject(r, p);
		            
		         
				}
			zoom.sendAckToCell(continuos.STEP);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Problemi in dflockerUpdate");
		}
		
	}
   
}
