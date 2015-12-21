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

import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.HashMap;

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
	@Override
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