package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

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

	}



	protected void createSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID+File.separator+"runs";
		MyFileSystem.make(path);
	}

	protected void deleteSimulationDirectoryByID(String simID){
		String path=simulationsDirectories+File.separator+simID;
		File c=new File(path);
		MyFileSystem.delete(c);
	}



	protected void startCopyServer(final String fileToSend){

		(new Thread() {

			@Override
			public void run() {

				ServerSocket welcomeSocket = null;
				Socket connectionSocket = null;
				BufferedOutputStream outToClient = null;
				InetAddress address=null;
				while (true) {
					try {
						address=InetAddress.getByName("127.0.0.1");
						welcomeSocket = new ServerSocket(PORT_COPY_SERVER,1000,address);

						connectionSocket = welcomeSocket.accept();
						System.out.println("listening for a connection...");
						outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
					} catch (IOException ex) {
						ex.printStackTrace();
					}

					if (outToClient != null) {
						File myFile = new File(fileToSend);
						myFile.setReadable(true);
						byte[] mybytearray = new byte[(int) myFile.length()];

						FileInputStream fis = null;

						try {
							fis = new FileInputStream(myFile);
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
						}
						BufferedInputStream bis = new BufferedInputStream(fis);

						try {
							bis.read(mybytearray, 0, mybytearray.length);
							outToClient.write(mybytearray, 0, mybytearray.length);
							outToClient.flush();
							outToClient.close();
							connectionSocket.close();

							System.out.println("File sended");   

						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}).start();
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

	private void createInitialTopic(String topic){


		try {
			conn.createTopic(topic, 1);
			conn.subscribeToTopic(topic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	//getters and setters
	public String getIpActivemq() {return IP_ACTIVEMQ;}
	public void setIpActivemq(String iP) {IP_ACTIVEMQ = iP;}
	public String getPortActivemq() {return PORT_ACTIVEMQ;}
	public void setPortActivemq(String port) {PORT_ACTIVEMQ = port;}
}
