/**
 * Copyright 2016 Universita' degli Studi di Salerno


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
package it.isislab.dmason.util.connection.jms;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import it.isislab.dmason.experimentals.util.management.wrapper.activemq.rmi.Command;
/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
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
