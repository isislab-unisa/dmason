package dmason.sim.engine;

import java.util.ArrayList;

import dmason.sim.field.DistributedField;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * The Distributed Schedule for Distributed Mason
 * It's necessary for the synchronization of multiply environment 
 * for each step.
 * @param <E>
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class DistributedScheduleMulti<E> extends Schedule  {

	ArrayList<DistributedField<E>> fields;
	Steppable zombie=null;
	
	public DistributedScheduleMulti() {
		fields = new ArrayList<DistributedField<E>>();
	}
	
	/**
	 * Steps the schedule for each field, gathering and ordering all the items to step on the next time step (skipping
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
		boolean b=true;
		
		for(DistributedField<E> f : fields) {
			
			if(!f.synchro()){
				
				b=false;
			}
			
		}
		
		return a && b;
    }
	
	//getters and setters
	public ArrayList<DistributedField<E>> getFields() {
		return fields;
	}
	public void setFields(ArrayList<DistributedField<E>> fields) {
		this.fields = fields;
	}
	public void addField(DistributedField<E> f){
		
		fields.add(f);
	}
}
