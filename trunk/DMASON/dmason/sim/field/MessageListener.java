/**
 * Copyright 2012 Università degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package dmason.sim.field;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;

import dmason.sim.field.network.DNetwork;
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
				
				if(field instanceof DNetwork<?>)
					continue;
				
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