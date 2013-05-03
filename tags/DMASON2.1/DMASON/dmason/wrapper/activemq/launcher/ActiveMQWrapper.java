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


package dmason.wrapper.activemq.launcher;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;

import dmason.wrapper.activemq.rmi.Command;
import dmason.wrapper.data.BroadcastTask;



public class ActiveMQWrapper implements Command
{
	private static String ACTIVEMQ_HOME;

	private final static String WRAPPER_CONF = "configuration";
	private static ArrayList<String> command = new ArrayList<String>();
	
	private static long delay = 1;
	private static long period = 2000;
	
	
	private boolean isStarted = false;
	
	public ActiveMQWrapper() 
	{		
		try {
			startActiveMQ();
				
			
			InetAddress address = InetAddress.getLocalHost(); 
			String port = "61616";
			BroadcastTask bTask = new BroadcastTask(address.getHostAddress(), port);
			
			Timer t = new Timer();
			t.scheduleAtFixedRate(bTask, delay, period);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	@Override
	public boolean startActiveMQ() throws RemoteException 
	{
		// TODO Auto-generated method stub
		try {
			if(!isStarted())
			{
				command.clear();
				setCommand();
				command.add("start");
				
				System.out.println(command.toString());
				ProcessBuilder builder = new ProcessBuilder(command);
				
				try {
					Process process = builder.start();
					process.waitFor();
					isStarted = true;
					return true;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public boolean stopActiveMQ() throws RemoteException {
		// TODO Auto-generated method stub
		if(isStarted())
		{
			try {
				command.clear();
				setCommand();
				command.add("stop");

				System.out.println(command.toString());
				ProcessBuilder builderStop = new ProcessBuilder(command);

				try {
					Process process = builderStop.start();
					
					process.waitFor();
					//System.exit(0);
					isStarted = false;
					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return false;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean restartActiveMQ() throws RemoteException {
		// TODO Auto-generated method stub
		if(isStarted)
		{	
			if(stopActiveMQ())
			{
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(startActiveMQ())
					return true;
			}
				
			
			return false;
		}
		
		return false;
	}
	
	@Override
	public boolean isStarted() throws RemoteException {
		// TODO Auto-generated method stub
		return isStarted;
	}

	private static void setCommand() 
	{
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		
		if(os.getName().contains("Windows"))
		{
			command.add("cmd");
			command.add("/c");
			command.add("start");
			command.add(ACTIVEMQ_HOME+"\\bin\\activemq-admin");

		}
			
		if(os.getName().contains("Linux") || os.getName().contains("OS X"))
		{
			command.add(ACTIVEMQ_HOME+"/bin/activemq");

		}
	}
	
	public static void main(String[] args) 
	{
		System.setProperty("java.security.policy", ClassLoader.getSystemClassLoader().getResource(WRAPPER_CONF + "/policyall.policy").toString());
		
		loadWrapperConf();
		
		try {
			LocateRegistry.createRegistry(61617);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "ActiveMQWrapper";
            ActiveMQWrapper wrapper = new ActiveMQWrapper();
            Command executor = wrapper;
            Command executorStub = (Command) UnicastRemoteObject.exportObject(executor, 0);
			
            Registry registry = LocateRegistry.getRegistry(61617);
            registry.rebind(name, executorStub);
            System.out.println("CommandExecutor bound");
        } catch (Exception e) {
            System.err.println("CommandExecutor exception:");
            e.printStackTrace();
        }
        
    }

	
	private static void loadWrapperConf() {
		Properties prop = new Properties();
		 
    	try {
            prop.load( ClassLoader.getSystemClassLoader().getResourceAsStream(WRAPPER_CONF + "/wrapper.conf") );
 
    		delay = Long.parseLong(prop.getProperty("delay"));
    		period = Long.parseLong(prop.getProperty("period"));
    		ACTIVEMQ_HOME = prop.getProperty("ACTIVEMQ_HOME");
    		
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
	}
	
}
