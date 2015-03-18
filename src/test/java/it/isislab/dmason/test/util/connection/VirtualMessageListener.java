package it.isislab.dmason.test.util.connection;

import it.isislab.dmason.sim.field.DistributedField;
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

					DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getID());
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