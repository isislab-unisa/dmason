package dmason.util.connection;

import java.io.Serializable;
import java.util.ArrayList;

import dmason.sim.field.CellType;

/**
 * This interface abstracts a communication-layer using publish/subscribe paradigm.
 * Anyone can implement these methods using a preferred way to communicate.
 */
public interface Connection {
	
	/**
	 * Estabilishes a connection with a provider located at given address.
	 * @param providerAddr Address of the provider.
	 */
	public boolean setupConnection(Address providerAddr) throws Exception;
	
	/**
	 * Create an identifier to physical topic using the given string. 
	 */
	public boolean createTopic(String topicName, int numFields) throws Exception;
	
	/**
	 * Write a Serializable object to a topic. Using the generic type
	 * Serializable  allows from type indipendency.
	 * @param object The Serializable object to publish.
	 * @param topicName The name of the topic where <code>obejct</code> will be published.
	 * @param key A string associated to the <code>object</code>.
	 */
	public boolean publishToTopic(Serializable object, String topicName, String key) throws Exception;
	
	/**
	 * Subscribes the peer to a topic named as the given string.
	 * @param topicName Name of the topic to subscribe to.
	 */
	public boolean subscribeToTopic(String topicName) throws Exception;
	
	/**
	 * Allow client to to receive messages asynchronously.
	 * @param key A string associated to the object to receive.
	 */
	public boolean asynchronousReceive(String key);
	
	/**
	 * Retrieves the list of active topics on the provider.
	 */
	public ArrayList<String> getTopicList() throws Exception;
	
	//modificato aggiunto da me
	public boolean unsubscribe(String topicName) throws Exception;

}
