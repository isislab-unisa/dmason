/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field;


import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.support.loadbalancing.MyCellIntegerField;

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
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
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
	public  PriorityQueue<MyCellIntegerField> getUpdates(long step,int num_updates) throws InterruptedException,DMasonException
	{
		/*	result of failed test UpdateCellTester 
		 * 	on method testGetUpdatesNegativeNumUpdates
		 * */
		if(num_updates < 0)
			throw new DMasonException("Number of updates must be positive");
		
		lock.lock();
		PriorityQueue<MyCellIntegerField> tmp=this.get(step);

		while(tmp==null)
		{
			block.await();
			tmp=this.get(step);
		}
		
		/*	result of failed test UpdateCellTester 
		 * 	on method testGetUpdatesOverQueueLength
		 * */
		if(tmp.size() < num_updates)
			throw new DMasonException("The number of updates is not correct");
		
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
	    @Override
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