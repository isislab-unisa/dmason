package dmason.util.inspection;

import sim.engine.Schedule;

public class InspectableSchedule extends Schedule
{
	protected long steps;
	protected double time;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InspectableSchedule()
	{
		
	}
	
	@Override
	public long getSteps()
	{
		return steps;
	}
	
	@Override
	public double getTime()
	{
		return time;
	}
	
	public void timeWarp(long step, double time)
	{
		this.steps = step;
		this.time = time;
	}

}
