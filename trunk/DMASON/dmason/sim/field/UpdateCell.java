package dmason.sim.field;


import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import dmason.sim.loadbalancing.MyCellIntegerField;

/**
 * A hash map supporting concurrency for methods put and get.
 * The DistributedRegions are saved using the number of step as key.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class UpdateCell<E,F> extends HashMap<Long,PriorityQueue<MyCellIntegerField>> implements Serializable
{
    
	private static final long serialVersionUID = 1L;
	private final ReentrantLock lock;
    private final Condition block;
    
    public UpdateCell()
    {
    	lock=new ReentrantLock();
    	block=lock.newCondition();
    }
    
    /**
     * This method returns an ArrayList of updates at the same step.
     * It's blocking until the number of updates is the same of the number of requested updates.
     * @param step The number of step which we want the updates
     * @param num_updates The number of updates
     * @return an ArrayList with DistributedRegion at the same step
     * @throws InterruptedException 
     */
	public  PriorityQueue<MyCellIntegerField> getUpdates(long step,int num_updates) throws InterruptedException
	{
		lock.lock();
		PriorityQueue<MyCellIntegerField> tmp=this.get(step);

		while(tmp==null)
		{
			block.await();
			tmp=this.get(step);
		}
		
		while(tmp.size()!=num_updates)
		{
			block.await();
		}
		this.remove(step);
		lock.unlock();
		return tmp;
	}
	
	/**
	 * This method inserts the updates into the ArrayList indexed using the step as key, 
	 * then sends a signal to all waiting objects.
	 * @param step The number of step
	 * @param d_reg The DistributedRegion to be inserted
	 */
	public void put(long step,MyCellIntegerField d_reg)
	{		
		lock.lock();
		
		PriorityQueue<MyCellIntegerField> tmp=this.get(step);
		if(tmp!=null)
		{
			tmp.add(d_reg);
		//	System.out.println("Accesso in scrittura coda > 1");
		}
		else
		{
			/*initial capacity of the Priority Queue is set to eight because it is the number of
			 neighbors in a 2D field*/
			PriorityQueue<MyCellIntegerField> queue_update=new PriorityQueue<MyCellIntegerField>(8,
					new MyDivisionComparator());
			
			queue_update.add(d_reg);

			super.put(step,queue_update); 
		}
		
		block.signal();	
		lock.unlock();
	}
	
	/**
	 * Inner class used to share the updates ever in the same way
	 */
	class MyDivisionComparator implements Comparator
	{		   
	    public int compare(Object d1, Object d2)
	    {	   	       
	    	int dd1 = ((MyCellIntegerField)d1).getPosition();
	    	int dd2 = ((MyCellIntegerField)d2).getPosition();  
	       
	    	if(dd1 > dd2)
	    		return 1;
	    	else if(dd1 < dd2)
	    		return -1;
	    	else
	    		return 0;	 
	    
	    }
	}
}