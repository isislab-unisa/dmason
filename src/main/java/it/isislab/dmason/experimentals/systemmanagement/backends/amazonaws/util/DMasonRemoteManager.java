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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.AmazonService;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model.LocalInstanceState;

/**
 * 
 * The <code>DREmoteManager</code> class deals with instructions to remote
 * EC2 instances on Amazon AWS to install, run and stop DMASON.
 * 
 * @author Simone Bisogno
 *
 */
public class DMasonRemoteManager
{
	/**
	 * 
	 * The <strong><code>installDMason(String)</code></strong> method deals
	 * with installing DMASON on the EC2 instance specified by given <code>
	 * instanceId</code> parameter.<br>
	 * The method checks for Java Development Kit installation, Maven
	 * installation and finally retrieves DMASON from GitHub repository and
	 * compiles it with Maven; in case of installation, an update of APT
	 * repository is automatically required.<br>
	 * The method double-checks if DMASON is already installed by checking
	 * the <code>LocalInstanceState</code> object with given ID and by
	 * remotely checking if the <em><code>isislab/dmason/target</code></em>
	 * folder exists.
	 * 
	 * @param instanceId - The ID of the instance where DMASON will be
	 * 		installed
	 */
	public static void installDMason(String instanceId)
	{
		// check if DMASON is already installed on required instance machine
		LocalInstanceState localInstanceState = AmazonService.getLocalInstances().get(instanceId);

		// check if instance is running
		if (localInstanceState != null && !localInstanceState.isRunning())
		{
			LOGGER.warning("Instance " + instanceId + " is not running!");
			return;
		}

		// check if instance has already DMASON installed
		Session session = null;
		if (localInstanceState.isReady())
		{
			LOGGER.warning("DMASON is already installed on instance " + instanceId + "!");
			return;
		}

		LOGGER.info("Installing DMASON on instance " + instanceId + "...");

		// establish a ssh session
		LOGGER.info("Establishing a new session...");
		try
		{
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false); // username is set according to AMI
			session.connect(AmazonService.SESSION_TIMEOUT);
			int exitStatus = 0;

			// check if DMASON is installed already
			// if 'target' folder exists, maven built DMASON already
			final String LOG_FILE_NAME = "ls.log";
			LOGGER.info("Check if DMASON is remotely installed...");
			exitStatus = AmazonService.executeCommand(
					session,
					"ls isislab/dmason/target/ > " + LOG_FILE_NAME, // in case of ls error, an empty file gets generated
					true);
			LOGGER.info("Remote check completed!");
			LOGGER.info("Connection returned " + exitStatus);
			session.disconnect();
			AmazonService.retrieveFile(instanceId, "", "", LOG_FILE_NAME);
			File logFile = new File(LOG_FILE_NAME);
			if (logFile.length() > 0)
			{
				// mark DMASON installed for instance in local map and return control
				localInstanceState.setReady(true);
				AmazonService.getLocalInstances().put(instanceId, localInstanceState);
				LOGGER.warning("DMASON was already installed on instance " + instanceId + "!");
				return;
			}

			// re-establish session because of SFTP request
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false);
			session.connect();

			// update remote repositories
			LOGGER.info("Update repositories...");
			exitStatus = AmazonService.executeCommand(
					session,
					"sudo apt-get update -q -y",
					true
			);
			LOGGER.info("Connection returned " + exitStatus);
			LOGGER.info("Remote repositories have been updated!");

			// install Java Development Kit
			// if JDK is installed already, it doesn't get installed by -y
			LOGGER.info("Installing Java Development Kit...");
			exitStatus = AmazonService.executeCommand(
					session,
					"sudo apt-get install default-jdk -q -y", // -q quiet, -y assert-all
					true
			);
			LOGGER.info("Connection returned " + exitStatus);
//			exitStatus = AmazonService.executeCommand(
//					session,
//					"dpkg --get-selections | grep jdk",
//					true
//			);
//			LOGGER.info("Connection returned " + exitStatus);
			LOGGER.info("Java Development Kit has been installed!");

			// install maven
			LOGGER.info("Installing Maven...");
			exitStatus = AmazonService.executeCommand(
					session,
					"sudo apt-get install maven -y",
					true
			);
			LOGGER.info("Connection returned " + exitStatus);
			LOGGER.info("Maven has been installed!");

			// install DMASON
			LOGGER.info("Downloading DMASON...");
			exitStatus = AmazonService.executeCommand(
					session,
					"mkdir isislab" + ";" +
					"cd isislab/" + ";" +
					"git clone https://github.com/isislab-unisa/dmason.git",
					true
			);
			LOGGER.info("Connection returned " + exitStatus);
			LOGGER.info("DMASON has been downloaded to instance " + instanceId + "!");

