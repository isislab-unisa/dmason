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
package it.isislab.dmason.experimentals.sim.field.support.globals;

import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.DistributedProperties;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import sim.engine.SimState;

/**
 * Contains static methods useful to manage Global Parameters. These methods should be used
 * within <code>DistributedField</code>s' <code>synchro()</code> method.
 * @author Michele Carillo, Ada Mancuso, Flavio Serrapica, Carmine Spagnuolo, Francesco Raia, Luca Vicidomini
 */
public class GlobalParametersHelper
{
	/**
	 * Builds globalsNames and globalsMethods. This method should be called in a
	 * <code>DistributedField</code>'s constructor.
	 * @param sm Current simulation instance.
	 * @param connection Current connection server. 
	 * @param globalsNames A list that will be filled with discovered global parameters' names.
	 * @param globalsMethods A list that will be filled with discovered global parameters' methods.
	 */
	public static void buildGlobalsList(DistributedState<?> sm, ConnectionJMS connection, String topicPrefix, List<String> globalsNames, List<Method> globalsMethods)
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
				 * Create/subscribe topic "GLOBAL_DATA". This topic isn't related
				 * to any field, so we pass '1' as second parameter so getUpdates()
				 * will work.
				 */
				connection.createTopic(topicPrefix + "GLOBAL_DATA", 1);
				connection.createTopic(topicPrefix + "GLOBAL_REDUCED", 1);
				//connection.subscribeToTopic(topicPrefix + "GLOBAL_DATA");
				//connection.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
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
						globalsMethods.add(simClass.getMethod("setGlobal" + ds.getName(pi), Object.class));
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
	public static void sendGlobalParameters(SimState sm, ConnectionJMS connection, String topicPrefix, CellType cellId, double currentTime, List<String> globalsNames)
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
				
				/*if(!globalsNames.get(i).contains("Every"))
				{
					Method m = simClass.getMethod("get" + globalsNames.get(i), (Class<?>[])null);
					Object res = m.invoke(sm, new Object [0]);
					globalMap.stats.put(globalsNames.get(i), res);
				}
				else
				{
					
					Method m2 = simClass.getMethod("get" + globalsNames.get(i)+"ValueOf", (Class<?>[])null);
					long EVERY_STEP = (Long)m2.invoke(sm, new Object [0]);
					if((sm.schedule.getSteps()%EVERY_STEP)==0) 
					{
						
						Method m = simClass.getMethod("get" + globalsNames.get(i), (Class<?>[])null);
						Object res = m.invoke(sm, new Object [0]);
						globalMap.stats.put(globalsNames.get(i), res);
					}
				}*/
				//Method m = simClass.getMethod("getEveryValueOf" + globalsNames.get(i), (Class<?>[])null);
				//long EVERY_STEP =(long)m.invoke(sm, new Object [0]);
				//if(/*STEP==EVERY_STEP*/)DO
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Publish global parameters to topic "GLOBAL_DATA" 
		try
		{
			connection.publishToTopic(globalMap, topicPrefix + "GLOBAL_DATA", "GLOBALS");
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
	public static void receiveAndUpdate(DistributedField2D<?> field, List<String> globalsNames, List<Method> globalsMethods)
	{
		SimState sm = field.getState();
		Class<?> simClass = sm.getClass();
		HashMap<String, Object> globalUpdates = null;
		try {
			// Second parameter is 1 because we receive a single, reduced, message
			globalUpdates = field.getGlobals().getUpdates(sm.schedule.getSteps(), 1);
			RemoteSnap globalSnap = (RemoteSnap)(globalUpdates.values().toArray()[0]);
			Set<String> propertyNames = globalSnap.stats.keySet();
			HashMap<Method, Object[]> reinitMethod = new HashMap<Method, Object[]>();
			for (String propName : propertyNames)
			{
				Method m;
				try {
				
					m = simClass.getMethod("setGlobal" + propName, Object.class);
					if (m.getName().contains("Reinitialize")) {
						reinitMethod.put(m, new Object[] { globalSnap.stats.get(propName) }); 
						continue;
					}
					m.invoke(sm, new Object[] { globalSnap.stats.get(propName) } );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			for (Method method : reinitMethod.keySet()) { method.invoke(sm, reinitMethod.get(method));}
		} catch (InterruptedException e) {
			e.printStackTrace();
			globalUpdates = new HashMap<String, Object>();
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
		return;
	}

}
