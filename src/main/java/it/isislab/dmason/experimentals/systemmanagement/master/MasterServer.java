package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.experimentals.systemmanagement.utils.MyFileSystem;
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
import java.util.Properties;
import java.util.Map.Entry;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.activemq.broker.BrokerService;


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
	
	private static String winzozz=System.getProperty("user.home")+File.separator+"Desktop"+File.separator;//togliere
	private static String linux=System.getProperty("user.home")+File.separator+"Scrivania"+File.separator;//toliere
	
	private static final String masterDirectoryFolder=linux+"master";
	private static final String masterTemporaryFolder=masterDirectoryFolder+File.separator+"temporay";
	private static final String masterHistoryFolder=masterDirectoryFolder+File.separator+"history";
	private static final String simulationsDirectoriesFolder=masterDirectoryFolder+File.separator+"simulations";



	//copyserver
	Socket sock=null;
	ServerSocket welcomeSocket;

	//info 
	protected HashMap<String/*IDprefixOfWorker*/,String/*MyIDTopicprefixOfWorker*/> topicIdWorkers;
	protected ArrayList<String> topicIdWorkersForSimulation;
	public HashMap<String,String> infoWorkers;



	
	



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
		this.topicIdWorkersForSimulation=new ArrayList<String>();
		this.infoWorkers=new HashMap<String,String>();


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
	 * Check if all workers for this simulation
	 */
	protected void checkSimulationWorkers(String id){
		for (String topic : topicIdWorkersForSimulation) {
			checkWorker(topic);
		}
	} 



	public void sumbit(){

		
		/*infoWorkers=new HashMap<String,String>();

		for(String x: this.getTopicIdForSimulation() this.getTopicIdWorkers().keySet()){

			checkWorker(x);

			//invio richiesta di invio jar
		}*/

		//riscontro richiesta dell ack con conatore finchè non li ho ricevuti tutti 

		//avvio copyserver e invio notifica ai worker per inviare

		//mi metto in attesa con un contatore per corretta ricezione di tutti i riscontri di avvenuta ricezione 

		//termino copyserver

	}




	public void start(){
		for(String x: /*this.getTopicIdForSimulation()*/ this.getTopicIdWorkers().keySet()){
			//invio la richiesta di esecuzione della simulazione con le cellette da simulare

		}

		//mi metto in attesa del riscontro da parte dei worker

		for(String x: /*this.getTopicIdForSimulation()*/ this.getTopicIdWorkers().keySet()){
			//invio ai worker la richiesta di start

		}



	}


	protected void listenonREADY(){

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


						//se il worker ha terminato lo scaricamento jar
						if(mh.containsKey("downloaded")){
							System.out.println("tappò"+mh.get("downloaded"));

							welcomeSocket.close();

							try {
								sock.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				} catch (JMSException e) {e.printStackTrace();} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
			}
		});
	}

    
	private void processSignRequest(String topicOfWorker){

		final String topic=topicOfWorker;

		try {
			//mi sottoscrivo al worker
			getConnection().subscribeToTopic(topicOfWorker);

			//creo prefix univoco per comunicazione da master a worker 1-1 e creo topic
			String myTopicForWorker=String.valueOf(topicOfWorker.hashCode());
			getTopicIdWorkers().put(topicOfWorker,myTopicForWorker);
			getConnection().createTopic(myTopicForWorker, 1);
			getConnection().subscribeToTopic(myTopicForWorker);
            System.out.println(topicOfWorker+"_____------"+myTopicForWorker);
			//mando al worker il mioID univoco per esso di comunicazione
			getConnection().publishToTopic(myTopicForWorker, "MASTER", topicOfWorker);
			
			getConnection().asynchronousReceive(topicOfWorker, new MyMessageListener() {
				           
				@Override
				public void onMessage(Message msg) {

					Object o;
					try {
						o=parseMessage(msg);
						MyHashMap map=(MyHashMap) o;

						for (Entry<String, Object> string : map.entrySet()) {
							System.out.println(string.getKey()+"|"+string.getValue());
							if(map.containsKey("info")){
								System.out.println("Key"+string.getKey()+"Value"+string.getValue());
								infoWorkers.put(topic,""+ map.get("info"));

							}

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
	 * Sevlet
	 * Create directory for a simulation 
	 * @param simID name of directory to create
	 */
	public void createSimulationDirectoryByID(String simID){
		String path=simulationsDirectoriesFolder+File.separator+simID+File.separator+"runs";
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


   public void sendSimulationProcess(String idfolder){
	   //c è da riempire l array dei topic dei worker che partecipano
	/*   for(String topic: topicIdWorkersForSimulation){
		  this.getConnection().publishToTopic(idfolder,topic, "simulation");
		  this.getConnection().asynchronousReceive(key)
		  //invio porta per jar al worker
		  this.getConnection().publishToTopic(object, topicName, key)}*/
	      //se tutti downloaded  
   }

    
	public void invokeCopyServer(int port,String jarFile){
		InetAddress address;
		welcomeSocket=null;
		try {
			address = InetAddress.getByName(this.IP_ACTIVEMQ);
			welcomeSocket = new ServerSocket(port,1,address);
			System.out.println("Listening");
			while (true) {
				sock = welcomeSocket.accept();
				System.out.println("Connected");
				new Thread(new CopyMultiThreadServer(sock,jarFile)).start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (welcomeSocket != null && !welcomeSocket.isClosed()) {
				try {
					welcomeSocket.close();
				} catch (IOException exx)
				{
					exx.printStackTrace(System.err);
				}
			}
		}

	}


	protected String getTopicPrefix(String path){
		return ""+path.hashCode();
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




	//getters and setters
	public MasterServer getMasterServer(){return this;}
	public HashMap<String,String> getTopicIdWorkers(){return topicIdWorkers;}	//all connected workers 
	public ArrayList<String> getTopicIdForSimulation(){return topicIdWorkersForSimulation;} //all workers for a simulation from id of their topix
    
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
	public HashMap<String, String> getInfoWorkers() {return infoWorkers;}
}
