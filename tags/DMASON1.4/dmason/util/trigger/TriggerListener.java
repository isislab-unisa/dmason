package dmason.util.trigger;

import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JTextArea;

import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;


public class TriggerListener extends MyMessageListener {

	private JTextArea area;
	private FileOutputStream file;
	private PrintStream ps;

	public TriggerListener(JTextArea textArea, FileOutputStream f, PrintStream stream){
		area = textArea;
		file = f;
		ps = stream;
	}
	
	@Override
	public void onMessage(Message arg0) {
		
		ActiveMQObjectMessage obj = (ActiveMQObjectMessage)arg0;
		try {
			MyHashMap mh = (MyHashMap)obj.getObject();
			if (mh.get("trigger")!=null){
				
				String message = (String) mh.get("trigger");
				area.append(message+"\n");
				ps.println(message);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Problems with casting....maybe.");
			e.printStackTrace();
		}

	}

}
