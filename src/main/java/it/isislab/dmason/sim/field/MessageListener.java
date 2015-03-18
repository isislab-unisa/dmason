/**
 * Copyright 2012 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.field;

import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.ArrayList;

import javax.jms.JMSException;
/**
 *	A Listener for the messages swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public class MessageListener extends MyMessageListener
{	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String topic;
	public ArrayList<DistributedField2D> fields;
	

	
	public MessageListener(ArrayList<DistributedField2D> fields,String topic) 
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

			for (DistributedField2D field : fields) {
				
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