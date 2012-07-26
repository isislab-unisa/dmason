package dmason.util.visualization;

import sim.engine.Steppable;

public abstract class  Updater implements Steppable{
	
	public ZoomViewer zoom;
	public Updater(ZoomViewer zoom){this.zoom=zoom;}
}
