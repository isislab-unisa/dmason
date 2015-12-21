package it.isislab.dmason.sim.app.DNetworkSIR;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemoteAgentState;

public interface DVertexState15 extends RemoteAgentState{
		public boolean getVal(DistributedMultiSchedule schedule);
		public void setVal(DistributedMultiSchedule schedule,boolean isVisited);
}
