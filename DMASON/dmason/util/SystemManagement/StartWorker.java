/**
 * Copyright 2012 Università degli Studi di Salerno
 

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

package dmason.util.SystemManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.text.DefaultEditorKit.BeepAction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import dmason.util.connection.Address;
import dmason.util.connection.BeaconMessageListener;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.exception.NoDigestFoundException;

/**
 * Executable, command-line version worker.
 */
public class StartWorker implements StartWorkerInterface,  Observer {
	private static Logger logger;

	private static boolean updated;

	private static boolean autoStart;

	private static boolean isBatch;

	private static String topicPrefix;

	/**
	 * Connection with a provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	/**
	 * Provider's address.
	 */
	private Address ipAddress;
	
	private String myTopic;
	private static final String version = "1.0";
	private String digest;

	private static BeaconMessageListener beaconListener;
	
	
	private static boolean isReady = false;
	private final static ReentrantLock lock = new ReentrantLock();
	private static Condition readyCondition = lock.newCondition();

	private static String ip;

	private static String port;

	
	
	/**
	 * Constructor.
	 * @param ip IP Address of the provider.
	 * @param port Port where the provider is listening.
	 * @param topic 
	 */
	public StartWorker(String ip, String port, String topic)
	{
		this.ip = ip;
		this.port = port;
		if(!autoStart)
		{
			beaconListener = new BeaconMessageListener();
			beaconListener.addObserver(this);
			new Thread(beaconListener).start();
			
			while(!isReady)
			{
				lock.lock();
				{
					try {
						readyCondition.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //wait beacon
				}
				lock.unlock();
			}
			isReady = false;
		}
		System.out.println(this.ip+" "+this.port);
		connection = new ConnectionNFieldsWithActiveMQAPI();
	    ipAddress = new Address(this.ip, this.port);
	    
	    myTopic = topic;
	    
	    // Get the path from which worker was started
	    String path;
		try {
			path = URLDecoder.decode(StartWorkerWithGui.class.getProtectionDomain().getCodeSource().getLocation().getFile(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			path = "";
		}
		logger.debug("Path: "+path);
	    if(path.contains(".jar")) //from jar
	    {

	    	File jarfile = new File(path);

	    	Digester dg = new Digester(DigestAlgorithm.MD5);

	    	try {
	    		InputStream in = new FileInputStream(path);
	    		
	    		digest = dg.getDigest(in);

	    		String fileName = FilenameUtils.removeExtension(jarfile.getName());
	    		//save properties to project root folder
	    		dg.storeToPropFile(fileName+".hash");

	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	    	} catch (NoDigestFoundException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}

	    }
	    else
	    { // not from jar
	    	digest = null;
	    }
	}
	
	public static void main(String[] args)
	{
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		 
	    //
	    // Get name representing the running Java virtual machine.
	    // It returns something like 6460@AURORA. Where the value
	    // before the @ symbol is the PID.
	    //
	    String jvmName = bean.getName();
	    
	    //Used for log4j properties
		System.setProperty("logfile.name","worker"+jvmName);
		
	    //Used for log4j properties
		System.setProperty("steplog.name","workerStep"+jvmName);
		

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SS");
		Date date = new Date();
		dateFormat.format(date);
		
		System.setProperty("timestamp", date.toLocaleString());
		
		System.setProperty("paramsfile.name", "params");
		try {
			File logPath = new File("Logs/workers");
			if(logPath.exists())
				FileUtils.cleanDirectory(logPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		logger = Logger.getLogger(StartWorker.class.getCanonicalName());
		logger.debug("StartWorker "+version);
		
		
		autoStart = false;	
		ip = null;
		port = null;
		String topic = null;
		updated = false;
		isBatch = false;
		topicPrefix = "";
		
		if(args.length != 2 && args.length != 4)
			System.out.println("Usage StartWorker IP PORT");
		else
		{
			if(args.length == 2)
			{
				ip = args[0];
				port = args[1];
				
			}
			if(args.length == 4)
			{	
				autoStart = true;
				if(args[3].equals("update"))
					updated = true;
				if(args[3].equals("reset"))
				{
					updated = false;
					isBatch = false;
				}
				if(args[3].contains("Batch"))
				{
					updated = false;
					isBatch = true;
					topicPrefix = args[3];
				}
				ip = args[0];
				port = args[1];
				topic = args[2];
			}
			
			
			
			
			StartWorker worker = new StartWorker(ip,port,topic);
			
			worker.startConnection();
			
			logger.debug("IP "+worker.ipAddress.getIPaddress());
			logger.debug("Port "+worker.ipAddress.getPort());
			if(topic != null)
				logger.debug("Topic: "+topic);
		}
	}

	/**
	 * Setup the connection with the provider.
	 * @return
	 */
	public boolean startConnection(){
		try
		{
			connection.setupConnection(ipAddress);
			
			if(!autoStart)
				new PeerDaemonStarter(connection, this,version,digest);
			else
				new PeerDaemonStarter(connection, this,myTopic,version,digest,updated,isBatch,topicPrefix);
				
			
			return true;
		} catch (Exception e1) {
			System.out.println("Failed to connect with the provider at " + ipAddress);
			return false;
		}
	}
	
	
	@Override
	public void writeMessage(String message)
	{
		logger.debug(message);
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		System.exit(0);
	}

	//Notify from BeaconMessageListener
	@Override
	public void update(Observable o, Object arg) {
		
		System.out.println("Found an instance of ActiveMQ at ip: "+beaconListener.getIp()+" and port: "+beaconListener.getPort()
				+" Do you want use it? (y or n)");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String answer = stdin.readLine();
			while(!((answer.equals("y") || (answer.endsWith("n")))))
			{
				System.out.println("Type y or n");
				answer = stdin.readLine();
			}
			
			if(answer.equals("y"))
			{	
				ip = beaconListener.getIp();
				port = beaconListener.getPort();
			}
			
			isReady = true;
			lock.lock();
			{
				readyCondition.signalAll();
			}
			lock.unlock();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
