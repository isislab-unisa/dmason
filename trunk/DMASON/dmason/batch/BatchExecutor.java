/**
 * Copyright 2012 Università degli Studi di Salerno
 

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import dmason.batch.data.EntryParam;
import dmason.batch.data.EntryParam.ParamType;
import dmason.batch.data.GeneralParam;
import dmason.batch.data.TestParam;
import dmason.util.SystemManagement.EntryVal;
import dmason.util.SystemManagement.MasterDaemonStarter;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * Thi class is the core of the batch test features.
 * 
 * @author marvit
 *
 */
public class BatchExecutor extends Thread
{

	private DelegatedObservable obs = new DelegatedObservable();
	private ConcurrentLinkedQueue<List<EntryParam<String, Object>>> testQueue;
	private String simulationName;
	private MasterDaemonStarter master;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition isResetted = lock.newCondition();
	private boolean canStartAnother = false;

	
	private String myTopic = "Batch1";
	private int numPeers;
	private Address fptAddress;
	
	private List<String> myWorkers;
	private static AtomicInteger testCounter = new AtomicInteger(1);
	private int currentTest;	

	public BatchExecutor(String simName, ConcurrentLinkedQueue<List<EntryParam<String, Object>>> testQueue, MasterDaemonStarter master, ConnectionNFieldsWithActiveMQAPI con,int NumPeers,Address ftpAddress, List<String> workers,String prefix, int init) 
	{
		super();
		this.testQueue = testQueue;
		this.master = master;
		
		this.simulationName = simName;
		this.connection = con;
		this.numPeers = workers.size();
		this.fptAddress =  ftpAddress;
		this.myWorkers = workers;
		this.myTopic = prefix;
		
		currentTest = this.testCounter.getAndIncrement();
		
		
		registerListener();
	}



	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		super.run();

		System.out.println("Test: "+testQueue.size());

		while(!testQueue.isEmpty())
		//for (List<EntryParam<String, Object>> paramList : testQueue) 
		{
			System.out.println("Params: "+testQueue.peek());
			
			TestParam testParam = preProcess(testQueue.poll());

			HashMap<String, EntryVal<Integer, Boolean>> config = assignRegions(testParam.getGenParams().getRows()*testParam.getGenParams().getRows(),numPeers);
			System.out.println("oooooo: "+config.toString());
			
			System.out.println("CURRENT TEST: "+currentTest);
			master.startBatch(testParam.getGenParams(), config, simulationName , testParam.getSimParams(),fptAddress,myTopic,currentTest);

			System.out.println("Wait until the workes are ready");
			while(!isCanStartAnother())
			{
				lock.lock();
				{
					try {
						isResetted.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} //wait until the current test are done
				}
				lock.unlock();
			}
			canStartAnother = false;
			
			System.out.println("play");
			try {
				master.play(myWorkers);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {

				System.out.println("Wait until the test are done");
				while(!isCanStartAnother())
				{
					lock.lock();
					{
						isResetted.await(); //wait until the current test are done
					}
					lock.unlock();
				}
				canStartAnother = false;

				System.out.println("test Done!!!");
				
				// must clear topic
				connection.resetBatchTopic(myTopic);
				
				//Thread.sleep(30000);
				try {


					master.reset(myWorkers);

					System.out.println("Wait until the worker are reset");
					while(!isCanStartAnother())
					{
						lock.lock();
						{
							isResetted.await();
						}
						lock.unlock();
					}
					canStartAnother = false;	
					System.out.println("resetted");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				//Notify JMasterUI, used for update progress bar
				obs.setChanged();
				obs.notifyObservers("Test done");
				
				
				currentTest = testCounter.getAndIncrement();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		testCounter.set(1);
		System.out.println("Finished!!!");
		
	}



	public Condition getIsResetted() {
		return isResetted;
	}



	public boolean isCanStartAnother() {
		return canStartAnother;
	}



	public void setCanStartAnother(boolean canStartAnother) {
		this.canStartAnother = canStartAnother;
	}



	public ReentrantLock getLock() {
		return lock;
	}

	public Observable getObservable() {return obs;}

	private HashMap<String, EntryVal<Integer, Boolean>> assignRegions(int numRegions, int numPeers) {
		HashMap<String,EntryVal<Integer,Boolean>> config = new HashMap<String, EntryVal<Integer,Boolean>>();
		
		int regionsToPeers = numRegions / numPeers;
		int remainder = numRegions % numPeers;
		
		if(remainder == 0) // all the workers will have the same number of regions
		{
			EntryVal<Integer, Boolean> value; 
			try{
				for(String topic : myWorkers){
					value = new EntryVal<Integer, Boolean>(regionsToPeers, false/*withGui*/);
					//config.put(topic, div);
					config.put(topic, value);
				}

			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			try { // there will be some workers(remainders) that will have one more regions
				int count = 0;
				EntryVal<Integer, Boolean> value;
				for(String topic : myWorkers) {
					if(count < remainder)
						value = new EntryVal<Integer, Boolean>(regionsToPeers+1,false/*withGui*/);
					else
						value = new EntryVal<Integer, Boolean>(regionsToPeers,false/*withGui*/);
					
					config.put(topic, value);
					
					count++;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return config;
	}


	public boolean registerListener()
	{
		try
		{
			if(connection.createTopic(myTopic,1)==true)
			{
				if(connection.subscribeToTopic(myTopic) == true)
				{
					connection.asynchronousReceive(myTopic, new BatchListener(this,numPeers));

				}
				else return false;
			}
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	private static TestParam preProcess(List<EntryParam<String, Object>> params) 
	{

		ArrayList<EntryParam<String, Object>> simParam = new ArrayList<EntryParam<String,Object>>();

		GeneralParam genPar =  new GeneralParam();

		for (EntryParam<String, Object> entryParam : params ) {
			if(entryParam.getType() == ParamType.GENERAL)
			{
				try {
					genPar.getClass().getDeclaredField(entryParam.getParamName()).set(genPar, entryParam.getParamValue());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			else
				simParam.add(entryParam);
		}

		return new TestParam(genPar, simParam);
	}


	//A subclass of Observable that allows delegation.
		public class DelegatedObservable extends Observable 
		{
			public void clearChanged() {
				super.clearChanged();
			}
			public void setChanged() {
				super.setChanged();
			}
		}

}
