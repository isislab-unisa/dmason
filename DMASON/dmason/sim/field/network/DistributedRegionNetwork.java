package dmason.sim.field.network;

import java.io.Serializable;
import java.util.ArrayList;

import sim.util.Bag;

import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegionInterface;

public class DistributedRegionNetwork implements Serializable, DistributedRegionInterface {

	public ArrayList<AgentCell> getOutlist() {
		return outlist;
	}

	public void setOutlist(ArrayList<AgentCell> outlist) {
		this.outlist = outlist;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public void setType(CellType type) {
		this.type = type;
	}

	public void setNodeList(Bag nodeList) {
		this.nodeList = nodeList;
	}

	public long step;
	public CellType type;
	public Bag nodeList;
	public ArrayList<AgentCell> outlist=new ArrayList<AgentCell>();
	//public Bag outList;
	public boolean notificaSpostamento;
	
	public DistributedRegionNetwork(long step, CellType type, Bag nodeList,ArrayList<AgentCell> outlist,boolean flag){
		this.step=step;
		this.type=type;
		this.nodeList=nodeList;
		this.outlist=outlist;
		this.notificaSpostamento=flag;
	}
	
	public CellType getType() {
		return type;
	}

	public boolean isNotificaSpostamento() {
		return notificaSpostamento;
	}

	public void setNotificaSpostamento(boolean flag) {
		this.notificaSpostamento = flag;
	}

	public ArrayList<AgentCell> getOutList() {
		return outlist;
	}

	@Override
	public long getStep() {
		return step;
	}
	
	public Bag getNodeList(){
		return nodeList;
	}

	@Override
	public int getPosition() {
		return 0;
	}

}
