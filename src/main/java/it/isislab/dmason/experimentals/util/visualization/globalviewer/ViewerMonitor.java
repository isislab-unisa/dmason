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
package it.isislab.dmason.experimentals.util.visualization.globalviewer;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
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
		while(!queue.contains(step))
		{
			if(FORCE) { FORCE=false; break;}
			block.await();
		}
		
		queue.remove(step);
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
		lock.lock();
			FORCE=true;
			block.signal();
		lock.unlock();
	}
}
