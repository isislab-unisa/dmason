/* 
   Copyright 2012 Università degli Studi di Salerno

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

package dmason.sim.globals;

import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;

import dmason.sim.field.DistributedField;
import dmason.sim.field.MessageListener;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;
import dmason.util.visualization.RemoteSnap;

public class MessageListenerGlobals extends MessageListener
{	
	
	public MessageListenerGlobals(ArrayList<DistributedField> fields, String topic) 
	{
		super(fields, topic);
	}
	
   /**
	*	It's called when a message is listen 
	*/
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
			for (DistributedField field : fields)
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
	
	public String getTopic(){
		return topic;
	}
	
}