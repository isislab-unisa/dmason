package dmason.util.visualization;

import java.util.HashMap;
import javax.jms.JMSException;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class ZoomCellMessageListener extends MyMessageListener
{	

	private ZoomViewer zoom;
	public ZoomCellMessageListener(ZoomViewer zoom)
	{
		super();
		this.zoom=zoom;
	
		
	}
	
   /**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			MyHashMap hash=((MyHashMap)parseMessage(arg0));
			
			Long step=null;
			
			System.out.println("Ricevuto messaggio con dimensione hash "+hash.size());
			HashMap<String,Object> hh=new HashMap<String, Object>();
			for(String key :zoom.fields.keySet())
				{

					ZoomArrayList z_a=(ZoomArrayList)hash.get(key);
					if(z_a==null)System.exit(0);
					hh.put(key,z_a);
					step=new Long(z_a.STEP);
					
					
					
				}
			System.out.println("Inserisco aggiornamento per "+step);
			zoom.update.putSblock(step, hh);
			if(zoom.STEP==null)
			{
				zoom.STEP=step;
				zoom.setInStep();
			}
	
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}

	
}