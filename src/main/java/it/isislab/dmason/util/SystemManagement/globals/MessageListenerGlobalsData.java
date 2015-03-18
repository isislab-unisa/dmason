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

package it.isislab.dmason.util.SystemManagement.globals;

import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.visualization.globalviewer.RemoteSnap;

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
public class MessageListenerGlobalsData extends MessageListener
{	
	
	Reducer reducer;
	
	public MessageListenerGlobalsData(Reducer reducer, String topic) 
	{
		super(null, topic);
		this.reducer = reducer;
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
			RemoteSnap obj = (RemoteSnap)bo.get("GLOBALS");
			reducer.getUpdatesMap().put(obj.step, obj);				
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
	@Override
	public String getTopic(){
		return topic;
	}
	
}