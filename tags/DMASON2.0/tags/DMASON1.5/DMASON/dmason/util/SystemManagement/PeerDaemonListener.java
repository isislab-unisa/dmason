package dmason.util.SystemManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.jms.Message;
import org.apache.activemq.command.ActiveMQObjectMessage;
import dmason.sim.field.MessageListener;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

/**
 * @author Ada Mancuso 
 * Even if it seems a little bit complex,this is just a listener that receive all commands from the MasterDaemonStarter
 * executes them.For out System Management architecture we've chosen a Master-Worker view,in which Master commands and
 * Workers execute.To make a Worker reusable after a simulation,in order to having not need of restarting it,i've created a
 * Master Worker that only connects to the server and many sub-worker(one for each field's region assigned) that do run their
 * simulation portion and after that exit.So i've used a Master-Worker architecture also in the Worker part :)
 * Then, when the listener receive a command as "pause","play","stop",it has to send the same command to all the sub-worker,using 
 * a Socket communication channel(i also thought to use a PipedIOStream).
 */
public class PeerDaemonListener extends MyMessageListener{

	Object t;
	String NumPeer;
	int STATUS=3;
	static int PLAY=0;
	static int PAUSE=1;
	static int STOP=2;
	static int START=3;
	static int cnt;
	public StartWorkerInterface gui;	
	public int STEP=0;
	PeerDaemonStarter daemon;
	private ArrayList<Worker> workers;
	private ArrayList<StartUpData> regions;
	private HashMap<String,MessageListener> table;
	private ConnectionNFieldsWithActiveMQAPI connection;
	Logger log;

	public PeerDaemonListener(PeerDaemonStarter pds,Connection con){
		super();
		daemon = pds;
		this.gui = daemon.gui;


		connection = (ConnectionNFieldsWithActiveMQAPI)con;
	}

	@Override
	public void onMessage(Message arg0) {
		try {

			ActiveMQObjectMessage obj = (ActiveMQObjectMessage)arg0;
			MyHashMap mh = (MyHashMap)obj.getObject();

			if(mh.get("classes") !=null){
				regions = (ArrayList<StartUpData>) mh.get("classes");
				table = new HashMap<String, MessageListener>();
				workers = new ArrayList<Worker>();


				gui.writeMessage("-->"+regions.size()+" class definitions received!\n");



				for(StartUpData data : regions){
					Worker wui = new Worker(data,connection);
					workers.add(wui);
				}
				initializeTable();
			}



			if(mh.get("info")!=null)
				daemon.info();

			if(mh.get("play")!=null){
				if(STATUS == START){

					gui.writeMessage("-->Start Simulation!\n");



					for(Worker w : workers)
						new starter(w).start();
					STATUS = PLAY;
				}
				else if(STATUS == PAUSE){
					gui.writeMessage("--->Restart\n");


					for(Worker w : workers)
						w.signal();
					STATUS = PLAY;
					//for(Worker w : workers)
					//w.oneStep();
				}

				else if(STATUS==STOP){	


					gui.writeMessage("--->Restart\n");


					for(StartUpData data : regions){
						Worker wui = new Worker(data,connection);
						workers.add(wui);
					}



					for(Worker w : workers)
						new starter(w).start();
					STATUS = PLAY;
				}
			}
			if(mh.get("pause")!=null){
				if(STATUS == PLAY){
					gui.writeMessage("-->Pause\n");


					for(Worker w : workers)
						w.await();
					STATUS = PAUSE;
				}
				//				else if(STATUS == PAUSE)
				//				{
				//				   
				//							gui.writeMessage("--->Restart\n");
				//					
				//					
				//					
				//					for(Worker w : workers)
				//						w.signal();
				//					STATUS = PLAY;
				//				}
			}
			if(mh.get("stop")!=null){

				gui.writeMessage("-->Stop\n");

				for(Worker w : workers)
					w.stop_play();	
				workers = new ArrayList<Worker>();
				//					table = null;
				//					regions = null;
				STATUS = STOP;
			}
		}catch (Exception e) {e.printStackTrace();}
	}

	public void initializeTable()
	{
		for(Worker w : workers)
		{
			ArrayList<MessageListener> list = w.getListeners();
			for(MessageListener x : list)
			{
				table.put(x.getTopic(),x);
			}
		}

		for (Worker w : workers)
		{
			w.setTable(table);
		}
	}

	class starter extends Thread{

		Worker w;

		public starter(Worker w) {
			super();
			this.w = w;
		}

		@Override
		public void run() {
			super.run();
			cnt--;
			if(cnt == 0)
				STATUS = PLAY;
			w._start();
		}
	}

}