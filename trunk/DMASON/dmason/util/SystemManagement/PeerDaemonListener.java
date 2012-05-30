package dmason.util.SystemManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQObjectMessage;
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
 * @author Ada Mancuso
 * @author Luca Vicidomini
 */
public class PeerDaemonListener extends MyMessageListener
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

	public PeerDaemonListener(PeerDaemonStarter pds, Connection con)
	{
		super();
		this.starter = pds;
		this.gui = starter.gui;
		this.connection = (ConnectionNFieldsWithActiveMQAPI)con;
	}

	@Override
	public void onMessage(Message msg)
	{
		try
		{
			MyHashMap mh = (MyHashMap)parseMessage(msg);
			
			// Received startup data
			if (mh.get("classes") != null)
			{
				regions = (ArrayList<StartUpData>) mh.get("classes");
				table = new HashMap<String, MessageListener>();
				workers = new ArrayList<Worker>();
				gui.writeMessage("--> " + regions.size() + " class definitions received!\n");

				for (StartUpData data : regions)
				{
					Worker wui = new Worker(data, connection);
					workers.add(wui);
				}
				initializeTable();
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
					gui.writeMessage("--> Start Simulation!\n");
					for (Worker w : workers)
						new Starter(w).start();
					status = RUNNING;
				}
				// Worked was previously paused
				else if (status == PAUSED)
				{
					gui.writeMessage("---> Resume\n");
					for (Worker w : workers)
						w.signal();
					status = RUNNING;
				}
				// Worker was previously stopped
				else if (status == STOPPED){	
					gui.writeMessage("---> Restart\n");
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
					gui.writeMessage("--> Pause\n");
					for (Worker w : workers)
						w.await();
					status = PAUSED;
				}
			}
			
			// Received command to stop the simulation
			if (mh.get("stop") != null)
			{
				gui.writeMessage("--> Stop\n");
				for (Worker w : workers)
					w.stop_play();	
				workers = new ArrayList<Worker>();
				// table = null;
				// regions = null;
				status = STOPPED;
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

}