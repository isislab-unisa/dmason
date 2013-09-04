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

package dmason.util.connection;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQTopicPublisher;
import org.apache.activemq.ActiveMQTopicSession;
import org.apache.activemq.ActiveMQTopicSubscriber;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.transport.TransportListener;

public class ConnectionNFieldsWithActiveMQAPI extends Observable implements ConnectionWithJMS, Serializable, TransportListener 
{
	private static final long serialVersionUID = -3803252417146440187L;

	private ActiveMQConnection connection;
	
	/**
	 * Publishers' topic session.
	 */
	private ActiveMQTopicSession pubSession;
	
	/**
	 * Address of the provider this object is connected to.
	 */
	private Address providerAddress;
	
	public Address getProviderAddress() {
		return providerAddress;
	}

	/**
	 * Subscribers' topic session.
	 */
	private ActiveMQTopicSession subSession;
	
	private HashMap<String,ActiveMQTopicPublisher> publishers;
	private HashMap<String, MyHashMap> contObj;
	private HashMap<String,ActiveMQTopicSubscriber> subscribers;
	private HashMap<String,ActiveMQTopic> topics;
	private MessageListener listener;
	
	/**
	 * Allows getting the list of topics.
	 */
	DestinationSource provider;
	
	
	private boolean isConnected = false;
	

	
	/** If you're implementing Connection your program has a standard behavior after receiving:
	 * you should use only a message listener and with this constructor you can set the 'class listener'.
	 * For more complex after-receiving actions you had to customize your class or interface...
	 * @param listener
	 */
	public ConnectionNFieldsWithActiveMQAPI(MessageListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Default constructor if you're implementing ConnectionWithJMS.
	 */
	public ConnectionNFieldsWithActiveMQAPI(){}
	
	/** 
	 * Establishes a connection with an ActiveMQ provider.
	 * @return true if the connection was successfully established, false otherwise.
	 */ 
	@Override
	public boolean setupConnection(Address providerAddr)
	{
		String strAddr = "failover:tcp://" + providerAddr.getIPaddress() + ":" + providerAddr.getPort();
		

		// Create an ActiveMQConnectionFactory
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(strAddr);		
		
		try
		{
			// Use the ActiveMQConnectionFactory to get an ActiveMQConnection object
			connection = (ActiveMQConnection) factory.createTopicConnection();
			// Create a topic session for publishers
			pubSession = (ActiveMQTopicSession) connection.createTopicSession(false, ActiveMQTopicSession.AUTO_ACKNOWLEDGE);
			// Create a topic session for subscribers
			subSession = (ActiveMQTopicSession) connection.createTopicSession(false, ActiveMQTopicSession.AUTO_ACKNOWLEDGE);
			
			//for reconnection 
			connection.addTransportListener(this);
			
			// initialize HashMaps
			publishers = new HashMap<String, ActiveMQTopicPublisher>();
			contObj = new HashMap<String, MyHashMap>();
			subscribers = new HashMap<String, ActiveMQTopicSubscriber>();
			topics = new HashMap<String, ActiveMQTopic>();
			// Enable the in-bound flow of messages
			providerAddress = providerAddr;
			connection.start();
			
			
			
			isConnected = true;
			
			provider = connection.getDestinationSource();
			
			return true;
		}catch (Exception e) {
			System.err.println("Unable to create a connection with the provider at address " + strAddr);
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void close() throws JMSException
	{
		connection.close();
	}
	
	public boolean isConnected()
	{
		return isConnected;
	}
	
	
	public Address getAddress()
	{
		return providerAddress;
	}

	@Override
	public boolean subscribeToTopic(String topicName) throws Exception{
		try
		{
			subscribers.put(topicName, (ActiveMQTopicSubscriber) subSession.createSubscriber(subSession.createTopic(topicName)));
			return true;
		} catch (Exception e) {
			System.err.println("Unable to subscribe to topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public synchronized boolean publishToTopic(Serializable object, String topicName, String key)
	{
		if (!topicName.equals("") || !(object == null))
		{
			MyHashMap mh = contObj.get(topicName);
			mh.put(key, (Object)object);
			contObj.put(topicName, mh);
			if(mh.isFull())
			{				
				ActiveMQObjectMessage msg ;
				try
				{		
					msg = (ActiveMQObjectMessage) pubSession.createObjectMessage(contObj.get(topicName));
					publishers.get(topicName).publish(topics.get(topicName), msg);
					MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
					contObj.put(topicName, mm);
					return true;
				} catch (Exception e) {
					System.err.println("Can't publish:" + "\n"
							+ "    topicName: " + topicName          + "\n"
							+ "    key      : " + key                + "\n"
							+ "    object   : " + object.toString() );
					e.printStackTrace();
					return false;
				}
			}	
		}
		
		return false;
	}

	/** 
	 * Allows to asynchronously receive updates, using a MessageListener
	 * that intercept the message as soon it is published.
	 * Since in a large number peer simulations we would need lots of
	 * subscribers, we associate a single MessageListener to each subscriber.
	 */
	@Override
	public boolean asynchronousReceive(String key){
		try {
			subscribers.get(key).setMessageListener(listener);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to enable asynchronous reception... probably no message listener set.");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * We extend Connection with ConnectionWithJMS only if we need a 
	 * customized listener for every (or many) topic.
	 * @oaram key A string associated to the object to receive.
	 * @param listener The listener to run when an object of type <code>key</code> is received. 
	 */
	@Override
	public boolean asynchronousReceive(String key, MyMessageListener listener)
	{
		try 
		{
			subscribers.get(key).setMessageListener(listener);
			
			
			return true;
		} catch (Exception e) {
			System.err.println("Failed to enable asynchronous reception.");
			e.printStackTrace();
			return false;
		}
	}
	
	// metodo che dovrebbe cancellare la sottoscrizone al topic ma in realtà non si capisce come fare per farlo????
		public boolean unsubscribe(String topicName) throws Exception{
			try{
				//pubSession.unsubscribe(topicName);
				//System.out.println("topicName: "+topicName);
				if (subscribers.get(topicName)==null)
					return false;
				subscribers.get(topicName).setMessageListener(null);
				subscribers.get(topicName).close();
				subscribers.get(topicName).stop();
				subscribers.remove(topicName);
				
				
				//publishers.remove(topicName);

				//subscribers.get(topicName).dispose();
				
				//subscribers.remove(topicName);
				
				/*Set<String> set=subscribers.keySet();
				Iterator<String> iter=set.iterator();
				String s;
				while(iter.hasNext()){
					s=iter.next();
					ActiveMQTopicSubscriber a=subscribers.get(s);
					System.out.println(a.toString());    
				}*/
				
				return true;
			}catch (Exception e) {
				System.err.println("Unable to unsubscribe to topic: " + topicName);
				e.printStackTrace();
				return false;
			}
		}
	

	/**
	 * Given a string, creates a topic's identifier, referring to a
	 * physical topic on the provider. Also creates a publisher, because
	 * when a peer creates a topic, certainly it will publish on it.
	 * @param topicName Identifier to assign to the newly created topic.
	 * @param numFields
	 * @return <code>true</code> if the connection was successfully established.
	 */
	@Override
	public boolean createTopic(String topicName, int numFields)
	{
		try
		{
			ActiveMQTopic topic = new ActiveMQTopic(topicName);
			topics.put(topicName,topic);
			contObj.put(topicName, new MyHashMap(numFields));
			ActiveMQTopicPublisher p = (ActiveMQTopicPublisher) pubSession.createPublisher(topic);
			p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			publishers.put(topicName,p);
			return true;
		} catch (Exception e) {
			System.err.println("Unable to create topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public ArrayList<String> getTopicList() throws Exception
	{
		
		// The list is retrieved using a DestinationSource object
		//DestinationSource provider = connection.getDestinationSource();
		/* If we call getDestinationSource() and then immediately after we call getTopics(),
		 * the latter may return an incomplete list. Waiting a second before building the list
		 * tries to address the problem. */
		//Thread.sleep(1000);
		Set<ActiveMQTopic> topics = provider.getTopics();
		ArrayList<String> list = new ArrayList<String>();
		Iterator<ActiveMQTopic> iter = topics.iterator();
		while(iter.hasNext()){ 
			String topic = iter.next().getTopicName();
				list.add(topic);
		}
		return list;
	}
		
	@Override
	public void setTable(HashMap table)
	{
		
	}

	public void cleanBeforeUpdate(String mytopic)
	{
		JMXServiceURL url = connectToJMX();
		JMXConnector jmxc;
		try {
			BrokerViewMBean mbean = getBrokerView(url);


			for (ObjectName topic : mbean.getTopics()) {
				// da sistemare! Non mi piace!
				String topicDestination = topic.getKeyProperty("Destination");
				if(topicDestination.contains(mytopic))
					mbean.removeTopic(topicDestination);
			}

		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	
	//Connect to ActiveMQ using jmx interface for topic cleanup
	public void resetTopic() 
	{
		//log.info("Connecting...");
		JMXServiceURL url = connectToJMX();

		try {

			BrokerViewMBean mbean = getBrokerView(url);

			for (ObjectName topic : mbean.getTopics()) 
			{

				String topicDestination = topic.getKeyProperty("Destination");
				if((!topicDestination.contains("Connection"))&&
						(!topicDestination.contains("Topic.SERVICE"))&&
						(!topicDestination.contains("Topic.MASTER"))&&
						(!topicDestination.contains("Advisory.Topic"))&&
						(!topicDestination.equals("MASTER"))&&
						(!topicDestination.equals("Advisory.Queue"))&&
						(!topicDestination.equals("Advisory.TempQueue"))&&
						(!topicDestination.equals("Advisory.TempTopic"))&&
						(!topicDestination.contains("SERVICE")))
				{
					
					mbean.removeTopic(topicDestination);
				
				}



			} 
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	public void resetBatchTopic(String topicPrefix) 
	{
		//log.info("Connecting...");
		JMXServiceURL url = connectToJMX();

		try {

			BrokerViewMBean mbean = getBrokerView(url);

			for (ObjectName topic : mbean.getTopics()) 
			{
				//System.out.println("TOPIC TO REMOVE: " +topicPrefix);

				String topicDestination = topic.getKeyProperty("Destination");
				if(((!topicDestination.contains("Connection"))&&
						(!topicDestination.contains("Topic.SERVICE"))&&
						(!topicDestination.contains("Topic.MASTER"))&&
						(!topicDestination.contains("Advisory.Topic"))&&
						(!topicDestination.equals("MASTER"))&&
						(!topicDestination.equals("Advisory.Queue"))&&
						(!topicDestination.equals("Advisory.TempQueue"))&&
						(!topicDestination.equals("Advisory.TempTopic"))&&
						(!topicDestination.contains("SERVICE"))) && topicDestination.contains(topicPrefix))
				{
					
					//System.out.println("REMOVED TOPIC: "+topicDestination);
					mbean.removeTopic(topicDestination);
				
				}



			} 
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	private BrokerViewMBean getBrokerView(JMXServiceURL url)
			throws IOException, MalformedObjectNameException {
		JMXConnector jmxc;
		jmxc = JMXConnectorFactory.connect(url);
		MBeanServerConnection conn = jmxc.getMBeanServerConnection();
		
		ObjectName activeMQ = new ObjectName("org.apache.activemq:BrokerName=localhost,Type=Broker");
		BrokerViewMBean mbean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(conn, activeMQ,BrokerViewMBean.class, true);
		return mbean;
	}

	private JMXServiceURL connectToJMX() {
		JMXServiceURL url = null;
		try {
			url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+providerAddress.getIPaddress()+":1616/jmxrmi");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	//TransportListner method 
	@Override
	public void onCommand(Object arg0) {
		
		//not implemented 
	}

	@Override
	public void onException(IOException arg0) {
		
		//not implemented 
	}

	@Override
	public void transportInterupted() {
		// Notify observers of change
		
		isConnected = false;
		setChanged();
		notifyObservers();
		
	}

	@Override
	public void transportResumed() {
		
		isConnected = true;
		 // Notify observers of change
	    setChanged();
	    notifyObservers();
	}
}
