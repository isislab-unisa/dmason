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

package it.isislab.dmason.test.util.connection;

import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;
import java.util.ArrayList;
import javax.jms.JMSException;

public 	class VirtualMessageListener extends MyMessageListener
{	


	private static final long serialVersionUID = 1L;
	public String topic;
	public ArrayList<String> fields_name=null;
	public String id;
	public ArrayList<DistributedField2D> fields;


	public VirtualMessageListener(ArrayList<String> fields,String topic,String id) 
	{
		super();

		this.fields_name = fields;

		this.topic=topic;
		this.id=id;

	}
	public VirtualMessageListener(ArrayList<DistributedField2D> fields,String topic) 
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
		if(fields_name!=null)
		{	
			try
			{

				MyHashMap bo =(MyHashMap) arg0.getObjectProperty("data");

				for (String field : fields_name) {

					String obj = (String)bo.get(field);
					System.out.println(id+"]received for field "+field+" message "+obj);
				}

			} catch (JMSException e) { 
				e.printStackTrace(); 
			}		
		}else{
			try{
				MyHashMap bo = (MyHashMap) arg0.getObjectProperty("data");

				for (DistributedField2D field : fields) {

					DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getDistributedFieldID());
					field.getUpdates().put(obj.getStep(), obj);
				}

			} catch (JMSException e) { 
				e.printStackTrace(); 
			}			
		}
	}

	public String getTopic(){
		return topic;
	}

}