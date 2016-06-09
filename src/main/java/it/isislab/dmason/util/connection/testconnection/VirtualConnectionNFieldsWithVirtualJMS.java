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

package it.isislab.dmason.util.connection.testconnection;

import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQTopicPublisher;
import org.apache.activemq.ActiveMQTopicSession;
import org.apache.activemq.ActiveMQTopicSubscriber;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.transport.TransportListener;

public class VirtualConnectionNFieldsWithVirtualJMS extends Observable
		implements ConnectionJMS, Serializable, TransportListener {
	
	 static class SyncroAccessData{
			private  Lock aLock = new ReentrantLock();
			private  Condition condVar = aLock.newCondition();
			
			public SyncroAccessData() {
				// TODO Auto-generated constructor stub
			}
			private void ensureAccess(String topic_name)
			{
				aLock.lock();
				
				while((vsubscribers.get(topic_name)==null) /*|| (vsubscribers.get(topic_name).size()!=8)*/)
				{
					//System.out.println("[print from "+this.getClass().getSimpleName()+"] "+topic_name+" "+vsubscribers);
					try {
						 
						condVar.await();
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						System.out.println("unlock");
			            aLock.unlock();
			        }
				}

				aLock.unlock();
			}
			public void addSubscriber()
			{
				aLock.lock();
				 condVar.signalAll();
				aLock.unlock();
			}
		}
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

	private HashMap<String, ActiveMQTopicPublisher> publishers;
	private HashMap<String, MyHashMap> contObj;
	private HashMap<String, ActiveMQTopicSubscriber> subscribers;
	private HashMap<String, ActiveMQTopic> topics;
	private MessageListener listener;

	/**
	 * Allows getting the list of topics.
	 */
	DestinationSource provider;

	private boolean isConnected = false;

	/**
	 * If you're implementing Connection your program has a standard behavior
	 * after receiving: you should use only a message listener and with this
	 * constructor you can set the 'class listener'. For more complex
	 * after-receiving actions you had to customize your class or interface...
	 * 
	 * @param listener
	 */
	public VirtualConnectionNFieldsWithVirtualJMS(MessageListener listener) {
		this.listener = listener;
	}

	/**
	 * Default constructor if you're implementing ConnectionWithJMS.
	 */
	public VirtualConnectionNFieldsWithVirtualJMS() {
//		System.out.println("Fake Connection Created");
	}

	/**
	 * Establishes a connection with an ActiveMQ provider.
	 * 
	 * @return true if the connection was successfully established, false
	 *         otherwise.
	 */
	@Override
	public boolean setupConnection(Address providerAddr) {

		publishers = new HashMap<String, ActiveMQTopicPublisher>();
		contObj = new HashMap<String, MyHashMap>();
		subscribers = new HashMap<String, ActiveMQTopicSubscriber>();
		topics = new HashMap<String, ActiveMQTopic>();
		return true;

	}

	public void close() throws JMSException {
		// connection.close();
	}

	public boolean isConnected() {
		return true;
	}

	public Address getAddress() {
		return providerAddress;
	}

	@Override
	public boolean subscribeToTopic(String topicName) throws Exception {
		try {

			return true;
		} catch (Exception e) {
			System.err.println("Unable to subscribe to topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}
	
	
    SyncroAccessData accesstotopic=new VirtualConnectionNFieldsWithVirtualJMS.SyncroAccessData();
	
	
	@Override
	public synchronized boolean publishToTopic(Serializable object,
			String topicName, String key) {
		
		accesstotopic.ensureAccess(topicName);
	

		if (!topicName.equals("") || !(object == null)) {
			MyHashMap mh = contObj.get(topicName);
			mh.put(key, object);
			contObj.put(topicName, mh);
			if (mh.isFull()) {

				try {

					ActiveMQObjectMessage msg = new ActiveMQObjectMessage();
					msg.setObjectProperty("data", mh);

					try {
						for (VirtualMessageListener listener : vsubscribers
								.get(topicName)) {
							
							listener.onMessage(msg);
						}
						MyHashMap mm = new MyHashMap(mh.NUMBER_FIELDS);
						contObj.put(topicName, mm);
						return true;
					} catch (Exception e) {
						System.err.println("Can't publish:" + "\n"
								+ "    topicName: " + topicName + "\n"
								+ "    key      : " + key + "\n"
								+ "    object   : " + object.toString());
						e.printStackTrace();
						return false;
					}
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}

		return false;
	}
	/**
	 * Given a string, creates a topic's identifier, referring to a physical
	 * topic on the provider. Also creates a publisher, because when a peer
	 * creates a topic, certainly it will publish on it.
	 * 
	 * @param topicName
	 *            Identifier to assign to the newly created topic.
	 * @param numFields
	 * @return <code>true</code> if the connection was successfully established.
	 */
	@Override
	public boolean createTopic(String topicName, int numFields) {
		try {
			ActiveMQTopic topic = new ActiveMQTopic(topicName);
			topics.put(topicName, topic);
			contObj.put(topicName, new MyHashMap(numFields));
		
			// ActiveMQTopicPublisher p = (ActiveMQTopicPublisher)
			// pubSession.createPublisher(topic);
			// p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			// publishers.put(topicName,p);
			return true;
		} catch (Exception e) {
			System.err.println("Unable to create topic: " + topicName);
			e.printStackTrace();
			return false;
		}
	}

	static ConcurrentHashMap<String, ArrayList<VirtualMessageListener>> vsubscribers = new ConcurrentHashMap<String, ArrayList<VirtualMessageListener>>();

	/**
	 * Allows to asynchronously receive updates, using a MessageListener that
	 * intercept the message as soon it is published. Since in a large number
	 * peer simulations we would need lots of subscribers, we associate a single
	 * MessageListener to each subscriber.
	 */
	@Override
	public boolean asynchronousReceive(String key) {
		return false;
	}

	/**
	 * We extend Connection with ConnectionWithJMS only if we need a customized
	 * listener for every (or many) topic.
	 * 
	 * @param key A string associated to the object to receive.
	 * @param listener
	 *            The listener to run when an object of type <code>key</code> is
	 *            received.
	 */
	@Override
	public boolean asynchronousReceive(String key, MyMessageListener listener) {
		try {
			// subscribers.get(key).setMessageListener(listener);
			ArrayList<VirtualMessageListener> lists = vsubscribers.get(key);
			lists = (lists == null) ? new ArrayList<VirtualMessageListener>(): lists;
			lists.add((VirtualMessageListener) listener);
			accesstotopic.addSubscriber();
			return vsubscribers.put(key, lists) != null;

			// return true;
		} catch (Exception e) {
			System.err.println("Failed to enable asynchronous reception.");
			e.printStackTrace();
			return false;
		}
	}

	// metodo che dovrebbe cancellare la sottoscrizone al topic ma in realt� non
	// si capisce come fare per farlo????
	@Override
	public boolean unsubscribe(String topicName) throws Exception {
		return vsubscribers.remove(topicName) == null;
	}



	@Override
	public ArrayList<String> getTopicList() throws Exception {

		// The list is retrieved using a DestinationSource object
		// DestinationSource provider = connection.getDestinationSource();
		/*
		 * If we call getDestinationSource() and then immediately after we call
		 * getTopics(), the latter may return an incomplete list. Waiting a
		 * second before building the list tries to address the problem.
		 */
		// Thread.sleep(1000);
		Set<ActiveMQTopic> topics = provider.getTopics();
		ArrayList<String> list = new ArrayList<String>();
		Iterator<ActiveMQTopic> iter = topics.iterator();
		while (iter.hasNext()) {
			String topic = iter.next().getTopicName();
			list.add(topic);
		}
		return list;
	}

	@Override
	public void setTable(HashMap table) {

	}

	// TransportListner method
	@Override
	public void onCommand(Object arg0) {

		// not implemented
	}

	@Override
	public void onException(IOException arg0) {

		// not implemented
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

	/*public static void main(String[] args) {
		try {

			ArrayList<String> fakefieldone = new ArrayList<String>();
			fakefieldone.add("firstfieldone");
			fakefieldone.add("secondfieldone");

			ArrayList<String> fakefieldtwo = new ArrayList<String>();
			fakefieldtwo.add("firstfieldone");

			VirtualConnectionNFieldsWithVirtualJMS one = new VirtualConnectionNFieldsWithVirtualJMS();
			VirtualConnectionNFieldsWithVirtualJMS two = new VirtualConnectionNFieldsWithVirtualJMS();
			one.setupConnection(null);
			two.setupConnection(null);

			one.createTopic("MARIO-HOME", 2);
			one.createTopic("MARIO-WORK", 1);

			two.createTopic("RED-HOME", 1);

			one.subscribeToTopic("RED-HOME");
			two.subscribeToTopic("MARIO-HOME");
			two.subscribeToTopic("MARIO-WORK");

			one.asynchronousReceive("RED-HOME", new VirtualMessageListener(
					fakefieldtwo, "RED-HOME", "red"));

			two.asynchronousReceive("MARIO-HOME", new VirtualMessageListener(
					fakefieldone, "MARIO-HOME", "mario"));
			two.asynchronousReceive("MARIO-WORK", new VirtualMessageListener(
					fakefieldtwo, "MARIO-WORK", "mario"));

			
			
			
			one.publishToTopic("Come va a casa mario?", "MARIO-HOME",
					"firstfieldone");
			one.publishToTopic("Come va a casa mario 2 ?", "MARIO-HOME",
					"secondfieldone");
			one.publishToTopic("Come va a lavoro mario?", "MARIO-WORK",
					"firstfieldone");

			two.publishToTopic("Come va a casa red?", "RED-HOME",
					"firstfieldone");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/

}
