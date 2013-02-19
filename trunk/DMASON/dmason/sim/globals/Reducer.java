package dmason.sim.globals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import dmason.sim.engine.DistributedState;
import dmason.sim.field.CellType;
import dmason.sim.field.MessageListener;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.RemoteSnap;
import dmason.util.visualization.VisualizationUpdateMap;


public class Reducer extends Thread
{
	private ConnectionNFieldsWithActiveMQAPI connection;
	ArrayList<MessageListener> listeners;
	VisualizationUpdateMap<Long, RemoteSnap> updates = new VisualizationUpdateMap<Long, RemoteSnap>();
	private PriorityQueue<RemoteSnap> queue;
	DistributedState simulationInstance = null;
	
	private int numPeers;
	String topicPrefix;
	
	public Reducer(Class<?> simClass, ConnectionNFieldsWithActiveMQAPI connection, int numPeers, String topicPrefix)
	{
		this.connection = connection;
		this.numPeers = numPeers;
		this.topicPrefix = topicPrefix;
		this.queue = new PriorityQueue<RemoteSnap>();
		
		Constructor<?> constructor;
		try {
			constructor = simClass.getConstructor();
			simulationInstance = (DistributedState)constructor.newInstance(new Object[] { });
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
				
		try {
			// Publish reduced data
			connection.createTopic(topicPrefix + "GLOBAL_REDUCED", 1);
			
			// Gather globals data from workers
			connection.subscribeToTopic(topicPrefix + "GLOBAL_DATA");
			listeners = new ArrayList<MessageListener>();
			Thread listener = new UpdaterThreadForGlobalsDataListener(connection, this, topicPrefix + "GLOBAL_DATA", listeners);
			listener.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	@Override
	public void run()
	{
		// Cache this
		CellType cellId = new CellType(-1, -1);
		
		// Set current simulation step to 1, since Reducer will be active from the beginning of the simulation
		long step = 1;
		
		// Through the whole simulation
		while (true)
		{
			try {				
				// Retrieve data from workers
				HashMap<String,Object> snaps = (HashMap<String,Object>)updates.getUpdates(step, numPeers);
				
				// This will contain reduced data
				RemoteSnap reducedSnap = new RemoteSnap(
						cellId, // Cell Id will be ignored by workers
						step,
						0.0); // Time will be ignored by workers
				reducedSnap.stats = new HashMap<String, Object>();
				
				String[] snapIndices = snaps.keySet().toArray( new String[0] );
				int snapCount = snapIndices.length;
				
				// Retrieve information from an arbitrary snap object
				RemoteSnap zeroSnap = (RemoteSnap)snaps.get(snapIndices[0]);
				int propCount = zeroSnap.stats.size();
				
				// Reduce data: build properties table
				Object[] propNames = zeroSnap.stats.keySet().toArray();
				Object[][] propValues = new Object[propCount][numPeers];
				
				for (int snapIndex = 0; snapIndex < snapCount; snapIndex++)
				{
					RemoteSnap snap = (RemoteSnap)snaps.get(snapIndices[snapIndex]);					
					for (int propIndex = 0; propIndex < propCount; propIndex++)
					{
						propValues[propIndex][snapIndex] = snap.stats.get(propNames[propIndex]);
					}
				}
				
				// Perform reduction
				for (int propertyI = 0; propertyI < propCount; propertyI++)
				{
					String propName = (String)propNames[propertyI];
					try
					{
						Method reductionMethod = simulationInstance.getClass().getMethod("reduce" + propName, Object[].class);
						Object value = reductionMethod.invoke(simulationInstance, new Object[] { propValues[propertyI] } );
						reducedSnap.stats.put(propName, value);
					} catch (Exception e) {		
						e.printStackTrace();
					}
				}
				
				// Send reduced data to GLOBAL_REDUCED
				connection.publishToTopic(reducedSnap, topicPrefix + "GLOBAL_REDUCED", "GLOBALS");
				
				// Prepare for next step
				step++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public VisualizationUpdateMap<Long, RemoteSnap> getUpdatesMap()
	{
		return updates;
	}


}
