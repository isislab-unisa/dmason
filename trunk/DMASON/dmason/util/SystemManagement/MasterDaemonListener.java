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

package dmason.util.SystemManagement;

import java.util.HashMap;
import java.util.Observable;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQObjectMessage;

import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

/**
 * This is a simple MessageListener holds for PeerStatusInfo and provide an access method to let MasterDaemonStarter
 * get these informations,stored into an HashMap.
 */

public class MasterDaemonListener extends MyMessageListener
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4604016459215248301L;

	HashMap<String, PeerStatusInfo> infos = new HashMap<String, PeerStatusInfo>();
	
	private DelegatedObservable obs = new DelegatedObservable();
	
	private boolean isFirst = true;
	
	@Override
	public void onMessage(Message arg0) 
	{
		ActiveMQObjectMessage msg = (ActiveMQObjectMessage) arg0;
		try{
			MyHashMap mh = (MyHashMap)msg.getObject();
			
			
			if(mh.get("info")!=null)
			{
				//System.out.println("Ricevuto infoooooooooooooooo");
				PeerStatusInfo info = (PeerStatusInfo) mh.get("info");
				infos.put(info.getHostName(), info);
				
				//notify MasterDeamonStarter
				obs.setChanged();
				obs.notifyObservers(info);
			}
			
			if(mh.get("updated")!=null)
			{
				/*if(isFirst)
				{
					infos.clear();
					isFirst = false;
				}*/
				PeerStatusInfo info = (PeerStatusInfo) mh.get("updated");
				infos.put(info.getHostName(), info);
				
				//Notify MasterDeamonStarter
				obs.setChanged();
				obs.notifyObservers();
			}
			
			/*if(mh.get("batch")!=null)
			{
				//Notify MasterDeamonStarter
				obs.setChanged();
				obs.notifyObservers("batch");
			}*/
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public PeerStatusInfo getLatestUpdate(String id)
	{
		
		return infos.get(id);
	}

	public Observable getObservable() {return obs;}
	
	//A subclass of Observable that allows delegation.
	public class DelegatedObservable extends Observable 
	{
		public void clearChanged() {
			super.clearChanged();
		}
		public void setChanged() {
			super.setChanged();
		}
	}
}

