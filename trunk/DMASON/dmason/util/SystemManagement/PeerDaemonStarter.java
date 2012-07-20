package dmason.util.SystemManagement;

import java.net.InetAddress;
import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * This is the Master Worker:its roles are connecting to the server,creating Service-Topics,sending informations to the Master.
 */
public class PeerDaemonStarter extends Thread
{
	/**
	 * Master's topic name
	 */
	private String masterTopic = "MASTER";
	
	/**
	 * My topic name
	 */
	private String myTopic;
	
	/**
	 * Connection to the provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	/**
	 * Reference to this worker user interface (either console or graphical).
	 */
	public StartWorkerInterface gui;

	/**
	 * Constructor.
	 * @param Connection to provider.
	 * @param ui A reference to the worker's UI (either console or graphical), needed to show messages to the user.
	 */
	public PeerDaemonStarter(Connection conn, StartWorkerInterface ui) 
	{
		this.gui = ui;
	
		try
		{
			connection = (ConnectionNFieldsWithActiveMQAPI)conn;
			myTopic = "SERVICE" + "-" + InetAddress.getLocalHost().getHostAddress() + "-" + System.currentTimeMillis();
			connection.createTopic(myTopic, 1);
			connection.subscribeToTopic(myTopic);
			connection.asynchronousReceive(myTopic, new PeerDaemonListener(this, connection));
	
			if (connection.createTopic(masterTopic, 1))
				ui.writeMessage("Ready to Start!\n");
			else   
				ui.writeMessage("Connection Refused\nUnable to Connect to " + connection.getAdress().getIPaddress() + "\n");
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	/**
	 * Sends to the master console system informations as
	 * operating system, architecture, number of cores, etc.
	 * @throws Exception Can throw a JMS exception if connection problems occurs.
	 */ 
	/* In my implementation i send these three but the library i've used
	 * provides more technical informations as Java heap memory space,
	 * system load average, Java Virtual Machine version, etc... 
	 */
	public void info() throws Exception
	{
		SystemManager sys = new SystemManager(myTopic);
		PeerStatusInfo info = sys.generate();
		connection.publishToTopic(info, masterTopic, "info");
	}
}