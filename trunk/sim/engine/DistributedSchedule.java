package dmason.sim.engine;

import dmason.sim.field.DistributedField;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * The Distributed Schedule for Distributed Mason
 * It's necessary for the synchronization of the entire environment 
 * for each step.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class DistributedSchedule<E> extends Schedule 
{
	DistributedField<E> field;
	Steppable zombie=null;
	public DistributedSchedule() {}
	 
	/**
	 * Steps the schedule, gathering and ordering all the items to step on the next time step (skipping
	 * blank time steps), and then stepping all of them in the decided order.
	 * Returns FALSE if nothing was stepped -- the schedule is exhausted or time has run out.
	 */
	public synchronized boolean step(final SimState state)
    {	
		if(zombie==null)
		{ 
			zombie = new Steppable()
        	{
				public void step(SimState state) { /* do nothing*/ }
				
				static final long serialVersionUID = 6330208166095250478L;
        	};
          this.scheduleRepeating(zombie);
		}
		boolean a= super.step(state) ;
		
		boolean b=field.synchro();
		
		return a && b;
    }

	//getters and setters
	public DistributedField<E> getField() { return field; }
	public void setField(DistributedField<E> field) { this.field = field;}
}