package dmason.util.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
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
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
		
			if(((MyHashMap)parseMessage(arg0)).get("GRAPHICS") instanceof String)
			{
				String command = (String)((MyHashMap)parseMessage(arg0)).get("GRAPHICS");
			
			
				
				if(command.equals("ENTER"))
				{
					schedule.NUMVIEWER.increment();
				}
				else
					if(command.equals("EXIT"))
					{
						schedule.NUMVIEWER.decrement();	
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