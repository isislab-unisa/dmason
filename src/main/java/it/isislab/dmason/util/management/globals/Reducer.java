/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.util.management.globals;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.management.JarClassLoader;
import it.isislab.dmason.util.management.Worker.StartUpData;
import it.isislab.dmason.util.management.Worker.Updater;
import it.isislab.dmason.util.management.globals.util.UpdateGlobalVarAtStep;
import it.isislab.dmason.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.util.visualization.globalviewer.VisualizationUpdateMap;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import sim.display.GUIState;

/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
* @author Luca Vicedomini
*/
public class Reducer extends Thread
{
	private ConnectionNFieldsWithActiveMQAPI connection;
	ArrayList<MessageListener> listeners;
	VisualizationUpdateMap<Long, RemoteSnap> updates = new VisualizationUpdateMap<Long, RemoteSnap>();
	private PriorityQueue<RemoteSnap> queue;
	DistributedState simulationInstance = null;
	
	private int numPeers;
	String topicPrefix;
	
	/**
	 * True if the simulation uses global parameters, false otherwise
	 */
	private boolean isValid;
	
	public Reducer(StartUpData data, ConnectionNFieldsWithActiveMQAPI connection, Object state)
	{
		this.connection = connection;
		this.numPeers = data.getParam().getRows() * data.getParam().getColumns();
		this.topicPrefix = data.getTopicPrefix();
		this.queue = new PriorityQueue<RemoteSnap>();
		if( state instanceof GUIState)
		{
			simulationInstance =(DistributedState)((GUIState)state).state;
		}
		else simulationInstance =(DistributedState)state;
		// TODO NOTA I RAGAZZI HANNO SOSTITUITO QUESTA RIGA COL BLOCCO DI SOPRA, PER ORA COMMENTO. Luca
		// simulationInstance = makeState(data.getDef(), data.getParam(), data);
		
		if (simulationInstance != null)
		{
			createTopics(connection, topicPrefix);
			isValid = true;
		}
		else
		{
			isValid = false;
		}
		
//		Constructor<?> constructor;
//		try {
//			constructor = simClass.getConstructor();
//			simulationInstance = (DistributedState)constructor.newInstance(new Object[] { });
//			createTopics(connection, topicPrefix);
//		} catch (NoSuchMethodException e) {
//			/*
//			 * NoSuchMethodException means that the simulation doesn't have a void
//			 * constructor, so probably it doesn't support Global Parameters,
//			 * hence the Reducer won't start.
//			 */
//			isValid = false;
//		} catch (Exception e) {
//			// Fallback
//			e.printStackTrace();
//		}
	}


	void createTopics(ConnectionNFieldsWithActiveMQAPI connection, String topicPrefix)
	{
		try {
			// Publish reduced data
			connection.createTopic(topicPrefix + "GLOBAL_REDUCED", 1);
			
			// Gather globals data from workers
			connection.subscribeToTopic(topicPrefix + "GLOBAL_DATA");
			listeners = new ArrayList<MessageListener>();
			Thread listener = new UpdaterThreadForGlobalsDataListener(
					connection, 
					this, 
					topicPrefix + "GLOBAL_DATA", 
					listeners);
			listener.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	@Override
	public void run()
	{
		if (!isValid)
		{
			return;
		}
		
		// Cache this
		CellType cellId = new CellType(-1, -1);
		
		// Set current simulation step to 1, since Reducer will be active from the beginning of the simulation
		long step = 1;
		UpdateGlobalVarAtStep upVar=new UpdateGlobalVarAtStep(simulationInstance);
		
		// Through the whole simulation
		while (true)
		{
			try {				
				// Retrieve data from workers
				if( upVar.getAllGlobalVarForStep(step) == null ){
					step++;
					continue;
				}
				HashMap<String,Object> snaps = updates.getUpdates(step, numPeers);
				
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
				HashMap<Method, Object[]> reinitMethod = new HashMap<Method, Object[]>();
				
				// Perform reduction
				for (int propertyI = 0; propertyI < propCount; propertyI++)
				{
					String propName = (String)propNames[propertyI];
					try
					{
							Method reductionMethod = simulationInstance.getClass().getMethod("reduce" + propName, Object[].class);
							// the reinitialize method must be invoke last
							if (reductionMethod.getName().contains("Reinitialize")) {
								reinitMethod.put(reductionMethod, new Object[] { propValues[propertyI] }); 
								continue;
							}
							System.out.println("Reducer: Invoco "+reductionMethod.getName());	
							Object value = reductionMethod.invoke(simulationInstance, new Object[] { propValues[propertyI] } );
							reducedSnap.stats.put(propName, value);
	
					} catch (Exception e) {		
						e.printStackTrace();
					}
				}
				
				// invoke ReinitialMethod
				for (Method method : reinitMethod.keySet()) {
					Object value;
					try {
						value = method.invoke(simulationInstance, reinitMethod.get(method) );
						reducedSnap.stats.put(method.getName().substring(6), value);
						System.out.println("Reducer: Invoco "+method.getName());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
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
	
	// -------------------------------------------------------------------
	// SAME AS WORKER.JAVA
	
	public DistributedState makeState(Class simClass, GeneralParam args_gen, StartUpData data)
	{
		Object obj = null;

		if(simClass != null) //hardcoded simulation
		{
			Constructor constr;
			try {
				constr = simClass.getConstructor();
				obj = constr.newInstance(new Object[]{ });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else // Jar simulation
		{
			try
			{
				URL url = Updater.getSimulationJar(data);
				obj = getSimulationInstance(args_gen, url, false);
			} catch (Exception e) {
				throw new RuntimeException("Exception occurred while trying to construct the simulation " + simClass + "\n" + e);			
			}
		}


		if (obj instanceof DistributedState)
		{
			// The instantiated class is the proper simulation class
			return (DistributedState)obj;
		}
		else
		{
			// Read as "get <state> variable from object <obj>"
			try {
				return (DistributedState)obj.getClass().getField("state").get(obj);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		return null;
	}

	private Object getSimulationInstance(GeneralParam args_gen, URL url, boolean isGui)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException,
			InstantiationException 
			{
		JarClassLoader cl = new JarClassLoader(url);

		cl.addToClassPath();

		String name = null;
		try {
			name = cl.getMainClassName();
		} catch (IOException e) {
			System.err.println("I/O error while loading JAR file:");
			e.printStackTrace();
			System.exit(1);
		}
		if (name == null) {
			System.out.println("Specified jar file does not contain a 'Main-Class' manifest attribute");
		}

		if(isGui)
		{
			//name += "WithUI";
		}
		
		return cl.getInstance(name, args_gen);
	}


}
