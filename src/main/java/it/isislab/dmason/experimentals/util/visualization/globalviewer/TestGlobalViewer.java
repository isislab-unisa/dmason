package it.isislab.dmason.experimentals.util.visualization.globalviewer;

import java.util.ArrayList;

public class TestGlobalViewer {
	   public static void main(String[] args)
	    {	
			ArrayList<SimComboEntry> sims=new ArrayList<SimComboEntry>();
	    	sims.add(new SimComboEntry("Flockers", "it.isislab.dmason.util.visualization.sim.app.DFlockers.FlockersWithUIView", "it.isislab.dmason.sim.app.DFlockers.DFlockers"));
	    	sims.add(new SimComboEntry("Particles", "it.isislab.dmason.util.visualization.sim.app.DParticles.Tutorial3ViewWithUI", "it.isislab.dmason.sim.app.DParticles.DParticles"));
	    	sims.add(new SimComboEntry("Ants Foraging", "it.isislab.dmason.util.visualization.sim.app.DAntsForage.AntsForageWithUIZoom", "it.isislab.dmason.sim.app.DAntsForage.DAntsForage"));
			GlobalViewer lv = new GlobalViewer(sims);
			lv.initComponents();
			lv.LauncherViewer.setVisible(true);
		}
}
