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
package it.isislab.dmason.experimentals.util.trigger;

import java.io.Serializable;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

/**
 * 
 * This class is useful when we have goals to be achieved during the simulation
 * without the use of the user interface. The object "trigger" will create a
 * topic on which you subscribe to "know" when it reached the specified goal.
 * Once you become aware that the operation was performed, will send a message
 * to the main console, which will display it.
 *
 * --------ITALIAN VERSION------- Questa classe ? utile quando abbiamo degli
 * obiettivi da raggiungere durante al simulazione senza l'utilizzo
 * dell'interfaccia utente. L'oggetto "trigger" provveder?? a creare un topic,
 * sul quale si sottoscrive per "sapere" quando ?? stato raggiunto l'obiettivo
 * specificato. Una volta venuto a conoscenza che l'operazione ?? stata
 * eseguita, provveder?? ad inviare un messaggio alla Console Principale, la
 * quale provveder?? a visualizzarlo.
 *
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class Trigger implements TriggerConnection {
	private ConnectionJMS connection;
	private Address address;
	private boolean result;
	private String triggerTopicName = "trigger";

	/**
	 * 
	 * @param ip
	 * @param port
	 * @param triggerMessageRecived
	 *            is the name we associate at the topic on which the message
	 *            will be sent at the end of operation
	 */
	public Trigger(Connection con) {
		
			connection = (ConnectionJMS) con;
			try {
				createTriggerTopic();
				subcribeToTriggerTopic();

			} catch (Exception e) {
				e.printStackTrace();
			}
		
	}

	public Address getAddress() {
		return address;
	}

	@Override
	public boolean subcribeToTriggerTopic() throws Exception {
		// TODO Auto-generated method stub
		return connection.subscribeToTopic(triggerTopicName);
	}

	@Override
	public boolean publishToTriggerTopic(Serializable arg) throws Exception {

		return connection.publishToTopic(arg, triggerTopicName,
				triggerTopicName);
	}

	@Override
	public boolean createTriggerTopic() throws Exception {
		return connection.createTopic(triggerTopicName, 1);
	}

	@Override
	public boolean asynchronousReceiveToTriggerTopic(MyMessageListener arg1) {
		return connection.asynchronousReceive(triggerTopicName, arg1);
	}

}
