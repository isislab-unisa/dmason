package dmason.util.SystemManagement;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQObjectMessage;
import dmason.sim.field.MessageListener;
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
	int STATUS=2;
	static int PLAY=0;
	static int PAUSE=1;
	static int STOP=2;
	static int cnt;
	public StartWorkerWithGui gui;
	public int STEP=0;
	PeerDaemonStarter daemon;
	private ArrayList<Worker> workers;
	private ArrayList<StartUpData> regions;
	private HashMap<String,MessageListener> table;
	
	public PeerDaemonListener(PeerDaemonStarter pds){
		super();
		daemon = pds;
		this.gui = daemon.gui;
	}

	@Override
	public void onMessage(Message arg0) {
		try {
			
		ActiveMQObjectMessage obj = (ActiveMQObjectMessage)arg0;
		
			if(obj.getObject() instanceof ArrayList){
				regions = (ArrayList<StartUpData>) obj.getObject();
				table = new HashMap<String, MessageListener>();
				workers = new ArrayList<Worker>();
				gui.textArea.append("-->"+regions.size()+" class definitions received!\n");
				for(StartUpData data : regions)
				{
					Worker wui = new Worker(data);
					workers.add(wui);
				}
				initializeTable();
			}
			
			if(obj.getObject() instanceof String){
				String cmd = (String) obj.getObject();
				
				if(cmd.equals("info"))
					daemon.info();
				
				if(cmd.equals("play")){
					if(STATUS == STOP){
						gui.textArea.append("-->Start Simulation!\n");
						for(Worker w : workers)
							new starter(w).start();
						STATUS = PLAY;
					}
					if(STATUS == PAUSE)
					{
						for(Worker w : workers)
							w.oneStep();
					}
				}
				if(cmd.equals("pause")){
					if(STATUS == PLAY)
					{
						gui.textArea.append("-->Pause\n");
						for(Worker w : workers)
							w.await();
						STATUS = PAUSE;
					}
					if(STATUS == PAUSE)
					{
						gui.textArea.append("--->Restart\n");
						for(Worker w : workers)
							w.signal();
						STATUS = PLAY;
					}
				}
				if(cmd.equals("stop")){
					gui.textArea.append("-->Stop\n");
					for(Worker w : workers)
						w.stop_play();
					workers = null;
					table = null;
					regions = null;
					STATUS = 2;
				}
			}
	}catch (Exception e) {
		e.printStackTrace();
	}
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

