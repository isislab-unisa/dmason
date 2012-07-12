package dmason.sim.engine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.field.DistributedField;
import dmason.sim.field.UpdatePositionInterface;
import dmason.sim.loadbalancing.LoadBalancingInterface;
import dmason.sim.loadbalancing.MyCellInterface;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger(DistributedMultiSchedule.class.getCanonicalName());
	
	public ArrayList<DistributedField<E>> fields;
	Steppable zombie = null;
	
	private HashMap<String, String> peers;
	private boolean split;
	private boolean merge;
	private int numAgents;
	public int externalAgents;
	private int numExt;
	private DistributedState state;
	private HashMap<String, ArrayList<MyCellInterface>> h;



	
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
	
    
 // codice profiling
 	long startStep;
 	long endStep;
 	long numStep;
 	long time;
 	//double sleepTime = 0.002;
 	FileOutputStream file;
 	PrintStream ps;
 	FileOutputStream file2;
 	PrintStream ps2;
 	// fine codice profiling
    
	public DistributedMultiSchedule() {
		
		fields = new ArrayList<DistributedField<E>>();
		peers = new HashMap<String, String>();
		split = false;
		merge = false;
		numAgents = 0;
		externalAgents = 0;
		numExt = 0;
		
	
		// codice profiling
				startStep = 0;
				endStep = 0;
				file = null;
				file2 = null;
				ps = null;
				ps2 = null;
				numStep = 0;
				time = 0;
				// fine codice profiling
	}
	
	/**
	 * Steps the schedule for each field, gathering and ordering all the items to step on the next time step (skipping
	 * blank time steps), and then stepping all of them in the decided order.
	 * Returns FALSE if nothing was stepped -- the schedule is exhausted or time has run out.
	 */
	public synchronized boolean step(final SimState simstate)
    {
		state = (DistributedState)simstate;
		
		//load peers list
		if(getSteps()==0){
			int numP = (int) Math.sqrt(state.NUMPEERS);
			int z = 0;
			for (int i = 0; i < numP; i++) {
				for (int j = 0; j < numP; j++) {
					peers.put(""+z,i+"-"+j);
					z=z+3;
				}
			}
		}
		
		// codice profiling
				if(getSteps()==0){
					
					try {
						file=new FileOutputStream("Region"+((DistributedState)state).TYPE.toString()+".txt",true);
						file2=new FileOutputStream("Balance"+((DistributedState)state).TYPE.toString()+".txt",true);
					} catch (FileNotFoundException e) {
						
						e.printStackTrace();
					}
					
					ps=new PrintStream(file);
					ps2=new PrintStream(file2);
				}
				
				numStep = getSteps();
				// fine codice profiling
		
		
				
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
		
		// codice profiling
		numAgents = 0;
		numAgents = super.counter;
		startStep = System.currentTimeMillis();
			/**
			//ants fattened
			time = 0;
			time = (long)(numAgents*sleepTime);
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		// fine codice profiling
	
		//if(state.TYPE.toString().equals("2-2"))
    		//System.out.println(state.TYPE+") step: "+(numStep)+" numAgents: "+(numAgents));
		
		
		
		// Execute the simulation step
		super.step(state);
		
		verifyBalance();
	
		
		// codice profiling
    	endStep = System.currentTimeMillis();
    	if(getSteps()<40002){
	   		ps.println((numStep)+";"+(numAgents)+";"+(startStep)+";"+(endStep));
	   		ps.flush();
    	}
		// fine codice profiling
		
		
		// Create a thread for each field assigned to this worker, in order
		// to do synchronization
		for(DistributedField<E> f : fields)
		{
			MyThread t = new MyThread(f, this);
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
	
	public void manageBalance(HashMap<Integer,UpdatePositionInterface> hashUpdatesPosition, 
			DistributedField field, CellType cellType,LoadBalancingInterface balance) {
		if(getSteps()>state.NUMPEERS)
		{
			HashMap<CellType, MyCellInterface> h =field.getToSendForBalance();
			
			if(state.TYPE.toString().equals(peers.get((getSteps()%(3*state.NUMPEERS))+"")) && !field.isSplitted()
					&& split)
			{
				field.prepareForBalance(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreBalance(true);			
				hashUpdatesPosition.get(MyCellInterface.UP).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.DOWN).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreBalance(true);
				hashUpdatesPosition.get(MyCellInterface.LEFT).setPreBalance(true);
				
				ps2.println("Split: "+(numStep)+";"+(numAgents));
				//ps2.flush();

			}
			else
				if(state.TYPE.toString().equals(peers.get(((getSteps()%(3*state.NUMPEERS))-1)+"")) && !field.isSplitted()
						&& field.isPrepareForBalance())
				{
					field.setIsSplitted(true);		
					field.prepareForBalance(false);
					MyCellInterface m0 = h.get(MyCellInterface.CORNER_DIAG_UP_LEFT);
					m0.setPosition(balance.calculatePositionForBalance(m0.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setMyCell(m0);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreBalance(true);
					
					MyCellInterface m1 = h.get(MyCellInterface.UP);
					m1.setPosition(balance.calculatePositionForBalance(m1.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.UP).setMyCell(m1);
					hashUpdatesPosition.get(MyCellInterface.UP).setPreBalance(true);
					
					MyCellInterface m2 = h.get(MyCellInterface.CORNER_DIAG_UP_RIGHT);
					m2.setPosition(balance.calculatePositionForBalance(m2.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setMyCell(m2);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreBalance(true);
					
					MyCellInterface m3 = h.get(MyCellInterface.RIGHT);
					m3.setPosition(balance.calculatePositionForBalance(m3.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.RIGHT).setMyCell(m3);
					hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreBalance(true);
					
					MyCellInterface m4 = h.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT);
					m4.setPosition(balance.calculatePositionForBalance(m4.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setMyCell(m4);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreBalance(true);
					
					MyCellInterface m5 = h.get(MyCellInterface.DOWN);
					m5.setPosition(balance.calculatePositionForBalance(m5.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.DOWN).setMyCell(m5);
					hashUpdatesPosition.get(MyCellInterface.DOWN).setPreBalance(true);
					
					MyCellInterface m6 = h.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT);
					m6.setPosition(balance.calculatePositionForBalance(m6.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setMyCell(m6);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreBalance(true);
		
					MyCellInterface m7 = h.get(MyCellInterface.LEFT);
					m7.setPosition(balance.calculatePositionForBalance(m7.getPosition()));
					hashUpdatesPosition.get(MyCellInterface.LEFT).setMyCell(m7);
					hashUpdatesPosition.get(MyCellInterface.LEFT).setPreBalance(true);
				}
				else
				{
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreBalance(false);			
					hashUpdatesPosition.get(MyCellInterface.UP).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.DOWN).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreBalance(false);
					hashUpdatesPosition.get(MyCellInterface.LEFT).setPreBalance(false);
				}
			
		}
	}
	
	public void manageMerge(HashMap<Integer,UpdatePositionInterface> hashUpdatesPosition, 
			DistributedField field, CellType cellType) {
		if(getSteps()>state.NUMPEERS)
		{
			if(state.TYPE.toString().equals(peers.get((getSteps()%(3*state.NUMPEERS))+"")) && !field.isUnited() 
					&& merge)
			{
				field.prepareForUnion(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreUnion(true);			
				hashUpdatesPosition.get(MyCellInterface.UP).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.DOWN).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreUnion(true);
				hashUpdatesPosition.get(MyCellInterface.LEFT).setPreUnion(true);
				
				ps2.println("Merge: "+(numStep)+";"+(numExt));
				//ps2.flush();
				numExt = 0;
				
			}
			else
				if(state.TYPE.toString().equals(peers.get(((getSteps()%(3*state.NUMPEERS))-1)+"")) && !field.isUnited()
						&& !field.isSplitted())
				{
					field.prepareForUnion(true);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreUnion(false);			
					hashUpdatesPosition.get(MyCellInterface.UP).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.DOWN).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.LEFT).setPreUnion(false);
				}
				else
				{
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_LEFT).setPreUnion(false);			
					hashUpdatesPosition.get(MyCellInterface.UP).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_UP_RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_RIGHT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.DOWN).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.CORNER_DIAG_DOWN_LEFT).setPreUnion(false);
					hashUpdatesPosition.get(MyCellInterface.LEFT).setPreUnion(false);
				}
			
			HashMap<Integer, MyCellInterface> cellToSend = field.getToSendForUnion();
			for(Integer s : cellToSend.keySet())
			{
				if(cellToSend.get(s)!=null)
				{
					MyCellInterface mc = cellToSend.get(s);
	
					hashUpdatesPosition.get(s).setUnion(true);
					hashUpdatesPosition.get(s).setMyCell(mc);
				}
			}
			for (Integer s : cellToSend.keySet()) {
				cellToSend.put(s, null);
			}
		}
	}
	
	private void verifyBalance(){

		double average = state.NUMAGENTS/state.NUMPEERS;
		double thresholdSplit=3*average;
		double thresholdMerge=1.5*average;

		if(numAgents>thresholdSplit)
		{
			split = true;
			merge = false;
		}
		else 
			if(((numAgents+externalAgents)<thresholdMerge) && 
					(state.TYPE.toString().equals(peers.get((getSteps()%(3*state.NUMPEERS))+""))))
			{
				merge = true;
				split = false;
				numExt = numAgents+externalAgents;
			}
		externalAgents = 0;
	}
}
