package dmason.sim.app.DVampires;

import dmason.sim.app.DFlockers.RemoteFlock;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.Orientable2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * Represent a roost as a simulation agent.
 * @author Luca Vicidomini
 */
public class DRoost extends RemoteRoost<Double2D>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Max number of bats this roost can reach before splitting.
	 */
	protected int launchSize;
	public int getLaunchSize() { return launchSize; }

	/**
	 * Bats living in this roost.
	 */
	Bag bats = new Bag();
	public int getNumBats() { return bats.size(); }
	
	/**
	 * Number of cheaters in this roost.
	 */
	protected int numCheaters;
	public int getNumCheaters() { return numCheaters; }
	
	/**
	 * Roost's name, used by <code>toString</code> method.
	 */
	String name;
	
	/**
	 * Reference to current simulation step.
	 */
	protected DSimulation simulation;
	
	/**
	 * Reference to this agent scheduled object.
	 */
	protected Stoppable stoppable;
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public DRoost(DSimulation simulation, int launchSize, String name)
	{
		this.simulation = simulation;
		this.launchSize = launchSize;
		this.name = name;
	}
	
	/**
	 * Add a vampire to this roost.
	 * @param vampire The vampire to add.
	 */
	public void add(DVampire vampire)
	{
		this.bats.add(vampire);
		vampire.setRoost(this);
		//simulation.place(vampire);
		if (vampire.isCheater())
		{
			numCheaters++;
			if (numCheaters == 1)
				simulation.changeCheatersRoosts(1);
		}
	}
	
	/**
	 * Remove a vampire from this roost.
	 * @param vampire The vampire to remove.
	 */
	public void remove(DVampire vampire)
	{
		this.bats.remove(vampire);
		if (vampire.isCheater())
		{
			numCheaters--;
			if (numCheaters == 0)
				simulation.changeCheatersRoosts(-1);
		}
	}
	
	/**
	 * Split this roost into two roosts when launchSize is reached.
	 * Bats in this roost will be split in two halves: the younger
	 * half will migrate to a new roost, the older half will stay here.
	 * @return The created roosts, containing younger vampires.
	 */
	protected DRoost split()
	{
		// Calculate median age in this roost
		int size = bats.size();
		int[] ages = new int[size];
		for (int i = 0; i < size; i++)
			ages[i] = ((DVampire)bats.get(i)).getAge();
		java.util.Arrays.sort(ages);
		int medianAge = ages[(size + 1) / 2];
			
		DRoost newRoost = simulation.roostSplit(this);

		// Move younger bats to to new roost
		for (int i = size - 1; i >= 0; i--)
		{
			DVampire vampire = (DVampire)bats.get(i);
			if (vampire.getAge() < medianAge)
			{
				remove(vampire);
				newRoost.add(vampire);
			}
		}
		
		return newRoost;
	}

	/**
	 * Do a simulation step.
	 */
	@Override
	public void step(SimState state)
	{
		simulation = (DSimulation)state;

		Bag scan;
		int size;
		
		//-------------------------------------------------------------------
		
		// This is the "night" phase of the day.
		scan = new Bag(bats);
		size = scan.size();
		for (int i = 0; i < size; i++)
		{
			DVampire vampire = (DVampire)scan.get(i);
			vampireNight(vampire);
		}
		
		//-------------------------------------------------------------------
		
		// This is the "day" phase of the day
		// Select grooming couples and allow vampires helping each others
		doGrooming();
		
		//-------------------------------------------------------------------

		// After the day is spent, but before a new night begins, check
		// vampire health and age, and allow reproduction
		scan = new Bag(bats);
		size = scan.size();
		for (int i = 0; i < size; i++)
		{
			DVampire vampire = (DVampire)scan.get(i);
			vampireDusk(vampire);
		}
		
		//-------------------------------------------------------------------
		
		// Before beginning a new day, check if this roost
		// is dead or will split
		if (bats.size() == 0)
			destroy();
		else if (simulation.getMultipleRoosts() && bats.size() > launchSize)
			split();
		
		//-DMASON------------------------------------------------------------
		simulation.world.setDistributedObjectLocation(this.getPos(), this, state);
	}

	/**
	 * A subroutine for <code>step</code>. Called at the end of the day,
	 * check if the vampire is dead, then increase its age.
	 */
	private void vampireDusk(DVampire vampire)
	{
		vampire.setHealth(vampire.getHealth() - 12);
		if (vampire.isDead())
		{
			if (vampire.getHealth() <= 0)
				simulation.addStarved();
			remove(vampire);
			simulation.vampireDied(vampire);
		}
		vampire.setAge(vampire.getAge() + 1);
		vampireReproduce(vampire);
	}
	
	/**
	 * A subroutine for <code>step</code>. Called at end of the day,
	 * check if the vampire can reproduce.
	 */
	private void vampireReproduce(DVampire vampire)
	{
		if ( (vampire.getAge() % DVampire.BREEDING_INTERVAL == 0)						
				&& (vampire.getAge() > DVampire.BREEDING_MIN_AGE)
				&& (simulation.canBreed())
				&& (simulation.random.nextDouble() < DVampire.BREEDING_RATE) )
			simulation.vampireBred(vampire);
	}

	/**
	 * A subroutine for <code>step</code>. Select random bat couples
	 * for grooming/helping.
	 */
	private void doGrooming()
	{
		Bag shuffled = new Bag(bats);
		shuffled.shuffle(simulation.random);
	
		// Until there aren't more couples to groom
		while (shuffled.size() > 1)
		{
			DVampire one = null;
			DVampire two = null;
			
			// Select a random couple
			do {
				one = (DVampire)shuffled.remove(0);
			} while (one.isGroomed() && shuffled.size() > 0);
			
			do {
				two = (DVampire)shuffled.remove(0);
			} while (two.isGroomed() && shuffled.size() > 0);
			
			if (one != null)
				one.setGroomed(true);
			
			if (two != null) // Also one != null
			{
				two.setGroomed(true);
				
				if (one.getHealth() <= 12)
					vampireHelp(two, one);
				else if (two.getHealth() <= 12)
					vampireHelp(one, two);
			}
		}
	}

	/**
	 * A subroutine for <code>step</code>. Called at night, check if
	 * the bat is dead.
	 */
	private void vampireNight(DVampire vampire)
	{
		vampire.setGroomed(false);
		vampire.setHealth(vampire.getHealth() - 12);
		vampireHunt(vampire);
		if (vampire.isDead())
		{
			if (vampire.getHealth() <= 0)
				simulation.addStarved();
			remove(vampire);
			simulation.vampireDied(vampire);
		}
	}
	
	/**
	 * A subroutine for <code>step</code>. Called at night, allows a
	 * bat to hunt.
	 */
	public void vampireHunt(DVampire vampire)
	{
		if (simulation.random.nextDouble() < DVampire.HUNTING_SUCCESS_RATE)
			vampire.setHealth(DVampire.MAX_HEALTH);
	}
	
	/**
	 * A subroutine for <code>step</code>. Called during the grooming phase,
	 * ask the <code>donor</code> for donating food to the <code>recipient</code>.
	 * The donor my accept or refuse to help.
	 */
	public void vampireHelp(DVampire donor, DVampire recipient)
	{
		if (donor.isCheater() || donor.getHealth() <= 48)
		{
			// Deny help
			simulation.addRefusals();
		}
		else
		{
			// Give help
			donor.setHealth(donor.getHealth() - DVampire.FOODSHARING_LOSE);
			donor.setHelpsGiven(donor.getHelpsGiven() + 1);
			recipient.setHealth(recipient.getHealth() + DVampire.FOODSHARING_GAIN);
			recipient.setHelpsReceived(recipient.getHelpsReceived() + 1);
			simulation.addDonation();
		}
	}
	
	/**
	 * Meant to be called by <code>Simulation</code> after this agent has
	 * been scheduled.
	 * @param stoppable A stoppable object returned by Schedule.scheduleRepeat()
	 */
	public void setStoppable(Stoppable stoppable)
	{
		this.stoppable = stoppable;
	}
	
	/**
	 * Remove this roost from simulation.
	 */
	public void destroy()
	{
		//stoppable.stop();
		simulation.roostDestroy(this);
	}

}
