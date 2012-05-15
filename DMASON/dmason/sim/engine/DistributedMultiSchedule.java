package dmason.sim.engine;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import dmason.sim.field.DistributedField;
import dmason.util.visualization.ViewerMonitor;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;


/**
 * The Distributed Schedule for Distributed Mason with multiple fields
 * It's necessary for the synchronization of multiply environment 
 * for each step.
 * @param <E> the type of coordinates
 */
public class DistributedMultiSchedule<E> extends Schedule
{
	private Logger logger = Logger.getLogger(DistributedMultiSchedule.class.getCanonicalName());
	
	public ArrayList<DistributedField<E>> fields;
	Steppable zombie = null;
	
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition block = lock.newCondition();
    
    /**
     * Count how many viewers are active on this schedule. Using this
     * subclass allows to increment/decrement the counter atomically.
     */
    public class CounterViewer
    {
		private int count = 0;
		public synchronized void increment() { count++;	}
		public synchronized void decrement(){ count--; }
		public synchronized int getCount(){	return count; }
	}
    
    /**
     * Number of the viewers active on this schedules.
     */
	public CounterViewer numViewers = new CounterViewer();
	
	public boolean isEnableZoomView = false;
	
	public ViewerMonitor monitor = new ViewerMonitor();
	
	/**
	 * Counts the number of threads that have done synchronizing.
	 */
    private int n = 0;
    
    /**
     * Every region in this worker, after synchronization, will store a
     * boolean in this ArrayList stating if the synchronization itself
     * was successful or not.
     */
    private ArrayList<Boolean> synchResults = new ArrayList<Boolean>(); 
	
	public DistributedMultiSchedule() {
		
		fields = new ArrayList<DistributedField<E>>();
	}
	
	/**
	 * Steps the schedule for each field, gathering and ordering all the items to step on the next time step (skipping
	 * blank time steps), and then stepping all of them in the decided order.
	 * Returns FALSE if nothing was stepped -- the schedule is exhausted or time has run out.
	 */
	public synchronized boolean step(final SimState state)
    {
		// If not already present, adds a "zombie" agent to the schedule
		// in order to prevent stopping the simulation.
		if (zombie == null)
		{ 
			zombie = new Steppable()
			        	{
							static final long serialVersionUID = 6330208166095250478L;
							public void step(SimState state) { /* do nothing*/ }
			        	};
			this.scheduleRepeating(zombie);
		}
		
		synchronized (monitor)
		{
			if (monitor.isZoom)
				monitor.ZOOM = true;
			else
				monitor.ZOOM = false;
		}
		
		// Execute the simulation step
		super.step(state);
	
		// Create a thread for each field assigned to this worker, in order
		// to do synchronization
		for(DistributedField<E> f : fields)
		{
			MyThread<E> t = new MyThread(f, this);
			t.start();
		}
		
		// Waits for every synchronization thread.
		// Note: synchronization threads will update the synchResults array
		//       as well as the n variable.
		lock.lock();
		while(n < fields.size()){
			try
			{
				block.await(); // Will be signaled by a thread
			} catch (InterruptedException e) {
				logger.severe("Error during block.await()");
				e.printStackTrace();
			}
		}
		n = 0;
		lock.unlock();
	
		// Check if fields did synchronize successfully
		for (Boolean b : synchResults)
		{
			if (b == false)
			{
				return false;
			}
		}	
		
		// If there is an active zoom synchronous monitor, wait for it 
		if(monitor.ZOOM && monitor.isSynchro)
		{
			Long currentStep = this.getSteps() - 1;
			try
			{
				monitor.awaitForAckStep(currentStep);
			} catch (InterruptedException e) {
				logger.severe("Error on monitor.awaitForAckStep(" + currentStep + ")");
				e.printStackTrace();
			}
		}
		
		// Done
		return true;
    }
	
	/**
	 * This subclass is in charge of synchronizing a single region.
	 * @param <E> the type of coordinates
	 */
	class MyThread<E> extends Thread
	{ 
		DistributedField<E> field;
		DistributedMultiSchedule<E> schedule;

		public MyThread(DistributedField<E> f, DistributedMultiSchedule<E> s)
		{
			field = f;
			schedule = s;
		} 
		
		public void run()
		{ 
			// Synchronize the field, then report to the 
			// DistributedMultiSchedule if the operation was successful or not
			schedule.statusSyn(field.synchro());
		} 
	} 
	
	/**
	 * Stores result of a field synchronization's result. This method is meant
	 * to be be called by an inner thread <code>MyThread</code> in charge of
	 * executing field synchronization.
	 * @param b <code>true</code> if the synchronization was successful, <code>false</code> otherwise.
	 */
	public void statusSyn(boolean b)
	{
		lock.lock();
			n++;	             // Increase number of threads that did synchronize
			synchResults.add(b); // Update the array of synchronization results
			block.signal();      // Signal DistributedMultiSchedule
		lock.unlock();
	}
	
	// Getters and setters
	public ArrayList<DistributedField<E>> getFields() { return fields; }
	public void setFields(ArrayList<DistributedField<E>> fields) { this.fields = fields; }
	public void addField(DistributedField<E> f) { fields.add(f); }	
}
