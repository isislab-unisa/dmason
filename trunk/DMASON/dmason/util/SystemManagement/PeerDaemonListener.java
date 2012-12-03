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
package dmason.util.SystemManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.jms.Message;

import dmason.sim.field.MessageListener;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;


/**
 * Even if it seems a little bit complex, this is just a listener that receive
 * all commands from the MasterDaemonStarter and executes them.
 * For out System Management architecture we've chosen a Master-Worker view,
 * in which Master commands and Workers execute. To make a Worker reusable
 * after a simulation, in order to having not need of restarting it, I've 
 * created a Master Worker that only connects to the server and many
 * sub-worker (one for each field's region assigned) that do run their 
 * simulation portion and after that exit. So I've used a Master-Worker 
 * architecture also in the Worker part :)
 * Then, when the listener receive a command as "pause", "play", "stop", it
 * has to send the same command to all the sub-worker, using 
 * a Socket communication channel (I also thought to use a PipedIOStream).
 * 
 * Luca: added code for variables tracing
 * Mario: added code for reset simulation, update worker, upload log file
 * 
 * @author Ada Mancuso
 * @author Luca Vicidomini
 * @author Mario Fiore Vitale
 */
public class PeerDaemonListener extends MyMessageListener implements Observer
{
	// Valid values for status
	static final int RUNNING = 0;
	static final int PAUSED  = 1;
	static final int STOPPED = 2;
	static final int STARTED = 3;
	
	static int cnt;
	
	Object t;
	//String NumPeer;
	int status = STARTED;
	
	public int step = 0;
	public StartWorkerInterface gui;
	private PeerDaemonStarter starter;
	private ArrayList<Worker> workers;
	private ArrayList<StartUpData> regions;
	private HashMap<String,MessageListener> table;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private ArrayList<Thread> started;
	
	private String myTopic;
	private boolean isWorkerUI;
	
	private String updateDir;
	
	private int stoppedWorker = 0;
	private String topicPrefix;

	public PeerDaemonListener(PeerDaemonStarter pds, Connection con, String topic, boolean WorkerUI)
	{
		super();
		this.starter = pds;
		this.gui = starter.gui;
		this.connection = (ConnectionNFieldsWithActiveMQAPI)con;
		this.myTopic = topic;
		this.isWorkerUI = WorkerUI;
	}

