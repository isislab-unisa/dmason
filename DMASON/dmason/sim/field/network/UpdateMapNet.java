package dmason.sim.field.network;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import dmason.sim.field.DistributedRegionInterface;

public class UpdateMapNet<E> extends  HashMap<Long,PriorityQueue<Object>> implements Serializable {
	
	 private final ReentrantLock lock;
	 private final Condition block;
	    
	    public UpdateMapNet()
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
		public  PriorityQueue<Object> getUpdatesNet(long step,int num_updates) throws InterruptedException
		{
			lock.lock();
			if (num_updates==0){
				lock.unlock();
				return new PriorityQueue<Object>();
			}
			PriorityQueue<Object> tmp=this.get(step);
			//System.out.println("-------------PRIORITY step: "+step);
			while(tmp==null)
			{
				//System.out.println("-------------PRIORITY");
				block.await();
				tmp=this.get(step);
			}
			//System.out.println("tmp.size: "+tmp.size()+" num_updates: "+num_updates);
			while(tmp.size()!=num_updates)
			{
				//System.out.println("dentro il while");
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
		public void put(long step,Object d_reg)
		{		
			lock.lock();
			//System.out.println("-------------PRIORITY d_reg: "+d_reg.toString());
			PriorityQueue<Object> tmp=this.get(step);
			if(tmp!=null)
			{
				tmp.add(d_reg);
			//	System.out.println("-------------PRIORITY Accesso in scrittura coda > 1");
			}
			else
			{
				/*initial capacity of the Priority Queue is set to eight because it is the number of
				 neighbors in a 2D field*/
				PriorityQueue<Object> queue_update=new PriorityQueue<Object>(10,new DistributedRegionComparator());
				
				queue_update.add(d_reg);

				super.put(step,queue_update); 
			}
			
			block.signal();	
			lock.unlock();
		}
		
		/**
		 * Inner class used to share the updates ever in the same way
		 */
		class DistributedRegionComparator implements Comparator
		{		   
		    public int compare(Object d1, Object d2)
		    {	   	       
		    	long dd1 = ((DistributedRegionInterface)d1).getStep();
		    	long dd2 = ((DistributedRegionInterface)d2).getStep();  
		       
		    	if(dd1 > dd2)
		    		return 1;
		    	else if(dd1 < dd2)
		    		return -1;
		    	else
		    		return 0;	 
		    
		    }
		}

}
