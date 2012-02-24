package dmason.util.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class VisualizationMessageListener extends MyMessageListener
{	
	public String topic;
	public Display disp;
	
	public VisualizationMessageListener(String topic, Display disp) 
	{
		super();
		this.topic = topic;
		this.disp = disp;
	}
	
   /**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			if((((MyHashMap)parseMessage(arg0)).get("GRAPHICS") instanceof RemoteSnap))
			{
				RemoteSnap remSnap = (RemoteSnap)((MyHashMap)parseMessage(arg0)).get("GRAPHICS");
				if(!disp.STARTED){
				
					disp.STARTED=true;
					
					disp.STEP=remSnap.step+1;
					
				
					System.out.println("Starts from step:"+disp.STEP);
					disp.sblock();
				}
				
			
				disp.addSnapShot(remSnap);
				
				if(disp.PAUSE)
				{
					System.out.println("SBLOCOOOOOOOOOOOO");
					disp.updates.forceSblock(remSnap.step+1);
					disp.STEP=remSnap.step+1;
					disp.PAUSE=false;
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