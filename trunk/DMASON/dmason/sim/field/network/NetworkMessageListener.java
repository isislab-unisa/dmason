package dmason.sim.field.network;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.jms.JMSException;

import sim.util.Bag;

import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.DistributedField;
import dmason.sim.field.DistributedRegionInterface;
import dmason.util.connection.MyHashMap;
import dmason.util.connection.MyMessageListener;

public class NetworkMessageListener extends MyMessageListener {

	private static final long serialVersionUID = 1L;
	public String topic;
	public DistributedField field;
	
	public NetworkMessageListener(DistributedField field,String topic) 
	{
		super();
		this.field = field;
		this.topic=topic;

	}
	
	public String getTopic(){
		return topic;
	}
	
	/**
	*	It's called when a message is listen 
	*/
	public void onMessage(javax.jms.Message arg0) 
	{	
		try
		{
			//System.out.println("Mess");
			//System.out.println("l'oggetto nel mess: "+arg0.toString());
			MyHashMap bo = (MyHashMap)parseMessage(arg0);
			//System.out.println("MESSAGGIO "+arg0);
			/*Set<String> set=bo.keySet();
			Iterator i=set.iterator();
			System.out.println("dimensione del set: "+set.size());
			while (i.hasNext()){
				String s=(String)i.next();
				System.out.println("chiave: "+s);
				Object o=bo.get(s);
				System.out.println("il valore della chiave è: "+o);
				
				
			}*/
			//System.out.println("bho: "+field.getID());
			DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getID());
			/*Bag list=((DistributedRegionNetwork)obj).getNodeList();
			for(Object o:list){
				System.out.println("ALL'INTERNO DEL LISTENER L'IDNETWORK è: "+((RemoteNetwork)o).getNetworkId());
			}*/
			((DNetwork)field).getNetUpdates().put(obj.getStep(), obj);
			//((DNetwork)field).getUpdates().put(obj.getStep(), obj);
				
		} catch (JMSException e) { 
			e.printStackTrace(); 
		}				
	}
	
}
