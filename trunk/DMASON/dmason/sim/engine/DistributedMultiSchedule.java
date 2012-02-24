package dmason.sim.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


import dmason.sim.field.DistributedField;
import dmason.util.visualization.ViewerMonitor;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sun.tools.tree.ThisExpression;


/**
 * The Distributed Schedule for Distributed Mason with multiple fields
 * It's necessary for the synchronization of multiply environment 
 * for each step.
 * @param <E>
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class DistributedMultiSchedule<E> extends Schedule  {

	public ArrayList<DistributedField<E>> fields;
	Steppable zombie=null;
	
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition block = lock.newCondition();
    
    public class CounterViewer{
		private int count=0;
		public synchronized void increment(){
			count++;
		}
		public synchronized void decrement(){
			count--;
		}
		public synchronized int getCount(){
			return count;
		}
	}
	public CounterViewer NUMVIEWER=new CounterViewer();
	public boolean isEnableZoomView=false;
	public ViewerMonitor monitor=new ViewerMonitor();
	
    private int n = 0;
    
    private ArrayList<Boolean> bo = new ArrayList<Boolean>(); 
	
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
		
		if(zombie==null)
		{ 
			zombie = new Steppable()
        	{
				public void step(SimState state) { /* do nothing*/ }
				
				static final long serialVersionUID = 6330208166095250478L;
        	};
          this.scheduleRepeating(zombie);
          
		}
		boolean ZOOMforSTEP;
		synchronized (monitor) {
			
			if(monitor.isZoom)
				monitor.ZOOM=true;
			else
				monitor.ZOOM=false;
		}
		if(monitor.ZOOM)
			System.out.println("Vado in ZOOM per step "+this.getSteps());
		
		boolean a= super.step(state) ;
	
		int hh=0;
		for(DistributedField<E> f : fields) {
		
			MyThread<E> t = new MyThread(f,this);
			t.start();
		}
		lock.lock();
		while(n<fields.size()){

			try {
				
				block.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		n=0;
		lock.unlock();
	
		for (Boolean b : bo) {
			
			if(b == false){
				
				return false;
			}
		}	
		
		if(monitor.ZOOM)
		{
			System.out.println("Chiedo ack per step "+(this.getSteps()-1));
			Long actual_step=this.getSteps()-1;
			try {
				monitor.awaitForAckStep(actual_step);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return true;
		
    }
	
	class MyThread<E> extends Thread { 
		
		DistributedField<E> field;
		DistributedMultiSchedule<E> schedule;

		public MyThread(DistributedField<E> f, DistributedMultiSchedule<E> s){ 
			schedule = s;
			field = f;
		} 
		
		public void run(){ 
		
			schedule.statusSyn(field.synchro());
		} 
	} 
	
	public void statusSyn(boolean b){
		
		lock.lock();
			n++;
			bo.add(b);
			block.signal();
		lock.unlock();
	
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
