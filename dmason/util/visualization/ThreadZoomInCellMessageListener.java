package dmason.util.visualization;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.field.MessageListener;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ThreadZoomInCellMessageListener extends Thread{
	
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private DistributedMultiSchedule schedule;
	private String id_cell;
	public ThreadZoomInCellMessageListener(ConnectionNFieldsWithActiveMQAPI conn,String id_cell,
			DistributedMultiSchedule schedule)
	{
		this.connection=conn;
		this.id_cell=id_cell;
		this.schedule=schedule;
	}
	

	
	public void run()
	{
		try{
			//connection.createTopic("GRAPHICS"+id_cell,1);
			connection.subscribeToTopic("GRAPHICS"+id_cell);
			connection.asynchronousReceive("GRAPHICS"+id_cell,
					new ZoomInCellMessageListener("GRAPHICS"+id_cell,schedule,id_cell));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
