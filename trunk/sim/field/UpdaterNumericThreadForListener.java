package dmason.sim.field;

import java.io.Serializable;
import java.util.ArrayList;

import dmason.util.connection.ConnectionWithJMS;

/**
 * A Thread than instances a listener.
 * It's necessary to not block main thread.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 */
public class UpdaterNumericThreadForListener <E,F> extends Thread implements Serializable {

	ConnectionWithJMS con;
	CellType type;
	int num_peers;
	DistributedField<E> field;
	ArrayList<String> neighborhood;
	public String topic;
	public MessageListenerNumeric<E,F> m;
	public UpdateMapNumeric<E,F> updates;
	
	/**
	 * 
	 * @param con the connection with ApacheActiveMQ
	 * @param type the cell of field
	 * @param num_peers the number of peers
	 * @param field the field
	 * @param neighborhood the neighborhood of the field
	 * @param topic the topic to receive the updates
	 * @param updates the UpdateMapNumeric for updates
	 */
	public UpdaterNumericThreadForListener(ConnectionWithJMS con,CellType type,int num_peers,DistributedField<E> field,ArrayList<String> neighborhood,String topic,UpdateMapNumeric<E,F> updates)
	{
		this.con=con;
		this.type=type;
		this.num_peers=num_peers;
		this.field=field;
		this.neighborhood=neighborhood;
		this.topic=topic;
		this.updates=updates;
	}

	public void run()
	{
		try 
		{
			//System.out.println(topic);
			m=new MessageListenerNumeric<E,F>(type,num_peers,field,neighborhood,topic,updates);
			con.asynchronousReceive(topic,m);
		} catch (Exception e) { e.printStackTrace(); }
	}	
}
