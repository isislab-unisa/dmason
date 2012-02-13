package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;
import dmason.sim.field.continuous.DContinuous2DY;
import dmason.util.connection.ConnectionWithJMS;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class UpdaterThreadForListener<E,F> extends Thread implements Serializable
{
	ConnectionWithJMS con;
	CellType type;
	int num_peers;
	DistributedField<E> field;
	ArrayList<String> neighborhood;
	public String topic;
	public MessageListener<E,F> m;
	public UpdateMap<E,F> updates;
	private ArrayList<MessageListener> listeners;
	
	/**
	 * 
	 * @param con the connection with ApacheActiveMQ
	 * @param type the cell of field
	 * @param num_peers the number of peers
	 * @param field the field
	 * @param neighborhood the neighborhood of the field
	 * @param topic the topic to receive the updates
	 * @param updates the UpdateMap for updates
	 */
	public UpdaterThreadForListener(ConnectionWithJMS con,CellType type,int num_peers,DistributedField<E> field,ArrayList<String> neighborhood,String topic,UpdateMap<E,F> updates,ArrayList<MessageListener> listeners)
	{
		this.con=con;
		this.type=type;
		this.num_peers=num_peers;
		this.field=field;
		this.neighborhood=neighborhood;
		this.topic=topic;
		this.updates=updates;
		this.listeners = listeners;
	}

	public void run()
	{
		try 
		{
			m=new MessageListener<E,F>(type,num_peers,field,neighborhood,topic,updates);
			listeners.add(m);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public MessageListener getListener(){
		return m;
	}
}