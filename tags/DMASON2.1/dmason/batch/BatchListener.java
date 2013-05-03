/**
 * Copyright 2012 Universit� degli Studi di Salerno
 

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

package dmason.batch;

import java.util.concurrent.locks.Lock;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

public class BatchListener extends MyMessageListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BatchExecutor executor;
	private int numPeers;
	private int fineshed = 0;

	public BatchListener(BatchExecutor batchExecutor, int numPeers) 
	{
		this.executor = batchExecutor;
		this.numPeers = numPeers;
	}

	@Override
	public void onMessage(Message arg0) 
	{
		// TODO Auto-generated method stub
		ActiveMQObjectMessage msg = (ActiveMQObjectMessage) arg0;
		try{
			MyHashMap mh = (MyHashMap)msg.getObject();
			
			if(mh.get("ready")!=null)
			{
				//System.out.println("From Batch Listener ready");
				
				fineshed++;
				//System.out.println("ready: "+ fineshed +" tot: "+numPeers);
				if(fineshed == numPeers)
				{	
					fineshed = 0;
					
					nextTest();
				}
			}
			
			if(mh.get("test done")!=null)
			{
				//System.out.println("From Batch Listener test done");
				
				fineshed++;
				//System.out.println("done: "+ fineshed +" tot: "+numPeers);
				if(fineshed == numPeers)
				{	
					fineshed = 0;
					
					nextTest();
				}
			}
			
			if(mh.get("info")!=null)
			{
				//System.out.println("From Batch Listener resetted");
				
				fineshed++;
				//System.out.println("resetted: "+ fineshed +" tot: "+numPeers);
				if(fineshed == numPeers)
				{	
					fineshed = 0;
					
					nextTest();
				}
			}
		}catch (Exception e) {
		e.printStackTrace();
		}
	}
	
	public void nextTest() 
	{
		// TODO Auto-generated method stub
		//System.out.println("unlock");
		
		executor.setCanStartAnother(true);
		
		Lock batchLock = executor.getLock();
		batchLock.lock();
		{
			executor.getIsResetted().signalAll();
		}
		batchLock.unlock();
	}

}
