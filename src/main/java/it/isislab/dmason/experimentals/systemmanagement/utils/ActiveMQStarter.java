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
package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.activemq.usage.UsageCapacity;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

/**
 * Start Activemq 
 * @author miccar
 *
 */
public class ActiveMQStarter {

	
	private  BrokerService broker=null;
	private  Properties startProperties = null;
	private  String IP_ACTIVEMQ;
	private  String PORT_ACTIVEMQ;

	//ActivemQ settings file, default 127.0.0.1:61616 otherwise you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH="resources/systemmanagement/master/conf/config.properties";
	
	
	public ActiveMQStarter(){
		startProperties = new Properties();
		broker = new BrokerService();
		InputStream input=null;
		//load params from properties file 
		try {
			input=new FileInputStream(PROPERTIES_FILE_PATH);	
			startProperties.load(input);
			IP_ACTIVEMQ=startProperties.getProperty("ipmaster");
			PORT_ACTIVEMQ=startProperties.getProperty("portmaster");
			System.out.println(IP_ACTIVEMQ +" "+PORT_ACTIVEMQ);
			
			

		} catch (IOException e2) {
			System.err.println(e2.getMessage());
		}finally{
			try {input.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				}
			}
	}
	

	//start ActivemQ service
	public void startActivemq(){
		
		String address="tcp://"+IP_ACTIVEMQ+":"+PORT_ACTIVEMQ;
		try {
			/*code to set ActivemQ configuration 
			for tempUsage property a big value can cause error 
			for node with low disk space*/ 

			String os=System.getProperty("os.name").toLowerCase();
			File rootFileSystem=null;;

			Long val=new Long(1000000000);

			if(os.contains("linux")){
				rootFileSystem=new File("/");
				val=new Long(rootFileSystem.getFreeSpace()/2); 
			}else if(os.contains("windows")){
				//
				System.out.println("windows system using 1Gb for tempUsage");
			}

			TempUsage usage=new TempUsage();
			UsageCapacity c=broker.getSystemUsage().getTempUsage().getLimiter();
			c.setLimit(val);
			usage.setLimiter(c);
			SystemUsage su = broker.getSystemUsage();
			su.setTempUsage(usage);
			broker.setSystemUsage(su);
			/*     end code for tempUsage setting    */
			broker.addConnector(address);
			broker.start();
			
			ConnectionNFieldsWithActiveMQAPI conn=new ConnectionNFieldsWithActiveMQAPI();
			conn.setupConnection(new Address(IP_ACTIVEMQ, PORT_ACTIVEMQ));
			conn.createTopic("MANAGEMENT", 1);
			
			
		} catch (Exception e1) {e1.printStackTrace();}
	}
	
}
