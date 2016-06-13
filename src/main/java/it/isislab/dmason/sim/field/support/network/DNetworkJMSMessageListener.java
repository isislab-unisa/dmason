/**
 * Copyright 2016 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.field.support.network;
import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.ArrayList;

import javax.jms.JMSException;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class DNetworkJMSMessageListener extends MyMessageListener
{	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String topic;
	public ArrayList<DNetwork> fields;
	

	
	public DNetworkJMSMessageListener(ArrayList<DNetwork> fields,String topic) 
	{
		super();

		this.fields = fields;

		this.topic=topic;

	}
	
   /**
	*	It's called when a message is listen 
	*/
	@Override
	public void onMessage(javax.jms.Message arg0) 
	{	

		try
		{
			
				MyHashMap bo = (MyHashMap)parseMessage(arg0);
			
			for (DNetwork field : fields) {	
				DNetworkRegion obj = (DNetworkRegion)bo.get(field.getDistributedFieldID());
				field.getNetworkUpdates().put(obj.getStep(), obj);
				
			}
				
		} catch (JMSException e) { 
			
			e.printStackTrace(); 
		}				
	}
	
	public String getTopic(){
		return topic;
	}
	
}