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

import it.isislab.dmason.sim.field.CellType;

import java.io.Serializable;
import java.util.HashMap;
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
public class VisualizationUpdateMap<E,F> extends HashMap<Long,HashMap<String,Object>> implements Serializable
{
    private final ReentrantLock lock;
    private final Condition block;
    

    
    public VisualizationUpdateMap()
    {
    	lock=new ReentrantLock();
    	block=lock.newCondition();
    }
    private boolean FORCE=false;

	public HashMap<String,Object> getUpdates(long step,int num_cell) throws InterruptedException
	{
		long nowStep=step;
		lock.lock();
		HashMap<String,Object> tmp = this.get(nowStep);

		while(tmp==null)
		{
			block.await();
			if(FORCE)
			{
				FORCE=false;
				return null;
			}
			
			tmp=this.get(nowStep);
		}
		while(tmp.size()!=num_cell)
		{
			block.await();
			if(FORCE){
				FORCE=false;
			return null;
			}
		}
		this.remove(nowStep);
		lock.unlock();
		return tmp;
	}
	public void forceSblock()
	{
		lock.lock();
			FORCE=true;
			block.signal();
		lock.unlock();
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