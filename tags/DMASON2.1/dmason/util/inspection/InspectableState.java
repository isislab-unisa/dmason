package dmason.util.inspection;

import sim.engine.SimState;

/**
 * 
 * @author Luca Vicidomini
 *
 */
public class InspectableState extends SimState
{

	public InspectableState()
	{
		super(null, new InspectableSchedule());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
