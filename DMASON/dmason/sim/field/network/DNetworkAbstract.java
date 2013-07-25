package dmason.sim.field.network;

import java.util.ArrayList;

import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.Entry;
import dmason.sim.field.Region;
import dmason.sim.field.RegionMap;
import dmason.sim.field.UpdateMap;
import dmason.sim.field.continuous.RegionDouble;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import sim.engine.SimState;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;

public abstract class DNetworkAbstract<E> extends Network implements DistributedField<E> {

	public ConnectionWithJMS connection=new ConnectionNFieldsWithActiveMQAPI();
	public SimState sm;
		
	/*----------*/

	
	public DNetworkAbstract(boolean directed){
		super(directed);
	}
	
	public DNetworkAbstract(){
		super();
	}
	
	public DNetworkAbstract(Network other){
		super(other);
	}
	
	public void initEdge(){}
	
	public void addNode(Object o,Node v){}
	
	public void initTopicNetwork() {}
	
	private void createNetworkRegin(){}
	
	private boolean isMy(Node node){
		return false;
	}
	
	private CellDimension createNetworkRegin(int r,int c){
		return null;
	}
	
	public void changeOut(RemoteAgent<Double2D> rm){};
	
	public void addNewExternNode(RemoteAgent<Double2D> o) {};
	
	public void unLock(){};
	
	public void changeListaOut(RemoteAgent<Double2D> rm){};
	
	public void ListaOut(){};
	
	
	
}
