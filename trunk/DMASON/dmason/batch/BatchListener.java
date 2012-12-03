package dmason.batch;

import java.util.concurrent.locks.Lock;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

public class BatchListener extends MyMessageListener
{
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
				System.out.println("From Batch Listener ready");
				
				fineshed++;
				System.out.println("ready: "+ fineshed +" tot: "+numPeers);
				if(fineshed == numPeers)
				{	
					fineshed = 0;
					
					nextTest();
				}
			}
			
			if(mh.get("test done")!=null)
			{
				System.out.println("From Batch Listener test done");
				
				fineshed++;
				System.out.println("done: "+ fineshed +" tot: "+numPeers);
				if(fineshed == numPeers)
				{	
					fineshed = 0;
					
					nextTest();
				}
			}
			
			if(mh.get("info")!=null)
			{
				System.out.println("From Batch Listener resetted");
				
				fineshed++;
				System.out.println("resetted: "+ fineshed +" tot: "+numPeers);
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
		System.out.println("unlock");
		
		executor.setCanStartAnother(true);
		
		Lock batchLock = executor.getLock();
		batchLock.lock();
		{
			executor.getIsResetted().signalAll();
		}
		batchLock.unlock();
	}

}
