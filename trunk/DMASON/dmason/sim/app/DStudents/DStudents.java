package dmason.sim.app.DStudents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import sim.app.wcss.tutorial05.Students;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import dmason.batch.data.GeneralParam;
import dmason.sim.app.DFlockers.DFlocker;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import dmason.sim.field.network.DNetwork;
import dmason.sim.field.network.DNetworkAbstract;
import dmason.sim.field.network.DNetworkFactory;
import dmason.sim.field.network.AuxiliaryGraph;
import dmason.sim.field.network.Node;
import dmason.util.exception.DMasonException;

public class DStudents extends DistributedState<Double2D> {

	private static final long serialVersionUID = -3011211800868793951L;
	public DContinuous2D yard;
	public double forceToSchoolMultiplier = 0.001;
	public double randomMultiplier = 1;
	//public Bag lista=new Bag();

	/*-----------------network-----------------*/
	public DNetworkAbstract network;
	/*-------------------------------------*/

	/*parametri aggiuntivi*/
	public double gridWidth ;
	public double gridHeight ;   
	public int MODE;
	public int maxDistance;
	public static String topicPrefix = "";
	private static boolean isToroidal=true;
	public int I,J;
	double Fwidth,Fheight,Iwidth,Iheight;
	public double dimWidth,dimHeight;
	public AuxiliaryGraph supGraph;
	public int rows,columns;

	/*-----------*/

	public DStudents()
	{
		super();
	}

	public DStudents(GeneralParam params){
		super(params.getMaxDistance(),params.getRows(), params.getColumns(),params.getNumAgents(),params.getI(),
				params.getJ(),params.getIp(),params.getPort(),params.getMode(),
				isToroidal,new DistributedMultiSchedule<Double2D>(),topicPrefix);
		ip = params.getIp();
		port = params.getPort();
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		maxDistance=params.getMaxDistance();
		gridHeight=params.getHeight();
		I=params.getI();
		J=params.getJ();
		supGraph=new AuxiliaryGraph();
		this.rows=params.getRows();
		this.columns=params.getColumns();

		System.out.println("valori iniziali: "+I+" "+J);
	}

	@Override
	public DistributedField getField() {
		return yard;
	}

	@Override
	public void addToField(RemoteAgent<Double2D> rm, Double2D loc) {
		yard.setObjectLocation(rm,loc);
		setPortrayalForObject(rm);
	}

	@Override
	public SimState getState() {
		return this;
	}

	@Override
	public boolean setPortrayalForObject(Object o) {
		if (yard.p!=null){

			yard.p.setPortrayalForObject(o, new OvalPortrayal2D(Color.RED) );

			return true;

		}
		return false;
	}

	/* metodo start */
	public void start(){
		super.start();
		//yard.clear();

		try 
		{
			//maxDistance=8;
			yard = DContinuous2DFactory.createDContinuous2D(1.0,gridWidth,gridHeight,this,maxDistance,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"students",topicPrefix);
			dimWidth=yard.my_width;
			dimHeight=yard.my_height;
			network=DNetworkFactory.createDNetwork(false,gridWidth,gridHeight, TYPE.pos_i,TYPE.pos_j,"studentsNetwork", topicPrefix,this,MODE,supGraph,rows,columns,yard);
			init_connection();

		}catch (DMasonException e) { e.printStackTrace();}

		System.out.println(I+" "+J);
		Set<Node> lista=supGraph.getGraph().vertexSet();
		Iterator<Node> iter;
		Node agent;
		iter=lista.iterator();
		while(iter.hasNext()){
			agent=iter.next();

			DStudent student= new DStudent(this,new Double2D(agent.getX(),agent.getY()));
			student.setPos(yard.setAvailableRandomLocation(student));
			student.setNetworkId(agent.getId());
			yard.setObjectLocation(student, student.loc);
			network.addNode(student,agent);
			schedule.scheduleOnce(student);


		}
		network.initEdge();
		network.initTopicNetwork();
		((DistributedMultiSchedule)((DistributedState)this).schedule).addField(network);
		yard.network=(DNetwork) network;

		try {
			getTrigger().publishToTriggerTopic("Simulation cell "+yard.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		doLoop(Students.class, args);
		System.exit(0);
	}  

}
