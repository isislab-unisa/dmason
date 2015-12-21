package it.isislab.dmason.sim.app.DFlockersState;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemoteAgentState;


public interface DFlockerState extends RemoteAgentState{
	public int getVal(DistributedMultiSchedule schedule);
	public void setVal(DistributedMultiSchedule schedule,int val);
}
