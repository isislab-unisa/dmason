package dmason.util.visualization;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jmx.remote.internal.ArrayQueue;

public class ViewerMonitor {
	
	public ArrayList<Long> queue=new ArrayList<Long>();
	public boolean isZoom=false;
	public boolean isSynchro=false;
	public boolean ZOOM=false;
	private boolean FORCE=false;
	
	private final ReentrantLock lock = new ReentrantLock();
    private final Condition block = lock.newCondition();
    
	public void awaitForAckStep(Long step) throws InterruptedException
	{
		lock.lock();
		System.out.println("Apetto per step "+step);
		while(!queue.contains(step))
		{
			if(FORCE) { FORCE=false; break;}
			block.await();
		}
		
		queue.remove(step);
		System.out.println("Sincronizzazione riuscita per step "+step );
		lock.unlock();
	}
	public void putAck(Long step)
	{
		lock.lock();
			queue.add(step);
			block.signal();
		lock.unlock();
	}
	public void forceWakeUp() {
		// TODO Auto-generated method stub
		lock.lock();
			FORCE=true;
			block.signal();
			
		lock.unlock();
	}
}
