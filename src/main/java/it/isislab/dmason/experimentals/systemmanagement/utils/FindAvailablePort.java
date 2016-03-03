package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class FindAvailablePort {
	
	static final int MIN_PORT_NUMBER = 1000;
	static final int MAX_PORT_NUMBER = 3000;
	
	

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	private static boolean available(int port, int _min, int _max) {
	    if (port < _min || port > _max) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
	
	
	
	public static int getPortAvailable(){
		
		 int port=-1; 
		 for(int i=MIN_PORT_NUMBER; i< MAX_PORT_NUMBER; i++){
			 if(available(i, MIN_PORT_NUMBER, MAX_PORT_NUMBER)){
				 port=i; 
			     break;
			 }    
		 }
		 return port;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println(FindAvailablePort.getPortAvailable());
	}
	
	
}
