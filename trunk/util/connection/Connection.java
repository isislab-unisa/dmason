package dmason.util.connection;

import java.io.Serializable;
import java.util.ArrayList;

/** This interface abstracts a communication-layer using publish/subscribe paradigm.
 Anyone can implement these methods using a preferred way to communicate.*/
public interface Connection {
	
	/** Setup the connection with a server,a provider,a node managing the communication... */
	public boolean setupConnection(Address arg) throws Exception;
	
	/** Create an identifier to physical topic using the given string */
	public boolean createTopic(String arg) throws Exception;
	
	/** publish a Serializable object,to allow type-freedom, on a topic */
	public boolean publishToTopic(Serializable arg,String mytopic) throws Exception;
	
	/** Subscribe the client to the topic named with the peer. */
	public boolean subscribeToTopic(String arg)throws Exception;
	
	/** Allow client to to receive in asynchronous way messages. */
	public boolean asynchronousReceive(String arg);
	
	/** Return the list of activate topics. */
	public ArrayList<String> getTopicList()throws Exception;

}
