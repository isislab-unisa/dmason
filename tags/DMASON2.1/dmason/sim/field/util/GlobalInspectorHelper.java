package dmason.sim.field.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.visualization.RemoteSnap;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Contains static methods useful to manage Global Inspector. These methods should be used
 * within <code>DistributedField</code>s' <code>synchro()</code> method.
 * @author Luca Vicidomini
 */
public class GlobalInspectorHelper
{
	private static int[] white = { 255, 255, 255 }; 
	
	/**
	 * This should be called at the beginning of a <code>DistributedField</code>'s <code>synchro()</code> method.
	 * @param sim Current simulation.
	 * @param connection Current connection to server.
	 * @param topicPrefix Topic prefix of this batch run (empty string if this run is not in a batch).
	 * @param cellId Cell identifier.
	 * @param x X offset of the region relative to field's top-left (0,0) coordinate.
	 * @param y Y offset of the region relative to field's top-left (0,0) coordinate.
	 * @param currentTime Current simulation time.
	 * @param currentSnap Current image depicting agents' positions.
	 * @param currentStats A map of inspected parameters to send to the Global Inspector.
	 * @param tracingFields A list of field names the user chose to inspect.
	 * @param traceGraphics True if user requested an image depicting agents' positions.
	 */
	public static void synchronizeInspector(DistributedState<?> sim, ConnectionWithJMS connection, String topicPrefix, CellType cellId, int x, int y, double currentTime, BufferedImage currentSnap, HashMap<String, Object> currentStats, List<String> tracingFields, boolean traceGraphics)
	{
		RemoteSnap snap = new RemoteSnap(cellId, sim.schedule.getSteps() - 1, currentTime);
		
		// Pack the bitmap (if enabled) into RemoteSnap, then clear the bitmap buffer 
		if (traceGraphics)
		{
			try
			{
				ByteArrayOutputStream by = new ByteArrayOutputStream();
				ImageIO.write(currentSnap, "png", by);
				by.flush();
				snap.image = by.toByteArray();
				by.close();
				// We MUST clear the graphic buffer if using standard fields,
				// but this is not needed for load-balanced fields
				// because the image is re-created every step
				// TODO investigate
				currentSnap.getGraphics().clearRect(0, 0, currentSnap.getWidth(), currentSnap.getHeight());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Add the HashMap of the traced parameters into RemoteSnap and sends it
		try
		{
			snap.stats = currentStats;
			snap.x = x;
			snap.y = y;
			connection.publishToTopic(snap, topicPrefix + "GRAPHICS", "GRAPHICS");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Update statistics for the next step
		Class<?> simClass = sim.getClass();
		for (int i = 0; i < tracingFields.size(); i++)
		{
			try
			{
				Method m = simClass.getMethod("get" + tracingFields.get(i), (Class<?>[])null);
				Object res = m.invoke(sim, new Object [0]);
				snap.stats.put(tracingFields.get(i), res);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Paints an agent position on the bitmap.
	 * @param remoteSnap Current bitmap depicting agents' position.
	 * @param remoteAgent The agent to paint.
	 * @param location New agent's position.
	 * @param x X offset of the bitmap relative to field's top-left (0,0) coordinate.
	 * @param y Y offset of the bitmap relative to field's top-left (0,0) coordinate.
	 */
	public static void updateBitmap(BufferedImage remoteSnap, RemoteAgent<?> remoteAgent, Double2D location, double x, double y)
	{
		remoteSnap.getRaster().setPixel(
				(int)(location.x - x) % remoteSnap.getWidth(),
				(int)(location.y - y) % remoteSnap.getHeight(),
				white);
	}
	
	/**
	 * Paints an agent position on the bitmap.
	 * @param remoteSnap Current bitmap depicting agents' position.
	 * @param remoteAgent The agent to paint.
	 * @param location New agent's position.
	 * @param x X offset of the region relative to field's top-left (0,0) coordinate.
	 * @param y Y offset of the region relative to field's top-left (0,0) coordinate.
	 */
	public static void updateBitmap(BufferedImage remoteSnap, RemoteAgent<?> remoteAgent, Int2D location, int x, int y)
	{
		remoteSnap.getRaster().setPixel(
				(location.x - x) % remoteSnap.getWidth(),
				(location.y - y) % remoteSnap.getHeight(),
				white);
	}
}
