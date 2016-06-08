package it.isislab.dmason.sim.app.DFlockersState;

import it.isislab.dmason.annotation.BatchAnnotation;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedAgentFactory;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import java.awt.Color;
import java.util.List;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.AdjustablePortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.util.Double2D;

public class DFlockers extends DistributedState<Double2D>
{



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DContinuousGrid2D flockers;
	ContinuousPortrayal2D p;

	@BatchAnnotation(
			domain = "100-300",
			suggestedValue = "250"
			)
	public double width = 150;
	@BatchAnnotation
	public double height = 150;
	@BatchAnnotation
	public int numFlockers = 20;
	@BatchAnnotation
	public double cohesion = 1.0;
	@BatchAnnotation
	public double avoidance = 1.0;
	@BatchAnnotation
	public double randomness = 1.0;
	@BatchAnnotation
	public double consistency = 1.0;
	@BatchAnnotation
	public double momentum = 1.0;
	@BatchAnnotation
	public double deadFlockerProbability = 0.1;
	@BatchAnnotation
	public double neighborhood = 10;

	public double jump = 0.7;  // how far do we move in a timestep?

	public double getCohesion() { return cohesion; }
	public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }
	public double getAvoidance() { return avoidance; }
	public void setAvoidance(double val) { if (val >= 0.0) avoidance = val; }
	public double getRandomness() { return randomness; }
	public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
	public double getConsistency() { return consistency; }
	public void setConsistency(double val) { if (val >= 0.0) consistency = val; }
	public double getMomentum() { return momentum; }
	public void setMomentum(double val) { if (val >= 0.0) momentum = val; }
	public int getNumFlockers() { return numFlockers; }
	public void setNumFlockers(int val) { if (val >= 1) numFlockers = val; }
	public double getWidth() { return width; }
	public void setWidth(double val) { if (val > 0) width = val; }
	public double getHeight() { return height; }
	public void setHeight(double val) { if (val > 0) height = val; }
	public double getNeighborhood() { return neighborhood; }
	public void setNeighborhood(double val) { if (val > 0) neighborhood = val; }
	public double getDeadFlockerProbability() { return deadFlockerProbability; }
	public void setDeadFlockerProbability(double val) { if (val >= 0.0 && val <= 1.0) deadFlockerProbability = val; }

	public double gridWidth ;
	public double gridHeight ;   
	public int MODE;

	public static String topicPrefix = "";


	public DFlockers(GeneralParam params)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),topicPrefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();

	}


	public DFlockers(GeneralParam params,String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(),topicPrefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();

	}
	
	
	public DFlockers(GeneralParam params,List<EntryParam<String, Object>> simParams, String prefix)
	{    	
		super(params,new DistributedMultiSchedule<Double2D>(), prefix,params.getConnectionType());
		this.MODE=params.getMode();
		gridWidth=params.getWidth();
		gridHeight=params.getHeight();
		topicPrefix = prefix; 
		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				this.getClass().getDeclaredField(entryParam.getParamName()).set(this, entryParam.getParamValue());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (EntryParam<String, Object> entryParam : simParams) {

			try {
				System.out.println(this.getClass().getDeclaredField(entryParam.getParamName()).get(this));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



	}
	public DFlockers()
	{
		super();

	}

	public void start()
	{
		super.start();

		// set up the flockers field.  It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us.  Hmph,
		// that's 16 hash lookups! I would have guessed that 
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal.  Go figure.
		try 
		{

			flockers = DContinuousGrid2DFactory.createDContinuous2D(neighborhood/1.5,gridWidth, gridHeight,this,
					super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"flockers", topicPrefix,true);
			init_connection();
		} catch (DMasonException e) { e.printStackTrace(); }


		/*
		 * Spawn agents only on cell 0-0
		 */
		//if ( (TYPE.pos_i == 0 && TYPE.pos_j == 0) )
		{



			DFlocker f= (DFlocker) DistributedAgentFactory.newIstance(
					DFlocker.class,
					new Class[]{SimState.class,Double2D.class,Integer.class},
					new Object[]{this,new Double2D(0,0),10},
					DFlockerState.class);


			while(flockers.size() != super.NUMAGENTS / super.NUMPEERS)
			{
				f.setPos(flockers.getAvailableRandomLocation());

				//    			if (random.nextBoolean(deadFlockerProbability))
				//    				f.dead = true;



				if(flockers.setObjectLocation(f, f.pos))
				{
					Color c=new Color(
							128 + this.random.nextInt(128),
							128 + this.random.nextInt(128),
							128 + this.random.nextInt(128));
					f.setColor(c);
					schedule.scheduleOnce(f);
					f= (DFlocker) DistributedAgentFactory.newIstance(
							DFlocker.class,
							new Class[]{SimState.class,Double2D.class,Integer.class},
							new Object[]{this,f.getPos(),10},
							DFlockerState.class);
				}

			}
		} //if ( (TYPE.pos_i == 0 && TYPE.pos_j == 0) )

		try {
			getTrigger().publishToTriggerTopic("Simulation cell "+flockers.cellType+" ready...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		doLoop(DFlockers.class, args);
		System.exit(0);
	}

	public DistributedField getField() 
	{
		return flockers;
	}


	public SimState getState() 
	{
		return this;
	}

	public boolean setPortrayalForObject(Object o) 
	{
		if(p!=null)
		{
			DFlocker f=(DFlocker)o;
			SimplePortrayal2D pp = new AdjustablePortrayal2D(new MovablePortrayal2D(new OrientedPortrayal2D(new SimplePortrayal2D(),0,4.0,
					f.getColor(),
					OrientedPortrayal2D.SHAPE_COMPASS)));
			p.setPortrayalForObject(o, pp);
			return true;
		}
		return false;
	}
	@Override
	public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
		// TODO Auto-generated method stub
		flockers.setObjectLocation(rm,loc);
		setPortrayalForObject(rm);
	}

}