			// compile DMASON in 'maven' folder
			LOGGER.info("Compiling DMASON...");
			exitStatus = AmazonService.executeCommand(
					session,
					"cd ~/isislab/dmason/" + ";" + // every connection starts from home directory
					"mvn -Dmaven.test.skip=true clean package", // skip tests
					true
			);
			LOGGER.info("Connection returned " + exitStatus);
			LOGGER.info("DMASON has been compiled and is ready to run!");
		}
		catch (JSchException jsce)
		{
			LOGGER.severe(jsce.getClass().getSimpleName() + ": " + jsce.getMessage() + ".");
		}
		catch (IOException | InterruptedException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		finally
		{
			// close secure session
			if (session != null && session.isConnected())
			{
				session.disconnect();

				// discard session: this is a workaround for packet loss closing session after a SFTP channel connection
				localInstanceState.setSession(null);
				AmazonService.getLocalInstances().put(instanceId, localInstanceState);
			}
		}

		// marking instance as DMASON-ready
		localInstanceState.setReady(true);
		AmazonService.getLocalInstances().put(instanceId, localInstanceState);

		LOGGER.info("DMASON installation has been completed!");
	} // end installDMason(String, String)

	/**
	 * 
	 * The <strong><code>startDMason(String, boolean, int)</code></strong>
	 * method deals with running DMASON on the EC2 instance specified by
	 * given <code>instanceId</code> parameter.<br>
	 * The method double-checks for existing running Java process related to
	 * DMASON execution by checking the <code>LocalInstanceState</code>
	 * object with given ID and by remotely checking the list of running
	 * processes.<br>
	 * The method can run DMASON on the instance with <em>master</em> or <em>
	 * worker</em> role; in the latter case, the number of workers can be
	 * specified. 
	 * 
	 * @param instanceId - The ID of the instance where DMASON will be
	 * 		installed
	 * @param isMaster - A boolean determining the role of the instance
	 * @param numWorkers - The number of workers to run on the instance
	 */
	public static void startDMason(String instanceId, boolean isMaster)
	{
		LocalInstanceState localInstanceState = AmazonService.getLocalInstances().get(instanceId);

		// check if instance is running
		if (localInstanceState != null && !localInstanceState.isRunning())
		{
			LOGGER.warning("Instance " + instanceId + " is not running!");
			return;
		}

		// check if instance has got DMASON installed
		if (!localInstanceState.isReady())
		{
			LOGGER.warning("DMASON is not installed on instance " + instanceId + "!");
			return;
		}

		// check if selected instance is already running DMASON
		if (localInstanceState.isBusy())
		{
			if (localInstanceState.isMaster())
			{
				LOGGER.warning(
						"DMASON is already running on " + instanceId +
						" as master at http://" + localInstanceState.getDns() +
						":8080 !"
				);
			}
			else
			{
				LOGGER.warning(
						"DMASON is already running on " + instanceId +
						" as slave at http://" + localInstanceState.getDns() +
						":8080 !"
				);
			}
			return;
		}
		LOGGER.info("Running DMASON on " + localInstanceState.getDns() + "...");

		// establish a ssh session
		Session session = null;
		try {
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false); // username is set according to AMI
			session.connect(AmazonService.SESSION_TIMEOUT); // 30s timeout
			int exitStatus = 0;
			final String DMASON_ABS_PATH = "~/isislab/dmason/target/";
			final String version = VersionChooser.extract(); // determine which DMASON version is running

			// check if actually DMASON is running on instance
			// if remote 'ps x' output contains 'java' then skip start command
			final String LOG_FILE_NAME = "ps.log";
			LOGGER.info("Check if DMASON is already running...");
			exitStatus = AmazonService.executeCommand(
					session,
					"ps x | grep 'java -jar DMASON' | grep -v 'grep' > " + LOG_FILE_NAME, // in case of ls error, an empty file gets generated
					true);
			LOGGER.info("");
			LOGGER.info("Connection returned " + exitStatus);
			session.disconnect();
			AmazonService.retrieveFile(instanceId, "", "", LOG_FILE_NAME);
			File logFile = new File(LOG_FILE_NAME);
			if (logFile.length() > 0)
			{
				localInstanceState.setBusy(true);
				AmazonService.getLocalInstances().put(instanceId, localInstanceState);
				LOGGER.warning(
						"DMASON was already running on instance " + instanceId +
						" as master at http://" + localInstanceState.getDns() +
						":8080 !"
				);
				return;
			}

			// update selected instance state on local map
			// local instance status gets updated before time because
			// remote command blocks execution
			localInstanceState.setBusy(true);
			localInstanceState.setMaster(isMaster);
			AmazonService.getLocalInstances().put(instanceId, localInstanceState);
			if (isMaster)
			{
				LOGGER.info("DMASON is running as master on http://" + localInstanceState.getDns() + ":8080 !");
			}
			else
			{
				LOGGER.info("DMASON is running as worker on http://" + localInstanceState.getDns() + ":8080 !");
			}

			// re-establish session because of SFTP request
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false);
			session.connect();

			// 
			if (isMaster)
			{
				LOGGER.info("Running as master...");
				exitStatus = AmazonService.executeCommand(
						session,
						"cd " + DMASON_ABS_PATH + ";" +
						"java -jar DMASON-" + version + ".jar -m master",
						false
				);
				LOGGER.info("Connection returned " + exitStatus);
			}
			else
			{
				LOGGER.info("Running as worker...");
				String instanceType = localInstanceState.getType();
				int numSlots = EC2CoresForType.getCores(instanceType);
				LOGGER.info("Running a worker with " + numSlots + " slots...");
				exitStatus = AmazonService.executeCommand(
						session,
						"cd " + DMASON_ABS_PATH + ";" +
						"java -jar DMASON-" + version + ".jar -m worker -ns " + numSlots,
						false
				);
				LOGGER.info("Connection returned " + exitStatus);
			}
		}
		catch (JSchException jsce)
		{
			LOGGER.severe(jsce.getClass().getSimpleName() + ": " + jsce.getMessage() + ".");
			return;
		}
		catch (ParserConfigurationException | SAXException e)
		{
			LOGGER.severe(
					e.getClass().getSimpleName() + ": " + e.getMessage() + ".\n" +
					"Unable to retrieve DMASON version!"
			);
			return;
		}
		catch (IOException | InterruptedException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		finally
		{
			// close session
			if (session != null && session.isConnected())
			{
				session.disconnect();
			}
		}
	} // end startDMason(String, String)

	/**
	 * 
	 * The <strong><code>stopDMason(String)</code></strong> method deals with
	 * stopping DMASON running on the EC2 instance specified by given <code>
	 * instanceId</code> parameter.<br>
	 * The method double-checks for existing running Java process related to
	 * DMASOn execution by checking the <code>LocalInstanceState</code>
	 * object with given ID and by remotely checking the list of running
	 * processes.<br>
	 * 
	 * @param instanceId - The ID of the instance where DMASON will be
	 * 		installed
	 */
	public static void stopDMason(String instanceId)
	{
		LocalInstanceState localInstanceState = AmazonService.getLocalInstances().get(instanceId);

		// check if selected instance is running DMASON
		if (!localInstanceState.isRunning())
		{
			LOGGER.info("Instance " + instanceId + " is not running!");
			return;
		}

		if (!localInstanceState.isReady())
		{
			LOGGER.warning("DMASON is not installed on instance " + instanceId);
			return;
		}

		if (!localInstanceState.isBusy())
		{
			LOGGER.warning("DMASON is not running on instance " + instanceId);
			return;
		}

		LOGGER.info("Stopping DMASON on " + localInstanceState.getDns() + "...");

		// establish a ssh session
		Session session = null;
		try
		{
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false); // username is set according to AMI
			session.connect(AmazonService.SESSION_TIMEOUT); // 30s timeout
			int exitStatus = 0;

			// check if actually DMASON is running on instance
			// if remote 'ps x' output does not contains 'java' then skip stop command
			final String LOG_FILE_NAME = "ps.log";
			LOGGER.info("Check if DMASON is running...");
			exitStatus = AmazonService.executeCommand(
					session,
					"ps x | grep 'java -jar DMASON' | grep -v 'grep' > " + LOG_FILE_NAME, // in case of ls error, an empty file gets generated
					true);
			LOGGER.info("");
			LOGGER.info("Connection returned " + exitStatus);
			session.disconnect();
			AmazonService.retrieveFile(instanceId, "", "", LOG_FILE_NAME);
			File logFile = new File(LOG_FILE_NAME);
			if (logFile.length() == 0)
			{
				// mark DMASON running for instance in local map and return control
				localInstanceState.setBusy(false);
				AmazonService.getLocalInstances().put(instanceId, localInstanceState);
				LOGGER.warning("DMASON was not running on instance " + instanceId + "!");
				return;
			}

			// re-establish session because of SFTP request
			session = AmazonService.getSession(instanceId, AmazonService.getAmiUser(), false);
			session.connect();

			LOGGER.info("Running as master...");
			exitStatus = AmazonService.executeCommand(
					session,
					"pkill java", // could it be more specific?
					true
			);
			LOGGER.info("Connection returned " + exitStatus);

		}
		catch(JSchException jsce)
		{
			LOGGER.severe(jsce.getClass().getSimpleName() + ": " + jsce.getMessage() + ".");
			return;
		}
		catch (IOException | InterruptedException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		finally
		{
			if (session != null && session.isConnected())
			{
				// close the session
				session.disconnect();
			}
		}

		// save current status for selected instance
		localInstanceState.setRunning(false);
		AmazonService.getLocalInstances().put(instanceId, localInstanceState);
		try
		{
			LocalInstanceStateManager.saveLocalInstanceStates(AmazonService.getLocalInstances());
		}
		catch (IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		LOGGER.info("Stopped DMASON on " + localInstanceState.getDns() + "!");
	} // end stopDMason(String) method

	// constants
	private static final Logger LOGGER = Logger.getGlobal();
}
