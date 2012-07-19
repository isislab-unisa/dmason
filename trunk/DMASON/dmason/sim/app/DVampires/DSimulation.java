package dmason.sim.app.DVampires;

import java.awt.Color;
import java.awt.Graphics2D;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.continuous.DContinuous2DFactory;
import dmason.util.exception.DMasonException;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.HexagonalPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.*;

/**
 * This class represents the simulation model.
 * @author Luca Vicidomini
 */
public class DSimulation extends DistributedState<Double2D>
{
	private static final long serialVersionUID = 1L;
	
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
//
//SIMULATION PARAMETERS
//
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

	/**
	 * Number of active roosts when simulation starts.
	 */
	protected int numRoosts = 10;
	public int getNumRoosts() { return numRoosts; }
	public void setNumRoosts(int numRoosts) { this.numRoosts = numRoosts; }

	/**
	 * Number of bats alive when simulation starts.
	 */
	protected int numBats = 103;
	public int getNumBats() { return numBats; }
	public void setNumBats(int numBats) { this.numBats = numBats; }

	/**
	 * Percentage of cheater bats when simulation starts.
	 */
	protected double cheatersRate = 0.01;
	public double getCheatersRate() { return cheatersRate; }
	public void setCheatersRate(double cheatersRate) { this.cheatersRate = cheatersRate; }

	/**
	 * Max number of living bats in the world. Set 0 if bats
	 * may reproduce without limit.
	 */
	private int carryingCapacity = 500;
	public int getCarryingCapacity() { return carryingCapacity; }
	public void setCarryingCapacity(int carryingCapacity) { this.carryingCapacity = carryingCapacity; }

