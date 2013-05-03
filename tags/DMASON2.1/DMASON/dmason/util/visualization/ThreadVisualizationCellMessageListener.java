package dmason.util.visualization;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.field.MessageListener;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ThreadVisualizationCellMessageListener extends Thread{
	
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private DistributedMultiSchedule schedule;
	public ThreadVisualizationCellMessageListener(ConnectionNFieldsWithActiveMQAPI conn,
			DistributedMultiSchedule schedule)
	{
		this.schedule=schedule;
		this.connection=conn;
	}
	
	public void run()
	{
		try{
			connection.createTopic("GRAPHICS",1);
			connection.subscribeToTopic("GRAPHICS");
			connection.asynchronousReceive("GRAPHICS",
					new VisualizationCellMessageListener("GRAPHICS",schedule));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
