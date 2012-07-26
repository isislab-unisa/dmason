package dmason.sim.field;

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
public class MessageListener extends MyMessageListener
{	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String topic;
	public ArrayList<DistributedField> fields;
	

	
	public MessageListener(ArrayList<DistributedField> fields,String topic) 
	{
		super();

		this.fields = fields;

		this.topic=topic;

	}
	
   /**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			MyHashMap bo = (MyHashMap)parseMessage(arg0);

			for (DistributedField field : fields) {
				
				DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getID());
				
				field.getUpdates().put(obj.getStep(), obj);
			}
				
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	public String getTopic(){
		return topic;
	}
	
}