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
package it.isislab.dmason.experimentals.util.visualization.zoomviewerapp;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

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
 */
public class ZoomInCellMessageListener extends MyMessageListener
{	
	public String topic;
	public DistributedMultiSchedule schedule;
	public String id_Cell;
	
	public ZoomInCellMessageListener(String topic, DistributedMultiSchedule schedule,String id_Cell) 
	{
		super();
		this.topic = topic;
		this.schedule = schedule;
		this.id_Cell=id_Cell;
	
	}
	
   /**
	*	It's called when a message is listen 
	*/
	@Override
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
		
			if(((MyHashMap)parseMessage(arg0)).get("GRAPHICS"+id_Cell) instanceof String)
			{
				String command = (String)((MyHashMap)parseMessage(arg0)).get("GRAPHICS"+id_Cell);
	
				if(command.equals("ZOOM"))
				{
					synchronized (schedule.monitor) {
						schedule.monitor.isZoom=true;
						schedule.monitor.isSynchro=false;
					}
				}
				else
					if(command.equals("ZOOM_SYNCHRO"))
					{
						synchronized (schedule.monitor) {
							schedule.monitor.isZoom=true;
							schedule.monitor.isSynchro=true;
						}
					}
					else
						if(command.contains("ZOOM_STEP"))
						{
							synchronized (schedule.monitor) {
								
								Long step= Long.parseLong(command.split("ZOOM_STEP")[1]);
								schedule.monitor.putAck(step);
							}
						}
						else
							if(command.equals("EXIT_ZOOM"))
							{
								//schedule.NUMVIEWER.decrement();	
								synchronized (schedule.monitor) {
									schedule.monitor.isZoom=false;
									schedule.monitor.forceWakeUp();
								}
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