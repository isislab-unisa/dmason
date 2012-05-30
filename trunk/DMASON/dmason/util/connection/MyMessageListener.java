package dmason.util.connection;


import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQObjectMessage;

/**
 * In order to asynchronously receive messages, we have to create a listener
 * that implements MessageListener interface and its onMessage(Message arg0)
 * method (that is the method called when a message is published to a specific
 * topic). This class is abstract so developers have to rewrite it according 
 * to their necessities!
 */
public abstract class MyMessageListener implements MessageListener, Serializable
{
	
	public Object obj;
	
	public MyMessageListener() { obj = null; }
	
	/**
	 * Handles messages received on the queue.
	 */
	@Override
	public abstract void onMessage(Message msg);
	
	/**
	 * An utility method in order to avoid manually casting the received
	 * message.
	 * @param msg The message received through the queue.
	 * @return The object carried by the message.
	 * @throws JMSException 
	 */
	public final Object parseMessage(Message msg) throws JMSException{
		return ((ActiveMQObjectMessage)msg).getObject();
	}
	
	public Object getAsyncMessage(){
		return obj;
	}
}
