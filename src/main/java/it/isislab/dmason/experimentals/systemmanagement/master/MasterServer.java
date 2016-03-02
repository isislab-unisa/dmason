package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.systemmanagement.utils.MyFileSystem;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.broker.BrokerService;

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

		MyFileSystem.make(masterDirectoryFolder);// master
		MyFileSystem.make(masterTemporaryFolder);//temp folder
		MyFileSystem.make(masterHistoryFolder); //master/history
		MyFileSystem.make(simulationsDirectoriesFolder+File.separator+"jobs"); //master/simulations/jobs

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
							infoWorkers.put(myTopicForWorker,""+ map.get("info"));
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
	 * Servlet
	 * Create directory for a simulation 
	 * @param simID name of directory to create
	 */
	public void createSimulationDirectoryByID(String simName){
		String path=simulationsDirectoriesFolder+File.separator+simName+File.separator+"runs";
		MyFileSystem.make(path);
	}




	/**
	 * Sevlet
	 * Delete a directory for a simulation
	 * @param simID name of a directory to delete
	 */
	public void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectoriesFolder+File.separator+simID;
		File c=new File(path);
		MyFileSystem.delete(c);
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

			//System.out.println("Listening");
			ArrayList<Thread> threads=new ArrayList<Thread>();
			Thread t=null;
			while (counter<checkControl) {
				sock = welcomeSocket.accept();
				System.out.println("Connected");
				counter++;
				t=new Thread(new CopyMultiThreadServer(sock,jarFile));
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
			System.out.println("Starting activemq "+address);
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
		System.out.println("Creating connection to server "+address);
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

		ArrayList<String> workerID=new ArrayList<String>(slots.keySet());
		int mode=simul.getMode();
		int LP=simul.getP();
		int rows=(int) (mode==0?simul.getRows(): Math.ceil(Math.sqrt(LP/*get LP from geneoparam*/))); 
		int cols=(int) (mode==0?simul.getColumns()/*getfrom genparm*/: Math.ceil(Math.sqrt(LP/*get LP from geneoparam*/))); 

		LP=mode==0?rows*cols:LP/*get from gene parame*/;

		int assignedLP=LP;
		try {
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

							throw new DMasonException("Error! Not enough slots on the workers for the given partitioning.");

						if(lastIndex==-1) lastIndex=w;
					}
					w=(w+1)%slots.size();
					if(assignedLP < 1) break;
				}

			}
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			return null;
		}

		return workerlist;
	}


	public synchronized boolean submitSimulation(Simulation sim) {
		final Simulation simul=sim;

		simulationsList.put(simul.getSimID(), simul);
		this.topicIdWorkersForSimulation.put(simul.getSimID(), simul.getTopicList());
		HashMap<String, Integer> slotsAvalaible=slotsAvailableForSimWorker(simul.getTopicList(),infoWorkers);
		HashMap<String, List<CellType>> assignmentToworkers=assignCellsToWorkers(slotsAvalaible, simul);

		if(assignmentToworkers==null) return false;

		//for testing 
		for (String topickey: assignmentToworkers.keySet()){
			System.out.println(topickey+" "+assignmentToworkers.get(topickey));
		}	


		for (String topicName: assignmentToworkers.keySet()){
			simul.setListCellType(assignmentToworkers.get(topicName));
			getConnection().publishToTopic(simul, topicName, "newsim");
		}

		String pathJar=simul.getSimulationFolder()+File.separator+sim.getJarName();



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
									System.out.println(s.getStep());
								}
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


	///////////methods  START STOP PAUSE	

	/**
	 * @Override
	 */
	public void start(int idSimulation){

		System.out.println("launch start command for simiD "+idSimulation);

		Simulation simulationToExec=getSimulationsList().get(idSimulation);
		int iDSimToExec=simulationToExec.getSimID();

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

		for(String workerTopic : simulationToPause.getTopicList()){

			this.getConnection().publishToTopic(iDSimToPause, workerTopic, "pause");
		}

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
