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

import java.net.InetAddress;
import java.rmi.server.UID;

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
	
	private String batchTopic;
	
	/**
	 * Connection to the provider.
	 */
	private ConnectionNFieldsWithActiveMQAPI connection;
	
	/**
	 * Reference to this worker user interface (either console or graphical).
	 */
	public StartWorkerInterface gui;

	private String version;

	private String digest;

	/**
	 * Constructor.
	 * @param Connection to provider.
	 * @param ui A reference to the worker's UI (either console or graphical), needed to show messages to the user.
	 */
	public PeerDaemonStarter(Connection conn, StartWorkerInterface ui,String version,String digest) 
	{
		this.gui = ui;
		this.version = version;
		this.digest = digest;
		
		boolean isWorkerUI = false;
		if (ui instanceof StartWorker) 
			isWorkerUI = false;
		
		if (ui instanceof StartWorkerWithGui)
			isWorkerUI = true;
	
		try
		{
			connection = (ConnectionNFieldsWithActiveMQAPI)conn;
			
			// We create a global univoque topic name by concatenating localhosts's IP and a Java RMI UID
			myTopic = "SERVICE" + "-" + InetAddress.getLocalHost().getHostAddress() + "-" + new UID();
			connection.createTopic(myTopic, 1);
			connection.subscribeToTopic(myTopic);
			
			//myTopic is used for delete the relative when the peer shutdown for update
			connection.asynchronousReceive(myTopic, new PeerDaemonListener(this, connection,myTopic,isWorkerUI));
	
			if (connection.createTopic(masterTopic, 1))
				ui.writeMessage("Connection successfull! Ready to start...\n");
			else   
				ui.writeMessage("Connection Refused\nUnable to Connect to " + connection.getAddress().getIPaddress() + "\n");
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	public PeerDaemonStarter(Connection conn, StartWorkerInterface ui, String topic,String version, String digest,boolean updated, boolean isBatch, String topicPrefix) 
	{
		
		this.gui = ui;
		this.version = version;
		this.digest = digest;
		
		boolean isWorkerUI = false;
		if (ui instanceof StartWorker) 
			isWorkerUI = false;
		
		if (ui instanceof StartWorkerWithGui)
			isWorkerUI = true;
		
		try
		{
			connection = (ConnectionNFieldsWithActiveMQAPI)conn;
			myTopic = topic;
			//connection.createTopic(topic, 1);
			
			connection.subscribeToTopic(topic);
			ui.writeMessage("Subscribed to topic: "+topic+"\n");
			//myTopic is used for delete the relative when the peer shutdown for update
			
			connection.asynchronousReceive(topic, new PeerDaemonListener(this, connection,topic,isWorkerUI));
			
			if (connection.createTopic(masterTopic, 1))
			{
				ui.writeMessage("Connection successfull! Ready to start...\n");
				
				if(updated)
					updateDone();
				else 
				{
					if(!isBatch)
						info();
					else
					{
						info();
						subscribeToBatch(topicPrefix);
						sendBatchInfo("info");
					}
				}
			}
			else   
				ui.writeMessage("Connection Refused\nUnable to Connect to " + connection.getAddress().getIPaddress() + "\n");
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
	}

	/**
	 * Sends to the master console the message that the worker was updated
	 * @throws Exception 
	 */
	public void updateDone() throws Exception
	{
		PeerStatusInfo info = getInfo();
		connection.publishToTopic(info, masterTopic, "updated");
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
	public void info(String statusMessage) throws Exception
	{
		PeerStatusInfo info = getInfo();
		info.setStatus(statusMessage);
		connection.publishToTopic(info, masterTopic, "info");
	}
	
	public void info() throws Exception
	{
		// TODO Should remove info() since workers should always specify their status
		info("");
	}

	private PeerStatusInfo getInfo() throws Exception {
		SystemManager sys = new SystemManager(myTopic);
		PeerStatusInfo info = sys.generate();
		info.setVersion(version); //used only for show it on master console
		info.setDigest(digest); // the core of update mechanism
		info.setTopic(myTopic);
		return info;
	}

	public void subscribeToBatch(String topic)
	{
		try {
			connection.createTopic(topic, 1);
			
			batchTopic = topic;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendBatchInfo(String msg)
	{
		connection.publishToTopic("batch", batchTopic, msg);
	}
	public void testFinished() 
	{

		connection.publishToTopic("batch",masterTopic, "batch");
	}
}