package it.isislab.dmason.experimentals.systemmanagement.master;


import it.isislab.dmason.experimentals.systemmanagement.utils.ClientSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.ServerSocketCopy;
import it.isislab.dmason.experimentals.systemmanagement.utils.DMasonFileSystem;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jms.JMSException;
import javax.jms.Message;

import oracle.jrockit.jfr.events.DynamicValueDescriptor;

import org.apache.activemq.broker.BrokerService;

import com.sun.nio.zipfs.ZipPath;

import scala.annotation.meta.field;

/**
 * 
 * @author miccar
 *
 */
public class MasterServer implements MultiServerInterface{


	//default 127.0.0.1:61616 else you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH="resources/systemmanagement/master/conf/config.properties";

	//connection and topic
	private static final String MASTER_TOPIC="MASTER";
	private  String IP_ACTIVEMQ="";
	private  String PORT_ACTIVEMQ="";
	private int DEFAULT_PORT_COPY_SERVER=1414;
	private BrokerService broker=null;
	private Properties prop = null;
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
	protected HashMap<Integer,ArrayList<String>> topicIdWorkersForSimulation;
	public HashMap<String,String> infoWorkers;
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
		prop = new Properties();
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
		this.topicIdWorkersForSimulation=new HashMap<>();
		this.infoWorkers=new HashMap<String,String>();
		//waiting for workers connecetion	
		this.listenForSignRequest();

