package dmason.util.SystemManagement;

import java.net.InetAddress;
import dmason.util.connection.Address;
import dmason.util.connection.ConnectionWithActiveMQAPI;

/**
 * @author Ada Mancuso
 * This is the Master Worker:its roles are connecting to the server,creating Service-Topics,sending informations to the Master.
 */
public class PeerDaemonStarter extends Thread{
	

	private String masterTopic = "MASTER";
	private String mytopic;
	private ConnectionWithActiveMQAPI connection;
	public StartWorkerWithGui gui;
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
		connection.publishToTopic(info,masterTopic);
	}
	
	/**
	 * Constructor.
	 * @param ip address
	 * @param gui Because of this class is called by a simple UI,to update UI's labels during the simulation,we keep the
	 * 		  graphic component.
	 */
	public PeerDaemonStarter(String ip,StartWorkerWithGui gui)
	{
		this.gui = gui;
		connection = new ConnectionWithActiveMQAPI();
		try{
			address = new Address(ip,"61616");
			result=connection.setupConnection(address);
			if(result)
			{
				String add = InetAddress.getLocalHost().getHostAddress();
				mytopic = "SERVICE"+"-"+add;
				connection.createTopic(mytopic);
				connection.subscribeToTopic(mytopic);	
				connection.asynchronousReceive(mytopic,new PeerDaemonListener(this));
				connection.createTopic(masterTopic);
				gui.textArea.append("Ready to Start!\n");
			}
			else
				gui.textArea.append("Impossible to connect to the server!\n");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}