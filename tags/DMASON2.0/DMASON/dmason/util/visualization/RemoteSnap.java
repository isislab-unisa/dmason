package dmason.util.visualization;

import java.io.Serializable;
import java.util.HashMap;

import dmason.sim.field.CellType;

/**
 * This class holds the informations that every region sends
 * to a possibly active viewer.
 * @author unascribed
 * @author Luca Vicidomini
 *
 */
public class RemoteSnap implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Coordinates of the region that generated this snap 
	 */
	public short i, j;
	
	/**
	 * Simulation's step these informations are referring to.
	 */
	public long step;
	
	/**
	 * Simulation's time these informations are referring to.
	 */
	public double time;
	
	/**
	 * A visual snapshot of agents' positions in current region
	 * (if requested by the viewer)
	 */
	public byte[] image;
	
	/**
	 * A list of statistics read from simulations (as requested by
	 * the viewer). These are in the form (key, value). Suppose that
	 * the simulation instance has a method called getNumAgents
	 * returning and <code>int</code>. Then a valid entry for
	 * <code>stats</code> may be ("NumAgents", 200). 
	 */
	public HashMap<String, Object> stats;
	
	/**
	 * Constructor
	 * @param type Identifies the cell generating this snap
	 * @param step Simulation's step
	 * @param time Simulation's time (needed for graphs)
	 */
	public RemoteSnap(CellType type, long step, double time)
	{
		this.i = (short)type.pos_i;
		this.j = (short)type.pos_j;
		this.step = step;
		this.time = time;
		this.image = null;
		this.stats = null;
	}

}