		this.keySimulation=new AtomicInteger(0);
		simulationsList=new HashMap<>();
		try {
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
	 *If worker is sctive, it responds on his topic with key "info" 
	 * with a info message of worker 
	 * 
	 * @param topicWorker
	 */
	protected void checkWorker(String topicWorker){

		getConnection().publishToTopic("", getTopicIdWorkers().get(topicWorker), "check");


	}

	/**
	 * Check if all workers connected is on 
	 */
	public void checkAllConnectedWorkers(){

		for (String topic : topicIdWorkers.keySet()) {
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


	private void processSignRequest(String topicOfWorker){


		try {
			//mi sottoscrivo al worker
			getConnection().subscribeToTopic(topicOfWorker);


			//creo prefix univoco per comunicazione da master a worker 1-1 e creo topic
			final String myTopicForWorker=""+topicOfWorker.hashCode();

			getTopicIdWorkers().put(topicOfWorker,myTopicForWorker);
			getConnection().createTopic(myTopicForWorker, 1);
			getConnection().subscribeToTopic(myTopicForWorker);

			//mando al worker il mioID univoco per esso di comunicazione
			getConnection().publishToTopic(myTopicForWorker, "MASTER", topicOfWorker);

			this.getConnection().publishToTopic(DEFAULT_PORT_COPY_SERVER, "MASTER", "port");
			//mi metto in ricezione sul topic del worker
			getConnection().asynchronousReceive(topicOfWorker, new MyMessageListener() {

				@Override
				public void onMessage(Message msg) {

					Object o;
					try {
						o=parseMessage(msg);
						MyHashMap map=(MyHashMap) o;

						if(map.containsKey("info")){
							String infoReceived=""+map.get("info");
							infoWorkers.put(myTopicForWorker, infoReceived);
							processInfoForCopyLog(infoReceived,topicOfWorker);

						}

						if(map.containsKey("logready")){
							int simID=(int) map.get("logready");
							System.out.println("start copy of logs for sim id "+simID);
							downloadLogsForSimulationByID(simID,topicIdWorkers.get(topicOfWorker));
						}



					} catch (JMSException e) {
						e.printStackTrace();
					}

				}
			});

		} 
		catch (Exception e){e.printStackTrace();}

	}

	private synchronized void downloadLogsForSimulationByID(int simID,String topicOfWorker){

		Simulation sim=simulationsList.get(simID);
		String folderCopy=sim.getSimulationFolder()+File.separator+"runs";
		String fileCopy=folderCopy+File.separator+topicOfWorker+".zip";
		System.out.println("folder per la copia "+fileCopy);



		Address address= workerListForCopyLogs.get(topicOfWorker);

		String iplog= address.getIPaddress().replace("\"", "");
		int port = Integer.parseInt(address.getPort());


		Socket clientSocket;
		try {
			clientSocket = new Socket( iplog ,port );
			Thread tr=null;
			tr=new Thread(new ClientSocketCopy(clientSocket, fileCopy));
			tr.start();
			tr.join();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("End download "+fileCopy);
		System.out.println(new File(fileCopy).exists());
		ZipDirectory.unZipDirectory(fileCopy, folderCopy);

	}



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
	 * Servlet
	 * Create directory for a simulation 
	 * @param simID name of directory to create
	 */
	public void createSimulationDirectoryByID(String simName){
		String path=simulationsDirectoriesFolder+File.separator+simName+File.separator+"runs";
		DMasonFileSystem.make(path);
	}




	/**
	 * Sevlet
	 * Delete a directory for a simulation
	 * @param simID name of a directory to delete
	 */
	public void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectoriesFolder+File.separator+simID;
		File c=new File(path);
		DMasonFileSystem.delete(c);
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


	//Start methods to open a connection and a topic for initial communication 
	private void startActivemq(){
		String address="tcp://"+IP_ACTIVEMQ+":"+PORT_ACTIVEMQ;
		try {
			broker.addConnector(address);
			broker.start();
		} catch (Exception e1) {e1.printStackTrace();}
	}

	private void loadProperties(){

		InputStream input=null;
		//load params from properties file 
		try {
			input=new FileInputStream(PROPERTIES_FILE_PATH);	
			prop.load(input);
			this.setIpActivemq(prop.getProperty("ipmaster"));
			this.setPortActivemq(prop.getProperty("portmaster"));
			this.setCopyServerPort(Integer.parseInt(prop.getProperty("copyport")));
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

		//ArrayList<String> workerID=new ArrayList<String>(slots.keySet());
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
	public synchronized boolean submitSimulation(Simulation sim) {
		final Simulation simul=sim;

		simulationsList.put(simul.getSimID(), simul);
		this.topicIdWorkersForSimulation.put(simul.getSimID(), simul.getTopicList());
		HashMap<String, Integer> slotsAvalaible=slotsAvailableForSimWorker(simul.getTopicList(),infoWorkers);
		HashMap<String, List<CellType>> assignmentToworkers=assignCellsToWorkers(slotsAvalaible, simul);

		if(assignmentToworkers==null) return false;


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

							synchronized (simulationsList) {
								Simulation s=(Simulation) map.get("workerstatus");
								Simulation s_master=simulationsList.get(simul.getSimID());

								if(s_master.getStartTime() < s.getStartTime())
								{   
									s_master.setStartTime(s.getStartTime());
								}
								if(s_master.getStep() < s.getStep())
								{
									s_master.setStep(s.getStep());
								}

								s_master.setStatus(s.getStatus());
							}


						} 

					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});


		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		return this.invokeCopyServer(pathJar ,simul.getTopicList().size());
	}









	///////////methods  START STOP PAUSE LOG	

	/**
	 * @Override
	 */
	public void start(int idSimulation){

		Simulation simulationToExec=getSimulationsList().get(idSimulation);
		int iDSimToExec=simulationToExec.getSimID();
		System.out.println("Start command received for simulation with id "+idSimulation);
		for(String workerTopic : simulationToExec.getTopicList()){

			this.getConnection().publishToTopic(iDSimToExec, workerTopic, "start");
		}


	}



	/**
	 * @Override
	 */
	public void stop(int idSimulation) {
		Simulation simulationToStop=getSimulationsList().get(idSimulation);
		int iDSimToStop=simulationToStop.getSimID();
		System.out.println("Stop command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToStop.getTopicList()){

			this.getConnection().publishToTopic(iDSimToStop, workerTopic, "stop");
		}

	}


	/**
	 * @Override
	 */
	public void pause(int idSimulation) {
		Simulation simulationToPause=getSimulationsList().get(idSimulation);
		int iDSimToPause=simulationToPause.getSimID();
		System.out.println("Pause command received for simulation with id "+idSimulation);

		for(String workerTopic : simulationToPause.getTopicList()){
			this.getConnection().publishToTopic(iDSimToPause, workerTopic, "pause");
		}

	}  

	public String logRequestForSimulationByID(int idSimulation){
		System.out.println("Request for logs for simulation with id servlet"+idSimulation);
		Simulation simulationForLog=getSimulationsList().get(idSimulation);

		String folderCopy= simulationForLog.getSimulationFolder()+File.separator+"runs";

		ArrayList<String> topicWorkers= simulationForLog.getTopicList();
		for(String topic :topicWorkers)
			getConnection().publishToTopic(simulationForLog.getSimID(), topic, "logreq");

		return folderCopy;
	}

	///////////end  START STOP PAUSE

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	//getters and setters
	public MasterServer getMasterServer(){return this;}
	public HashMap<String,String> getTopicIdWorkers(){return topicIdWorkers;}	//all connected workers 
	public HashMap getTopicIdForSimulation(){return topicIdWorkersForSimulation;} //all workers for a simulation from id of their topix

	//activemq address port connection
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public ConnectionNFieldsWithActiveMQAPI getConnection(){return conn;}	

	// copy server info
	public int getCopyServerPort(){return DEFAULT_PORT_COPY_SERVER;}
	public void setCopyServerPort(int port){ this.DEFAULT_PORT_COPY_SERVER=port;}

	//folder for master
	public String getMasterdirectoryfolder(){return masterDirectoryFolder;}
	public String getMasterTemporaryFolder() {return masterTemporaryFolder;}
	public String getMasterHistory() {return masterHistoryFolder;}
	public String getSimulationsDirectories() {return simulationsDirectoriesFolder;}


	public HashMap<String, String> getInfoWorkers() { HashMap<String, String> toReturn=infoWorkers; infoWorkers=new HashMap<>();  return toReturn;}
	public HashMap<Integer,Simulation> getSimulationsList(){return simulationsList;}


	//private void simReceivedProcess(String topic){

	//getConnection().publishToTopic(DEFAULT_PORT_COPY_SERVER, topic, "jar");

	//}

}
