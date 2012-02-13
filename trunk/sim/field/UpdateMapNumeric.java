package dmason.sim.field;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A hash map supporting concurrency for methods put and get.
 * The DistributedRegions are saved using the number of step as key.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class UpdateMapNumeric<E,F> extends HashMap<Long,PriorityQueue<DistributedRegionNumeric<E,F>>> implements Serializable {

    private final ReentrantLock lock;
    private final Condition block;
    
    public UpdateMapNumeric()
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
	public  PriorityQueue<DistributedRegionNumeric<E,F>> getUpdates(long step,int num_updates) throws InterruptedException
	{
		lock.lock();
		PriorityQueue<DistributedRegionNumeric<E,F>> tmp=this.get(step);
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
	public void put(long step,DistributedRegionNumeric<E,F> d_reg)
	{		
		lock.lock();
		PriorityQueue<DistributedRegionNumeric<E,F>> tmp=this.get(step);
		if(tmp!=null)
		{
			tmp.add(d_reg);	
		}
		else
		{
			/*initial capacity of the Priority Queue is set to eight because it is the number of
			 neighbors in a 2D field*/
			PriorityQueue<DistributedRegionNumeric<E,F>> queue_update=new PriorityQueue<DistributedRegionNumeric<E,F>>(8, 
					new DistributedRegionNumericComparator());
			
			queue_update.add(d_reg);
			this.put(step,queue_update);
		}
		
		block.signal();
		lock.unlock();
	}
	
	/**
	 * Inner class used to share the updates ever in the same way
	 */
	class DistributedRegionNumericComparator implements Comparator
	{		   
	    public int compare(Object d1, Object d2)
	    {	   	       
	        int dd1 = ((DistributedRegionNumeric<E,F>)d1).POSITION;
	        int dd2 = ((DistributedRegionNumeric<E,F>)d2).POSITION;  
	       
	        if(dd1 > dd2)
	            return 1;
	        else if(dd1 < dd2)
	            return -1;
	        else
	            return 0;	    	
	    }
	}
}
