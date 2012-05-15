package dmason.util.SystemManagement;

import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

/**
 * Executable, command-line version worker.
 */
public class StartWorker implements StartWorkerInterface {
	/**
	 * Connection with a provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	/**
	 * Provider's address.
	 */
	private Address ipAddress;
	
	/**
	 * Constructor.
	 * @param ip IP Address of the provider.
	 * @param port Port where the provider is listening.
	 */
	public StartWorker(String ip, String port)
	{
		connection = new ConnectionNFieldsWithActiveMQAPI();
	    ipAddress = new Address(ip, port);
	}
	
	/**
	 * Setup the connection with the provider.
	 * @return
	 */
	public boolean startConnection(){
		try
		{
			connection.setupConnection(ipAddress);
			new PeerDaemonStarter(connection, this);
			return true;
		} catch (Exception e1) {
			System.out.println("Failed to connect with the provider at " + ipAddress);
			return false;
		}
	}
	
	
	public static void main(String[] args){
		StartWorker worker = new StartWorker(args[0],args[1]);
		worker.startConnection();
		System.out.println("IP "+worker.ipAddress.getIPaddress());
		System.out.println("Port "+worker.ipAddress.getPort());
	}


	@Override
	public void writeMessage(String message)
	{
		System.out.println(message);
	}

}
