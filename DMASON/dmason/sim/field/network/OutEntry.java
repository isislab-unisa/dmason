package dmason.sim.field.network;

import sim.util.Double2D;
import dmason.sim.engine.RemoteAgent;

public class OutEntry {

	private int idNetwork;
	private RemoteAgent<Double2D> agent;
	
	public OutEntry(int id,RemoteAgent<Double2D> a){
		this.idNetwork=id;
		this.agent=a;
	}


	public int getIdNetwork() {
		return idNetwork;
	}


	public RemoteAgent<Double2D> getAgent() {
		return agent;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutEntry other = (OutEntry) obj;
		if (idNetwork != other.idNetwork)
			return false;
		return true;
	}
	
	
	
}
