package dmason.util.visualization;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.field.MessageListener;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ThreadZoomCellMessageListener extends Thread{
	
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private String id_cell;
	private ZoomViewer zoom;
	public ThreadZoomCellMessageListener(ConnectionNFieldsWithActiveMQAPI conn,String id_cell,ZoomViewer zoom)
	{
		this.id_cell=id_cell;
		this.connection=conn;
		this.zoom=zoom;
	}
	
	public void run()
	{
		try{
			connection.createTopic("GRAPHICS"+id_cell,1);
			connection.subscribeToTopic("GRAPHICS"+id_cell);
			connection.asynchronousReceive("GRAPHICS"+id_cell,
					new ZoomCellMessageListener(zoom));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
