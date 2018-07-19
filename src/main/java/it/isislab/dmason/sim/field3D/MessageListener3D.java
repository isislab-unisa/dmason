package it.isislab.dmason.sim.field3D;


import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;

import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.util.connection.MyHashMap;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

public class MessageListener3D extends MyMessageListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String topic;
    public ArrayList<DistributedField3D> fields;



    public MessageListener3D(ArrayList<DistributedField3D> fields,String topic)
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
        try
        {
            //System.out.println(arg0);
            MyHashMap bo = (MyHashMap)parseMessage(arg0);

            for (DistributedField3D field : fields) {

                DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getDistributedFieldID());
                field.getUpdates().put(obj.getStep(), obj);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String getTopic(){
        return topic;
    }

}