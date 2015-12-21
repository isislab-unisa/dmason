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

package it.isislab.dmason.util.management.globals;

import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.MyHashMap;

import java.util.ArrayList;

import javax.jms.JMSException;
/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
*/
public class MessageListenerGlobals extends MessageListener
{	
	
	public MessageListenerGlobals(ArrayList<DistributedField2D> fields, String topic) 
	{
		super(fields, topic);
	}
	
   /**
	*	It's called when a message is listen 
	*/
	@Override
	public void onMessage(javax.jms.Message arg0) 
	{	
		//System.out.println("MessageListenerGlobal ONMESSAGE");
		try
		{
			//System.out.println(System.currentTimeMillis() +",-1,onMessage");
			MyHashMap bo = (MyHashMap)parseMessage(arg0);
			//System.out.println(System.currentTimeMillis() +",-1,parseMessage");
			RemoteSnap obj = (RemoteSnap)bo.get("GLOBALS");
			//System.out.println(System.currentTimeMillis() +",-1,get");
			for (DistributedField2D field : fields)
			{
				//DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getID());
				//field.getGlobals().put(obj.getStep(), obj);
				field.getGlobals().put(obj.step, obj);
				//System.out.println("MessageListenerGlobal: PUT GLOBALS. STEP " + obj.getStep());
				//System.out.println(System.currentTimeMillis() +"," +obj.step + ",onMessage put globals");
			}
				
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	@Override
	public String getTopic(){
		return topic;
	}
	
}