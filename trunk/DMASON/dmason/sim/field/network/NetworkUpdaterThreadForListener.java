package dmason.sim.field.network;

import java.io.Serializable;
import java.util.ArrayList;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.util.connection.ConnectionWithJMS;

public class NetworkUpdaterThreadForListener extends Thread implements Serializable {

	private static final long serialVersionUID = 1L;
	ConnectionWithJMS con;
	CellType type;
	DistributedField field;
	//ArrayList<NetworkMessageListener> listeners;
	public String topic;
	
	public NetworkUpdaterThreadForListener(ConnectionWithJMS con,String topic,DistributedField field, ArrayList<NetworkMessageListener> listeners) {

		this.con=con;
		this.field=field;
		this.topic=topic;
		//this.listeners = listeners;
	
	}
	
	public void run()
	{
		try 
		{
			NetworkMessageListener m = new NetworkMessageListener(field, topic);
			//listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
}
