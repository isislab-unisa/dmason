package dmason.sim.field.network;

import java.io.Serializable;

import dmason.sim.field.CellType;

public class AgentCell implements Serializable {

	//inizialmente era Object tenere presente se qualcosa non va...
	public Object agent;
	public CellType cellType;
	
	public AgentCell(Object agent,CellType celltype){
		this.agent=agent;
		this.cellType=celltype;
	}

	public Object getAgent() {
		return agent;
	}

	public CellType getCellType() {
		return cellType;
	}

	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}
	
	
	
}
