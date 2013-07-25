package dmason.sim.field.network;

import sim.engine.SimState;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.DistributedField;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.util.exception.DMasonException;

public final class DNetworkFactory {
	
	public static final DNetworkAbstract createDNetwork(boolean directed,double gridWidth, double gridHeight, int i, int j, String name, String topicPrefix,SimState sm,int MODE,AuxiliaryGraph supGraph,int rows,int columns,DContinuous2D f) throws DMasonException{
		
		DistributedField field = new DNetwork(directed, i, j,name, topicPrefix, sm,supGraph,MODE,gridWidth,gridHeight,rows,columns,f);
		
		//((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
		
		return (DNetworkAbstract) field;
		
	}

}
