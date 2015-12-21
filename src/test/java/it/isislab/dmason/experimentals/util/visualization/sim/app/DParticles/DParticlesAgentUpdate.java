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
package it.isislab.dmason.experimentals.util.visualization.sim.app.DParticles;

import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.Updater;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomViewer;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.support.field2D.EntryNum;

import java.util.HashMap;

import sim.engine.SimState;
import sim.util.Int2D;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class DParticlesAgentUpdate extends Updater {

	public DParticlesAgentUpdate(ZoomViewer zoom) {
		super(zoom);
	}
	
	@Override
	public void step(SimState state) {
		
		Tutorial3View tut = (Tutorial3View)state;
		tut.particles.clear();
		
		try 
		{
			HashMap<String, Object> hash = zoom.synchronizedWithSimulation();
			
			ZoomArrayList<RemotePositionedAgent> list = (ZoomArrayList<RemotePositionedAgent>) hash.get("particles");
			for(RemotePositionedAgent p : list)
			{
				Int2D pos = ((Int2D)zoom.getZoomAgentLocation(p.getPos()));
				tut.particles.setObjectLocation(p, pos);
			}
			
			ZoomArrayList<EntryNum<Double, Int2D>> listTrails = (ZoomArrayList<EntryNum<Double, Int2D>>) hash.get("trails");
			for(EntryNum<Double, Int2D> t : listTrails)
			{
				Int2D pos = (Int2D)zoom.getZoomAgentLocation(t.l);
				tut.trails.field[pos.getX()][pos.getY()] = t.r;
			}
			zoom.sendAckToCell(list.STEP);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
