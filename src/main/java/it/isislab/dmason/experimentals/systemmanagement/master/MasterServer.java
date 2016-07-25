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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import it.isislab.dmason.experimentals.systemmanagement.utils.ClientSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.FindAvailablePort;
import it.isislab.dmason.experimentals.systemmanagement.utils.ServerSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.systemmanagement.utils.ZipDirectory;
import it.isislab.dmason.experimentals.systemmanagement.utils.loader.DMasonClassLoader;
import it.isislab.dmason.experimentals.systemmanagement.worker.Worker;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

/**
 * 
 * Master for DMason System Management
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class MasterServer implements MultiServerInterface{
	private static final Integer TTL = 30000;
	//ActivemQ settings file, default 127.0.0.1:61616 otherwise you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH="resources"+File.separator+"systemmanagement"+File.separator+"master"+File.separator+"conf"+File.separator+"config.properties";

	private static final String JSON_ID_PATH="resources"+File.separator+"systemmanagement"+File.separator+"master"+File.separator+"conf"+File.separator+"simid.json";

	//example jars path from resources dmason main path 
	private static final String JARS_EXAMPLE_PATH="resources"+File.separator+"examples";

	private JSONParser parser=null;

	//connection and topic
	private static final String MANAGEMENT="DMASON-MANAGEMENT";
	private  String IP_ACTIVEMQ="";
	private  String PORT_ACTIVEMQ="";
	private int DEFAULT_PORT_COPY_SERVER;
	private Properties startProperties = null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;

	//path directories 
	private static String dmasonDirectory=System.getProperty("user.dir")+File.separator+"dmason";
	private static final String masterDirectoryFolder=dmasonDirectory+File.separator+"master";
	private static final String jsonIdFile=masterDirectoryFolder+File.separator+"simid.json";
	private static final String masterTemporaryFolder=masterDirectoryFolder+File.separator+"temporary";
	private static final String masterHistoryFolder=masterDirectoryFolder+File.separator+"history";
	private static final String simulationsDirectoriesFolder=masterDirectoryFolder+File.separator+"simulations";
	private static final String masterSimulationsJarsFolder=masterDirectoryFolder+File.separator+"jars";
	private static final String masterExampleJarsFolder=masterSimulationsJarsFolder+File.separator+"examples";
	private static final String masterCustomJarsFolder=	masterSimulationsJarsFolder+File.separator+"customs";



	//copyserver for socket
	protected Socket sock=null;
	protected ServerSocket welcomeSocket;

	//info 
	protected HashMap<Integer,AtomicInteger> counterAckSimRcv;// number of ack received of <simrcv> 
	private HashMap<String,String> infoWorkers;// basic information on workers node
	private HashMap<String,Integer> ttlinfoWorkers;//list of connected workers with time to live updated if it is still alive 

	private HashMap<Integer,Simulation> simulationsList; //list of simulations <ID,Simulation> 
	private AtomicInteger IDSimulation; // generate an unique id for a simulation 
	private FindAvailablePort availableport; // for socket server 
	private HashMap<String /*workertopicforrequest*/, Address /*portcopyLog*/> workerListForCopyLogs=new HashMap<String,Address>();


	/// INTERNAL LOGGER FOR DEBUG 
	private static final Logger LOGGER=Logger.getLogger(Worker.class.getName()); //show constructor to enable Logger


	/**
	 * Master Server constructor
	 */
	public MasterServer(){

		//comment below line to enable Logger 
		LOGGER.setUseParentHandlers(false);  
		LOGGER.info("LOGGER ENABLE");
		//

		startProperties = new Properties();
		conn=new ConnectionNFieldsWithActiveMQAPI();
		parser=new JSONParser();


		//create dmason file system for master
		DMasonFileSystem.make(masterDirectoryFolder);// master
		DMasonFileSystem.make(masterTemporaryFolder);//temp folder
		DMasonFileSystem.make(masterHistoryFolder); //master/history
		DMasonFileSystem.make(masterExampleJarsFolder);//master/jars/examples
		DMasonFileSystem.make(masterCustomJarsFolder);//master/jars/customs
		DMasonFileSystem.make(simulationsDirectoriesFolder+File.separator+"jobs"); //master/simulations/jobs


		this.loadProperties(); 
		loadJarsExample(); //load jar of example 


		this.infoWorkers=new HashMap<String,String>();
		this.ttlinfoWorkers=new HashMap<String,Integer>();
		this.counterAckSimRcv=new HashMap<Integer,AtomicInteger>();

		/**
		 * Copy json with id for a new simulation in dmason main directory 
		 * if not exist a previous version of file. Each simulation have an unique identifier
		 * This file contains an auto-increment identifier for simulation    
		 */
		Thread t=new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					if(!new File(jsonIdFile).exists())
						DMasonFileSystem.copyFolder(new File(JSON_ID_PATH), new File(jsonIdFile));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		t.start();
		try {
			t.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		//read the value of next id for a new simulation
		this.readJSONLastID();

		simulationsList=new HashMap<Integer,Simulation>();

		/*Find an available port in this range [1000,3000] of ports */
		try {
			availableport=new FindAvailablePort(1000, 3000);
			DEFAULT_PORT_COPY_SERVER=availableport.getPortAvailable();
			//LOGGER.info("copy server start on port "+DEFAULT_PORT_COPY_SERVER);
			welcomeSocket = new ServerSocket(DEFAULT_PORT_COPY_SERVER,1000,InetAddress.getByName(this.IP_ACTIVEMQ));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.createConnection();//create connection with activemq server
		this.createInitialTopic();//start communication with workers

		/*start time to live mechanism for connected workers */
		new TTLWorker().start();

	}



	/**
	 * 
	 * Check info from workers still alive 
	 *
	 */
	class TTLWorker extends Thread{
		@Override
		public void run() {
			while(true)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				synchronized (this) {

					List<String> toRemove=new ArrayList<String>();
					for(String workerid:ttlinfoWorkers.keySet())
					{
						Integer ttl=ttlinfoWorkers.get(workerid);
						ttl-=100;
						if(ttl <= 0)
						{
							toRemove.add(workerid);
						}
						ttlinfoWorkers.put(workerid,ttl);

					}
					for(String workerid:toRemove)
					{
						infoWorkers.remove(workerid);
						ttlinfoWorkers.remove(workerid);

					}

				}
			}
		}
	}


	/**
	 * Set what is the next id available from master folder 
	 * An id identify a simulation
	 * You can manage this id from file
	 * from json file /conf/simid.json
	 */
	private void readJSONLastID(){

		try {
			Object obj=parser.parse(new FileReader(jsonIdFile));
			JSONObject jsonID=(JSONObject)obj;
			String id=(String) jsonID.get("simid");
			this.IDSimulation=new AtomicInteger(Integer.parseInt(id));
		} 
		catch (Exception e) {e.printStackTrace();}
	}


	/**
	 * Open a asynchronous receive listener on  topic
	 * @param topic of Worker
	 */
	private void listenOnTopicWorker(String topic){

		Thread listenerThread=new Thread(new Runnable() {

			@Override
			public void run() {
				getConnection().asynchronousReceive(topic, new MyMessageListener() {

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
							else
								// response to master logs req	
								if(map.containsKey("logready")){
									int simID=(int) map.get("logready");
									LOGGER.info("start copy of logs for sim id "+simID);
									downloadLogsForSimulationByID(simID,topic,false);
								}
								else
									// response to master logs req(when a sim is stopped )
									if(map.containsKey("loghistory")){
										int simID=(int) map.get("loghistory");
										LOGGER.info("start copy of logs history for sim id "+simID);
										downloadLogsForSimulationByID(simID,topic,true);
									}



						} catch (JMSException e) {
							e.printStackTrace();
						}

					}
				});

			}
		});
		listenerThread.start();


	}


	/**
	 * Set initial configuration 
	 * 
	 */
	private void createInitialTopic(){
		try {
			conn.createTopic(MANAGEMENT, 1);
			conn.subscribeToTopic(MANAGEMENT);

			(new Thread(){
				public void run() {

					conn.asynchronousReceive(MANAGEMENT,new MyMessageListener() {

						@Override
						public void onMessage(Message msg) {
							Object o;
							try {
								o=parseMessage(msg);
								MyHashMap map=(MyHashMap) o;

								if(map.containsKey("WORKER")){

									synchronized (this) {
										String info=(String) map.get("WORKER");
										info.replace("{", "");
										info.replace("}", "");
										String[] ainfo=info.split(":");
										String ID = ainfo[ainfo.length-1].replace("\"", "");
										ID=ID.replace("}", "");
										conn.publishToTopic(DEFAULT_PORT_COPY_SERVER, MANAGEMENT, "WORKER-ID-"+ID);

										if(!infoWorkers.containsKey(ID)){
											processInfoForCopyLog(info,ID);
											getConnection().createTopic(ID, 1);
											try {
												getConnection().subscribeToTopic(ID);
											} catch (Exception e1) {e1.printStackTrace();}
											listenOnTopicWorker(ID);
										}
										infoWorkers.put(ID, info);
										ttlinfoWorkers.put(ID, TTL);
									}


								} 

							} catch (JMSException e) {
								e.printStackTrace();
							}
						}
					});


				};
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connects Master to ActivemQ
	 * Connection on ActivemQ
	 * @return true id connection is created
	 */
	private boolean createConnection(){
		Address address=new Address(IP_ACTIVEMQ, PORT_ACTIVEMQ);
		return conn.setupConnection(address);

	}

	/**
	 * Load example jars from example folder
	 */
	private void loadJarsExample() {
		File src=new File(JARS_EXAMPLE_PATH);
		File dest=new File(masterExampleJarsFolder);
		try {
			DMasonFileSystem.copyFolder(src, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			//LOGGER.info("Download from "+iplog+":"+port);
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

				//i must know last process of history download to start remove process  
				if(getSimulationsList().get(simID).getTopicList().size()==0){
					removeSimulationProcessByID(simID);
				}

			}	


		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	/**
	 * 
	 * Create zip of simulation files
	 * 
	 * @param sim_id simulation id of simulation 
	 * @return true if the copy is completed
	 */
	public synchronized boolean createZipForHistory(int sim_id){

		Simulation s = this.getSimulationsList().get(sim_id);		
		String log_path=s.getSimulationFolder()+File.separator+"runs";
		String filePath = this.getMasterTemporaryFolder()+File.separator+s.getSimName()+sim_id+".zip";

		return ZipDirectory.createZipDirectory(filePath, log_path);
	}



	/**
	 * Receive, from worker, ip and port for Socket connection
	 * 
	 * @param info info to process 
	 * @param topicOfWorker unique worker identifier
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
		Address address=new Address(iPaddress, port);
		workerListForCopyLogs.put(topicOfWorker, address);
	}

	/**
	 * 
	 * Create directory for a simulation 
	 * @param simID name of directory to create
	 * @param simName name of folder
	 */
	public void createSimulationDirectoryByID(String simName,int simID){
		String createNameFolder=simName+simID;
		String simpath=simulationsDirectoriesFolder+File.separator+createNameFolder+File.separator+"runs";
		String jarPath=simulationsDirectoriesFolder+File.separator+createNameFolder+File.separator+"jar";
		DMasonFileSystem.make(simpath);
		DMasonFileSystem.make(jarPath);

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

		if(getSimulationsList().get(simID).getStatus().equals(Simulation.FINISHED) || getSimulationsList().get(simID).getStatus().equals(Simulation.STOPPED) )
			createCopyInHistory(folderCopy,simID);

		DMasonFileSystem.delete(new File(folder));
		getSimulationsList().remove(simID);
	}


	/**
	 * Send to all workers simulations' jar with a socket connection  
	 * @param jarFile   
	 * @param stopParam number of accept to do
	 */
	private boolean invokeCopyServer(String jarFile,int stopParam){

		int checkControl=stopParam;

		int counter=0;
		try {
			ArrayList<Thread> threads=new ArrayList<Thread>();
			Thread t=null;
			while (counter<checkControl) {
				//System.out.println("open stream "+jarFile );
				sock = welcomeSocket.accept();
				//System.out.println("esco della accept");
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

	/**
	 * Load properties from file path
	 * IP and Port of ActivemQ
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


	/**
	 * Calculates slots number available for each worker	
	 * @param topicWorkers
	 * @param listAllWorkers
	 *
	 */
	private HashMap<String, Integer> slotsAvailableForSimWorker(ArrayList<String> topicWorkers, HashMap<String, String> listAllWorkers){

		HashMap<String,Integer> slotsForWorkers=new HashMap<String,Integer>();
		synchronized (this) {
			//set number of cells for all worker identified by their topic
			for(String topicToFind: topicWorkers ){
				String numcells=listAllWorkers.get(topicToFind).split(",")[0].split(":")[1];
				slotsForWorkers.put(topicToFind, Integer.parseInt(numcells));
			}

		}

		return slotsForWorkers;
	}


	/**
	 * Assign cells to selected workers 
	 * @param slots number of slote 
	 * @param simul Simulation to execute
	 * 
	 */
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


	/**
	 * From simulation configuration field division, 
	 *  
	 * 
	 * @param rows number of rows of field 
	 * @param cols number of columns of field
	 * @param slots number of available slots for each workers
	 * @param workerlist an hashmap to identify with key the topic of workers  
	 * @param assignedLP number of slots
	 * @return an hashmap with cells to execute for each worker
	 */
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
					//	throw new DMasonException("Error! Not enough slots on the workers for the given partitioning.");

					if(lastIndex==-1) lastIndex=w;
				}
				w=(w+1)%slots.size();
				if(assignedLP < 1) break;
			}
		}
		return workerlist;
	}

	/**
	 * 
	 * @param LP number of slots
	 * @param slots number of available slots for each workers
	 * @param workerlist an hashmap to identify with key the topic of workers
	 * @param assignedLP
	 * @return an hashmap with cells to execute for each worker
	 */
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
					System.err.println("Error! Not enough slots on the workers for the given partitioning.");


				if(lastIndex==-1) lastIndex=w;
			}
			w=(w+1)%slots.size();
			if(assignedLP < 1) break;


		}				
		return workerlist;
	}

	/**
	 * Checks if a jar is not corrupted 
	 * @param pathJar path of jar
	 * @return true if the jar file is well-formed for dmason
	 */
	protected boolean validateSimulationJar(String pathJar)
	{

		try{
			JarFile jar=new JarFile(new File(pathJar));
			Enumeration e=jar.entries();
			File file  = new File(pathJar);
			String u = file.toURI().toURL().toString(); 
			URL url=new URL(u);
			URL[] urls = new URL[]{url};

			URLClassLoader aUrlCL = new URLClassLoader(urls, new DMasonClassLoader());
			Thread.currentThread().setContextClassLoader(aUrlCL);
			Class distributedState=null;

			while(e.hasMoreElements()){

				JarEntry je=(JarEntry)e.nextElement();
				if(!je.getName().contains(".class")) continue;

				Class c=aUrlCL.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));

				if(c.getSuperclass().equals(DistributedState.class)){
					distributedState=c;
				}

			}
			if(distributedState==null) return false;

			Class<?> urlClass = aUrlCL.getClass();//URLClassLoader.class;////

			Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
			method.invoke(aUrlCL, new Object[]{url});;

			Class simClass = aUrlCL.loadClass(distributedState.getName());
			Constructor constr = simClass.getConstructor(new Class[]{ GeneralParam.class ,String.class});
			return true;
		} catch(Exception e){
			System.err.println("JAR CORRUPTED, export as a Jar File and not as a runnable jar file");
			return false;
		}


	}

	/**
	 * UPDATE value of current simid in the json file
	 *   
	 * @param simid
	 */
	private void updateJSONIDFile(String simid){

		try {
			JSONObject jsonID=new JSONObject();
			jsonID.put("simid", simid);
			FileWriter jsonFile=new FileWriter(jsonIdFile);
			jsonFile.write(jsonID.toJSONString());
			jsonFile.close();

		} catch (Exception e) {e.printStackTrace();}


	}

	/**
	 * Send a simulation object to all workers 
	 * 
	 * @param sim Simulation to send
	 * @return true if all workers have received the simulation 
	 */
	public synchronized boolean submitSimulation(Simulation sim) {

		updateJSONIDFile(""+sim.getSimID());
		final Simulation simul=sim;

		getSimulationsList().put(simul.getSimID(), simul);
		HashMap<String, Integer> slotsAvalaible=slotsAvailableForSimWorker(simul.getTopicList(),infoWorkers);
		HashMap<String, List<CellType>> assignmentToworkers=assignCellsToWorkers(slotsAvalaible, simul);

		if(assignmentToworkers==null) {return false;}




		getCounterAckSimRcv().put(simul.getSimID(), new AtomicInteger(0));

		for (String topicName: assignmentToworkers.keySet()){
			simul.setListCellType(assignmentToworkers.get(topicName));
			getConnection().publishToTopic(simul, topicName, "newsim");}


		String pathJar=simul.getJarPath();

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
									s_master.setSnapshots(s.getSnapshots());
								}
								
								s_master.setStatus(s.getStatus());
							
								
								if(s.getStatus().equals(Simulation.FINISHED) || s.getStatus().equals(Simulation.STOPPED)){

									LOGGER.info("Received FINISHED for "+s.getSimID());
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


		this.invokeCopyServer(pathJar ,simul.getTopicList().size());

		while((simul.getTopicList().size()) > (getCounterAckSimRcv().get(simul.getSimID())).intValue()){}

		return true;


	}

	///////////methods  START STOP PAUSE LOG	


	/**
	 * Send start command for a simulation to its workers 
	 * @param idSimulation simulation id to start
	 */
	public void start(int idSimulation){

		Simulation simulationToExec=getSimulationsList().get(idSimulation);
		int iDSimToExec=simulationToExec.getSimID();
		LOGGER.info("Start command received for simulation with id "+idSimulation);
		for(String workerTopic : simulationToExec.getTopicList()){
			//LOGGER.info("send start command to "+workerTopic+"   "+getTopicIdForSimulation());wait
			this.getConnection().publishToTopic(iDSimToExec, workerTopic, "start");
		}

		//waitEndSim(idSimulation); //method at the end of this class, a timer for end a simulation
	}


	/**
	 * Send stop command for a simulation to its workers
	 * @param idSimulation simulation id to stop 
	 */
	public void stop(int idSimulation) {
		Simulation simulationToStop=getSimulationsList().get(idSimulation);
		int iDSimToStop=simulationToStop.getSimID();
		LOGGER.info("Stop command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToStop.getTopicList()){

			this.getConnection().publishToTopic(iDSimToStop, workerTopic, "stop");
		}

	}

	/**
	 * Send pause command for a simulation to its workers
	 * @param idSimulation simulation id to pause 
	 */
	public void pause(int idSimulation) {
		Simulation simulationToPause=getSimulationsList().get(idSimulation);
		int iDSimToPause=simulationToPause.getSimID();
		LOGGER.info("Pause command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToPause.getTopicList()){
			this.getConnection().publishToTopic(iDSimToPause, workerTopic, "pause");
		}

	}
 
	
	
	/**
	 * Send request to all workers to request snapshot 
	 * @param simID unique identifier for simulation
	 * @param command type of command startViewer/stopViewer 
	 */
	public void showImage(int simID, String command){
		for (String topic: getSimulationsList().get(simID).getTopicList()){
			
			getConnection().publishToTopic(simID, topic, command);
		}
	}
	
	
	/**
	 * Shutdown command for workers on cluster 
	 * if something wrong during publish or other, waitingTime
	 * allows to release resource
	 * @param toShutdown list of topics identifier of worker
	 * @return true if correct, false otherwise 
	 */
	public synchronized boolean shutdownAllWorkers(ArrayList<String> toShutdown){

		for(String topic:toShutdown){
			getConnection().publishToTopic("", topic, "shutdown");
			ttlinfoWorkers.put(topic, 1000);
		}


		long start=System.currentTimeMillis();
		long maxWaitingTime=1000*toShutdown.size(); 

		HashSet<String> toremove=new HashSet<>(toShutdown);


		HashSet<String> check=null;		
		while((System.currentTimeMillis()-start) < maxWaitingTime  ){
			check=new HashSet<String>(getInfoWorkers().keySet());
			if(check.containsAll(toremove)){
				break;
			}
			check=new HashSet<String>();
		}
		infoWorkers=new HashMap<String,String>();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		

		return true;

	}


	/**
	 * Delete history for a list of simultion
	 * @param paths simulation path list to delete
	 * @return true if files are deleted
	 */
	public synchronized boolean deleteHistory(List<String> paths){
		Thread delete=new Thread(new Runnable() {

			@Override
			public void run() {
				for (String pathname : paths) {
					DMasonFileSystem.delete(new File(pathname));
				}

			}
		});

		delete.start();
		try {
			delete.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Delete all files in the history folder
	 * @return true if files are deleted
	 */
	public synchronized boolean deleteHistoryFolder(){
		Thread delete=new Thread(new Runnable() {

			@Override
			public void run() {
				DMasonFileSystem.delete(new File(masterHistoryFolder));

			}
		});

		delete.start();
		try {
			delete.join();
			DMasonFileSystem.make(masterHistoryFolder);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return true;
	}



	/**
	 * Create history folder for a finished or stopped simulation 
	 * 
	 * @param src   folder of simulation 
	 * @param simid id of simulation 
	 * @return if the copy is completed
	 */
	private boolean createCopyInHistory(String src, int simid){

		String pathHistory=masterHistoryFolder+File.separator+getSimulationsList().get(simid).getSimName()+simid;
		Simulation s = getSimulationsList().get(simid);


		Thread c=new Thread(new Runnable() {

			@Override
			public void run() {
				File resume = new File(s.getSimulationFolder()+File.separator+"runs"+File.separator+s.getSimName()+".history");
				Properties props = new Properties();
				FileOutputStream f=null;

				try {
					if(!resume.exists())
						resume.createNewFile();
					f = new FileOutputStream(resume);
				} catch (IOException e) {
					e.printStackTrace();
				}
				props.put("simID", 				""+s.getSimID());
				props.put("simName", 			""+s.getSimName());
				props.put("simWidth", 			""+s.getWidth());
				props.put("simHeight", 			""+s.getHeight());

				if(s.getMode()==DistributedField2D.UNIFORM_PARTITIONING_MODE ){
					props.put("simRows", 			""+s.getRows());
					props.put("simColumns", 		""+s.getColumns());
				}
				else if(s.getMode()==DistributedField2D.NON_UNIFORM_PARTITIONING_MODE){
					props.put("simRows", 			"-");
					props.put("simColumns", 		"-");
				}
				props.put("simNumAgents", 		""+s.getNumAgents());
				props.put("simAOI", 			""+s.getAoi());
				props.put("simStartTime", 		""+s.getStartTimeAsDate());
				props.put("simEndTime", 		""+s.getEndTimeAsDate());
				props.put("simTime", ""+s.getSimTimeAsDate());
				props.put("simTimeAsMillis", ""+s.getSimTime());
				props.put("simStepNumber", 		""+(s.getStep()+1)+"/"+s.getNumberStep());
				props.put("simNumCells", 		""+s.getNumCells());
				props.put("simStatus", 			""+s.getStatus());
				props.put("simNumWorkers",		""+s.getNumWorkers());
				props.put("simPartitioning", 	(s.getMode()==0)?"uniform":"non-uniform");
				props.put("simLogZipFile",		pathHistory+File.separator+s.getSimName()+"_history.zip");


				try {
					props.store(f, "Resume for sim "+s.getSimName());
					f.flush();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

		c.start();
		try {
			c.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}


		//start process to copy simulation files in /master/history
		Thread t=new Thread(new Runnable() {

			@Override
			public void run() {

				DMasonFileSystem.make(masterHistoryFolder);
				File srcFolder = new File(src);
				File destFolder = new File(pathHistory);

				//make sure source exists
				if(!srcFolder.exists()){
					LOGGER.info("Directory does not exist.");


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
			String simname=getSimulationsList().get(simid).getSimName();
			ZipDirectory.createZipDirectory(pathHistory+File.separator+simname+"_history.zip", pathHistory);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return true;
	}



	/**
	 * Send a log request to workers for a simulation with a given id 
	 * @param idSimulation the id of Simulation
	 * @param typeReq logreq(a request for logs file) | history(a request for logs file when delete a simulation) 
	 * @return the path of folder
	 */
	public String logRequestForSimulationByID(int idSimulation, String typeReq){
		LOGGER.info("Request for logs for simulation with id servlet"+idSimulation);
		Simulation simulationForLog=getSimulationsList().get(idSimulation);

		String folderCopy= simulationForLog.getSimulationFolder()+File.separator+"runs";

		ArrayList<String> topicWorkers= simulationForLog.getTopicList();

		for(String topic :topicWorkers){
			getConnection().publishToTopic(simulationForLog.getSimID(), topic, typeReq/*"logreq"*/);
			LOGGER.info("send "+typeReq +"to "+topic);
		}	
		return folderCopy;
	}


	/**
	 * Copy new jar in master folder 
	 * @param simPathJar the path of jar to copy
	 * @param jarSim file item object
	 */
	public void copyJarOnDirectory(String simPathJar,FileItem jarSim){

		Thread f=null;

		Thread t=new Thread(new Runnable() {

			@Override
			public void run() {
				File dir = new File(simPathJar);
				File file = new File(dir, jarSim.getName());
				try {
					jarSim.write(file);
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Thread j=	new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					FileUtils.copyFileToDirectory(new File(simPathJar+File.separator+jarSim.getName()),new File(getMasterCustomJarsFolder()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

		j.start();
		try {
			j.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}



	/**
	 * Return the list of example simulations
	 * 
	 * @return return the list of example simulations
	 * 
	 */
	public HashMap<String , String> getListExampleSimulationsJars(){
		HashMap<String, String> list=new HashMap<String, String>();
		File file=new File(masterExampleJarsFolder);

		for (File filentry : file.listFiles()) {
			if(filentry.getAbsolutePath().endsWith(".jar")){
				list.put(filentry.getName().replace(".jar", ""), filentry.getAbsolutePath());
			}
		}

		return list;
	}

	/**
	 * Return list of submitted simulations
	 * 
	 * @return return list of submitted simulations
	 */
	public HashMap<String, String> getListCustomSimulationsJars(){
		HashMap<String, String> list=new HashMap<String, String>();
		File file=new File(masterCustomJarsFolder);

		for (File filentry : file.listFiles()) {
			if(filentry.getAbsolutePath().endsWith(".jar")){
				list.put(filentry.getName().replace(".jar", ""), filentry.getAbsolutePath());
			}
		}

		return list;

	}


	///////////end  START STOP PAUSE

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	//GETTER AND SETTERS

	public MasterServer getMasterServer(){return this;}
	//	public HashMap<String,String> getTopicIdWorkers(){return topicIdWorkers;}	//all connected workers 
	public HashMap<Integer,AtomicInteger> getCounterAckSimRcv(){return counterAckSimRcv;} 
	public AtomicInteger getKeySim(){return IDSimulation;} 

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
	public String getMasterExampleJarsFolder(){return masterExampleJarsFolder;}
	public String getMasterCustomJarsFolder(){return masterCustomJarsFolder;}


	public synchronized Map<String, String> getInfoWorkers() {return infoWorkers;}
	public synchronized HashMap<Integer,Simulation> getSimulationsList(){return simulationsList;}




	/***************************************************************************************************/
	/**
	 * TESTING CLUSTER SECTION.
	 ***************************************************************************************************/
	/** insert in start method 
	 * @param id id of simulation
	 */
	private void waitEndSim(int id){
		int minutes=10;
		new Thread(new Runnable() {

			@Override
			public void run() {
				/***************TESTING*********************/
				long start=System.currentTimeMillis();
				boolean check=true;
				while(true && check){
					long nowTime=System.currentTimeMillis();
					long checkTime=nowTime-start;				
					if(checkTime> minutes*60*1000) { 
						check=false;
						stop(id);

					}
				}	


			}
		}).start();

	}/*********END**TESTING**CLUSTER**SECTION********************/

}
