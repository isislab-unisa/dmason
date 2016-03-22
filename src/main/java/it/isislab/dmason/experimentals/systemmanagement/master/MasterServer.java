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
package it.isislab.dmason.experimentals.systemmanagement.master;


import it.isislab.dmason.experimentals.systemmanagement.utils.ClientSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.ServerSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.FindAvailablePort;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.systemmanagement.utils.ZipDirectory;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.activemq.broker.BrokerService;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class MasterServer implements MultiServerInterface{


	//default 127.0.0.1:61616 else you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH="resources/systemmanagement/master/conf/config.properties";

	//connection and topic
	private static final String MASTER_TOPIC="MASTER";
	private  String IP_ACTIVEMQ="";
	private  String PORT_ACTIVEMQ="";
	private int DEFAULT_PORT_COPY_SERVER;
	private BrokerService broker=null;
	private Properties startProperties = null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;

	//path directories 
	private static String dmasonDirectory=System.getProperty("user.dir")+File.separator+"dmason";
	private static final String masterDirectoryFolder=dmasonDirectory+File.separator+"master";
	private static final String masterTemporaryFolder=masterDirectoryFolder+File.separator+"temporay";
	private static final String masterHistoryFolder=masterDirectoryFolder+File.separator+"history";
	private static final String simulationsDirectoriesFolder=masterDirectoryFolder+File.separator+"simulations";



	//copyserver
	protected Socket sock=null;
	protected ServerSocket welcomeSocket;

	//info 
	protected HashMap<String/*IDprefixOfWorker*/,String/*MyIDTopicprefixOfWorker*/> topicIdWorkers;
	//protected HashMap<Integer,ArrayList<String>> topicIdWorkersForSimulation;
	protected HashMap<Integer,AtomicInteger> counterAckSimRcv;//cnto numero di ack ricevuti per le simrcv dai newsim per la submit
	public HashMap<String,String> infoWorkers;
	public HashMap<String,String> support_infoWorkers;
	private HashMap<Integer,Simulation> simulationsList; //list simulation 
	private AtomicInteger keySimulation;

	//copy logs
	private HashMap<String /*workertopicforrequest*/, Address /*portcopyLog*/> workerListForCopyLogs=new HashMap<String,Address>();


	public AtomicInteger getKeySim(){
		return keySimulation;
	}


	/**
	 * @param infoWorkers the infoWorkers to set
	 */
	protected void setInfoWorkers(HashMap<String, String> infoWorkers) {
		this.infoWorkers = infoWorkers;
	}



	/**
	 * start activemq, initialize master connection, create directories and create initial topic for workers
	 */
	public MasterServer(){
		startProperties = new Properties();
		broker = new BrokerService();
		conn=new ConnectionNFieldsWithActiveMQAPI();

		DMasonFileSystem.make(masterDirectoryFolder);// master
		DMasonFileSystem.make(masterTemporaryFolder);//temp folder
		DMasonFileSystem.make(masterHistoryFolder); //master/history
		DMasonFileSystem.make(simulationsDirectoriesFolder+File.separator+"jobs"); //master/simulations/jobs

		this.loadProperties();
		this.startActivemq();
		this.createConnection();
		this.createInitialTopic(MASTER_TOPIC);

		//topicPrefix of connected workers  
		this.topicIdWorkers=new HashMap<String,String>();
		//this.topicIdWorkersForSimulation=new HashMap<>();
		this.infoWorkers=new HashMap<String,String>();
		support_infoWorkers = new HashMap<>();
		this.counterAckSimRcv=new HashMap<Integer,AtomicInteger>();
		//waiting for workers connecetion	
		this.listenForSignRequest();

		this.keySimulation=new AtomicInteger(0);
		simulationsList=new HashMap<>();
		try {
			DEFAULT_PORT_COPY_SERVER=FindAvailablePort.getPortAvailable();
			//System.out.println("copy server start on port "+DEFAULT_PORT_COPY_SERVER);
			welcomeSocket = new ServerSocket(DEFAULT_PORT_COPY_SERVER,1000,InetAddress.getByName(this.IP_ACTIVEMQ));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	//
	/**
	 *send a check message to worker on <topic topicworker>
	 *If worker is active, it responds on his topic with key "info" 
	 * with a info message of worker 
	 * 
	 * @param topicWorker the topic of info-request 
	 */
	protected void checkWorker(String topicWorker){

		getConnection().publishToTopic("", getTopicIdWorkers().get(topicWorker), "check");


	}

	/**
	 * Check if all workers connected is on 
	 */
	public void checkAllConnectedWorkers(){
		infoWorkers = support_infoWorkers;
		support_infoWorkers = new HashMap<>();

		for (String topic : getTopicIdWorkers().keySet()) {
			checkWorker(topic);
		}
	}




	/**
	 * Listen for new Worker connection
	 */
	public void listenForSignRequest(){

		final MasterServer master=this.getMasterServer();

		master.getConnection().asynchronousReceive("READY", new MyMessageListener() {

			@Override
			public void onMessage(Message msg) {
				Object o;
				try {
					o=parseMessage(msg);
					MyHashMap mh = (MyHashMap)o;

					for ( Entry<String, Object> string : mh.entrySet()) {

						//se ricevo una richiesta di sottoscrizione salvo e mi sottoscrivo al topic del worker
						if(mh.containsKey("signrequest")){

							String topicOfWorker=string.getValue().toString(); 
							master.processSignRequest(topicOfWorker);

						}



					}

				} catch (JMSException e) {e.printStackTrace();} 
			}
		});
	}

	/**
	 * When a sign request is detected by a worker
	 * 1. create a topic for communication master-> worker
	 * 2. listening on topic worker for communication worker->master 
	 *  
	 * @param topicOfWorker worker->master 
	 */
	private void processSignRequest(String topicOfWorker){


		try {
			//subscribe topic worker
			getConnection().subscribeToTopic(topicOfWorker);


			//create an univocal prefix  for  master->worker 1-1 communication
			final String myTopicForWorker=""+topicOfWorker.hashCode();

			getTopicIdWorkers().put(topicOfWorker,myTopicForWorker);
			getConnection().createTopic(myTopicForWorker, 1);
			getConnection().subscribeToTopic(myTopicForWorker);

			//send to worker prefix master->worker for communication
			getConnection().publishToTopic(myTopicForWorker, "MASTER", topicOfWorker);

			this.getConnection().publishToTopic(DEFAULT_PORT_COPY_SERVER, "MASTER", "port");
			//listening on topic worker
			getConnection().asynchronousReceive(topicOfWorker, new MyMessageListener() {

				//messages received from workers
				public void onMessage(Message msg) {

					Object o;
					try {
						o=parseMessage(msg);
						MyHashMap map=(MyHashMap) o;
                        
						//sim(id) downloaded from the worker
						if(map.containsKey("simrcv")){
							int id=(int) map.get("simrcv");
							AtomicInteger value=getCounterAckSimRcv().get(id);
							int temp=value.incrementAndGet();
							getCounterAckSimRcv().put(id, new AtomicInteger(temp));//used as a flag, i'm sure that all jars have been downloaded

						}

						//response to master info req
						if(map.containsKey("info")){
							String infoReceived=""+map.get("info");
							support_infoWorkers.put(myTopicForWorker, infoReceived);
							infoWorkers.putAll(support_infoWorkers);
							//infoWorkers.put(myTopicForWorker, infoReceived);
							processInfoForCopyLog(infoReceived,topicOfWorker);

						}
						// response to master logs req	
						if(map.containsKey("logready")){
							int simID=(int) map.get("logready");
							System.out.println("start copy of logs for sim id "+simID);
							downloadLogsForSimulationByID(simID,getTopicIdWorkers().get(topicOfWorker),false);
						}
						// response to master logs req(when a sim is stopped )
						if(map.containsKey("loghistory")){
							int simID=(int) map.get("loghistory");
							System.out.println("start copy of logs history for sim id "+simID);
							downloadLogsForSimulationByID(simID,getTopicIdWorkers().get(topicOfWorker),true);
						}



					} catch (JMSException e) {
						e.printStackTrace();
					}

				}
			});

		} 
		catch (Exception e){e.printStackTrace();}

	}



	/**
	 * Download logs with socket 
	 * @param simID id of simulation 
	 * @param topicOfWorker my topic for this worker
	 * @param removeSimulation true if is a history req, false otherwise
	 */
	private synchronized void downloadLogsForSimulationByID(int simID,String topicOfWorker,boolean removeSimulation){

		Simulation sim=getSimulationsList().get(simID);
		String folderCopy=sim.getSimulationFolder()+File.separator+"runs";
		String fileCopy=folderCopy+File.separator+topicOfWorker+".zip";
		Address address= workerListForCopyLogs.get(topicOfWorker);
		String iplog= address.getIPaddress().replace("\"", "");
		int port = Integer.parseInt(address.getPort());

		Socket clientSocket;
		try {
			//System.out.println("Download from "+iplog+":"+port);
			clientSocket = new Socket( iplog ,port );
			Thread tr=null;
			tr=new Thread(new ClientSocketCopy(clientSocket, fileCopy));
			tr.start();
			tr.join();

			Thread t=new Thread(new Runnable() {

				@Override
				public void run() {
					ZipDirectory.unZipDirectory(fileCopy, folderCopy);
				}
			});
			t.start();
			t.join();


			DMasonFileSystem.delete(new File(fileCopy));

			if(removeSimulation){
				getConnection().publishToTopic(simID, topicOfWorker, "simrm");
				getSimulationsList().get(simID).getTopicList().remove(topicOfWorker);
                
				//i must know last process of history download  
				if(getSimulationsList().get(simID).getTopicList().size()==0){
					removeSimulationProcessByID(simID);
				}

			}	


		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}



	public synchronized boolean createZipForHistory(int sim_id){

		Simulation s = this.getSimulationsList().get(sim_id);		
		String log_path=s.getSimulationFolder()+File.separator+"runs";
		String filePath = this.getMasterTemporaryFolder()+File.separator+s.getSimName()+sim_id+".zip";

		return ZipDirectory.createZipDirectory(filePath, log_path);
	}



	/**
	 * 
	 * @param info
	 * @param topicOfWorker
	 */
	private synchronized void processInfoForCopyLog(String info,String topicOfWorker){

		//parse message to get ip e port for logs
		String [] split=info.split(",");

		String iPaddress="";
		String port="";

		for(String x: split){
			if(x.contains("ip")) 
				iPaddress=x.split(":")[1];
			if(x.contains("portcopylog"))
				port=x.split(":")[1];


		}
		String topic=getTopicIdWorkers().get(topicOfWorker);
		Address address=new Address(iPaddress, port);
		workerListForCopyLogs.put(/*topic per inviare al worker*/topic, address);
	}

	/**
	 * 
	 * Create directory for a simulation 
	 * @param simID name of directory to create
	 */
	public void createSimulationDirectoryByID(String simName,int simID){
		String createNameFolder=simName+simID;
		String path=simulationsDirectoriesFolder+File.separator+createNameFolder+File.separator+"runs";
		DMasonFileSystem.make(path);
	}




	/**
	 *
	 * Delete a directory for a simulation
	 * @param simID name of a directory to delete
	 */
	public synchronized void removeSimulationProcessByID(int simID){
		String status=getSimulationsList().get(simID).getStatus();
		if(status.equals(Simulation.CREATED)){
			for(String topic: getSimulationsList().get(simID).getTopicList())
				getConnection().publishToTopic(simID, topic, "simrm");
		}

		String folder=getSimulationsList().get(simID).getSimulationFolder();
		String folderCopy=folder+File.separator+"runs";

		if(getSimulationsList().get(simID).getStatus().equals(Simulation.FINISHED))
			createCopyInHistory(folderCopy,simID);

		DMasonFileSystem.delete(new File(folder));
		getSimulationsList().remove(simID);
	}


	/**
	 * 
	 * @param port
	 * @param jarFile
	 * @param stopParam
	 */
	private boolean invokeCopyServer(String jarFile,int stopParam){

		int checkControl=stopParam;

		int counter=0;
		try {
			ArrayList<Thread> threads=new ArrayList<Thread>();
			Thread t=null;
			while (counter<checkControl) {
				sock = welcomeSocket.accept();
				counter++;
				t=new Thread(new ServerSocketCopy(sock,jarFile));
				t.start();
				threads.add(t);
			}
			for(Thread a: threads){
				a.join();
			}
			return true;

		} 
		catch (UnknownHostException e) {e.printStackTrace();} catch (IOException e) {
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {welcomeSocket.close();} 
				catch (IOException exx){exx.printStackTrace(System.err); return false;}
			}
		} catch (InterruptedException e) {
			e.printStackTrace(System.err);
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {welcomeSocket.close();} 
				catch (IOException exx){exx.printStackTrace(System.err); return false;}
			}}

		return false;
	}


	//start ActivemQ service
	private void startActivemq(){
		String address="tcp://"+IP_ACTIVEMQ+":"+PORT_ACTIVEMQ;
		try {
			broker.addConnector(address);
			broker.start();
		} catch (Exception e1) {e1.printStackTrace();}
	}

	/**
	 * Load properties from file path
	 */
	private void loadProperties(){

		InputStream input=null;
		//load params from properties file 
		try {
			input=new FileInputStream(PROPERTIES_FILE_PATH);	
			startProperties.load(input);
			this.setIpActivemq(startProperties.getProperty("ipmaster"));
			this.setPortActivemq(startProperties.getProperty("portmaster"));
		} catch (IOException e2) {
			System.err.println(e2.getMessage());
		}finally{try {input.close();} catch (IOException e) {System.err.println(e.getMessage());}}
	}

	private boolean createConnection(){
		Address address=new Address(IP_ACTIVEMQ, PORT_ACTIVEMQ);
		return conn.setupConnection(address);

	}



	private void createInitialTopic(String topic){


		try {
			conn.createTopic(topic, 1);
			conn.subscribeToTopic(topic);
			conn.subscribeToTopic("READY");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 
	 */
	private HashMap<String, Integer> slotsAvailableForSimWorker(ArrayList<String> topicWorkers, HashMap<String, String> listAllWorkers){

		HashMap<String,Integer> slotsForWorkers=new HashMap<String,Integer>();

		//riempo una hashmap con topic-numcell per i worker della sim
		for(String topicToFind: topicWorkers ){
			String numcells=listAllWorkers.get(topicToFind).split(",")[0].split(":")[1];
			slotsForWorkers.put(topicToFind, Integer.parseInt(numcells));
		}

		return slotsForWorkers;
	}



	private HashMap<String , List<CellType>> assignCellsToWorkers(HashMap<String, Integer> slots,Simulation simul){

		HashMap<String/*idtopic*/, List<CellType>> workerlist = new HashMap<String, List<CellType>>(); 


		int mode=simul.getMode();
		int LP=simul.getP();
		int rows=(int) (mode==0?simul.getRows(): Math.ceil(Math.sqrt(LP/*get LP from geneoparam*/))); 
		int cols=(int) (mode==0?simul.getColumns()/*getfrom genparm*/: Math.ceil(Math.sqrt(LP/*get LP from geneoparam*/))); 

		LP=mode==DistributedField2D.UNIFORM_PARTITIONING_MODE?rows*cols:LP/*get from gene parame*/;

		int assignedLP=LP;

		if(mode==DistributedField2D.UNIFORM_PARTITIONING_MODE){  
			workerlist= divideForUniform(rows,cols,slots,workerlist,assignedLP);

		}else if (mode==DistributedField2D.NON_UNIFORM_PARTITIONING_MODE){ //non uniform
			workerlist=divideForNonUniform( LP, slots, workerlist,assignedLP);

		}				

		return workerlist;
	}



	private HashMap<String/*idtopic*/, List<CellType>> divideForUniform(int rows,int cols,HashMap<String, Integer> slots,HashMap<String/*idtopic*/, List<CellType>> workerlist, int assignedLP){
		ArrayList<String> workerID=new ArrayList<String>(slots.keySet());
		int w=0;
		int lastIndex=-1;
		boolean goNext=false;
		for(int i=0; i < rows; i++){
			goNext=false; lastIndex=-1;
			for(int j=0; j < cols;){

				if(slots.get(workerID.get(w)) > 0)
				{
					slots.put(workerID.get(w), slots.get(workerID.get(w))-1);
					List<CellType> cells=workerlist.get(workerID.get(w))==null?new ArrayList<CellType>():workerlist.get(workerID.get(w));
					cells.add(new CellType(i,j));
					workerlist.put(workerID.get(w),cells);
					assignedLP--;
					goNext=true;

				}
				if(goNext){
					j++;
					goNext=false;
					lastIndex=-1;
				}
				else{
					if(lastIndex==w)
						System.err.println("errore"); 
					//						throw new DMasonException("Error! Not enough slots on the workers for the given partitioning.");

					if(lastIndex==-1) lastIndex=w;
				}
				w=(w+1)%slots.size();
				if(assignedLP < 1) break;
			}
		}
		return workerlist;
	}

	private HashMap<String/*idtopic*/, List<CellType>> divideForNonUniform(int LP,HashMap<String, Integer> slots,HashMap<String/*idtopic*/, List<CellType>> workerlist, int assignedLP){

		ArrayList<String> workerID=new ArrayList<String>(slots.keySet());
		int w=0;
		int lastIndex=-1;
		boolean goNext=false;
		for(int i=0; i < LP; i++){
			goNext=false; lastIndex=-1;
			if(slots.get(workerID.get(w)) > 0)
			{
				slots.put(workerID.get(w), slots.get(workerID.get(w))-1);
				List<CellType> cells=workerlist.get(workerID.get(w))==null?new ArrayList<CellType>():workerlist.get(workerID.get(w));
				cells.add(new CellType(0,i));
				workerlist.put(workerID.get(w),cells);
				assignedLP--;
				goNext=true;

			}
			if(goNext){
				goNext=false;
				lastIndex=-1;
			}
			else{
				if(lastIndex==w)
					System.err.println("errore");
				//						throw new DMasonException("Error! Not enough slots on the workers for the given partitioning.");

				if(lastIndex==-1) lastIndex=w;
			}
			w=(w+1)%slots.size();
			if(assignedLP < 1) break;


		}				
		return workerlist;
	}


	protected boolean validateSimulationJar(String pathJar)
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
				String classPath = je.getName();
				if(!je.getName().contains(".class")) continue;

				String[] nameclass = classPath.split("/");
				nameclass[0]=((nameclass[nameclass.length-1]).split(".class"))[0];

				byte[] classBytes = new byte[(int) je.getSize()];
				InputStream input = jar.getInputStream(je);
				BufferedInputStream readInput=new BufferedInputStream(input);

				Class c=cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class))
					distributedState=c;

			}
			if(distributedState==null) return false;
			JarClassLoader cload = new JarClassLoader(new URL("jar:file://"+path_jar_file+"!/"));

			cload.addToClassPath();

			return true;
		} catch (Exception e){
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * 
	 * @param sim
	 * @return
	 */
	public synchronized boolean submitSimulation(Simulation sim) {
		final Simulation simul=sim;

		getSimulationsList().put(simul.getSimID(), simul);
		//this.topicIdWorkersForSimulation.put(simul.getSimID(), simul.getTopicList());
		HashMap<String, Integer> slotsAvalaible=slotsAvailableForSimWorker(simul.getTopicList(),infoWorkers);
		HashMap<String, List<CellType>> assignmentToworkers=assignCellsToWorkers(slotsAvalaible, simul);

		if(assignmentToworkers==null) {return false;}

		getCounterAckSimRcv().put(simul.getSimID(), new AtomicInteger(0));

		for (String topicName: assignmentToworkers.keySet()){
			simul.setListCellType(assignmentToworkers.get(topicName));
			getConnection().publishToTopic(simul, topicName, "newsim");
		}

		String pathJar=simul.getSimulationFolder()+File.separator+sim.getJarName();

		if(!validateSimulationJar(pathJar)) return false;


		try {
			getConnection().createTopic("SIMULATION_"+sim.getSimID(), 1);
			getConnection().subscribeToTopic("SIMULATION_"+sim.getSimID());

			conn.asynchronousReceive("SIMULATION_"+sim.getSimID(), new MyMessageListener() {

				@Override
				public void onMessage(Message msg) {

					Object o;
					try {
						o=parseMessage(msg);
						MyHashMap map=(MyHashMap) o;

						if(map.containsKey("workerstatus")){

							synchronized (getSimulationsList()) {
								Simulation s=(Simulation) map.get("workerstatus");
								Simulation s_master=getSimulationsList().get(simul.getSimID());

								if(s_master.getStartTime() < s.getStartTime()){
									s_master.setStartTime(s.getStartTime());
								}
								if(s_master.getStep() < s.getStep()){
									s_master.setStep(s.getStep());
								}

								s_master.setStatus(s.getStatus());

								if(s.getStatus().equals(Simulation.FINISHED)){
									System.out.println("Receved FINISHED for "+s.getSimID());
									if(s_master.getEndTime()<s.getEndTime()){
										getSimulationsList().get(s.getSimID()).setEndTime(s.getEndTime());
									}
								}
							}

						} 

					} catch (JMSException e) {e.printStackTrace();}
				}
			});


		} catch (Exception e1) {e1.printStackTrace();}


		//return this.invokeCopyServer(pathJar ,simul.getTopicList().size());
		this.invokeCopyServer(pathJar ,simul.getTopicList().size());

		while((simul.getTopicList().size()) > (getCounterAckSimRcv().get(simul.getSimID())).intValue()){}
		
		return true;


	}



	///////////methods  START STOP PAUSE LOG	


	public void start(int idSimulation){

		Simulation simulationToExec=getSimulationsList().get(idSimulation);
		int iDSimToExec=simulationToExec.getSimID();
		System.out.println("Start command received for simulation with id "+idSimulation);
		for(String workerTopic : simulationToExec.getTopicList()){
			//System.out.println("send start command to "+workerTopic+"   "+getTopicIdForSimulation());
			this.getConnection().publishToTopic(iDSimToExec, workerTopic, "start");
		}


	}



	public void stop(int idSimulation) {
		Simulation simulationToStop=getSimulationsList().get(idSimulation);
		int iDSimToStop=simulationToStop.getSimID();
		System.out.println("Stop command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToStop.getTopicList()){

			this.getConnection().publishToTopic(iDSimToStop, workerTopic, "stop");
		}

	}


	private boolean createCopyInHistory(String src, int simid){

		String pathHistory=masterHistoryFolder+File.separator+getSimulationsList().get(simid).getSimName()+simid;
		Simulation s = getSimulationsList().get(simid);


		Thread c=new Thread(new Runnable() {

			@Override
			public void run() {
				File resume = new File(s.getSimulationFolder()+File.separator+"runs"+File.separator+s.getSimName()+".history");
				Properties props = new Properties();
				FileOutputStream f=null;
				//PrintWriter p =null;
				try {
					if(!resume.exists())
						resume.createNewFile();
					f = new FileOutputStream(resume);
					//p = new PrintWriter(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				props.put("simID", 				""+s.getSimID());
				props.put("simName", 			""+s.getSimName());
				props.put("simWidth", 			""+s.getWidth());
				props.put("simHeight", 			""+s.getHeight());
				props.put("simRows", 			""+s.getRows());
				props.put("simColumns", 		""+s.getColumns());
				props.put("simNumAgents", 		""+s.getNumAgents());
				props.put("simAOI", 			""+s.getAoi());
				props.put("simStartTime", 		""+s.getStartTimeAsDate());
				props.put("simEndTime", 		""+s.getEndTimeAsDate());
				props.put("simStepNumber", 		""+s.getNumStep());
				props.put("simNumCells", 		""+s.getNumCells());
				props.put("simStatus", 			""+s.getStatus());
				props.put("simNumWorkers",		""+s.getNumWorkers());
				props.put("simPartitioning", 	(s.getMode()==0)?"uniform":"non-uniform");
				props.put("simLogZipFile",		pathHistory+File.separator+"backupsim.zip");


				try {
					props.store(f, "Resume for sim "+s.getSimName());
					f.flush();
					//props.list(p);
					//p.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		c.start();
		try {
			c.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		Thread t=new Thread(new Runnable() {

			@Override
			public void run() {

				DMasonFileSystem.make(masterHistoryFolder);
				File srcFolder = new File(src);
				File destFolder = new File(pathHistory);


				//make sure source exists
				if(!srcFolder.exists()){
					System.out.println("Directory does not exist.");


				}else{

					try{
						DMasonFileSystem.copyFolder(srcFolder,destFolder);
					}catch(IOException e){
						e.printStackTrace();

					}
				}
			}
		});

		t.start();
		try {
			t.join();

			ZipDirectory.createZipDirectory(pathHistory+File.separator+"backupsim.zip", pathHistory);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return true;
	}


	public void pause(int idSimulation) {
		Simulation simulationToPause=getSimulationsList().get(idSimulation);
		int iDSimToPause=simulationToPause.getSimID();
		System.out.println("Pause command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToPause.getTopicList()){
			this.getConnection().publishToTopic(iDSimToPause, workerTopic, "pause");
		}

	}  

	/**
	 * 
	 * @param idSimulation
	 * @param typeReq logreq(a request for logs file) | history(a request for logs file when delete a simulation) 
	 * @return
	 */
	public String logRequestForSimulationByID(int idSimulation, String typeReq){
		System.out.println("Request for logs for simulation with id servlet"+idSimulation);
		Simulation simulationForLog=getSimulationsList().get(idSimulation);

		String folderCopy= simulationForLog.getSimulationFolder()+File.separator+"runs";

		ArrayList<String> topicWorkers= simulationForLog.getTopicList();

		for(String topic :topicWorkers){
			getConnection().publishToTopic(simulationForLog.getSimID(), topic, typeReq/*"logreq"*/);
			System.out.println("send "+typeReq +"to "+topic);
		}	
		return folderCopy;
	}

	///////////end  START STOP PAUSE

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	//getters and setters
	public MasterServer getMasterServer(){return this;}
	public HashMap<String,String> getTopicIdWorkers(){return topicIdWorkers;}	//all connected workers 
	//public synchronized HashMap getTopicIdForSimulation(){return topicIdWorkersForSimulation;} //all workers for a simulation from id of their topix
	public HashMap<Integer,AtomicInteger> getCounterAckSimRcv(){return counterAckSimRcv;} 

	//activemq address port connection
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public ConnectionNFieldsWithActiveMQAPI getConnection(){return conn;}	


	//folder for master
	public String getMasterdirectoryfolder(){return masterDirectoryFolder;}
	public String getMasterTemporaryFolder() {return masterTemporaryFolder;}
	public String getMasterHistory() {return masterHistoryFolder;}
	public String getSimulationsDirectories() {return simulationsDirectoriesFolder;}


	public HashMap<String, String> getInfoWorkers() { return infoWorkers;}
	public synchronized HashMap<Integer,Simulation> getSimulationsList(){return simulationsList;}

}
