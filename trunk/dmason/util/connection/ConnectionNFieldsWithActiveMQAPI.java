package dmason.util.connection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.jms.DeliveryMode;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQTopicPublisher;
import org.apache.activemq.ActiveMQTopicSession;
import org.apache.activemq.ActiveMQTopicSubscriber;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTopic;

import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegionInterface;
import dmason.sim.field.DistributedRegionNumeric;

public class ConnectionNFieldsWithActiveMQAPI implements ConnectionWithJMS,Serializable{
	
	private ActiveMQConnection connection;
	private ActiveMQTopicSession pubSession;
	private ActiveMQTopicSession subSession;
	private HashMap<String,ActiveMQTopicPublisher> publishers;
	private Address adress;
	private HashMap<String, MyHashMap> contObj;
	private HashMap<String,ActiveMQTopicSubscriber> subscribers;
	private HashMap<String,ActiveMQTopic> topics;
	private MessageListener listener;
	
	/** If you're implementing Connection your program has a standard behavior after receiving:
	 * you should use only a message listener and with this constructor you can set the 'class listener'.
	 * For more complex after-receiving actions you had to customize your class or interface...
	 * @param listener
	 */
	public ConnectionNFieldsWithActiveMQAPI(MessageListener listener) {
		this.listener = listener;
	}
	
	/** Default constructor if you're implementing ConnectionWithJMS. */
	public ConnectionNFieldsWithActiveMQAPI(){}
	
	/** First we create an ActiveMQConnection object to connect to the provider,located at the given address,through
	 *  an ActiveMQConnectionFactory object .
		Then we create two topic session,one for publishers and one for subscribers .
		We initialize HashMaps.
		Finally we have to invoke the start() method on the connection to enable the inbound flow of messages.*/
	@Override
	public boolean setupConnection(Address arg) throws Exception{
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://"+arg.getIPaddress()+":"+arg.getPort());
		try{
			connection = (ActiveMQConnection) factory.createTopicConnection();
			pubSession = (ActiveMQTopicSession) connection.createTopicSession(false, ActiveMQTopicSession.AUTO_ACKNOWLEDGE);
			subSession = (ActiveMQTopicSession) connection.createTopicSession(false, ActiveMQTopicSession.AUTO_ACKNOWLEDGE);
			publishers = new HashMap<String, ActiveMQTopicPublisher>();
			
			contObj = new HashMap<String, MyHashMap>();
			
			subscribers = new HashMap<String, ActiveMQTopicSubscriber>();
			topics = new HashMap<String, ActiveMQTopic>();
			adress = arg;
			connection.start();
			return true;
		}catch (Exception e) {
			System.out.println("Cannot create a connection with the provider");
			e.printStackTrace();
			return false;
		}
		
	}
	public Address getAdress() {
		return adress;
	}

	/**
	 * Subscribes the peer to a topic named as the given string.
	 */
	@Override
	public boolean subscribeToTopic(String arg) throws Exception{
		try{
			subscribers.put(arg, (ActiveMQTopicSubscriber) subSession.createSubscriber(subSession.createTopic(arg)));
			return true;
		}catch (Exception e) {
			System.out.println("Impossibile to subscribe to given topic");
			e.printStackTrace();
			return false;
		}
	}


	
	/** This method write any type of Serializable object to a topic, identified by its name.*/
	@Override
	public synchronized boolean publishToTopic(Serializable arg,String mytopic, String name) throws Exception
	{
		if (!mytopic.equals("") || !(arg == null))
		{
			MyHashMap mh = contObj.get(mytopic);
			mh.put(name, (Object)arg);
			contObj.put(mytopic, mh);
			if(mh.isFull()){
				
				ActiveMQObjectMessage msg ;
				try{				
					if(mytopic.equals("GRAPHICS1-1"))
						System.out.println("Invio con numero "+mh.NUMBER_FIELDS);
						msg = (ActiveMQObjectMessage) pubSession.createObjectMessage(contObj.get(mytopic));
						
						publishers.get(mytopic).publish(topics.get(mytopic),msg);
						
						MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
						contObj.put(mytopic, mm);
						
						return true;
					}catch (Exception e) {
					
						System.out.println("Impossibile to publish the given message");
						e.printStackTrace();
						return false;
					}
				}	
			}
		
		return false;
	}

	/** It allows to asynchronously receive updates,using a MessageListener that intercept the message as soon it is published.
	 * 	Because of, in a large number peer simulations, we would need lots of subscribers, we associate a MessageListener
	 *  to for each subscriber.
	 * */
	@Override
	public boolean asynchronousReceive(String arg){
		try {
			subscribers.get(arg).setMessageListener(listener);
			return true;
		} catch (Exception e) {
			System.out.println("Impossibile to receive...probably no message listener set.");
			e.printStackTrace();
			return false;
		}
	}

	/** Given a string , it creates a topic's identifier, reffering to a physical topic on the provider,
	 * 	create a publishers, because of when we a peer create a topic, certainly it will publish
	 * 	updates on it. */
	@Override
	public boolean createTopic(String arg, int numFields) throws Exception {
		try{
			
			ActiveMQTopic topic = new ActiveMQTopic(arg);
			topics.put(arg,topic);
			contObj.put(arg, new MyHashMap(numFields));
			ActiveMQTopicPublisher p = (ActiveMQTopicPublisher) pubSession.createPublisher(topic);
			p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			publishers.put(arg,p);
			if(arg.equals("GRAPHICS1-1"))
				System.out.println("Creo hash con taglia "+numFields);
			return true;
			
		}catch (Exception e) {
			System.out.println("Impossibile creare topic!");
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	/** We extend Connection with ConnectionWithJMS only if we need a customized listener for every (or many) topic .*/
	@Override
	public boolean asynchronousReceive(String arg0,MyMessageListener arg1){
		try {
			subscribers.get(arg0).setMessageListener(arg1);
			return true;
		} catch (Exception e) {
			System.out.println("Impossibile ricevere in maniera asincrona");
			e.printStackTrace();
			return false;
		}
	}
	
	
	/** Using a DestinationSource object,we can obtain the list of physical topics on the provider. */
	@Override
	public ArrayList<String> getTopicList() throws Exception {
		DestinationSource provider = connection.getDestinationSource();
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
	public void setTable(HashMap table) {	
	}
}
