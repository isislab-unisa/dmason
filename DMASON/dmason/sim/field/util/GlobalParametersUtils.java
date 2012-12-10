package dmason.sim.field.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedField;
import dmason.sim.util.DistributedProperties;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.visualization.RemoteSnap;

/**
 * 
 * @author Luca Vicidomini
 *
 */
public class GlobalParametersUtils
{
	/**
	 * Builds globalsNames and globalsMethods. This method should be called in a
	 * DistributedField's constructor.
	 * @param sm
	 * @param connection
	 * @param globalsNames
	 * @param globalsMethods
	 */
	public static void buildGlobalsList(DistributedState sm, ConnectionWithJMS connection, ArrayList<String> globalsNames, ArrayList<Method> globalsMethods)
	{
		Class<?> simClass = sm.getClass();
		
		/* 
		 * Inspect the simulation class using Java Reflection and look for
		 * distributed properties.
		 */
		DistributedProperties ds = new DistributedProperties(sm);
		
		// Go on if any distributed property is found
		if (ds.numProperties() > 0)
		{
			try {
				/*
				 * Create/subscribe topic "GLOBALS". This topic isn't related
				 * to any field, so we pass '1' as second parameter so getUpdates()
				 * will work.
				 */
				connection.createTopic("GLOBALS", 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// For every distributed property
			for (int pi = 0; pi < ds.numProperties(); pi++)
			{
				if (ds.isGlobal(pi))
				{
					// Build the list containing global parameters' names
					globalsNames.add(ds.getName(pi));
					try {
						// Build the list containing global parameters' method reference
						// This list allows for faster calling later.
						globalsMethods.add(simClass.getMethod("reduce" + ds.getName(pi), Object[].class));
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// This should not happen
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * This method should be called by a field at the beginning of the synchro() method.
	 * Please note that when D-MASON calls the synchro() method for the n-th time, the
	 * field must send information about the (n-1)-th simulation step. The previous step
	 * number is automatically calculated (it's the current step number minus one), the
	 * the previous time must be passed manually.  
	 * @param sm
	 * @param cellId
	 * @param currentTime Time occurring at the previous simulation step.
	 * @param globalsNames
	 * @param connection
	 */
	public static void sendGlobalParameters(SimState sm, ConnectionWithJMS connection, CellType cellId, double currentTime, ArrayList<String> globalsNames)
	{
		Class<?> simClass = sm.getClass();
		
		// Prepare the object that will contain names and values of the globals 
		RemoteSnap globalMap = new RemoteSnap(
				cellId,
				sm.schedule.getSteps(),
				currentTime);
		globalMap.stats = new HashMap<String, Object>();
		
		// Build the list
		for (int i = 0; i < globalsNames.size(); i++)
		{
			try
			{
				Method m = simClass.getMethod("get" + globalsNames.get(i), (Class<?>[])null);
				Object res = m.invoke(sm, new Object [0]);
				globalMap.stats.put(globalsNames.get(i), res);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Publish global parameters to topic "GLOBALS" 
		try
		{
			connection.publishToTopic(globalMap, "GLOBALS", "GLOBALS");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method should be called by a field after the call to
	 * GlobalParametersUtils.sendGlobalParameters and BEFORE the simulation
	 * phase of the current step begins.
	 * @param field
	 * @param numPeers 
	 * @param globalsNames
	 * @param globalsMethods
	 */
	public static void receiveAndUpdate(DistributedField field, int numPeers, ArrayList<String> globalsNames, ArrayList<Method> globalsMethods)
	{
		SimState sm = field.getState();
		
		/*
		 * globalUpdates is an hash map that will contain entries
		 * like ("region name", RemoteSnap).
		 */
		HashMap<String, Object> globalUpdates;
		try {
			globalUpdates = field.getGlobals().getUpdates(sm.schedule.getSteps(), numPeers);
		} catch (InterruptedException e1) {
			globalUpdates = new HashMap<String, Object>();
			e1.printStackTrace();
		}
		
		// Extract RemoteSnaps from globalUpdates. 
		RemoteSnap[] globalSnaps = new RemoteSnap[globalUpdates.size()];
		int i = 0;
		for(java.util.Map.Entry<String, Object> entry : globalUpdates.entrySet())
		{
			globalSnaps[i++] = (RemoteSnap)entry.getValue();
		}
		
		/*
		 * RemoteSnaps in globalUpdates refer to the same simulation step,
		 * so we can inspect an arbitrary RemoteSnap to extract some common
		 * information (such as number of properties).
		 * So we inspect the first RemoteSnap of the list, the we build an array
		 * containing global parameters' names (propNames) and a matrix of
		 * values (props).   
		 */
		RemoteSnap zeroSnap = globalSnaps[0];
		int numProps = zeroSnap.stats.size();
		Object[] propNames = zeroSnap.stats.keySet().toArray();
		Object[][] props = new Object[numProps][numPeers];
		for (i = 0; i < globalSnaps.length; i++)
		{
			RemoteSnap snap = (RemoteSnap)globalSnaps[i];
			for (int j = 0; j < numProps; j++)
			{
				// Cells on the columns, Properties on the rows
				props[j][i] = snap.stats.get(propNames[j]);
			}
		}
		
		// Update the simulation
		for (int propertyI = 0; propertyI < numProps; propertyI++)
		{
			String propName = (String)propNames[propertyI];
			try
			{
				int methodI = globalsNames.indexOf(propName);
				Method m = globalsMethods.get(methodI);
				/* Invoke the reduce method for a certain global parameter,
				 * passing an array containing a value for each cell for that
				 * global parameter.
				 */
				m.invoke(sm, new Object[] { props[propertyI] } );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
