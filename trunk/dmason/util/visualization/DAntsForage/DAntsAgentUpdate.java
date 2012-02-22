package dmason.util.visualization.DAntsForage;

import java.util.HashMap;

import sim.engine.SimState;
import sim.field.SparseField;
import sim.field.grid.DoubleGrid2D;
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
		System.out.println("Pre sincro local step "+state.schedule.getSteps());
		HashMap<String, Object> hash=zoom.synchronizedWithSimulation();
		
		System.out.println("Post sincro local step "+state.schedule.getSteps());
		
			ZoomArrayList sparse = (ZoomArrayList)hash.get("buggrid");
	
		
			for(Object s: sparse)
			{
				RemoteAgent<Int2D> r=(RemoteAgent)s;
				ants.buggrid.setObjectLocation(r, r.getPos());
			}
			
			ZoomArrayList<EntryNum<Double, Int2D>> homeGrid=(ZoomArrayList<EntryNum<Double, Int2D>>)hash.get("toHomeGrid");
			
			for(EntryNum<Double, Int2D> entry:homeGrid)
			{
				ants.toHomeGrid.field[entry.l.getX()][entry.l.getY()] =entry.r;
			}

			ZoomArrayList<EntryNum<Double, Int2D>> foodGrid=(ZoomArrayList<EntryNum<Double, Int2D>>)hash.get("toFoodGrid");
			
			for(EntryNum<Double, Int2D> entry:foodGrid)
			{
				ants.toFoodGrid.field[entry.l.getX()][entry.l.getY()] =entry.r;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Problemi nella hash per step "+state.schedule.getSteps());
			//e.printStackTrace();
		}
	}

}
