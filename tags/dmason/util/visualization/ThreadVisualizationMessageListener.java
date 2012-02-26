package dmason.util.visualization;

import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ThreadVisualizationMessageListener extends Thread{
	
	
	private ConnectionNFieldsWithActiveMQAPI connection;
	private Display dis;
	public ThreadVisualizationMessageListener(ConnectionNFieldsWithActiveMQAPI conn,
			Display dis)
	{
		this.dis=dis;
		this.connection=conn;
	}
	
	public void run()
	{
		try{
			connection.createTopic("GRAPHICS",1);
			connection.subscribeToTopic("GRAPHICS");
			connection.asynchronousReceive("GRAPHICS",
					new VisualizationMessageListener("GRAPHICS",dis));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
