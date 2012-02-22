package dmason.util.visualization.DParticles;

import java.util.HashMap;

import sim.engine.SimState;
import sim.util.Int2D;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.EntryNum;
import dmason.util.visualization.Updater;
import dmason.util.visualization.ZoomArrayList;
import dmason.util.visualization.ZoomViewer;

public class DParticlesAgentUpdate extends Updater {

	public DParticlesAgentUpdate(ZoomViewer zoom) {
		super(zoom);
	}
	
	public void step(SimState state) {
		
		Tutorial3View tut = (Tutorial3View)state;
		tut.particles.clear();
		
		try 
		{
			HashMap<String, Object> hash = zoom.synchronizedWithSimulation();
			
			ZoomArrayList<RemoteAgent> list = (ZoomArrayList<RemoteAgent>) hash.get("particles");
			
			for(RemoteAgent a : list)
			{
				tut.particles.setObjectLocation(a, (Int2D)a.pos);
			}
			
			ZoomArrayList<EntryNum<Double, Int2D>> listTrails = (ZoomArrayList<EntryNum<Double, Int2D>>) hash.get("trails");
			System.out.println("DIMENSIONEEEEEE: "+listTrails.size());
			for(EntryNum<Double, Int2D> a : listTrails)
			{
				tut.trails.field[a.l.getX()][a.l.getY()] = a.r;
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
