package it.isislab.dmason.sim.app.DNetworkSIR;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemoteAgentState;
import it.isislab.dmason.sim.engine.RemoteAgentStateMethodHandler;

public interface DVertexState15 extends RemoteAgentState{
		public boolean getVal(DistributedMultiSchedule schedule);
		public void setVal(DistributedMultiSchedule schedule,boolean isVisited);
}
