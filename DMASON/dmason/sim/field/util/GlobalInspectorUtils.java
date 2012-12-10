package dmason.sim.field.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.visualization.RemoteSnap;
import sim.util.Double2D;

public class GlobalInspectorUtils
{
	private static int[] white = { 255, 255, 255 }; 
	
	public static void synchronizeInspector(DistributedState<?> sim, ConnectionWithJMS connection, CellType cellId, double currentTime, BufferedImage currentSnap, HashMap<String, Object> currentStats, ArrayList<String> tracingFields, boolean traceGraphics)
	{
		RemoteSnap snap = new RemoteSnap(cellId, sim.schedule.getSteps() - 1, currentTime);
		if (traceGraphics)
		{
			try
			{
				ByteArrayOutputStream by = new ByteArrayOutputStream();
				ImageIO.write(currentSnap, "png", by);
				by.flush();
				snap.image = by.toByteArray();
				by.close();
				currentSnap.getGraphics().clearRect(0, 0, currentSnap.getWidth(), currentSnap.getHeight());
			} catch (Exception e) {
				//logger.severe("Error while serializing the snapshot");
				e.printStackTrace();
			}
		}

		try
		{
			snap.stats = currentStats;
			connection.publishToTopic(snap, "GRAPHICS", "GRAPHICS");
		} catch (Exception e) {
			//logger.severe("Error while publishing the snap message");
			e.printStackTrace();
		}

		// Update statistics
		Class<?> simClass = sim.getClass();
		for (int i = 0; i < tracingFields.size(); i++)
		{
			try
			{
				Method m = simClass.getMethod("get" + tracingFields.get(i), (Class<?>[])null);
				Object res = m.invoke(sim, new Object [0]);
				snap.stats.put(tracingFields.get(i), res);
			} catch (Exception e) {
				//logger.severe("Reflection error while calling get" + tracingFields.get(i));
				e.printStackTrace();
			}
		}
	}
	
	public static void updateBitmap(BufferedImage remoteSnap, RemoteAgent<?> remoteAgent, Double2D location)
	{
		remoteSnap.getRaster().setPixel(
				(int)location.x % remoteSnap.getWidth(),
				(int)location.y % remoteSnap.getHeight(),
				white);
	}
}
