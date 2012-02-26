package dmason.util.trigger;


import java.io.Serializable;

import dmason.util.connection.Address;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.MyMessageListener;

/**
 * 
 * This class is useful when we have goals to be achieved during the simulation without the use of the user interface. 
 * The object "trigger" will create a topic on which you subscribe to "know" when it reached the specified goal. 
 * Once you become aware that the operation was performed, will send a message to the main console, which will display it.
 *
 *											--------ITALIAN VERSION-------
 *Questa classe � utile quando abbiamo degli obiettivi da raggiungere durante al simulazione senza l'utilizzo dell'interfaccia utente. 
 *L'oggetto "trigger" provvederà a creare un topic, sul quale si sottoscrive per "sapere" quando è stato raggiunto l'obiettivo specificato.
 *Una volta venuto a conoscenza che l'operazione è stata eseguita, provvederà ad inviare un messaggio alla Console Principale, la quale provvederà a visualizzarlo.
 *
 */
public class Trigger implements TriggerConnection{
	private ConnectionNFieldsWithActiveMQAPI connection;
	private Address address;
	private boolean result;
	private String triggerTopicName = "trigger";
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param triggerMessageRecived is the name we associate at the topic on which the message will be sent at the end of operation
	 */
	public Trigger(Connection con){
		connection =(ConnectionNFieldsWithActiveMQAPI) con;
		try{
				createTriggerTopic();
				subcribeToTriggerTopic();
				
		}catch (Exception e) {
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

		return connection.publishToTopic(arg, triggerTopicName, triggerTopicName);
	}

	@Override
	public boolean createTriggerTopic() throws Exception {
		return connection.createTopic(triggerTopicName,1);
	}

	@Override
	public boolean asynchronousReceiveToTriggerTopic(MyMessageListener arg1) {
		return connection.asynchronousReceive(triggerTopicName,arg1);
	}
	

}
