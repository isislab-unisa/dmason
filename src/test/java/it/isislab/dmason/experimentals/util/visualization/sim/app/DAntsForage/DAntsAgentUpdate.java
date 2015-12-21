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
package it.isislab.dmason.experimentals.util.visualization.sim.app.DAntsForage;

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
public class DAntsAgentUpdate extends Updater{

	public DAntsAgentUpdate(ZoomViewer zoom) {
		super(zoom);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void step(SimState state) {
		
		
		AntsForageZoom ants=(AntsForageZoom)state;
		ants.buggrid.clear();
		
		try {

			HashMap<String, Object> hash=zoom.synchronizedWithSimulation();
		
			ZoomArrayList sparse = (ZoomArrayList)hash.get("buggrid");
			for(Object s : sparse)
			{
				RemotePositionedAgent<Int2D> r=(RemotePositionedAgent)s;
				Int2D pos = ((Int2D)zoom.getZoomAgentLocation(r.getPos()));
				ants.buggrid.setObjectLocation(r, pos);
			}
			
			ZoomArrayList<EntryNum<Double, Int2D>> homeGrid=(ZoomArrayList<EntryNum<Double, Int2D>>)hash.get("toHomeGrid");
			for(EntryNum<Double, Int2D> e : homeGrid)
			{
				Int2D pos = (Int2D)zoom.getZoomAgentLocation(e.l);
				ants.toHomeGrid.field[pos.getX()][pos.getY()] =e.r;
			}

			ZoomArrayList<EntryNum<Double, Int2D>> foodGrid=(ZoomArrayList<EntryNum<Double, Int2D>>)hash.get("toFoodGrid");
			for(EntryNum<Double, Int2D> e : foodGrid)
			{
				Int2D pos = (Int2D)zoom.getZoomAgentLocation(e.l);
				ants.toFoodGrid.field[pos.getX()][pos.getY()] =e.r;
			}
			
			zoom.sendAckToCell(sparse.STEP);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Problemi nella hash per step "+state.schedule.getSteps());
			//e.printStackTrace();
		}
	}

}