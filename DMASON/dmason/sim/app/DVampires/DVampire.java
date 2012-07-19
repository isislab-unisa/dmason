package dmason.sim.app.DVampires;

import java.io.Serializable;

/**
 * A Java Bean representing a vampire bat.
 * @author Luca Vicidomini
 */
public class DVampire implements Serializable
{
	String name;
	public String toString() { return name + "[" + String.format("%02d", getHealth()) + "]"; }
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The minimum age for the bat to breed. <b>Important note:</b> the bat
	 * will first breed at the first <code>BREEDING_INTERVAL</code>
	 * <b>after</b> (i.e. not including) <code>BREEDING_MIN_AGE</code>. 
	 */
	public static final int BREEDING_MIN_AGE = 300;
	
	/**
	 * Every how many days the bat can breed.
	 */
	public static final int BREEDING_INTERVAL = 300;
	
	/**
	 * Change for the bat to breed. A value of 0.5 (= 50%) can be used to
	 * compensate for the lack of genders.
	 */
	public static final double BREEDING_RATE = 0.5;
	
	/**
	 * Amount of health a recipient receives when some bat shares food with it.
	 */
	public static final int FOODSHARING_GAIN = 18;
	
	/**
	 * Amount of health a donor loses when shares food with some other bat.
	 */
	public static final int FOODSHARING_LOSE = 6;
	
	/**
	 * Hunting success rate.
	 */
	public static final double HUNTING_SUCCESS_RATE = 0.93;
	
	/**
	 * Life expectancy (in days). When a bat reaches the age
	 * <code>MAX_AGE</code>, it dies. 
	 */
	public static final int MAX_AGE = 3600;
	
	/**
	 * Max health. The amount of health replenished after a successful hunt.
	 */
	public static final int MAX_HEALTH = 60;
	
	/**
	 * Bat's age (in days).
	 */
	private int age;
	public int getAge() { return age; }
	public void setAge(int age) { this.age = age; }
	
	/**
	 * True if bat is a cheater (that is, it never shares food)
	 */
	private boolean cheater;
	public boolean isCheater() { return cheater; }
	
	/**
	 * True if the vampire already groomed this day.
	 */
	private boolean groomed;
	public boolean isGroomed() { return groomed; }
	public void setGroomed(boolean groomed) { this.groomed = groomed; }
	
	/**
	 * Bat's health.
	 */
	private int health;
	public int getHealth() { return health; }
	public void setHealth(int health) { this.health = health; }
	
	/**
	 * Number of times this bat gave help to others.
	 */
	private int helpsGiven;
	public int getHelpsGiven() { return helpsGiven; }
	public void setHelpsGiven(int helpsGiven) { this.helpsGiven = helpsGiven; }
	
	/**
	 * Number of times this bat received help from others.
	 */
	private int helpsReceived;
	public int getHelpsReceived() { return helpsReceived; }
	public void setHelpsReceived(int helpsReceived) { this.helpsReceived = helpsReceived; }
	
	/**
	 * Bat's roost.
	 */
	private DRoost roost;
	public void setRoost(DRoost roost) { this.roost = roost; }
	public DRoost getRoost() { return roost; }
	
	/**
	 * Create a new bat with given age. This is meant to be called both
	 * when setting-up the simulation and when a vampire gives
	 * birth to a child.
	 * @param roost The roost this bat will live in.
	 * @param age Bat's age (in days) when simulation starts.
	 * @param cheater True if this bat is a cheater.
	 * @param name Bat's name (used by <code>toString</code>)
	 */
	public DVampire(DRoost roost, int age, boolean cheater, String name)
	{
		this.setRoost(roost);
		this.age = age;
		this.cheater = cheater;
		setHealth(36);
		this.name = name;
	}
	
	/**
	 * Check if the vampire is dead (by starvation or age).
	 * @return true if the vampire is starved or too old.
	 */
	protected boolean isDead()
	{
		return (getHealth() <= 0 || age > DVampire.MAX_AGE);
	}
}
