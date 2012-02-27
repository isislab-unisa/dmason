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
			if( ((MyHashMap)parseMessage(arg0)).get("GRAPHICS"+zoom.id_cell) == null )
			{
					MyHashMap hash=((MyHashMap)parseMessage(arg0));
					Long step=null;
					HashMap<String,Object> hh=new HashMap<String, Object>();
					for(String key :zoom.fields.keySet())
						{
							ZoomArrayList z_a=(ZoomArrayList)hash.get(key);
							if(z_a==null)System.exit(0);
							hh.put(key,z_a);
							step=new Long(z_a.STEP);
	
						}
					if(zoom.STEP==null)
					{
						zoom.STEP=step;
						zoom.setInStep();
					}
					zoom.update.putSblock(step, hh);
					
			}
	
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}

	
}