	/**
	 * Percentage of agents that will invert the altruistic trait at birth.
	 */
	private double mutationRate = 0.01;
	public double getMutationRate() { return mutationRate; }
	public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }
	public Interval domMutationRate() { return new Interval(0.0, 1.0); }

	/**
	 * Max number of bats a DRoost can reach before splitting.
	 */
	private int roostMaxSize = 30;
	public int getRoostMaxSize() { return roostMaxSize; }
	public void setRoostMaxSize(int roostMaxSize) { this.roostMaxSize = roostMaxSize; }

	/**
	 * Percentage of roosts that will change their launch size when split.
	 */
	private double roostMutationRate = 0.02;
	//public double getRoostMutationRate() { return roostMutationRate; }
	//public void setRoostMutationRate(double roostMutationRate) { this.roostMutationRate = roostMutationRate; }

	/**
	 * Factor controlling how much a new DRoost can expand/shrink compared
	 * to the original DRoost. A factor of 0.2 means that the new DRoost's
	 * launch size will be: <code>oldLaunchSize + c * oldLaunchSize</code>
	 * being c a number in the [-0.2; +0.2] range.
	 */
	private double roostMutationFactor = 0.2;
	public double getRoostMutationFactor() { return roostMutationFactor; }
	public void setRoostMutationFactor(double roostMutationFactor) { this.roostMutationFactor = roostMutationFactor; }

	/**
	 * If true, allow roosts splitting.
	 */
	private boolean multipleRoosts = true;
	public boolean getMultipleRoosts() { return multipleRoosts; }
	public void setMultipleRoosts(boolean multipleRoosts) { this.multipleRoosts = multipleRoosts; }

	/**
	 * Controls how vampires migrate from a newly created DRoost.
	 * If true, new roosts created by splitting are populated by a random
	 * selection of vampires from the parent DRoost. If false, new roosts
	 * created by splitting are populated with the youngest half of the
	 * parent's population.
	 */
	private boolean randomiseOnSplit = false;
	public boolean getRandomiseOnSplit() { return randomiseOnSplit; }
	public void setRandomiseOnSplit(boolean randomiseOnSplit) { this.randomiseOnSplit = randomiseOnSplit; }

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
	//  RUN-TIME STATISTICS  
	//
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Number of living bats in the world.
	 */
	protected int population;
	public int getPopulation() { return population; }

	/**
	 * Number of living cheater vampires.
	 */
	protected int cheatersPopulation;
	public int getCheaters() { return cheatersPopulation; }

	/**
	 * Number of active roosts.
	 */
	public int getRoosts() { return roosts.size(); }

	/**
	 * Number of roosts having at least one cheater bat inside.
	 */
	protected int cheatersRoosts;
	public int getCheatersRoosts() { return cheatersRoosts; }
	public void changeCheatersRoosts(int delta) { cheatersRoosts += delta; }

	/**
	 * Number of roosts created during simulation
	 */
	protected int roostsCreated;
	public int getRoostsCreated() { return roostsCreated; }

	/**
	 * Number of roosts destroyed during simulation
	 */
	protected int roostsDestroyed;
	public int getRoostsDestroyed() { return roostsDestroyed; }

	/**
	 * Number of bats died by starvation
	 */
	protected int starved;
	public int getStarved(){return starved;}
	public void addStarved(){starved++;}

	/**
	 * Total number of food requests accepted.
	 */
	protected int donations;
	public int getDonations(){ return donations; }
	public void addDonation(){ donations++; }

	/**
	 * Total number of food requests refused.
	 */
	protected int refusals;
	public int getRefusals(){ return refusals; }
	public void addRefusals(){ refusals++; }


	// The followings are because we don't want to scan the
	// roosts vector two time per step

	protected long distrLastUpdateStep = -1;
	protected int[] launchDistr;
	protected int[] foodDistr;

	private void updateDistr()
	{
		// Avoid updating launchDistr & foodDistr if already
		// updated at current simulation step
		if (distrLastUpdateStep == schedule.steps)
			return;
		distrLastUpdateStep = schedule.steps;

		IntBag launch = new IntBag(population);
		IntBag food = new IntBag(population);
		int size = roosts.size();
		for (int i = 0; i < size; i++)
		{
			DRoost r = (DRoost)roosts.get(i);
			launch.add(r.launchSize);
			int bats = r.bats.size();
			for (int j = 0; j < bats; j++)
				food.add(((DVampire)r.bats.get(j)).getHealth());
		}
		launchDistr = launch.toArray();
		foodDistr = food.toArray();
	}

	/**
	 * LaunchSize distribution
	 */
	public int[] getLaunchSizeDistr()
	{
		updateDistr();
		return launchDistr;
	}

	/**
	 * Food distribution
	 */
	public int[] getFoodDistr()
	{
		updateDistr();
		return foodDistr;
	}
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
	//  DISTRIBUTED-ENABLED RUN-TIME STATISTICS  
	//
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	
	public int reducePopulation(Object[] shards)
	{
		int pop = 0;
		int n = shards.length;
		for (int i = 0; i < n; i++)
			pop += (Integer)shards[i];
		this.population = pop;
		return pop;
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
	//IMPLEMENTATION  
	//
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Useful for batch executions :)
	 */
	private static String[] commandLineArgs;

	/**
	 * Used to generate unique names for roosts and vampires.
	 */
	private int counter;

	/**
	 * Vector of active roosts.
	 */
	Bag roosts = new Bag();

	/**
	 * World space where the bats live in.
	 * At present time, this is used for inspection purposes only via GUI.
	 */
	DContinuous2D world;

	/**
	 * Distributed simulation constructor.
	 */
	public DSimulation(Object[] params)
    {    	
    	super(
    			(Integer)params[2],
    			(Integer)params[3],
    			(Integer)params[4],
    			(Integer)params[7],
    			(Integer)params[8],
    			(String)params[0],
    			(String)params[1],
    			(Integer)params[9],
    			isToroidal,
    			new DistributedMultiSchedule<Double2D>());
    	
    	ip = params[0]+"";
    	port = params[1]+"";
    	fieldMode=(Integer)params[9];
    	gridWidth=(Integer)params[5];
    	gridHeight=(Integer)params[6];
    }
	
	/**
	 * Simulation constructor.
	 * @param seed The seed used to initialize the random numbers generator
	 */
