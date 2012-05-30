package dmason.util.visualization;
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
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			if((((MyHashMap)parseMessage(arg0)).get("GRAPHICS") instanceof RemoteSnap))
			{
				RemoteSnap remSnap = (RemoteSnap)((MyHashMap)parseMessage(arg0)).get("GRAPHICS");
				if(!disp.isStarted){
				
					disp.isStarted=true;
					
					disp.step=remSnap.step+1;
				
					if(disp.isFirstTime) {
						disp.sblock();
						disp.isFirstTime=false;
					}
					else disp.updates.forceSblock();
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