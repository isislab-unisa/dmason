package dmason.util.SystemManagement;

import java.util.HashMap;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQObjectMessage;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

/**
 * This is a simple MessageListener holds for PeerStatusInfo and provide an access method to let MasterDaemonStarter
 * get these informations,stored into an HashMap.
 */

public class MasterDaemonListener extends MyMessageListener{
	
	HashMap<String, PeerStatusInfo> infos = new HashMap<String, PeerStatusInfo>();

	@Override
	public void onMessage(Message arg0) {
		ActiveMQObjectMessage msg = (ActiveMQObjectMessage) arg0;
		try{
			MyHashMap mh = (MyHashMap)msg.getObject();
			if(mh.get("info")!=null){
				PeerStatusInfo info = (PeerStatusInfo) mh.get("info");
				infos.put(info.getHostName(), info);
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public PeerStatusInfo getLatestUpdate(String id){
		return infos.get(id);
	}

}
