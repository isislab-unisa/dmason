package dmason.util.trigger;

import java.io.Serializable;

import dmason.util.connection.MyMessageListener;

public interface TriggerConnection {
	/**
	 * Subscrives to a trigger topicover
	 * @return
	 * @throws Exception
	 */
	public boolean subcribeToTriggerTopic() throws Exception;
	
	/**
	 * 
	 * @param arg param to send at trigger topic
	 * @return
	 * @throws Exception
	 */
	public boolean publishToTriggerTopic(Serializable arg) throws Exception;
	
	/**
	 * Create a topic for the trigger
	 * 
	 * @return true, if the creation is completed. False, otherwise.
	 * @throws Exception
	 */
	public boolean createTriggerTopic()throws Exception;
	
	/**
	 * Receive the message published on trigger topic
	 * @param arg1, a message containing a the requested information 
	 * @return
	 */
	public boolean asynchronousReceiveToTriggerTopic(MyMessageListener arg1);
}