//	public DSimulation(long seed)
//	{
//		super(seed);
//		setCommandLineArgs(this);
//		System.out.println(
//				"\nVAMPIRES 3.6" +
//				"\n-------------------------------------------------------------------------------" +
//				"\nYou can set following parameters at run-time" +
//				"\n" +
//				"\nPARAMATER           TYPE          DEFAULT     CURRENT" +
//				"\n-------------------------------------------------------------------------------" +
//				"\nnumRoosts               int           10          "+getNumRoosts()+
//				"\nnumBats                 int           103         "+getNumBats()+
//				"\ncheatersRate            double        0.01        "+getCheatersRate()+
//				"\ncarryingCapacity        int           500         "+getCarryingCapacity()+
//				"\nmutationRate            double        0.01        "+getMutationRate()+
//				"\nroostMaxSize            int           30          "+getRoostMaxSize()+
//				//"\nroostMutationRate       double       0.02        "+getRoostMutationRate()+
//				"\nroostMutationFactor     double        0.02        "+getRoostMutationFactor()+
//				"\nmultipleRoosts          boolean       true        "+getMultipleRoosts()+
//				"\nrandomiseOnSplit        boolean       false       "+getRandomiseOnSplit()+
//				"\n" +
//				"\ni.e. -numRoosts 10" +
//				"\n-------------------------------------------------------------------------------" +
//				"\n\n"
//		);
//	}

	/**
	 * Check if bats are allowed to breed. Bats can't breed if maximum
	 * capacity <code>carryingCapacity</code> is reached.
	 * @return true if bats can breed.
	 */
	public boolean canBreed()
	{
		// If carryingCapacity == 0, let the DVampire reproduce
		return carryingCapacity == 0 || population < carryingCapacity;
	}

	/**
	 * Adds a bat to the simulation. This is an utility method meant to
	 * be called both when setting-up the simulation and when
	 * a new bat is born.
	 * @param DRoost The DRoost where the new bat belongs to
	 * @param age Initial bat age.0 for newborns.
	 * @param cheater True if the bat is a cheater.
	 */
	private DVampire createVampire(DRoost DRoost, int age, boolean cheater)
	{
		String name = String.format("V%05d", ++counter);
		DVampire DVampire = new DVampire(DRoost, age, cheater, name);
		DRoost.add(DVampire);
		population++;
		if (DVampire.isCheater())
			cheatersPopulation++;
		return DVampire;
	}

	/**
	 * Initialize the simulation
	 * @see sim.engine.SimState#start()
	 */
	@Override
	public void start()
	{
		super.start();
		
		try 
    	{
    		world = DContinuous2DFactory.createDContinuous2D(
    				1.0,
    				gridWidth,
    				gridHeight,
    				this,
    				super.MAX_DISTANCE,
    				TYPE.pos_i,
    				TYPE.pos_j,
    				super.NUMPEERS,
    				fieldMode,
    				"world");
    		init_connection();
    	} catch (DMasonException e) { e.printStackTrace(); }

		world.clear();
		roosts.clear();

		population = 0;
		cheatersPopulation = 0;
		cheatersRoosts = 0;
		roostsCreated = 0;
		roostsDestroyed = 0;
		starved = 0;
		donations = 0;
		refusals = 0;

		counter = 0;

		/*
		 * Decide how many bats will contains each DRoost.
		 * Also assures that exactly numBats bats will be created.
		 * The NetLogo implementation creates
		 * [numRoost * (numBats / numRoost + 1)] bats
		 * and then kills the exceeding bats.
		 * Note: needs numBats >= numRoosts
		 */
		int[] batsPerRoost = new int[numRoosts];

		// In this way, sum of batsPerRoost will be > numBats...
		for (int r = 0; r < numRoosts; r++)
			batsPerRoost[r] = numBats / numRoosts + 1;

		// ... so we need to prevent the creation of exceeding bats
		for (int cnt = batsPerRoost[0] * numRoosts; cnt > numBats; cnt--)
			batsPerRoost[random.nextInt(numRoosts)]--;

		// We are now ready to create exactly [numBats] bats
		for (int r = 0; r < numRoosts; r++)
		{
			String rname = String.format("DRoost %05d", ++counter);
			DRoost roost = new DRoost(this, roostMaxSize, rname);
			roosts.add(roost);
			place(roost);
			roost.simulation = this;
			//roost.setStoppable(schedule.scheduleRepeating(roost));
			schedule.scheduleOnce(roost);

			for (int v = 0; v < batsPerRoost[r]; v++)
			{
				int age = random.nextInt(DVampire.MAX_AGE);
				boolean cheater = random.nextDouble() < cheatersRate;
				createVampire(roost, age, cheater);
			}
		}
		
		try {
			getTrigger().publishToTriggerTopic("Simulation cell "+world.cellType+" ready...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Places a DRoost in a random location in the space.
	 * @param DRoost The DRoost we need to place.
	 */
	protected void place(DRoost roost)
	{
		roost.setPos(world.setAvailableRandomLocation(roost));
	}

	/**
	 * Notify a bat's death. Meant to be called by the dead bat itself.
	 * @param dead The dead bat
	 */
	public void vampireDied(DVampire dead)
	{
		//world.remove(dead);
		population--;
		if (dead.isCheater())
			cheatersPopulation--;
		dead = null;
	}

	/**
	 * Gives birth to a new bat.
	 * @param parent The breeding bat.
	 * @return A new bat, that is child of <code>parent</code>
	 */
	public DVampire vampireBred(DVampire parent)
	{
		boolean cheater = random.nextDouble() < mutationRate
				? !parent.isCheater()
				: parent.isCheater();
		DVampire child = createVampire(parent.getRoost(), 0, cheater);
		return child;
	}

	/**
	 * Called when every bat in a DRoost die.
	 * @param DRoost HT DRoost to detroy.
	 */
	protected void roostDestroy(DRoost roost)
	{
		roostsDestroyed++;
		world.remove(roost);
		roosts.remove(roost);
	}

	/**
	 * Called when a DRoost reaches its launchSize and needs to split.
	 * @param parent The splitting DRoost
	 * @return The newly created DRoost
	 */
	protected DRoost roostSplit(DRoost parent)
	{
		// Old model: varies launchSize in the [-2;+2] range
		//int launchSize = random.nextDouble() < roostMutationRate
		//? parent.launchSize + random.nextInt(5) - 2
		//: parent.launchSize;

		// New model: varies launchSize by a [0.8;1.2] factor
		// TODO non usa roostmutationfactor al momento	
		int launchSize = (int)Math.round(
				(double)parent.launchSize *
				(random.nextDouble() * .4 + .8) );
		String name = String.format("DRoost %05d", ++counter);
		DRoost newRoost = new DRoost(this, launchSize, name);

		roostsCreated++;
		roosts.add(newRoost);
		place(newRoost);

		newRoost.simulation = this;
		//newRoost.setStoppable(schedule.scheduleRepeating(newRoost));
		schedule.scheduleOnce(newRoost);

		return newRoost;
	}

	// Due metodi comodi barbaramente copiati da SimState
	static boolean keyExists(String key, String[] args) { for(int x=0;x<args.length;x++) if (args[x].equalsIgnoreCase(key)) return true; return false; }
	static String argumentForKey(String key, String[] args) { for(int x=0;x<args.length-1;x++) if (args[x].equalsIgnoreCase(key)) return args[x + 1]; return null; }

	public static void setCommandLineArgs(DSimulation simulation)
	{
		if (commandLineArgs == null)
			return;
		if (keyExists("-numroosts", commandLineArgs))
		{
			String arg = argumentForKey("-numroosts", commandLineArgs);
			simulation.setNumRoosts( Integer.parseInt(arg) );
		}
		if (keyExists("-numbats", commandLineArgs))
		{
			String arg = argumentForKey("-numbats", commandLineArgs);
			simulation.setNumBats( Integer.parseInt(arg) );
		}
		if (keyExists("-cheatersrate", commandLineArgs))
		{
			String arg = argumentForKey("-cheatersrate", commandLineArgs);
			simulation.setCheatersRate( Double.parseDouble(arg) );
		}
		if (keyExists("-carryingcapacity", commandLineArgs))
		{
			String arg = argumentForKey("-carryingcapacity", commandLineArgs);
			simulation.setCarryingCapacity( Integer.parseInt(arg) );
		}
		if (keyExists("-mutationrate", commandLineArgs))
		{
			String arg = argumentForKey("-mutationrate", commandLineArgs);
			simulation.setMutationRate( Double.parseDouble(arg) );
		}
		if (keyExists("-roostmaxsize", commandLineArgs))
		{
			String arg = argumentForKey("-roostmaxsize", commandLineArgs);
			simulation.setRoostMaxSize( Integer.parseInt(arg) );
		}
		//if (keyExists("-roostmutationrate", commandLineArgs))
		//{
		//String arg = argumentForKey("-roostmutationrate", commandLineArgs);
		//simulation.setRoostMutationRate( Double.parseDouble(arg) );
		//}
		if (keyExists("-multipleroosts", commandLineArgs))
		{
			String arg = argumentForKey("-multipleroosts", commandLineArgs);
			simulation.setMultipleRoosts( Boolean.parseBoolean(arg) );
		}
		if (keyExists("-randomiseonsplit", commandLineArgs))
		{
			String arg = argumentForKey("-randomiseonsplit", commandLineArgs);
			simulation.setRandomiseOnSplit( Boolean.parseBoolean(arg) );
		}

	}

	public static void main(String[] args)
	{
		commandLineArgs = args;
		doLoop(DSimulation.class, args);
		System.exit(0);
	}
	
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
//
// D-MASON  
//
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
	
	private static boolean isToroidal = true;
	public double gridWidth;
    public double gridHeight;
    public int fieldMode;
	
	@Override
	public DistributedField<Double2D> getField()
	{
		return world;
	}
	@Override
	public void addToField(RemoteAgent<Double2D> rm, Double2D loc)
	{
		world.setDistributedObjectLocation(loc, rm, this);
	}
	@Override
	public SimState getState()
	{
		return this;
	}
	@Override
	public boolean setPortrayalForObject(Object o)
	{
		if(world.p != null)
    	{
			Class<? extends Object> objClass = o.getClass();
			if (objClass == DVampire.class.getClass())
			{
	    		SimplePortrayal2D pp = new OvalPortrayal2D(){
	    			private static final long serialVersionUID = 1L;
	    			@Override
	    			public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    			{
	    				DVampire bat = (DVampire)object;
	    				int redComponent = bat.isCheater() ? 255 : 0;
	    				// Bat is painted red if cheater, blue otherwise
	    				paint = new Color(redComponent, 0, 255 - redComponent);
	    				super.draw(object, graphics, info);
	    			}
	    		};
	    		world.p.setPortrayalForObject(o, pp);
	    		return true;
			}
			else if (objClass == DVampire.class.getClass())
			{
				SimplePortrayal2D pp = new HexagonalPortrayal2D(){
					private static final long serialVersionUID = 1L;
					@Override
					public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
					{
						DRoost DRoost = (DRoost)object;
						int redComponent = DRoost.getNumCheaters() > 0 ? 120 : 0;
						// DRoost is painted red if cheater, green otherwise
						scale = 5.0;
						paint = new Color(redComponent, 120 - redComponent, 0);
						super.draw(object, graphics, info);
					}
				};
				world.p.setPortrayalForObject(o, pp);
				return true;
			}
    		return false;
    	}
    	return false;
	}
	
}
