package dmason.util.SystemManagement;

import dmason.util.connection.Address;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class StartWorker implements StartWorkerInterface {

	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private Address addressIP;
	

	public StartWorker(String ip,String port) {
		connection = new ConnectionNFieldsWithActiveMQAPI();
	    addressIP=new Address(ip, port);
	
	}
	
	
	public boolean start_connection(){
		try {
			connection.setupConnection(addressIP);

			PeerDaemonStarter p = new PeerDaemonStarter(connection,this);
			return true;
		} catch (Exception e1) {
			
			e1.printStackTrace();
			return false;
		}
	}
	
	
	
	public static void main(String[] args){

		StartWorker worker = new StartWorker(args[0],args[1]);
		worker.start_connection();
		System.out.println("IP "+worker.addressIP.getIPaddress());
		System.out.println("Port "+worker.addressIP.getPort());
	}


	@Override
	public void writeMessage(String message) {
		
		System.out.println(message);
		
	}
	
	
	
}
