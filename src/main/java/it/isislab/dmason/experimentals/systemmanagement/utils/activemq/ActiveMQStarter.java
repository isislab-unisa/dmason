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
package it.isislab.dmason.experimentals.systemmanagement.utils.activemq;

import java.io.File;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.activemq.usage.UsageCapacity;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;


/**
 * Embedded ActivemQ Starter  
 * set config.properties to change ActivemQ ip:port  
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class ActiveMQStarter {
	private BrokerService broker = null;
	private Configuration startConfig = null;
	private String IP_ACTIVEMQ;
	private String PORT_ACTIVEMQ;

	// ActiveMQ settings file, default 127.0.0.1:61616 otherwise you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH = "resources/systemmanagement/master/conf/config.properties";

	/**
	 * Embedded starter for ActivemQ
	 */
	public ActiveMQStarter() {
		broker = new BrokerService();

		// use Apache Commons Configuration to
		// extract configuration from properties file
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
				new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
				.configure(
						params.properties().setFileName(PROPERTIES_FILE_PATH)
				);
		try
		{
			startConfig = builder.getConfiguration();
		}
		catch (ConfigurationException e)
		{
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		final String PROPERTY_PREFIX = "activemq".concat(".");
		IP_ACTIVEMQ = startConfig.getString(PROPERTY_PREFIX.concat("ipmaster"));
		PORT_ACTIVEMQ = startConfig.getString(PROPERTY_PREFIX.concat("portmaster"));
		System.out.println(IP_ACTIVEMQ + " " + PORT_ACTIVEMQ);
	}

	/**
	 * Start ActivemQ service
	 */
	public void startActivemq() {
		String address = "tcp://" + IP_ACTIVEMQ + ":" + PORT_ACTIVEMQ;
		try {
			/* code to set ActivemQ configuration 
			for tempUsage property a big value can cause error 
			for node with low disk space */ 

			String os = System.getProperty("os.name").toLowerCase();
			File rootFileSystem = null;;

			Long val = new Long(1000000000);

			if (os.contains("linux")) {
				rootFileSystem = new File("/");
				val = new Long(rootFileSystem.getFreeSpace()/2); 
			} else if(os.contains("windows")) {
				//
				System.out.println("windows system using 1Gb for tempUsage");
			}

			TempUsage usage = new TempUsage();
			UsageCapacity c = broker.getSystemUsage().getTempUsage().getLimiter();
			c.setLimit(val);
			usage.setLimiter(c);
			SystemUsage su = broker.getSystemUsage();
			su.setTempUsage(usage);
			broker.setSystemUsage(su);
			/*     end code for tempUsage setting    */
			broker.addConnector(address);
			broker.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	// start ActivemQ
	public static void main(String[] args) {
		ActiveMQStarter activemq = new ActiveMQStarter();
		activemq.startActivemq();
	}
}