	@Override
	public void onMessage(Message msg)
	{
		try
		{
			
			//System.out.println("Topic :" + msg.getJMSDestination().toString());
			MyHashMap mh = (MyHashMap)parseMessage(msg);
			
			// Received startup data
			if (mh.get("classes") != null)
			{
				regions = (ArrayList<StartUpData>) mh.get("classes");
				
				table = new HashMap<String, MessageListener>();
				workers = new ArrayList<Worker>();
				gui.writeMessage(regions.size() + " class definitions received!\n");

				updateDir =  regions.get(0).getUploadDir();
				topicPrefix = regions.get(0).getTopicPrefix();
				started = new ArrayList<Thread>();
				for (StartUpData data : regions)
				{
					
					Worker wui = new Worker(data, connection);
					//register for notification about stop after totSteps
					wui.addObserver(this);
					workers.add(wui);
				}
				
				
				initializeTable();
				if(regions.get(0).getParam().isBatch)
				{
					starter.subscribeToBatch(topicPrefix); 
				
					starter.sendBatchInfo("ready");
				}
			}

			// Received request to publish peer informations
			if (mh.get("info") != null)
			{
				starter.info();
			}

			// Received command to start the simulation
			if (mh.get("play") != null)
			{
				
				// Worker is at initial state
				if (status == STARTED)
				{
					gui.writeMessage("Start\n");
					
					// ????????
					for (Worker w : workers)
						started.add(new Starter(w));
					for (Thread w : started)
						w.start();
					status = RUNNING;
					
				}
				// Worked was previously paused
				else if (status == PAUSED)
				{
					gui.writeMessage("---> Resume\n");

					gui.writeMessage("Resume\n");

					for (Worker w : workers)
						w.signal();
					status = RUNNING;
					
					
				}
				// Worker was previously stopped
				else if (status == STOPPED){	
					gui.writeMessage("Restart\n");
					
					workers = new ArrayList<Worker>();
					for (StartUpData data : regions)
					{
						Worker wui = new Worker(data, connection);
						workers.add(wui);
					}
					for (Worker w : workers)
					{
						new Starter(w).start();
					}
					status = RUNNING;
				}
			}
			
			// Received command to pause the simulation
			if (mh.get("pause") != null)
			{
				// Worker was running the simulation
				if (status == RUNNING)
				{
					gui.writeMessage("Pause\n");
					for (Worker w : workers)
						w.await();
					status = PAUSED;
				}
			}
			
			// Received command to stop the simulation
			if (mh.get("stop") != null)
			{
				gui.writeMessage("Stop\n");
				for (Worker w : workers)
					w.stop_play();	
				
				//table = null;
				//regions = null;
				status = STOPPED;
				
				
				UpdateData up = (UpdateData) mh.get("stop");
				Updater.uploadLog(up,updateDir,false);
				
			}

			
			// Received command to reset the simulation
			if (mh.get("reset") != null)
			{
				String content = (String) mh.get("reset");
				boolean isBatch = false;
				
				if(content.equals("batch"))
					isBatch = true;
				
				
				gui.writeMessage("--> Reset\n");
				
				Updater.restart(myTopic,connection.getAddress(),isBatch,topicPrefix);
				
				gui.exit();
				
				/*for (Worker w : workers)
					w.kill();
				
				
				Worker w = workers.get(workers.size()-1);
				if(w.isGUI())
					w.getConsole().doClose();
				
				regions = null;
				table = null;
				workers = null;
				
				Runtime.getRuntime().gc();
				
				status = STARTED;*/
				
			}
			

			// Received command to update worker
			if (mh.get("update") != null)
			{
				
				UpdateData up = (UpdateData) mh.get("update");
				
				
				if(isWorkerUI)
					Updater.updateWithGUI(up.getFTPAddress(),up.getJarName(),myTopic,connection.getAddress());
				else
					Updater.updateNoGUI(up.getFTPAddress(),up.getJarName(),myTopic,connection.getAddress());
				
				
				gui.exit();
				
			}
			// Received command to begin tracing a value
			if (mh.get("trace") != null)
			{
				String toTrace = (String) mh.get("trace");
				gui.writeMessage("Trace " + toTrace + "\n");
				for (Worker w : workers)
					w.trace(toTrace, true);
			}
			
			// Received command to stop tracing a value
			if (mh.get("untrace") != null)
			{
				String toUntrace = (String) mh.get("untrace");
				gui.writeMessage("Stop tracing " + toUntrace + "\n");
				for (Worker w : workers)
					w.trace(toUntrace, false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initializeTable()
	{
		for (Worker w : workers)
		{
			ArrayList<MessageListener> list = w.getListeners();
			for (MessageListener x : list)
			{	
				table.put(x.getTopic(),x);
			}
		}

		for (Worker w : workers)
		{
			w.setTable(table);
		}
	}

	class Starter extends Thread
	{
	
		Worker w;

		public Starter(Worker w)
		{
			super();
			this.w = w;
		}

		@Override
		public void run()
		{
			super.run();
			cnt--;
			if (cnt == 0)
				status = RUNNING;
			w._start();
		
			
		}
		
		
		
	}

	//Reiceve info by Worker about simulation stops
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		synchronized (this) {
			stoppedWorker++;
		}
		
		System.out.println("Stop "+stoppedWorker);
		if(stoppedWorker == workers.size())
		{
			stoppedWorker = 0;
			System.out.println("send message to master");
			
			UpdateData up =  new UpdateData("",regions.get(0).getFTPAddress());
			
			Updater.uploadLog(up,updateDir,true);
			
			//starter.testFinished();
			starter.sendBatchInfo("test done");
		}
	}

}