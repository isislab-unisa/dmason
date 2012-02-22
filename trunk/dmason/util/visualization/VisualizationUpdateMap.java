package dmason.util.visualization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import dmason.sim.field.CellType;

public class VisualizationUpdateMap<E,F> extends HashMap<Long,HashMap<String,Object>> implements Serializable
{
    private final ReentrantLock lock;
    private final Condition block;
    
    public VisualizationUpdateMap()
    {
    	lock=new ReentrantLock();
    	block=lock.newCondition();
    }
    
	public HashMap<String,Object> getUpdates(long step,int num_cell) throws InterruptedException
	{
		lock.lock();
		HashMap<String,Object> tmp = this.get(step);

		while(tmp==null)
		{
			block.await();
			tmp=this.get(step);
		}
		
		while(tmp.size()!=num_cell)
		{
			block.await();
		}
		this.remove(step);
		lock.unlock();
		return tmp;
	}
	
	public void put(long step, Object d_reg)
	{		
		lock.lock();
		
		CellType ct = new CellType(((RemoteSnap)d_reg).i, ((RemoteSnap)d_reg).j);
		
		HashMap<String,Object> tmp=this.get(step);
		
		if(tmp!=null)
		{
			tmp.put(((RemoteSnap)d_reg).i+"-"+((RemoteSnap)d_reg).j,d_reg);
		}
		else
		{
			HashMap<String,Object> queue_update=new HashMap<String,Object>();
			
			queue_update.put(((RemoteSnap)d_reg).i+"-"+((RemoteSnap)d_reg).j,d_reg);

			super.put(step,queue_update); 
		}
		
		block.signal();	
		lock.unlock();
	}
}