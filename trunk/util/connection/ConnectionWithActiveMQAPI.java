package dmason.util.connection;

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
/**
 * @author Ada Mancuso
 	This is a Connection implementations using JMS API and org.apache.activemq package for simplify the communication with the provider.
	The scenario is the following:
	-SCENARIO- A peer ,we don't make difference among the distribution, has generally a number of border regions for updates.
	 		   It initially have to create many topics as the number of regions,and in these ones it publishes updates.
	  		   In the same way, it has to subscribe itself to his neighborhoods regions' topics and set a listener to receive updates
	  		   in an asynchronous way.
	So,for our purpose, we've wrote this class to add some facilities.
	ConnectionWithActiveMQAPI provide a Connection object to enable the communication with the provider.Connection rules
	are completely hidden by ActiveMQConnection , so i have not had to worry about looking up objects from namespaces..:)
	The class also provide two separate sessions for publishers and subscribers, in order to obviously separates them in different
	threads.In our scenario there can be lots of publishers and subscribers, so we take them in two separate HashMap, indexed with
	topic's name.When we want to publish message0 to topic "topic://Spring" we can invoke publish method in this way:
				publish("Spring",message0);
	and, by indexing of the map, we take the publisher for that topic fast.
	Moreover we take also a topics' HashMap (we needed a list of created topics to access them by name, so we found HashMap appropriate).
	A detail of subscribe method is this: when we invoke subscribe method with a topic name doesn't match with an existing topic
	on the provider, the method automatically creates the topic, bacause of a peer when starts the simulation knows its
	neighborhoods,so it can immediately subscribe itself to them. If peer's neighborhoods haven't yet created their topics
	(network latency...) , peer can have a JavaNullPointerException, because there isn't a topic. 
	
	 */
	

public class ConnectionWithActiveMQAPI implements ConnectionWithJMS,Serializable{
	
	private ActiveMQConnection connection;
	private ActiveMQTopicSession pubSession;
	private ActiveMQTopicSession subSession;
	public HashMap<String,ActiveMQTopicPublisher> publishers;
	public HashMap<String,ActiveMQTopicSubscriber> subscribers;
	public HashMap<String,ActiveMQTopic> topics;
	public MessageListener listener;
	
	/** If you're implementing Connection your program has a standard behavior after receiving:
	 * you should use only a message listener and with this constructor you can set the 'class listener'.
	 * For more complex after-receiving actions you had to customize your class or interface...
	 * @param listener
	 */
	public ConnectionWithActiveMQAPI(MessageListener listener) {
		this.listener = listener;
	}
	
	/** Default constructor if you're implementing ConnectionWithJMS. */
	public ConnectionWithActiveMQAPI(){}
	
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
			subscribers = new HashMap<String, ActiveMQTopicSubscriber>();
			topics = new HashMap<String, ActiveMQTopic>();
			connection.start();
			return true;
		}catch (Exception e) {
			System.out.println("Cannot create a connection with the provider");
			e.printStackTrace();
			return false;
		}
		
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
	public boolean publishToTopic(Serializable arg,String mytopic) throws Exception{
		if (!mytopic.equals("") || !(arg == null))
		{
			ActiveMQObjectMessage msg ;
			try{
					msg = (ActiveMQObjectMessage) pubSession.createObjectMessage(arg);
					publishers.get(mytopic).publish(topics.get(mytopic),msg);
					return true;
			}catch (Exception e) {
					System.out.println("Impossibile to publish the given message");
					e.printStackTrace();
					return false;
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
	public boolean createTopic(String arg) throws Exception {
		try{
			ActiveMQTopic topic = new ActiveMQTopic(arg);
			topics.put(arg,topic);
			ActiveMQTopicPublisher p = (ActiveMQTopicPublisher) pubSession.createPublisher(topic);
			p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			publishers.put(arg,p);
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
