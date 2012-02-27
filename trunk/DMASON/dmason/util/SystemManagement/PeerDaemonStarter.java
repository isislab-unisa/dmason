package dmason.util.SystemManagement;

import java.net.InetAddress;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * This is the Master Worker:its roles are connecting to the server,creating Service-Topics,sending informations to the Master.
 */
public class PeerDaemonStarter extends Thread{


	private String masterTopic = "MASTER";
	private String mytopic;
	private ConnectionNFieldsWithActiveMQAPI connection;
	public StartWorkerInterface gui;
	
	private boolean result;
	private Address address;


	/**
	 * This method sends to the master console system informations as operating system,
	 * architecture,number of cores,etc...(In my implementation i send these three but the library i've used provides more
	 * technical informations as Java heap memory space,system load average,Java Virtual Machine versione,etc...)
	 * @throws Exception Can throw a JMS exception if connection problems occurr.
	 */
	public void info() throws Exception{
		SystemManager sys = new SystemManager(mytopic);
		PeerStatusInfo info = sys.generate();
		connection.publishToTopic(info,masterTopic,"info");
	}

	/**
	 * Constructor.
	 * @param ip address
	 * @param gui Because of this class is called by a simple UI,to update UI's labels during the simulation,we keep the
	 * 		  graphic component.
	 */
	public PeerDaemonStarter(Connection con,StartWorkerInterface gui) 
	{
		
		this.gui = gui;

		
		try{
			connection =(ConnectionNFieldsWithActiveMQAPI) con;
			address = connection.getAdress();
			String add = InetAddress.getLocalHost().getHostAddress()+"-"+System.currentTimeMillis();
			mytopic = "SERVICE"+"-"+add;
			connection.createTopic(mytopic,1);
			connection.subscribeToTopic(mytopic);
			connection.asynchronousReceive(mytopic,new PeerDaemonListener(this,connection));

			if(connection.createTopic(masterTopic,1)==true){
				//gui.textArea.append("Ready to Start!\n");	
				gui.writeMessage("Ready to Start!\n");
				/*gui.getComboBoxServer().setEditable(false);
				gui.getComboBoxPort().setEditable(false);*/
			}
			else   
				gui.writeMessage("Connection Refused\nUnable to Connect to "+connection.getAdress().getIPaddress()+"\n");

		}catch (Exception e) { 
				e.printStackTrace();

		}

	}
		
}