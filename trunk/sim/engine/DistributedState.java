package dmason.sim.engine;

import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;

/**
 * An abstract class that inherits all SimState functionalities and adds necessary distributed informations:
 * the number of agents, used to calculate the sequence of agents id, the type of simulated cell, the maximum 
 * shift of agents, the number of peers involved in the simulation, the ip and port of server.
 * @param <E> the type of locations
 */
public abstract class DistributedState<E> extends SimState  
{
	 public int NUMAGENTS;
	 private int count_id;
	 public CellType TYPE;
	 public int MAX_DISTANCE;
	 public int NUMPEERS;
	 public String ip;
	 public String port;
	 private MersenneTwisterFast randomRegion;
	 
	 public DistributedState(int max_d,int num_peers,int num_agents,int i, int j,DistributedScheduleMulti<E> sched) 
	 {
		super(null, sched);
		this.TYPE=new CellType(i, j);
		randomRegion=new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE=max_d;
		this.NUMPEERS=num_peers;
		this.NUMAGENTS=num_agents;
		this.count_id=NUMAGENTS*TYPE.getInitialValue();
		this.setRandom(randomRegion);
	 }
	 
	 /**
	  * 
	  * @param random the MersenneTwisterFast for SimState
	  * @param schedule a specific schedule
	  * @param max_d maximum shift of an agent
	  * @param num_peers the number of peers
	  * @param num_agents the number of agents
	  * @param i row in the matrix of peers
	  * @param j column in the matrix of peers
	  */ 
	 public DistributedState(int max_d,int num_peers,int num_agents,int i, int j) 
	 {
		super(null, new DistributedSchedule<E>());
		this.TYPE=new CellType(i, j);
		randomRegion=new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE=max_d;
		this.NUMPEERS=num_peers;
		this.NUMAGENTS=num_agents;
		this.count_id=NUMAGENTS*TYPE.getInitialValue();
		this.setRandom(randomRegion);
	 }
	 
	 /**
	  * 
	  * @param random the MersenneTwisterFast for SimState
	  * @param schedule a specific schedule
	  * @param max_d maximum shift of an agent
	  * @param num_peers the number of peers
	  * @param num_agents the number of agents
	  * @param i row in the matrix of peers
	  * @param j column in the matrix of peers
	  */ 
	 public DistributedState(Object[] objs) 
	 {
		super(null, new DistributedSchedule<E>());
		this.TYPE=new CellType((Integer)objs[5], (Integer)objs[6]);
		randomRegion=new MersenneTwisterFast(this.TYPE.getInitialValue());
		this.MAX_DISTANCE=(Integer)objs[0];
		this.NUMPEERS=(Integer)objs[1];
		this.NUMAGENTS=(Integer)objs[2];
		this.count_id=NUMAGENTS*TYPE.getInitialValue();
		this.setRandom(randomRegion);
	 }
	 
	 // abstract methods those must be implemented in the subclasses
	 public abstract DistributedField<E> getField();
	 public abstract void addToField(RemoteAgent<E> rm,E loc);
	 public abstract SimState getState();
	 public abstract boolean setPortrayalForObject(Object o);
	 
	 public CellType getType(){return TYPE;}
	
	 /**
	  * @return the next available Id 
	  */
	 public int nextId() { return ++count_id; }	
}