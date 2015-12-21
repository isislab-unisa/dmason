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
package it.isislab.dmason.experimentals.util.visualization.globalviewer;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import javax.jms.JMSException;
/**
 * A Listener for the messages swapped among the peers.
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 * 
 */
public class VisualizationCellMessageListener extends MyMessageListener
{	
	public String topic;
	public DistributedMultiSchedule schedule;
	
	public VisualizationCellMessageListener(String topic, DistributedMultiSchedule schedule) 
	{
		super();
		this.topic = topic;
		this.schedule = schedule;
	}
	
   /**
	* Process a message received through the queue.
	* @param msg The message to process.
	*/
	@Override
	public void onMessage(javax.jms.Message msg) 
	{	
		try
		{
			MyHashMap mh = (MyHashMap)parseMessage(msg);
			
			if (mh.get("GRAPHICS") instanceof String)
			{
				String command = (String)mh.get("GRAPHICS");

				if(command.equals("ENTER"))
				{
					schedule.numViewers.increment();
				}
				else if(command.equals("EXIT"))
				{
					schedule.numViewers.decrement();	
				}
			}
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	public String getTopic(){
		return topic;
	}
	
}