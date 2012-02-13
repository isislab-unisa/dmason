package dmason.sim.field;

import java.util.ArrayList;
import javax.jms.JMSException;
import dmason.util.connection.MyMessageListener;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class MessageListener<E,F> extends MyMessageListener
{	
	public CellType TYPE;
	public int NUM_PEERS;
	public String topic;
	//public DistributedField<E> field;
	public UpdateMap<E,F> updates;
	
	public MessageListener(CellType type, int num_peer,DistributedField<E> field,ArrayList<String> neighborhood,String topic,final UpdateMap<E,F> updates) 
	{
		super();
		TYPE = type;
		//this.field = field;
		NUM_PEERS=num_peer;
		this.topic=topic;
		this.updates=updates;	
	}
	
   /**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			DistributedRegion<E,F> box = (DistributedRegion<E,F>)parseMessage(arg0);
			updates.put(box.getstep(),box);
		} catch (JMSException e) { e.printStackTrace(); }				
	}
	
	public String getTopic(){
		return topic;
	}
	
}