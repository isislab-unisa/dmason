package dmason.sim.field.network;

import java.io.Serializable;
import java.util.ArrayList;

import dmason.sim.field.CellType;

public class RemoteNetwork implements Serializable{
	
	protected int nodeId;
	protected ArrayList<AgentCell> listaRegioni=new ArrayList<AgentCell>();
	protected boolean notSimulated=false;
	protected boolean startInContinuos=false;
	
	public boolean isStart() {
		return startInContinuos;
	}

	public RemoteNetwork(){}

	public void setNetworkId(int networkId) {
		this.nodeId = networkId;
	}

	public void setNotSimulated(boolean notSimulated) {
		this.notSimulated = notSimulated;
	}

	public boolean isNotSimulated() {
		return notSimulated;
	}

	public int getNetworkId() {
		return nodeId;
	}
	
	public void addRegione(AgentCell cell){
		listaRegioni.add(cell);
	}

	public ArrayList<AgentCell> getListaRegioni() {
		return listaRegioni;
	}
	
}
