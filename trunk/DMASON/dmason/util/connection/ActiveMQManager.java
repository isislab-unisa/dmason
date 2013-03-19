package dmason.util.connection;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import dmason.wrapper.activemq.rmi.Command;


public class ActiveMQManager {

	private boolean isStarted;
	private boolean isUnknow;
	private Command stub;
	
	public ActiveMQManager(String ip, int port) 
	{
		try {
			if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }
			System.out.println(ip+"-"+port);
		    Registry registry = LocateRegistry.getRegistry(ip, port);
		    stub = null;
			try {
				stub = (Command) registry.lookup("ActiveMQWrapper");
			} catch (Exception e) {
		    	//isStarted = false;
				 //e.printStackTrace();
				isUnknow = true;
			}
			
			if(stub != null)
			{	
				isStarted = true;
				isUnknow = false;
			}
		   
		} catch (Exception e) {
		    System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		    isStarted = false;
		    isUnknow = false;
		}
	}
	
	public boolean startActiveMQ()  
	{
		try {
			return stub.startActiveMQ();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	public boolean stopActiveMQ()  {
		try {
			return stub.stopActiveMQ();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean restartActiveMQ()  {
		
		try {
			return stub.restartActiveMQ();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	public boolean isUnknow() {
		return isUnknow;
	}

	public void setUnknow(boolean isUnknow) {
		this.isUnknow = isUnknow;
	}

	public boolean isStarted()  {
		
		if(!isStarted)
			return false;
		else
		{
			try {
				return stub.isStarted();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
	}

}
