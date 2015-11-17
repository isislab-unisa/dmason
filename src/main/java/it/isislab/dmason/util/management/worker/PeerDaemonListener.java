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
package it.isislab.dmason.util.management.worker;

import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.jms.Message;

import mpi.MPI;
import mpi.MPIException;

import org.apache.log4j.Logger;


/**
 * Even if it seems a little bit complex, this is just a listener that receive
 * all commands from the MasterDaemonStarter and executes them.
 * For our System Management architecture we've chosen a Master-Worker view,
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
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 * @author Mario Fiore Vitale
 */
public class PeerDaemonListener extends MyMessageListener implements Observer
{
	/** <code>status</code> value meaning the worker is running a simulation */ 
	static final int RUNNING = 0;

	/** <code>status</code> value meaning the worker is paused */
	static final int PAUSED  = 1;

	/** <code>status</code> value meaning the worker is stopped */
	static final int STOPPED = 2;

	/** <code>status</code> value meaning the worker received the simulation class but hasn't started yet */
	static final int STARTED = 3;

	/** <code>status</code> value meaning the worker is waiting for a simulation class */
	static final int IDLE = 4;

	/** Statuses' descriptions. */
	static final String[] statusMessage = {
		"Running", 
		"Paused", 
		"Stopped", 
		"Ready",
		"Idle"
	};

	static int cnt;

	Object t;

	/** Current status. */
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

	private static Logger logger;

	public PeerDaemonListener(PeerDaemonStarter pds, Connection con, String topic, boolean WorkerUI)
	{
		super();
		this.starter = pds;
		this.gui = starter.gui;
		this.connection = (ConnectionNFieldsWithActiveMQAPI)con;
		this.myTopic = topic;
		this.isWorkerUI = WorkerUI;

		logger = Logger.getLogger(this.getClass().getCanonicalName());
	}

	/**
	 * Process a message received on worker's topic.
	 */
	@Override
	public void onMessage(Message msg)
	{
		try
		{
			// Retrieve the actual message from ActiveMQ message envelope
			MyHashMap mh = (MyHashMap)parseMessage(msg);

			// Received startup data
			if (mh.get("classes") != null)
			{
				regions = (ArrayList<StartUpData>) mh.get("classes");

				table = new HashMap<String, MessageListener>();
				workers = new ArrayList<Worker>();
				
				updateDir =  regions.get(0).getUploadDir();
				topicPrefix = regions.get(0).getTopicPrefix();
				started = new ArrayList<Thread>();
				
				
				if(regions.get(0).getParam().getConnectionType()!=0)
				{
						try {
							MPI.Init(new String[]{});
						} catch (MPIException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				}
				for (StartUpData data : regions)
				{
					Worker wui = new Worker(data, connection);
					//register for notification about stop after totSteps
					wui.addObserver(this);
					workers.add(wui);
					
				}


				initializeTable();
				if(regions.get(0).getParam().isBatch())
				{
					starter.subscribeToBatch(topicPrefix); 
					starter.sendBatchInfo("ready");
				}

				status = STARTED;
				gui.writeMessage(regions.size() + " class definitions received!\n");
			}

			// Received request to publish peer informations
			if (mh.get("info") != null )
			{
				// If a simulation is loaded and is started,
				// retrieve steps
				long steps = workers != null && workers.size() > 0 ? workers.get(0).getSteps() : 0; 
				starter.info(statusMessage[status], steps);
			}

			// Received command to start the simulation
			if (mh.get("play") != null &&  workers!=null)
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
				// Worker was previously paused
				else if (status == PAUSED &&  workers!=null)
				{
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
			if (mh.get("pause") != null &&  workers!=null)
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
			if (mh.get("stop") != null &&  workers!=null)
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
			if (mh.get("reset") != null &&  workers!=null)
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
			if (mh.get("update") != null &&  workers!=null)
			{

				UpdateData up = (UpdateData) mh.get("update");


				if(isWorkerUI)
					Updater.updateWithGUI(up.getFTPAddress(),up.getJarName(),myTopic,connection.getAddress());
				else
					Updater.updateNoGUI(up.getFTPAddress(),up.getJarName(),myTopic,connection.getAddress());


				gui.exit();

			}
			// Received command to begin tracing a value
			if (mh.get("trace") != null &&  workers!=null)
			{
				String toTrace = (String) mh.get("trace");
				gui.writeMessage("Trace " + toTrace + "\n");
				for (Worker w : workers)
					w.trace(toTrace, true);
			}

			// Received command to stop tracing a value
			if (mh.get("untrace") != null &&  workers!=null)
			{
				String toUntrace = (String) mh.get("untrace");
				gui.writeMessage("Stop tracing " + toUntrace + "\n");
				for (Worker w : workers)
					w.trace(toUntrace, false);
			}

		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Error in received message from worker...");
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