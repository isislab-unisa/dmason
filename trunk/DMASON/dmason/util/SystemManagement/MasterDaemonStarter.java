/**
 * Copyright 2012 UniversitÓ degli Studi di Salerno


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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import com.sun.jmx.remote.internal.ArrayQueue;

import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class MasterDaemonStarter implements Observer
{


	/**
	 * The number of regions the field is split in.
	 */
	private int numRegions;

	/**
	 * Max distance an agent can travel in a single step. 
	 */
	private int jumpDistance;

	private int numAgents = 25;
	private int width = 201;
	private int height = 201;

	/**
	 * Field partitioning mode.
	 */
	private int fieldMode;

	/**
	 * Connection with a provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;

	/**
	 * Provider address.
	 */
	private Address address;

	/**
	 * Master's topic name.
	 */
	private String myTopic = "MASTER";

	/**
	 * A list of workers listening on the provider.
	 * Workers are identified by their topic
	 */
	private ArrayList<String> workerTopics;

	private MasterDaemonListener myml;

	private Address addr;

	private JMasterUI masterUi;

	/**
	 * Constructor.
	 * @param conn Connection with a provider.
	 */
	public MasterDaemonStarter(Connection conn, JMasterUI ui)
	{
		connection = (ConnectionNFieldsWithActiveMQAPI)conn;
		address = connection.getAddress();

		masterUi = ui;


	}

	/**
	 * 
	 * @param regions Number of regions the field is split in.
	 * @param width Field width.
	 * @param height Field height.
	 * @param agents Number of agents in the simulation.
	 * @param maxDistance Max distance an agent can travel in a single
	 *            simulation step.
	 * @param mode A value from <code>DSparseGrid2DFactory</code> specifying
	 *            if the field is horizontally split or as grid.
	 * @param config An HashMap of <code>&lt;String, EntryVal&gt;</code> where
	 * 		      each String is a worker's topic name, each EntryVal is
	 *            that topic configuration. Each EntryVal is in fact in the 
	 *            form <code>EntryVal&lt;Integer, Boolean&gt;</code> where 
	 *            each Integer is the number of fields to simulate on that
	 *            worker, each Boolean is <code>true</code> if the worked
	 *            must start simulation's GUIState.
	 * @param selSim Selected simulation class' canonical name.
	 * @param gui A reference to JMasterUI. 
	 */
	public void start(int regions, int width, int height, int agents, int maxDistance, int mode, HashMap<String, EntryVal<Integer, Boolean>> config, String selSim, JMasterUI gui,Address ftpAddress)
	{
		this.numRegions = regions;
		this.numAgents = agents;
		this.width = width;
		this.height = height;
		this.fieldMode = mode;
		this.jumpDistance = maxDistance;
		String ip = this.address.getIPaddress();
		Class<?> selClassUI = null;
		Class<?> selClass = null;	
	
	
		boolean isJarSimulation = true;
		if(!selSim.contains(".jar")) //hardcoded simulation
		{
			// Try to load the class definitions used by selected simulation
			try
			{
				selClass = Class.forName(selSim);
				//selClassUI = Class.forName(selSim);
				selClassUI = Class.forName(selSim + "WithUI");
	
				isJarSimulation = false;
			} catch (ClassNotFoundException e2) {
				System.err.println("Unable to load the simulation class " + selSim);
				e2.printStackTrace();
			}
		}
		else
		{
			addr = ftpAddress;
		}
	
		//
		if (ip.equals("127.0.0.1"))
		{
			try 
			{
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
	
	
		if (mode == DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE)
		{
			int cnt = 0;
			// Repeat for each worker
			for (String workerTopic : config.keySet())
			{
				int fieldsInWorker = config.get(workerTopic).getNum();
				ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
				// Repeat for each field managed by this worker
				for (int i = 0; i < fieldsInWorker; i++)
				{
					StartUpData data = new StartUpData();
					if (cnt == numRegions / 2)
						data.setStep(true);
	
					if (config.get(workerTopic).isFlagTrue())
					{
						data.graphic = true;
						if(!isJarSimulation)
							data.setDef(selClassUI);
						else
						{	
							data.setJarName(selSim);
							data.setFTPAddress(addr);
						}
					}
					else
					{
						data.graphic = false;
						if(!isJarSimulation)
							data.setDef(selClass);
						else
						{	
							data.setJarName(selSim);
							data.setFTPAddress(addr);
						}
					}
					data.setParam(new Object[]{ip,this.address.getPort(),jumpDistance,numRegions,numAgents,width,height,0,cnt,DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE});
					classes.add(data);
					cnt++;
				}
				// Publish informations about simulation to worker's topic
				connection.publishToTopic(classes, workerTopic, "classes");
			}
	
			gui.setSystemSettingsEnabled(false);
		}
		else if(mode == DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
		{
			// For each region...
			ArrayList<StartUpData> defs = new ArrayList<StartUpData>();
			for (int i=0;i<Math.sqrt(numRegions);i++){
				for (int k=0;k<Math.sqrt(numRegions);k++){
					StartUpData data = new StartUpData();
					// Set step on the central region
					if (i==k /*&& i == Math.sqrt(numRegions) / 2*/)
						data.setStep(true);
					data.setParam(new Object[]{ip,this.address.getPort(),jumpDistance,numRegions,numAgents,width,height,i,k,DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE});
					defs.add(data);
					data.graphic=false;
				}
			}
			int index=0;
			for (String workerTopic : config.keySet())
			{
				ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
				int fieldsInWorker = config.get(workerTopic).getNum();
				for(int i=0;i<fieldsInWorker;i++)
				{
					defs.get(index).graphic = config.get(workerTopic).isFlagTrue();
					if(config.get(workerTopic).isFlagTrue())
					{
						defs.get(index).setDef(selClassUI);
					}
					else
					{
						defs.get(index).setDef(selClass);
					}
					classes.add(defs.get(index));					
					index++;
				}
	
				if(connection.publishToTopic(classes, workerTopic, "classes")==true)
				{
					gui.setSystemSettingsEnabled(false);
				}
				else
				{
					JOptionPane.showMessageDialog(null,"Setting failed !");
				}
			}
		} else if (mode==DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE){
			// For each region...
			ArrayList<StartUpData> defs = new ArrayList<StartUpData>();
			for (int i=0;i<Math.sqrt(numRegions);i++){
				for (int k=0;k<Math.sqrt(numRegions);k++){
					StartUpData data = new StartUpData();
					// Set step on the central region
					if (i==k)
						data.setStep(true);
					//data.setDef(DAntsForage.class);
					data.setParam(new Object[]{ip,this.address.getPort(),jumpDistance,numRegions,numAgents,width,height,i,k,DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE});
					defs.add(data);
					data.graphic=false;
				}
			}
			int index=0;
			for (String workerTopic : config.keySet())
			{
				ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
				int fieldsInWorker = config.get(workerTopic).getNum();
				for(int i=0;i<fieldsInWorker;i++)
				{
					defs.get(index).graphic = config.get(workerTopic).isFlagTrue();
					if(config.get(workerTopic).isFlagTrue())
					{
						defs.get(index).setDef(selClassUI);
					}
					else
					{
						defs.get(index).setDef(selClass);
					}
					classes.add(defs.get(index));					
					index++;
				}
	
				try{
					connection.publishToTopic(classes,workerTopic,"classes");	
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean connectToServer()
	{
		try
		{
			if(connection.createTopic(myTopic,1)==true)
			{
				if(connection.subscribeToTopic(myTopic)==true)
				{
					connection.asynchronousReceive(myTopic, myml = new MasterDaemonListener());

					myml.getObservable().addObserver(this);
				}
				else return false;
			}
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Retrieve a list of workers' topics, that is the list of topics whose
	 * name begins with "SERVICE"
	 * @return An <code>ArrayList<String></code> of workers' topic names
	 * @throws Exception
	 */
	public ArrayList<String> getTopicList() throws Exception
	{
		workerTopics = new ArrayList<String>();
		for(String topicName : connection.getTopicList())
		{
			if(topicName.startsWith("SERVICE"))
			{
				workerTopics.add(topicName);
				connection.createTopic(topicName, 1);
			}
		}
		return workerTopics;
	}


	public PeerStatusInfo getLatestUpdate(String key)
	{
		return myml.getLatestUpdate(key);
	}

	/** Command Section */
	
	/**
	 * Pauses the workers.
	 * @throws Exception
	 */
	public void info(String key) throws Exception
	{
		connection.publishToTopic("info", key, "info");
	}

	/**
	 * Pauses the workers.
	 * @throws Exception
	 */
	public void pause() throws Exception
	{
		// Just publish to workers' topics the string "pause"
		// under the key "pause"
		for(String topicName : workerTopics)
			connection.publishToTopic("pause", topicName, "pause");
	}

	/**
	 * Stops the workers.
	 * @throws Exception
	 */
	public void stop(UpdateData ud) throws Exception
	{
		for(String topicName : workerTopics)
			connection.publishToTopic(ud, topicName, "stop");
	}

	/**
	 * Reset the workers.
	 * @throws Exception
	 */
	public void reset() throws Exception
	{
		for(String topicName : workerTopics)
			connection.publishToTopic("reset", topicName, "reset");
	}

	/**
	 * Starts/resumes the workers.
	 * @throws Exception
	 */
	public void play() throws Exception
	{
		for(String topicName : workerTopics)
			connection.publishToTopic("play", topicName, "play");
	}

	/**
	 * Notify the worker for the update
	 * @param ud 
	 * @throws Exception
	 */
	public void notifyUpdateAll(UpdateData ud) 
	{
		for(String topicName : workerTopics)
			connection.publishToTopic(ud, topicName, "update");
	}

	/**
	 * Notify some worker for the update
	 * @param ud 
	 * @throws Exception
	 */
	public void notifyUpdate(UpdateData ud, ArrayList<String> toUpdate) 
	{
		for(String topicName : toUpdate)
			connection.publishToTopic(ud, topicName, "update");
	}

	/** End Command Section */
	
	
	// reiceves notify from MasterDeamonListener related to worker update
	@Override
	public void update(Observable arg0, Object arg1) {
	
		if(arg1 == null) // used for check update process progress
			masterUi.incrementUpdatedWorker();
		else
		{  //automatic update, used for check worker version
			PeerStatusInfo workerInfo = (PeerStatusInfo) arg1;
			masterUi.addToUpdate(workerInfo);
		}
	
	}

	public void hilbert(ArrayList<StartUpData> defs,ArrayList<String> clients)
	{
		ArrayQueue<StartUpData> queue = new ArrayQueue<StartUpData>(25);
		queue.add(defs.get(0));
		queue.add(defs.get(1));
		queue.add(defs.get(5));
		queue.add(defs.get(6));
		queue.add(defs.get(2));
		queue.add(defs.get(7));
		queue.add(defs.get(12));
		queue.add(defs.get(17));
		queue.add(defs.get(16));
		queue.add(defs.get(11));
		queue.add(defs.get(10));
		queue.add(defs.get(15));
		queue.add(defs.get(20));
		queue.add(defs.get(21));
		queue.add(defs.get(22));
		queue.add(defs.get(23));
		queue.add(defs.get(24));
		queue.add(defs.get(19));
		queue.add(defs.get(18));
		queue.add(defs.get(13));
		queue.add(defs.get(14));
		queue.add(defs.get(9));
		queue.add(defs.get(8));
		queue.add(defs.get(3));
		queue.add(defs.get(4));
		/*CentralGuiState g = new CentralGuiState(new CentralSimState());
		Console c = (Console) g.createController();
		c.pressPause();*/
	}


}