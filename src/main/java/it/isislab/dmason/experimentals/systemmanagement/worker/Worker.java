/**

 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.systemmanagement.worker;


import it.isislab.dmason.experimentals.systemmanagement.utils.ClientSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.FindAvailablePort;
import it.isislab.dmason.experimentals.systemmanagement.utils.ServerSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.systemmanagement.utils.ZipDirectory;
import it.isislab.dmason.experimentals.systemmanagement.utils.loader.DMasonClassLoader;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.ConnectionType;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class Worker implements Observer {


	private String IP_ACTIVEMQ="";   
	private String PORT_ACTIVEMQ="";
	private int PORT_COPY_LOG;

	private int  slotsNumber=0; //number of available slots(cells of a field in dmason)
	private static int slotsNumberBackup=0; // a copy backup of slots number value for reconnection 

	private static final String MANAGEMENT="DMASON-MANAGEMENT";
	private static String dmasonDirectory=System.getProperty("user.dir")+File.separator+"dmason";
	private static  String workerDirectory; // worker main directory
	private static  String simulationsDirectories; // list of simulations' folder
	private String TOPIC_WORKER_ID=""; // worker's topic , worker write in this topic (publish) for all communication           
	private static String WORKER_IP="127.0.0.1";
	private int DEFAULT_COPY_SERVER_PORT=1414;
	private HashMap< Integer, Simulation> simulationList; //simulations' list of this worker
	private SimpleDateFormat sdf=null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;
	private FindAvailablePort availableport;


	//Socket for log services
	protected Socket sock=null;
	protected ServerSocket welcomeSocket;

	/// INTERNAL LOGGER FOR DEBUG 
	private static final Logger LOGGER=Logger.getLogger(Worker.class.getName()); //show constructor to enable Logger



	/**
	 * WORKER 
	 * @param ipMaster
	 * @param portMaster
	 * @param topicPrefix
	 */

	public Worker(String ipMaster,String portMaster, int slots/*, ConnectionNFieldsWithActiveMQAPI connect*/) {

		try {

			//comment below  line to enable Logger 
			LOGGER.setUseParentHandlers(false);  
			LOGGER.info("LOGGER ENABLE");
			//

			this.IP_ACTIVEMQ=ipMaster;
			this.PORT_ACTIVEMQ=portMaster;
			this.conn=new ConnectionNFieldsWithActiveMQAPI();
			WORKER_IP=getIP(); //set IP for this worker
			this.TOPIC_WORKER_ID="WORKER-"+WORKER_IP+"-"+new UID(); 
			/**
			 * @author miccar
			 * character ":" cause error in windows folder creation
			 * YOU MUST NOT REMOVE BELOW LINE OF CODE 
			 */
			TOPIC_WORKER_ID=TOPIC_WORKER_ID.replace(":", "");
			/******************/

			generateFolders(TOPIC_WORKER_ID); //generate folders for worker
			this.TOPIC_WORKER_ID=""+TOPIC_WORKER_ID.hashCode(); //my topic
			simulationList=new HashMap< /*idsim*/Integer, Simulation>();

			this.slotsNumber=slots;
			this.slotsNumberBackup=slots;

			availableport=new FindAvailablePort(1000, 3000);
			this.PORT_COPY_LOG=availableport.getPortAvailable(); //socket communication with master (server side, used for logs)
			welcomeSocket = new ServerSocket(PORT_COPY_LOG,1000,InetAddress.getByName(WORKER_IP)); //create server for socket communication 
			conn.addObserver(this); //EXPERIMENTAL 
			connectToMessageBroker();

		} catch (Exception e) {e.printStackTrace();}


	}

	private void connectToMessageBroker() throws UnknownHostException, IOException
	{
		System.out.println("Waiting for connection to Message Broker..");
		this.createConnection();

		System.out.println("Waiting master connection ..."); 

		this.startMasterComunication();

		System.out.println("connected.");
	}
	private boolean MASTER_ACK=false;
	private MasterLostChecker masterlost=null;
	final Lock lock = new ReentrantLock();
	final Condition waitMaster  = lock.newCondition(); 
	private MasterChecker masterchecker=null;

	private boolean CONNECTED=true;

	final Lock lockconnection = new ReentrantLock();
	final Condition waitconnection  = lockconnection.newCondition(); 

	/**
	 * EXPERIMENTAL
	 */
	public void update(Observable obs, Object arg) {

		if (obs==conn){
            System.exit(0); 
			if(!conn.isConnected()){
				this.simulationList=new HashMap< /*idsim*/Integer, Simulation>();
				this.slotsNumber=slotsNumberBackup;
				System.out.println("Waiting master connection ..."); 
			}
			if(conn.isConnected()){
				try {

					System.out.println("wco");
					conn.unsubscribe(MANAGEMENT);
					conn.createTopic(MANAGEMENT, 1);
					conn.unsubscribe(MANAGEMENT);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		}

		//		if (obs==conn){
		//
		//			if(!conn.isConnected()){
		//
		//				System.exit(0);
		//				CONNECTED=false;
		//				(new Thread(){
		//					public void run() {
		//
		//						try {
		//							masterchecker.interrupt();
		//							connectToMessageBroker();
		//							try {
		//
		//								lockconnection.lock();
		//								CONNECTED=true;
		//								waitconnection.signalAll();
		//
		//							}finally {
		//								lockconnection.unlock();
		//							}
		//
		//						} catch (IOException e) {
		//							e.printStackTrace();
		//						}
		//					};
		//				}).start();
		//
		//
		//				try {
		//					lockconnection.lock();
		//					while(!CONNECTED)
		//					{
		//						waitconnection.await();
		//
		//					}
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}finally {
		//					lockconnection.unlock();
		//				}
		//
		//			}
		//
		//		}
	}

	class MasterLostChecker extends Thread{
		public MasterLostChecker() {

		}
		@Override
		public void run() {

			while(true){
				try {
					Thread.sleep(new Random().nextInt(3)*1000 );
					getConnection().publishToTopic(getInfoWorker().toString(), MANAGEMENT,"WORKER");
				} catch (Exception e) {e.printStackTrace();}
			}

		}
	}

	class MasterChecker extends Thread{

		public MasterChecker() {}
		@Override
		public void run() {
			do
			{
				System.out.println("Start Master monitor...");
				if(masterlost==null)
				{
					masterlost=new MasterLostChecker();
					masterlost.start();
					System.out.println("done.");
					try {
						lock.lock();
						MASTER_ACK=false;
						while(!MASTER_ACK)
						{
							waitMaster.await();
						}
						if(masterlost!=null)
						{
							masterlost.interrupt();
							masterlost=null;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}finally{
						lock.unlock();
					}
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}while(true);

		}
	}


	@SuppressWarnings("serial")
	private void startMasterComunication() {

		try {
			conn.createTopic(MANAGEMENT, 1);
			conn.subscribeToTopic(MANAGEMENT);

			listenerForMasterComunication();
		} 
		catch (Exception e1) {e1.printStackTrace();}

		new MasterLostChecker().start();
		conn.asynchronousReceive(MANAGEMENT, new MyMessageListener() {
			@Override
			public void onMessage(Message msg) {

				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap map=(MyHashMap) o;
					if(map.containsKey("WORKER-ID-"+TOPIC_WORKER_ID)){
						///
						try {

							lock.lock();
							if(!MASTER_ACK)
							{
								MASTER_ACK=true;
								//System.out.println("Master connected...");
								DEFAULT_COPY_SERVER_PORT=(int)map.get("WORKER-ID-"+TOPIC_WORKER_ID);
								waitMaster.signalAll();
							}

						}finally{
							lock.unlock();
						}

					} 

				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		});

	} 	




	/**
	 * Return information of (sended to master)
	 * -hw resources(cpu,ram)
	 * -port of node for server socket
	 * -topic that identify this node
	 * -topic IP
	 * -slots still available for this node
	 */
	private WorkerInfo getInfoWorker() 
	{
		WorkerInfo info=new WorkerInfo();
		info.setIP(WORKER_IP);
		info.setWorkerID(this.TOPIC_WORKER_ID);
		info.setNumSlots(this.getSlotsNumber());
		info.setPortCopyLog(PORT_COPY_LOG);
		return info;

	}

	private boolean createConnection(){
		Address address=new Address(this.getIpActivemq(), this.getPortActivemq());
		return conn.setupConnection(address);

	}


	/**
	 * Subscribe to masters' topic  for communication [master->worker]
	 * Requests' list from master
	 */
	@SuppressWarnings("serial")
	private synchronized void listenerForMasterComunication(){
		try{
			getConnection().subscribeToTopic(TOPIC_WORKER_ID);
			getConnection().createTopic(TOPIC_WORKER_ID,1);
		}catch(Exception e){ e.printStackTrace();}


		getConnection().asynchronousReceive(TOPIC_WORKER_ID, new MyMessageListener() {

			@Override
			public void onMessage(Message msg) {

				Object o;
				try {
					o=parseMessage(msg);
					final MyHashMap map=(MyHashMap) o;


					// request of a storage a new simulation
					if(map.containsKey("newsim")){

						Simulation sim=(Simulation)map.get("newsim");
						createNewSimulationProcess(sim);
						//System.out.println("apro straem su porta "+DEFAULT_COPY_SERVER_PORT);
						downloadFile(sim,DEFAULT_COPY_SERVER_PORT);
						List<CellType> cellstype=sim.getCellTypeList();
						for (CellType cellType : cellstype) {
							slotsNumber--;
						}


					}else
						// request to start a simulation
						if (map.containsKey("start")){
							int id = (int)map.get("start");

							if( (getSimulationList().get(id).getStatus() )!= Simulation.FINISHED && 
									(getSimulationList().get(id).getStatus() )!= Simulation.STARTED)
								playSimulationProcessByID(id);

						}else
							//request to stop a simulation
							if (map.containsKey("stop")){
								int id = (int)map.get("stop");
								if( (getSimulationList().get(id).getStatus()) != Simulation.FINISHED)
									stopSimulation(id);
							}else
								//request to pause a simulation
								if (map.containsKey("pause")){
									int id = (int)map.get("pause");
									LOGGER.info("Command pause received for simulation "+id);
									if((getSimulationList().get(id).getStatus()!= Simulation.FINISHED) 
											&& (getSimulationList().get(id).getStatus()!= Simulation.PAUSED))
										pauseSimulation(id);
								}else
									//log request of a simulation
									if(map.containsKey("logreq")){
										int id=(int)map.get("logreq");
										LOGGER.info("Received request for logs for simid "+id);
										String pre_status=getSimulationList().get(id).getStatus();
										if((pre_status.equals(Simulation.STARTED))){ 
											pauseSimulation(id); 
											getLogBySimIDProcess(id,pre_status,"log");
										}  
										else{ //PAUSED
											LOGGER.info("invoke getlog con "+pre_status);
											getLogBySimIDProcess(id,pre_status,"log");
										}

									}else
										//request to remove a simulation
										if (map.containsKey("simrm")){
											int id = (int)map.get("simrm");
											LOGGER.info("Command remove received for simulation "+id);
											deleteSimulationProcessByID(id);
										}


				} catch (JMSException e) {e.printStackTrace();} 


			}
		});
	}


	private HashMap<Integer,ArrayList<CellExecutor>> executorThread=new HashMap<Integer,ArrayList<CellExecutor>>();

	/**
	 * Start Thread for simulation running
	 * @param sim_id
	 */
	private synchronized void runSimulation(int sim_id){

		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.start();
		}
		getSimulationList().get(sim_id).setStatus(Simulation.STARTED);
		getSimulationList().get(sim_id).setStartTime(System.currentTimeMillis());
		getSimulationList().get(sim_id).setStep(0);
		getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");


	}
	/**
	 * Start a simulation for first time, or a paused simulation -> when receive "start"
	 * start a simulation by id 
	 * @param id ID of simulation
	 */
	private	void playSimulationProcessByID(int id){
		try {
			if(getSimulationList().containsKey(id) && getSimulationList().get(id).getStatus().equals(Simulation.PAUSED))
			{  
				getSimulationList().get(id).setStatus(Simulation.STARTED);
				for(CellExecutor cexe:executorThread.get(id))
				{
					cexe.restartThread();
				}

				return;
			}

			GeneralParam params = null;
			Simulation simulation=getSimulationList().get(id);

			List<CellType> cellstype=simulation.getCellTypeList();
			int aoi=simulation.getAoi();
			int height= simulation.getHeight();
			int width= simulation.getWidth();
			int cols=simulation.getColumns();
			int rows=simulation.getRows();
			int agents=simulation.getNumAgents();
			int mode=simulation.getMode();
			int p=simulation.getP();
			@SuppressWarnings("unused")
			int typeConn=simulation.getConnectionType();

			System.err.println("TODO MANAGE CONNECTION MPI");
			long step=simulation.getNumberStep();

			params=(simulation.getMode()==DistributedField2D.UNIFORM_PARTITIONING_MODE)?
					new GeneralParam(width, height, aoi, rows, cols, agents, mode,step,ConnectionType.pureActiveMQ):
						new GeneralParam(width, height, aoi,p, agents, mode,step,ConnectionType.pureActiveMQ);
					params.setIp(IP_ACTIVEMQ);
					params.setPort(PORT_ACTIVEMQ);
					getSimulationList().put(simulation.getSimID(), simulation);
					executorThread.put(simulation.getSimID(),new ArrayList<CellExecutor>());
					for (CellType cellType : cellstype) {
						params.setI(cellType.pos_i);
						params.setJ(cellType.pos_j);
						FileOutputStream output = new FileOutputStream(simulation.getSimulationFolder()+File.separator+"out"+File.separator+cellType+".out");
						PrintStream printOut = new PrintStream(output);
						CellExecutor celle=(new CellExecutor(params,printOut,simulation.getSimID(),
								(cellstype.indexOf(cellType)==0?true:false)));

						executorThread.get(simulation.getSimID()).add(celle);
						celle.startSimulation();
						getConnection().publishToTopic(simulation.getSimID(),"SIMULATION_READY"+simulation.getSimID(), "cellready");

					}

					getConnection().createTopic("SIMULATION_"+simulation.getSimID(), 1);
					getConnection().publishToTopic(simulation,"SIMULATION_"+simulation.getSimID(), "workerstatus");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Start stop process for a sim -> when receive "stop" 
	 * @param sim_id id of simulation to stop 
	 */
	private synchronized void stopSimulation(int sim_id)
	{
		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			if(cexe.masterCell){ 
				getSimulationList().get(sim_id).setStatus(Simulation.STOPPED);	
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
			}
			cexe.stopThread();
		}
		//getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());

		//start process to create a log file for this simulation
		String pre_status=getSimulationList().get(sim_id).getStatus();
		getLogBySimIDProcess(sim_id,pre_status,"history");

	}
	/**
	 * Pause simulation process
	 * @param sim_id id of simulation to pause
	 */
	private synchronized void pauseSimulation(int sim_id){

		for(CellExecutor cexe:executorThread.get(sim_id))
		{
			cexe.pauseThread();
		}
		getSimulationList().get(sim_id).setStatus(Simulation.PAUSED);
		getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
		getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");

	}

	/*****************CELLEXECUTOR CLASS******************************/
	/**
	 * Class for start, stop, pause with Thread
	 *
	 */
	class CellExecutor extends Thread{

		public GeneralParam params;
		public String prefix;
		public String folder_sim;
		public String jar_pathname;
		private int sim_id;
		public boolean run=true;
		public boolean pause=false;
		public boolean masterCell=false;
		@SuppressWarnings("rawtypes")
		private DistributedState dis;

		final Lock lock = new ReentrantLock();
		final Condition isPause  = lock.newCondition(); 

		public CellExecutor(GeneralParam params,PrintStream out,int sim_id,boolean master_cell) {
			super();
			this.params = params;
			this.sim_id=sim_id;
			Simulation sim=getSimulationList().get(sim_id);
			this.prefix=sim.getTopicPrefix();//prefix;
			this.folder_sim=sim.getSimulationFolder();//folder_name;
			this.jar_pathname=sim.getJarPath();//jar_path;
			dis=makeSimulationWithNewLoader( params, prefix, jar_pathname);
			dis.setOutputStream(out);
			this.masterCell=master_cell;
		}

		public void startSimulation(){
			dis.start();

		}
		@Override
		public  void run() {
			LOGGER.info("Start cell for "+params.getMaxStep());
			int i=0;

			while(i!=params.getMaxStep() && run)
			{   
				if(i%500==0){LOGGER.info("STEP NUMBER "+dis.schedule.getSteps()+" for simid"+sim_id );}
				try{
					lock.lock();

					while(pause)
					{
						isPause.await();
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
				if(masterCell)
				{   
					getSimulationList().get(sim_id).setStep(i);
					getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
				}
				dis.schedule.step(dis);
				i++;

			}

			// simulation stopped 
			//			if( (i<params.getMaxStep()) && masterCell ){
			//				getSimulationList().get(sim_id).setStatus(Simulation.STOPPED);	
			//				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
			//				getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
			//			}

			//simulation finished             
			if(  (i==params.getMaxStep()) && masterCell) {
				getSimulationList().get(sim_id).setEndTime(System.currentTimeMillis());
				getSimulationList().get(sim_id).setStatus(Simulation.FINISHED);
				getConnection().publishToTopic(getSimulationList().get(sim_id),"SIMULATION_"+sim_id, "workerstatus");
				// process to create log file
				String pre_status=getSimulationList().get(sim_id).getStatus();
				getLogBySimIDProcess(sim_id,pre_status,"history");
			}



		}




		public synchronized void stopThread()
		{
			run=false;
		}
		public void restartThread() {
			try{
				lock.lock();

				pause=false;
				isPause.signalAll();

			}finally{
				lock.unlock();
			}
		}
		public void pauseThread() {
			try{
				lock.lock();

				pause=true;
				isPause.signalAll();

			}finally{
				lock.unlock();
			}
		}
	}

	/*****************CELLEXECUTOR CLASS******************************/



	/**
	 * Create a new sim execution process    
	 * @param sim
	 */
	@SuppressWarnings("serial")
	private synchronized void createNewSimulationProcess(Simulation sim){

		String path=this.createSimulationDirectoryByID(sim.getSimName()+""+sim.getSimID());
		sim.setSimulationFolder(path);
		getSimulationList().put(sim.getSimID(),sim);
		String createTopicSimReady="SIMULATION_READY"+sim.getSimID();
		getConnection().createTopic(createTopicSimReady, 1);
		try {
			getConnection().subscribeToTopic(createTopicSimReady);
		} catch (Exception e1) {e1.printStackTrace();}
		getConnection().asynchronousReceive(createTopicSimReady, new MyMessageListener() {

			public void onMessage(Message msg) {
				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap map=(MyHashMap) o;
					if(map.containsKey("cellready")){
						int sim_id=(int) map.get("cellready");

						getSimulationList().get(sim_id).setReceived_cell_type(getSimulationList().get(sim_id).getReceived_cell_type()+1);
						if(getSimulationList().get(sim_id).getReceived_cell_type()==getSimulationList().get(sim_id).getNumCells())
						{
							runSimulation(sim_id);
						}
					} 


				} catch (JMSException e) {e.printStackTrace();}

			}
		});	

	}

	/**
	 * Create a folder for a Simulation
	 * @param name name of folder 
	 * @return path of created folder
	 */
	private String createSimulationDirectoryByID(String  name){
		String path=simulationsDirectories+File.separator+name;
		DMasonFileSystem.make(path+File.separator+"out");
		return path;
	}

	/**
	 * Start process for remove a simulation
	 * @param simID id of Simulation
	 */
	public  synchronized void deleteSimulationProcessByID(int simID){
		Simulation s =getSimulationList().get(simID); 
		String folder=s.getSimulationFolder();
		DMasonFileSystem.delete(new File(folder));
		List<CellType> cellstype=s.getCellTypeList();
		for (CellType cellType : cellstype) {
			slotsNumber++;
		}
		getSimulationList().remove(simID);
	}




	/**
	 * Download with Socket the jar of a sim from master 
	 * @param sim
	 * @param serverSocketPort
	 */
	private synchronized void downloadFile(Simulation sim,int serverSocketPort){ 

		String jarName=System.currentTimeMillis()+".jar";

		String localJarFilePath=sim.getSimulationFolder()+File.separator+jarName;
		sim.setJarPath(localJarFilePath);
		Socket clientSocket;
		try {
			clientSocket = new Socket( this.IP_ACTIVEMQ , serverSocketPort );
			Thread tr=null;
			System.out.println("copy in "+localJarFilePath);
			tr=new Thread(new ClientSocketCopy(clientSocket, localJarFilePath));
			tr.start();
			tr.join();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.getConnection().publishToTopic(sim.getSimID(), TOPIC_WORKER_ID, "simrcv");

	}

	/**
	 * Get Distributed state instance of simulation from jar file	 * @param params
	 * @param prefix
	 * @param pathJar
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unused", "resource" })
	@Deprecated
	private DistributedState makeSimulation(GeneralParam params, String prefix,String pathJar)
	{
		String path_jar_file=pathJar;
		try{
			JarFile jar=new JarFile(new File(path_jar_file));
			Enumeration e=jar.entries();
			File file  = new File(path_jar_file);
			URL url = file.toURL(); 
			URL[] urls = new URL[]{url};
			ClassLoader cl = new URLClassLoader(urls);
			Class distributedState=null;
			while(e.hasMoreElements()){

				JarEntry je=(JarEntry)e.nextElement();
				if(!je.getName().contains(".class")) continue;

				Class c=cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class))
					distributedState=c;

			}
			if(distributedState==null) return null;
			//JarClassLoader cload = new JarClassLoader(new URL("jar:file://"+path_jar_file+"!/"));
			JarClassLoader cload = new JarClassLoader(new File(path_jar_file).toURI().toURL());
			cload.addToClassPath();
			return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;

	}


	/**
	 * Get Distributed state instance of simulation from jar file and 
	 * avoid dynamically classloading problem  of the same path from multiple jar
	 * @param params
	 * @param prefix
	 * @param pathJar
	 * @return
	 */
	private DistributedState makeSimulationWithNewLoader(GeneralParam params, String prefix,String pathJar){

		String path_jar_file=pathJar;
		try{

			JarFile jar=new JarFile(new File(path_jar_file));
			Enumeration e=jar.entries();
			File file  = new File(path_jar_file);
			String u = file.toURI().toURL().toString(); 
			URL url=new URL(u);
			URL[] urls = new URL[]{url};

			URLClassLoader aUrlCL = new URLClassLoader(urls, new DMasonClassLoader());
			Thread.currentThread().setContextClassLoader(aUrlCL);
			Class<?> distributedState=null;

			while(e.hasMoreElements()){

				JarEntry je=(JarEntry)e.nextElement();
				if(!je.getName().contains(".class")) continue;

				Class<?> c=aUrlCL.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class)){
					distributedState=c;
				}

			}
			if(distributedState==null) return null;

			Class<?> urlClass = aUrlCL.getClass();//URLClassLoader.class;////

			Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(aUrlCL, new Object[]{url});;

			Class<?> simClass = aUrlCL.loadClass(distributedState.getName());
			Constructor<?> constr = simClass.getConstructor(new Class[]{ GeneralParam.class ,String.class});
			Object obj = constr.newInstance(new Object[]{ params ,prefix});

			return (DistributedState) obj;

		} catch (Exception e){
			e.printStackTrace();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		return null;
	}


	/**
	 * Create log for a Simulation 
	 * @param simID
	 * @param status log(a request of log) | history(a request of a finished simulation)
	 * @param type simple log or log for  history 
	 */
	public synchronized void getLogBySimIDProcess(int simID,String status, String type){
		Simulation sim=getSimulationList().get(simID);
		String folderToCopy=sim.getSimulationFolder()+File.separator+"out";
		String fileToSend=sim.getSimulationFolder()+File.separator+"out"+File.separator+this.TOPIC_WORKER_ID+".zip";

		if(type.equals("log")){
			if(getLogBySimID(folderToCopy,fileToSend)){
				System.out.println("Zip File created to "+fileToSend);			
				if(status.equals(Simulation.STARTED))playSimulationProcessByID(simID);

				if(startServiceCopyForLog(fileToSend,simID,"logready"))
					DMasonFileSystem.delete(new File(fileToSend));
			}
		}

		else{ //type.equals("history")

			if(getLogBySimID(folderToCopy,fileToSend)){

				if(startServiceCopyForLog(fileToSend,simID,"loghistory")){
					System.out.println("Zip File created to "+fileToSend);
					DMasonFileSystem.delete(new File(fileToSend));
				}	
			}
		}	
	} 





	/**
	 * Start Server with socket to send log
	 * @param zipFile
	 * @param id
	 * @param type logready || loghistory
	 * @return
	 */
	private boolean startServiceCopyForLog(String zipFile,int id,String type){
		LOGGER.info("open stream copy of "+zipFile+" on port "+welcomeSocket.getLocalPort());
		getConnection().publishToTopic(id, TOPIC_WORKER_ID, type);
		try{
			Thread t=null;
			LOGGER.info("accept block");
			sock = welcomeSocket.accept();
			LOGGER.info("exit accept ");
			t=new Thread(new ServerSocketCopy(sock,zipFile));
			t.start();
			t.join();
			sock.close();

		}catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {welcomeSocket.close();} 
				catch (IOException exx){exx.printStackTrace(System.err); return false;}
			}
		} catch (InterruptedException e) {e.printStackTrace();}


		return true;
	}



	private boolean getLogBySimID(String folderToCopy, String zipPath){
		//System.out.println("Copy file from folder "+folderToCopy+" to "+zipPath);
		return ZipDirectory.createZipDirectory(zipPath, folderToCopy);	   

	}




	/**
	 * Create all folder for worker's environment 
	 * @param wID
	 * @throws FileNotFoundException
	 */
	private void generateFolders(String wID) throws FileNotFoundException {
		sdf = new SimpleDateFormat(); 
		sdf.applyPattern("dd-MM-yy-HH_mm");
		String dataStr = sdf.format(new Date()); // data corrente (20 febbraio 2014)
		workerDirectory=dmasonDirectory+File.separator+"worker"+File.separator+wID+File.separator+dataStr;
		//workerTemporary=workerDirectory+File.separator+"temporary";
		simulationsDirectories=workerDirectory+File.separator+"simulations";
		//DMasonFileSystem.make(workerTemporary);
		DMasonFileSystem.make(simulationsDirectories);
		DMasonFileSystem.make(workerDirectory+File.separator+"err");
		FileOutputStream output = new FileOutputStream(workerDirectory+File.separator+"err"+File.separator+"worker"+TOPIC_WORKER_ID+".err");
		PrintStream printOut = new PrintStream(output);
		System.setErr(printOut);
	}



	/**
	 * Explores NetworkInterface and finds IP Address 
	 * @return ip of Worker
	 */
	private static String getIP() {

		try {     
			String c=InetAddress.getByName("localhost").getHostAddress();
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();)
			{
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				for (; a.hasMoreElements();){
					InetAddress addr = a.nextElement();
					String p=addr.getHostAddress();
					if(p.contains(".") && p.compareTo(c)!=0)
						return p;
				}
			}
		} 
		catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		return WORKER_IP;
	} 


	//getters and setters
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public ConnectionNFieldsWithActiveMQAPI getConnection() {return conn;}
	public String getSimulationsDirectories() {return simulationsDirectories;}
	public synchronized Integer getSlotsNumber(){return slotsNumber;}
	public synchronized int setSlotsNumuber(int slots){return this.slotsNumber=slots;}
	public HashMap<Integer, Simulation> getSimulationList(){return simulationList;}




}
