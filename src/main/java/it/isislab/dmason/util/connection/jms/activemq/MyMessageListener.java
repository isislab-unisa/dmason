/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.util.connection.jms.activemq;


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
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public abstract class MyMessageListener implements MessageListener, Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
