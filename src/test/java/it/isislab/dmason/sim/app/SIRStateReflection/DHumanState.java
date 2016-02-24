package it.isislab.dmason.sim.app.SIRStateReflection;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemoteAgentState;

public interface DHumanState extends RemoteAgentState{
	
	public boolean getIsInfected(DistributedMultiSchedule schedule);
	public void setIsInfected(DistributedMultiSchedule schedule, boolean val);
	public boolean getIsResistent(DistributedMultiSchedule schedule);
	public void setIsResistent(DistributedMultiSchedule schedule, boolean val);
}