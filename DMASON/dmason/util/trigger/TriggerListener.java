package dmason.util.trigger;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JTextArea;

import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;


public class TriggerListener extends MyMessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea area;
	private Logger logger;

	public TriggerListener(JTextArea textArea, Logger logger){
		area = textArea;
		this.logger = logger;
	}
	
	@Override
	public void onMessage(Message arg0) {
		
		ActiveMQObjectMessage obj = (ActiveMQObjectMessage)arg0;
		try {
			MyHashMap mh = (MyHashMap)obj.getObject();
			if (mh.get("trigger")!=null){
				
				String message = (String) mh.get("trigger");
				area.append(message+"\n");
				logger.info(message);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Problems with casting....maybe.");
			e.printStackTrace();
		}

	}

}
