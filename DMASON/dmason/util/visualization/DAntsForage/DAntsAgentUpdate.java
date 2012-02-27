package dmason.util.visualization.DAntsForage;

import java.util.HashMap;
import sim.engine.SimState;
import sim.util.Int2D;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.EntryNum;
import dmason.util.visualization.Updater;
import dmason.util.visualization.ZoomArrayList;
import dmason.util.visualization.ZoomViewer;

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
				RemoteAgent<Int2D> r=(RemoteAgent)s;
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