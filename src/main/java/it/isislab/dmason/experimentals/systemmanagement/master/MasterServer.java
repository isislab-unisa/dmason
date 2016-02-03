package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.broker.BrokerService;

public class MasterServer{

	private static final String MASTER_TOPIC="MASTER";
	private  String IP_ACTIVEMQ="";
	private  String PORT_ACTIVEMQ="";
	private int PORT_COPY_SERVER=1414;
	private BrokerService broker=null;
	private Properties prop = null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;
	static String prova="/home/miccar/Scrivania/";
	private static final String masterDirectory=prova+"master";
	private static final String masterTemporary=masterDirectory+File.separator+"temporay";
	private static final String masterHistory=masterDirectory+File.separator+"history";
	private static final String simulationsDirectories=masterDirectory+File.separator+"simulations";
    public ArrayList<String> worker;

	/**
	 * start activemq, initialize master connection, create directories and create initial topic for workers
	 */
	public MasterServer(){
		prop = new Properties();
		broker = new BrokerService();
		conn=new ConnectionNFieldsWithActiveMQAPI();
		MyFileSystem.make(masterDirectory);// master
		MyFileSystem.make(masterTemporary);
		MyFileSystem.make(masterHistory); //master/history
		MyFileSystem.make(simulationsDirectories+File.separator+"jobs"); //master/simulations/jobs
		this.loadProperties();
		this.startActivemq();
		this.createConnection();
		this.createInitialTopic(MASTER_TOPIC);
		worker=new ArrayList<String>();
	     

	}

	public MasterServer getMasterServer(){
		return this;
	}

	protected void listenonREADY(){

		final MasterServer master=this.getMasterServer();
	
		
		//while(true)for(String x:master.getConnection().getTopicList())System.out.println(x);
		
		master.getConnection().asynchronousReceive("READY", new MyMessageListener() {
			
			@Override
			public void onMessage(Message msg) {
		        Object o;
				try {
					o=parseMessage(msg);
					MyHashMap mh = (MyHashMap)o;
					
					for ( Entry<String, Object> string : mh.entrySet()) {
						
						System.out.println(string.getKey()+"|"+string.getValue());
						if(mh.containsKey("sottoscrizione")){
						     master.worker.add(""+string.getValue());
						     try {
								master.getConnection().subscribeToTopic(""+string.getValue());
							} catch (Exception e) {e.printStackTrace();}
						}
						
						if(mh.containsKey("downloaded")){
							System.out.println("tappò"+mh.get("downloaded"));
						}
					}
						
				} catch (JMSException e) {e.printStackTrace();}	
			}
		});
	}
	
    /**
     * Create directory for a simulation 
     * @param simID name of directory to create
     */
	protected void createSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID+File.separator+"runs";
		MyFileSystem.make(path);
	}

	/**
	 * Delete a directory for a simulation
	 * @param simID name of a directory to delete
	 */
	protected void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID;
		File c=new File(path);
		MyFileSystem.delete(c);
	}


	protected void sumbit(){
		
	}
	

	protected void startCopyServer(int port){
		InetAddress address;
		ServerSocket welcomeSocket=null;
		try {
			address = InetAddress.getByName(this.IP_ACTIVEMQ);
			welcomeSocket = new ServerSocket(port,1,address);
			System.out.println("Listening");
			while (true) {
				Socket sock = welcomeSocket.accept();
				System.out.println("Connected");
				new Thread(new CopyMultiThreadServer(sock)).start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		//default 127.0.0.1:61616 else you have to change config.properties file
		String filePropPath="resources/systemmanagement/master/conf/config.properties";
		InputStream input=null;
		//load params from properties file 
		try {
			input=new FileInputStream(filePropPath);	
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

	
	private void sendAck(Serializable object,String key){
		conn.publishToTopic(object, "MASTER", key);
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
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
	public ConnectionNFieldsWithActiveMQAPI getConnection(){return conn;}
	public int getCopyPort(){return PORT_COPY_SERVER;}
	public void setCopyPort(int port){ this.PORT_COPY_SERVER=port;}
}
