package dmason.util.SystemManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

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
	/**
	 * The number of peers involved in the simulation.
	 */
	private int numPeers;
	
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

	/**
	 * Constructor.
	 * @param conn Connection with a provider.
	 * @throws Exception
	 */
	public MasterDaemonStarter(Connection conn) throws Exception
	{
		connection = (ConnectionNFieldsWithActiveMQAPI)conn;
		address = connection.getAdress();
	}

	public boolean connectToServer()
	{
		try
		{
			if(connection.createTopic(myTopic,1)==true)
			{
				if(connection.subscribeToTopic(myTopic)==true)
					connection.asynchronousReceive(myTopic, myml = new MasterDaemonListener());
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
	public ArrayList<String> getTopicList() throws Exception{
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

	public PeerStatusInfo getLatestUpdate(String key){
		return myml.getLatestUpdate(key);
	}

	public void info(String key)throws Exception{
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
	public void stop() throws Exception
	{
		for(String topicName : workerTopics)
			connection.publishToTopic("stop", topicName, "stop");
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


	public void start(int num,int width,int height,int agents,int maxDistance,int mode,HashMap<String,EntryVal<Integer, Boolean>> config, String selSim,JMasterUI gui){
		this.numPeers = num;
		this.numAgents = agents;
		this.width = width;
		this.height = height;
		this.fieldMode = mode;
		this.jumpDistance = maxDistance;
		String ip = this.address.getIPaddress();
		Class selClassUI = null;
		Class selClass = null;	
//		String curDir=System.getProperty("user.dir")+"/dmason/sim/app/"+selSim;
//		String packagePath="dmason.sim.app."+selSim+".";
//		File simulationList =new File(curDir);
//		for(File fileSimulation : simulationList.listFiles()){
//			if(fileSimulation.isFile()  && fileSimulation.getName().contains(".java"))
//				if(fileSimulation.getName().contains("WithUI")){
//
//					try {
//						
//						selClassUI=Class.forName(packagePath+""+fileSimulation.getName().split(".java")[0]);
//					    System.out.println(packagePath+""+fileSimulation.getName().split(".java")[0]);
//					} catch (ClassNotFoundException e) {
//						System.err.println("Unable to create class with UI :"+packagePath+fileSimulation.getName());
//						e.printStackTrace();
//					}
//
//
//				}
//				
//				else{
//					if(fileSimulation.getName().split(".java")[0].equals(selSim)){
//						try {
//							selClass=Class.forName(packagePath+""+fileSimulation.getName().split(".java")[0]);
//							System.out.println("2"+packagePath+""+fileSimulation.getName().split(".java")[0]);
//						
//						} catch (ClassNotFoundException e) {
//							System.err.println("Unable to create class :"+packagePath+fileSimulation.getName());
//							e.printStackTrace();
//						}
//					}
//
//				}
//
//		}

		// Select the class to send to workers		
		if (selSim.equals("DFlockers")){
			selClass = DFlockers.class;
			selClassUI =DFlockersWithUI.class;
		} else if(selSim.equals("DAntsForage")){
			selClass = DAntsForage.class;
			selClassUI = DAntsForageWithUI.class;	
		}
		if(selSim.equals("DParticles")){
			selClass = DParticles.class;
			selClassUI =DParticlesWithUI.class;
		}
		
		//
		if(ip.equals("127.0.0.1"))
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
			for(String s : config.keySet()){
				int x = config.get(s).getNum();
				ArrayList<StartUpData> classes = new ArrayList<StartUpData>();
				for(int j=0; j < x; j++){
					StartUpData data = new StartUpData();
					//data.graphic=false;
					data.graphic=config.get(s).isFlagTrue();
					if(cnt == numPeers/2)
						data.setStep(true);
					//data.setDef(DAntsForage.class);
					if(config.get(s).isFlagTrue()){
						data.setDef(selClassUI);
					}
					else{
						data.setDef(selClass);
					}
					data.setParam(new Object[]{ip,this.address.getPort(),jumpDistance,numPeers,numAgents,width,height,0,cnt,DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE});
					classes.add(data);
					//data.graphic=false;
					cnt++;
				}
				try{


					connection.publishToTopic(classes, s, "classes");

					//JOptionPane.showMessageDialog(null,"Setting completed !");
					gui.getTextFieldAgents().setEnabled(false);
					gui.getTextFieldHeight().setEnabled(false);
					gui.getTextFieldWidth().setEnabled(false);
					gui.getTextFieldMaxDistance().setEnabled(false);
					gui.getRadioButtonHorizontal().setEnabled(false);
					gui.getRadioButtonSquare().setEnabled(false);
					gui.getjComboRegions().setEnabled(false);
				}



				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else // (mode == DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE)
		{
			ArrayList<StartUpData> defs = new ArrayList<StartUpData>();
			for(int i=0;i<Math.sqrt(numPeers);i++){
				for(int k=0;k<Math.sqrt(numPeers);k++){
					StartUpData data = new StartUpData();
					if(i==k)
						data.setStep(true);
					//data.setDef(DAntsForage.class);
					data.setParam(new Object[]{ip,this.address.getPort(),jumpDistance,numPeers,numAgents,width,height,i,k,DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE});
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
					if(connection.publishToTopic(classes, s, "classes")==true){

						gui.getTextFieldAgents().setEditable(false);
						gui.getTextFieldHeight().setEditable(false);
						gui.getTextFieldWidth().setEditable(false);
						gui.getTextFieldMaxDistance().setEditable(false);
						gui.getRadioButtonHorizontal().setEnabled(false);
						gui.getRadioButtonSquare().setEnabled(false);
						gui.getjComboRegions().setEditable(false);

					}
					else
						JOptionPane.showMessageDialog(null,"Setting failed !");

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
			/*CentralGuiState g = new CentralGuiState(new CentralSimState());
		Console c = (Console) g.createController();
		c.pressPause();*/
		}
}