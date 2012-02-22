package dmason.util.connection;


import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQObjectMessage;

/** For receive asynchronously messages we have to create a listener that implements MessageListener interface,
to implements onMessage(Message arg0),that is the method called when a message is published
to a specific topic.
This class provide the parseMessageMethod that allows to avoid every time casts to get the object.
We declare 'abstract' to force the developers'rewriting...his is a developer's duty, according to his necessities! */
public abstract class MyMessageListener implements MessageListener,Serializable{
	
	public Object obj;
	
	public MyMessageListener() { obj = null; }
	
	@Override
	public abstract void onMessage(Message arg0) ;
	
	public final Object parseMessage(Message arg0) throws JMSException{
		return ((ActiveMQObjectMessage)arg0).getObject();
	}
	
	public Object getAsyncMessage(){
		return obj;
	}
}
