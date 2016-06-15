package it.isislab.dmason.test.util.connection;

import java.io.Serializable;
import java.util.ArrayList;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.Connection;

// TODO: Auto-generated Javadoc
/**
 * The Class VirtualConnection. This class is needed to simulate the mechanism of publish-subscrible. 
 * 
 * @author Mario Capuozzo
 */
public class VirtualConnection implements Connection {

	/** The topics. */
	public ArrayList<String> topics;
	
	/** The topics subscribed. */
	public ArrayList<String> topicsSubscribed;

	/**
	 * Instantiates a new virtual connection.
	 */
	public VirtualConnection() {
		topicsSubscribed = new ArrayList<String>();
		topics = new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#setupConnection(it.isislab.dmason.util.connection.Address)
	 */
	@Override
	public boolean setupConnection(Address providerAddr) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#createTopic(java.lang.String, int)
	 */
	@Override
	public boolean createTopic(String topicName, int numFields)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(topicName);
		return topics.add(topicName);
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#publishToTopic(java.io.Serializable, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean publishToTopic(Serializable object, String topicName,
			String key) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#subscribeToTopic(java.lang.String)
	 */
	@Override
	public boolean subscribeToTopic(String topicName) throws Exception {
		// TODO Auto-generated method stub
		return topicsSubscribed.add(topicName);

	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#asynchronousReceive(java.lang.String)
	 */
	@Override
	public boolean asynchronousReceive(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#getTopicList()
	 */
	@Override
	public ArrayList<String> getTopicList() throws Exception {
		// TODO Auto-generated method stub
		return topics;
	}

	/* (non-Javadoc)
	 * @see it.isislab.dmason.util.connection.Connection#unsubscribe(java.lang.String)
	 */
	@Override
	public boolean unsubscribe(String topicName) throws Exception {
		// TODO Auto-generated method stub
		for (String app : topicsSubscribed)
			if (topicName.equals(app))
				return topicsSubscribed.remove(topicName);

		return false;
	}

}
