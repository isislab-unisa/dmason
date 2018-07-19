package it.isislab.dmason.sim.field3D;


import java.io.Serializable;
import java.util.ArrayList;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;



public class UpdaterThreadForListener3D extends Thread implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected ConnectionJMS con;
    CellType type;
    protected ArrayList<DistributedField3D> fields;
    protected ArrayList<MessageListener3D> listeners;
    public String topic;


    public UpdaterThreadForListener3D(ConnectionJMS con,String topic,ArrayList<DistributedField3D> fields, ArrayList<MessageListener3D> listeners) {

        this.con=con;
        this.fields=fields;
        this.topic=topic;
        this.listeners = listeners;

    }

    @Override
    public void run()
    {
        try
        {
            MessageListener3D m = new MessageListener3D(fields, topic);
            listeners.add(m);
            con.asynchronousReceive(topic,m);
        } catch (Exception e) { System.out.println("ciao"+topic);e.printStackTrace(); }
    }
}
