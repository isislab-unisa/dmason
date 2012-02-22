package dmason.util.SystemManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import com.sun.jmx.remote.internal.ArrayQueue;
import dmason.sim.field.grid.DSparseGrid2DFactory;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.sim.app.DAntsForage.DAntsForage;
import dmason.sim.app.DAntsForage.DAntsForageWithUI;
import dmason.sim.app.DFlockers.DFlockers;
import dmason.sim.app.DFlockers.DFlockersWithUI;
import dmason.sim.app.DParticles.DParticles;
import dmason.sim.app.DParticles.DParticlesWithUI;

public class MasterDaemonStarter {
	
	private int NUM_PEERS;
	private int MAX_DISTANCE;
	private int NUM_AGENTS=25;
	private int WIDTH=201;
	private int HEIGHT=201;
	private int MODE;
	private boolean isTOROIDAL;
	private Address data;
	private ConnectionNFieldsWithActiveMQAPI connection;
	private String myTopic="MASTER"/*+InetAddress.getLocalHost().getHostAddress()*/;
	private ArrayList<String> list;
	private MasterDaemonListener myml;
	
	public MasterDaemonStarter(Connection con) throws Exception{
		connection = (ConnectionNFieldsWithActiveMQAPI)con;
		data = connection.getAdress();
	}
	
	public boolean connectToServer(){
		try {
			//boolean flag = connection.setupConnection(data);
			connection.createTopic(myTopic,1);
			connection.subscribeToTopic(myTopic);
			connection.asynchronousReceive(myTopic,myml =  new MasterDaemonListener());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<String> getTopicList() throws Exception{
		list = new ArrayList<String>();
		for(String s : connection.getTopicList()){
			if(s.startsWith("SERVICE")){
				list.add(s);
				connection.createTopic(s,1);
			}
		}
		return list;
	}
	
	public PeerStatusInfo getLatestUpdate(String key){
		return myml.getLatestUpdate(key);
	}
	
	public void info(String key)throws Exception{
		connection.publishToTopic("info", key, "info");
	}
	
	public void pause() throws Exception{
		for(String s : list)
			connection.publishToTopic("pause", s, "pause");
	}
	
	
	public void stop() throws Exception{
		for(String s : list)
			connection.publishToTopic("stop", s, "stop");
	}
	
	public void play() throws Exception{
		for(String s : list)
			connection.publishToTopic("play", s, "play");
	}
	
	
	public void start(int num,int width,int height,int agents,int maxDistance,int mode,HashMap<String,EntryVal<Integer, Boolean>> config, String selSim){
		NUM_PEERS = num;
		NUM_AGENTS = agents;
		WIDTH = width;
		HEIGHT = height;
		MODE = mode;
		MAX_DISTANCE = maxDistance;
		String ip = this.data.getIPaddress();
		Class selClassUI = null;
		Class selClass = null;
		
		if(selSim.equals("Flockers")){
			isTOROIDAL = true;
			selClass = DFlockers.class;
			selClassUI = DFlockersWithUI.class;
		}
		
		if(selSim.equals("Ant Foraging")){
			isTOROIDAL = false;
			selClass = DAntsForage.class;
			selClassUI = DAntsForageWithUI.class;	
		}
		
		if(selSim.equals("Particles")){
			isTOROIDAL = false;
			selClass = DParticles.class;
			selClassUI = DParticlesWithUI.class;
		}
		
		if(ip.equals("127.0.0.1"))
		try 
		{
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {e1.printStackTrace();}
		
		if(MODE == DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE){
		
			int cnt=0;
				for(String s : config.keySet()){
					int x = config.get(s).getNum();
					ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
					for(int j=0;j<x;j++){
						StartUpData data = new StartUpData();
						//data.graphic=false;
						data.graphic=config.get(s).isFlagTrue();
						if(cnt == NUM_PEERS/2)
							data.setStep(true);
						//data.setDef(DAntsForage.class);
						if(config.get(s).isFlagTrue()){
							data.setDef(selClassUI);
						}
						else{
							data.setDef(selClass);
						}
						data.setParam(new Object[]{ip,this.data.getPort(),MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEIGHT,0,cnt,DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,isTOROIDAL});
						classes.add(data);
						//data.graphic=false;
						cnt++;
						}
						try{
							connection.publishToTopic(classes, s, "classes");
						}catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		else{
			ArrayList<StartUpData> defs = new ArrayList<StartUpData>();
			for(int i=0;i<Math.sqrt(NUM_PEERS);i++){
				for(int k=0;k<Math.sqrt(NUM_PEERS);k++){
					StartUpData data = new StartUpData();
					if(i==k)
						data.setStep(true);
					//data.setDef(DAntsForage.class);
					data.setParam(new Object[]{ip,this.data.getPort(),MAX_DISTANCE,NUM_PEERS,NUM_AGENTS,WIDTH,HEIGHT,i,k,DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE,isTOROIDAL});
					defs.add(data);
					data.graphic=false;
					}
				}
			int index=0;
			for(String s : config.keySet()){
				ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
				int n = config.get(s).getNum();
				for(int i=0;i<n;i++){
					defs.get(index).graphic = config.get(s).isFlagTrue();
					if(config.get(s).isFlagTrue()){
						defs.get(index).setDef(selClassUI);
					}
					else{
						defs.get(index).setDef(selClass);
					}
					classes.add(defs.get(index));
					index++;
				}
				try{
					connection.publishToTopic(classes,s,"classes");	
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		/**CentralGuiState g = new CentralGuiState(new CentralSimState());
		Console c = (Console) g.createController();
		c.pressPause();*/
		}
}