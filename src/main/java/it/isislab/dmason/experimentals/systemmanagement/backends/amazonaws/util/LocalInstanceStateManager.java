/**
 * Copyright 2017 Universita' degli Studi di Salerno
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model.LocalInstanceState;

/**
 * 
 * The class <code>LocalInstanceStateManager</code> deals with persisting
 * {@link LocalInstanceState} objects representing Amazon EC2 instances
 * individually and not wrapped into a meta object like a {@link Collection},
 * {@link List} or {@link Map}.
 * @see LocalInstanceState
 * 
 * @author Bisogno Simone
 *
 */
public final class LocalInstanceStateManager
{
	/**
	 * 
	 * @param localInstanceStates - A {@link Map} of {@link LocalInstanceState} objects.
	 * @throws FileNotFoundException - File destination has not been found.
	 * @throws IOException - Error in reading or writing to file.
	 */
	public static void saveLocalInstanceStates(Map<String, LocalInstanceState> localInstanceStates)
			throws FileNotFoundException, IOException
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HOME.concat(FILE_NAME))))
		{
			Iterator<LocalInstanceState> statesIterator = localInstanceStates.values().iterator();
			while (statesIterator.hasNext())
			{
				oos.writeObject(statesIterator.next());
			}
		}
	}

	/**
	 * 
	 * @return A {@link Map} of {@link LocalInstanceState} objects.
	 * @throws FileNotFoundException - File source has not been found.
	 * @throws IOException - Error in reading or writing to file.
	 * @throws ClassNotFoundException - A class of objects read from file has not been found.
	 */
	public static Map<String, LocalInstanceState> loadLocalInstanceStates()
			throws FileNotFoundException, IOException, ClassNotFoundException
	{
		Map<String, LocalInstanceState> localInstanceStates = new HashMap<>();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HOME.concat(FILE_NAME))))
		{
			// read all the objects into the file
			while (true)
			{
				LocalInstanceState instanceState = (LocalInstanceState) ois.readObject();
				localInstanceStates.put(instanceState.getId(), instanceState);
			}
		}
		catch (EOFException eofe)
		{
			LOGGER.warning("End of file reached!");
		}

		return localInstanceStates;
	}

	/**
	 * 
	 * @param instanceId - Instance ID of {@link LocalInstanceState} object to look for.
	 * @return A {@link LocalInstanceState} object with specified ID if such an object exists,
	 * 		<strong><code>null</code></strong> otherwise.
	 * @throws FileNotFoundException - File source has not been found.
	 * @throws IOException - Error in reading or writing to file.
	 * @throws ClassNotFoundException - A class of objects read from file has not been found.
	 */
	public static LocalInstanceState retrieveLocalInstanceState(String instanceId)
			throws FileNotFoundException, IOException, ClassNotFoundException
	{
		LocalInstanceState localInstanceState = null;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HOME.concat(FILE_NAME))))
		{
			// read all the objects into the file
			while (true)
			{
				localInstanceState = (LocalInstanceState) ois.readObject();
				if (instanceId.equals(localInstanceState.getId()))
				{
					// local instance state associated to instanceId has been found
					break;
				}
			}
		}
		catch (EOFException eofe)
		{
			LOGGER.warning("End of file reached!");
		}

		return localInstanceState;
	}

	/**
	 * 
	 * @param localInstances - A {@link Map} of {@link LocalInstanceState} objects.
	 */
	public static void hookShutdown(Map<String, LocalInstanceState> localInstances)
	{
		Shutdown.hookShutdown(localInstances);
	}

	// inner classes
	/**
	 * This inner class takes care to save the local instances when the program
	 * is about to close: it adds a shutdown hook so that it gets automatically
	 * run to perform exiting actions, in this case to save local instances
	 * into file.<br>
	 * It won't trigger if a <code>SIGKILL</code> signal is sent to JVM process.
	 * 
	 * @author Simone Bisogno
	 * @see LocalInstanceStateManager
	 *
	 */
	public static class Shutdown
	{
		public Shutdown(Map<String, LocalInstanceState> localInstances)
		{
			// add the shutdown hook to runtime
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					LOGGER.warning("Application is shutting down, saving local instances states...");
					try
					{
						LocalInstanceStateManager.saveLocalInstanceStates(localInstances);
					}
					catch (IOException e)
					{
						LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
					}
				}
			}));
		}

		public static void hookShutdown(Map<String, LocalInstanceState> localInstances)
		{
			if (Shutdown.hook == null)
			{
				Shutdown.hook = new Shutdown(localInstances);
			}
		}

		// variables
		/**
		 * A shutdown hook that triggers automatic saving of
		 * {@link LocalInstanceState} objects passed in a {@link Map}.
		 */
		private static Shutdown hook;
	} // end inner class Shutdown

	// constants
	/**
	 * The file where {@link LocalInstanceState} objects dwell or where they will be saved.
	 */
	private static final String FILE_NAME = "LocalInstanceStates.dat";
	private static final String HOME = System.getProperty("user.home") + "/.aws/";
	private static final Logger LOGGER = Logger.getGlobal();
}
