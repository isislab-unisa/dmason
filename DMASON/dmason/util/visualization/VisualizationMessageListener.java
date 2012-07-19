package dmason.util.visualization;
import java.util.HashMap;

import javax.jms.JMSException;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
/**
 *	A Listener for the messages swapped among the peers.
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
	* Process a message received through the queue.
	* @param msg The message to process.
	*/
	public void onMessage(javax.jms.Message msg) 
	{	
		try
		{
			MyHashMap mh = (MyHashMap)parseMessage(msg);
		
			if (mh.get("GRAPHICS") instanceof RemoteSnap)
			{
				RemoteSnap remSnap = (RemoteSnap)mh.get("GRAPHICS");
				if(!disp.isStarted)
				{
					disp.isStarted=true;
					disp.step=remSnap.step+1;
					if(disp.isFirstTime)
					{
						disp.sblock();
						disp.isFirstTime = false;
					}
					else
						disp.updates.forceSblock();
				}
				disp.addSnapShot(remSnap);
			}		
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	public String getTopic(){
		return topic;
	}
	
}