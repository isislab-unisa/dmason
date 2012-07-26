package dmason.util.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
/**
 * A Listener for the messages swapped among the peers.
 * @author unascribed
 * @author Luca Vicidomini
 */
public class VisualizationCellMessageListener extends MyMessageListener
{	
	public String topic;
	public DistributedMultiSchedule schedule;
	
	public VisualizationCellMessageListener(String topic, DistributedMultiSchedule schedule) 
	{
		super();
		this.topic = topic;
		this.schedule = schedule;
	}
	
   /**
	* Process a message received through the queue.
	* @param msg The message to process.
	*/
	public void onMessage(javax.jms.Message msg) 
	{	
		try
		{
			MyHashMap mh = (MyHashMap)parseMessage(msg);
			
			if (mh.get("GRAPHICS") instanceof String)
			{
				String command = (String)mh.get("GRAPHICS");

				if(command.equals("ENTER"))
				{
					schedule.numViewers.increment();
				}
				else if(command.equals("EXIT"))
				{
					schedule.numViewers.decrement();	
				}
			}
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	public String getTopic(){
		return topic;
	}
	
}