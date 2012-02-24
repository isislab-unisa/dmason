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
public class ZoomInCellMessageListener extends MyMessageListener
{	
	public String topic;
	public DistributedMultiSchedule schedule;
	public String id_Cell;
	
	public ZoomInCellMessageListener(String topic, DistributedMultiSchedule schedule,String id_Cell) 
	{
		super();
		this.topic = topic;
		this.schedule = schedule;
		this.id_Cell=id_Cell;
	
	}
	
   /**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
		
			if(((MyHashMap)parseMessage(arg0)).get("GRAPHICS"+id_Cell) instanceof String)
			{
				String command = (String)((MyHashMap)parseMessage(arg0)).get("GRAPHICS"+id_Cell);
	
				if(command.equals("ZOOM"))
				{
					synchronized (schedule.monitor) {
						schedule.monitor.isZoom=true;
					}
					
					System.out.println("Ricevuto comando di zoom");
				}
				else

					if(command.contains("ZOOM_STEP"))
					{
						synchronized (schedule.monitor) {
							
							Long step= Long.parseLong(command.split("ZOOM_STEP")[1]);
							schedule.monitor.putAck(step);
						}
						
						System.out.println("Ricevuto comando di zoom step");
					}
					else
					
					if(command.equals("EXIT_ZOOM"))
					{
						//schedule.NUMVIEWER.decrement();	
						synchronized (schedule.monitor) {
							schedule.monitor.isZoom=false;
							schedule.monitor.forceWakeUp();
						}
						
						System.out.println("Ricevuto comando di exit_zoom");
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