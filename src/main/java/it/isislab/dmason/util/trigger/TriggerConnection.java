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
package it.isislab.dmason.util.trigger;

import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.io.Serializable;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
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


