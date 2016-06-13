package it.isislab.dmason.test.support;

import java.util.Comparator;

import it.isislab.dmason.sim.engine.RemotePositionedAgent;

public class AgentComparator implements Comparator<RemotePositionedAgent> {
	
	public AgentComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(RemotePositionedAgent o1, RemotePositionedAgent o2) {
		// TODO Auto-generated method stub
		return o1.getId().compareTo(o2.getId());
	}

}
