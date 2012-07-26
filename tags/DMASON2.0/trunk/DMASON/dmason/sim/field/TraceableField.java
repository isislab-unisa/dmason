package dmason.sim.field;

/**
 * This interface is meant to be implemented by distributed fields
 * that allows tracing simulation's statistics in a
 * distributed way.
 * 
 * @author Luca Vicidomini
 *
 */

public interface TraceableField
{
	/**
	 * Enables tracing. If the simulation instance offers methods such as
	 * "getCohesion", then a valid parameter name will be "Cohesion".
	 * @param param The parameter we want to start tracing
	 */
	public void trace(String param);
	
	/**
	 * Stops tracing.
	 * @param param The parameter we want to stop tracing
	 */
	public void untrace(String param);
}
