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
package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesResult;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.EC2Service;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model.LocalInstanceState;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.DMasonRemoteFileManager;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.DMasonRemoteManager;

/**
 * @author Simone Bisogno
 *
 */
public class EC2ServiceTester
{
	// constants
	private static final BufferedReader BUFFER = new BufferedReader(new InputStreamReader(System.in));
	private static final PrintStream CONSOLE = new PrintStream(System.out);
	private static final PrintStream ERROR_CONSOLE = new PrintStream(System.out);
	private static final Logger LOGGER = Logger.getGlobal();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)
			throws Exception
	{
		EC2Service.boot();
		new EC2ServiceTester().runClient(args);
	}

	// helper methods
	private static String ask()
			throws IOException
	{
		return BUFFER.readLine();
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	private void runClient(String[] args)
			throws Exception
	{
		LOGGER.info("Running " + this.getClass().getSimpleName() + " class");

		// parse command line parameters
//		CmdLineParser parser = new CmdLineParser(this);
//		parser.parseArgument(args);

		// check region value
//		if (AmazonService.region == null || AmazonService.region.isEmpty())
//		{
//			LOGGER.warning("No region specified with '-r' parameter!");
//			CONSOLE.println("Setting 'us-east-1' (North Virginia) as default region.");
//			AmazonService.region = "us-east-1";
//		}
//
//		// check security group name value
//		if (AmazonService.name == null || AmazonService.name.isEmpty())
//		{
//			LOGGER.warning("No security group name specified with '-n' parameter!");
//			CONSOLE.println("Setting 'dmason' as security group name.");
//			AmazonService.name = "dmason";
//		}
//
//		// check number of instances
//		if (AmazonService.size == 0)
//		{
//			LOGGER.warning("No number of instances specified with '-s' parameter!");
//			CONSOLE.println("Setting '1' as default instances number.");
//			AmazonService.size = 1;
//		}

		// test reading properties from config file
		CONSOLE.println("Read properties from configuration file");
		CONSOLE.print("AMI: " + EC2Service.getAmi() + ", ");
		CONSOLE.print("AMI username: " + EC2Service.getAmiUser() + ", ");
		CONSOLE.print("Security group: " + EC2Service.getGroupName() + ", ");
		CONSOLE.print("Region: " + EC2Service.getRegion() + ", ");
		CONSOLE.print("Size: " + EC2Service.getSize() + ", ");
		CONSOLE.println("Type: " + EC2Service.getType() + ", ");
		CONSOLE.println("\nPress ENTER key to continue...");
		ask();

		// test print map
		CONSOLE.println("Listing all local instances...");
//		Iterator<Map.Entry<String, Instance>> instancesIterator = AmazonService.instances.entrySet().iterator();
		Iterator<String> localInstancesIdIterator = EC2Service.getLocalInstances().keySet().iterator();
		int i = 0;
		while (localInstancesIdIterator.hasNext())
		{
			String instanceID = localInstancesIdIterator.next();
			CONSOLE.println("Instance " + ++i + ": " + instanceID);
		}

		CONSOLE.println("Listing active instances...");
		Iterator<LocalInstanceState> localInstancesIterator = EC2Service.getLocalInstances().values().iterator();
		while (localInstancesIterator.hasNext())
		{
			LocalInstanceState localInstanceState = localInstancesIterator.next();
			if (localInstanceState.isReady())
			{
				CONSOLE.println("Instance " + localInstanceState.getId() + " is running!");
			}
		}

		//
		try
		{
			// show availability zones
			DescribeAvailabilityZonesResult availabilityZoneResult = EC2Service.getEc2().describeAvailabilityZones();
			CONSOLE.print("You have access to " + availabilityZoneResult.getAvailabilityZones().size());
			CONSOLE.println(" Availability Zone(s).");

			DescribeInstancesResult describeInstancesResult = EC2Service.getEc2().describeInstances();
			List<Reservation> reservations = describeInstancesResult.getReservations();
			Set<Instance> instances = new HashSet<>();

			for (Reservation reservation: reservations)
			{
				instances.addAll(reservation.getInstances());
			}

			CONSOLE.println("You have " + instances.size() + " Amazon EC2 instance(s).");

			if (EC2Service.getSize() == 0)
			{
				ERROR_CONSOLE.println("The number of instances should be greater than 0!");
				System.exit(-1);
			}

			// create security group for cluster with /usr/bin/ssh
//			String groupName = name + (new Long(System.currentTimeMillis()).hashCode());
			String groupName = EC2Service.GROUP_PREFIX.concat(EC2Service.getGroupName());
			EC2Service.createSecurityGroupByClusterName(groupName);

			// create /usr/bin/ssh access
			EC2Service.createKeyPair();

			RunInstancesResult instancesResult = null;
			String option = null;
			String optionsMessage = "Type !CREATE to create a new remote instance, press ENTER key to skip this passage: ";
			CONSOLE.print(optionsMessage);
			boolean proceed = false;
			while (!(option = BUFFER.readLine().toUpperCase()).equals(""))
			{
				option = option.toUpperCase();
				switch (option)
				{
				case "!CREATE":
				{
					CONSOLE.println("Start a cluster with name " + EC2Service.getGroupName() + " of " + EC2Service.getSize() + " instance(s).");
					instancesResult = EC2Service.createInstance(groupName, "dmason", 1);
					Iterator<Instance> instanceIterator = instancesResult.getReservation().getInstances().iterator();
					while (instanceIterator.hasNext())
					{
						CONSOLE.println("Generated instance " + instanceIterator.next().getInstanceId());
					}
					proceed = true;
					break;
				}

				default:
				{
					ERROR_CONSOLE.println("Unknown command!");
					CONSOLE.print(optionsMessage);
					break;
				}
				}
				// skip while cycle if instance gets created
				if (proceed)
				{
					break;
				}
			}

			// run node
			String instanceId = null;
			try
			{
				// pick the first non terminated instance
				Iterator<LocalInstanceState> localInstanceStateIterator = EC2Service.getLocalInstances().values().iterator();
				while (localInstanceStateIterator.hasNext())
				{
					LocalInstanceState local = localInstanceStateIterator.next();
					if (!local.isTerminated())
					{
						instanceId = local.getId();
						LOGGER.info("Instance " + instanceId + " is not terminated!");
						break;
					}
				}

			}
			catch (Exception e)
			{
				ERROR_CONSOLE.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				System.exit(-1);
			}
			CONSOLE.println("Picked instance " + instanceId);

			// check picked instance
			if (instanceId == null)
			{
				LOGGER.severe("No instance has been picked!");
				System.exit(-1);
			}

			// test start instance
			CONSOLE.println("\nPress ENTER to start the " + instanceId + " instance...");
			ask();
			EC2Service.startInstance(instanceId);

			// wait for /usr/bin/ssh connection
//			String dns = AmazonService.instances.get(instanceId).getPublicDnsName();

			// install DMASON
			CONSOLE.println("Press ENTER to install DMASON on instance " + instanceId + "...");
			ask();
			boolean installed = false;
			do
			{
				try
				{
					DMasonRemoteManager.installDMason(instanceId);
					installed = true;
				}
				catch (Exception e)
				{
					LOGGER.severe(
							e.getClass().getSimpleName() + ": " + e.getMessage() +
							".\nCause: " + e.getCause()
					);
					Thread.sleep(1000);
				}
			}
			while (!installed);

			// test remote SFTP
			CONSOLE.print("\nPress ENTER to copy a file to instance " + instanceId);
			ask();
			DMasonRemoteFileManager.putFile(instanceId, "", "", "testfile");

			CONSOLE.print("\nPress ENTER to copy a file from instance " + instanceId);
			ask();
			DMasonRemoteFileManager.retrieveFile(instanceId, "", "", "remotefile");

			// start DMASON on instance as separate thread
			CONSOLE.print("\nPress ENTER to start DMASON on instance " + instanceId);
			ask();
//			startDMason(instanceId, true, 1);
			final String INSTANCE_ID = instanceId;
			Thread startDMASONThread = new Thread(
				new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(250);
						}
						catch (InterruptedException e)
						{
							LOGGER.severe(e.getClass().getName() + ": " + e.getMessage() + ".");
						}
						// TODO add activeMQ IP and port before running a worker
						DMasonRemoteManager.setActiveMQIP("0.0.0.0");
						DMasonRemoteManager.setActiveMQPort("61616");
						DMasonRemoteManager.startDMason(INSTANCE_ID, false);
					}
				}
			);
			startDMASONThread.start();
			startDMASONThread.join();

			// stop DMASON on instance
			CONSOLE.print("\nPress ENTER to stop DMASON on instance " + instanceId);
			ask();
			DMasonRemoteManager.stopDMason(instanceId);
			startDMASONThread.interrupt();

			// test restart instance
			CONSOLE.print("\nPress ENTER to reboot the " + instanceId + " instance");
			ask();
			EC2Service.rebootInstance(instanceId);

			// test stop instance
			CONSOLE.print("\nPress ENTER to shut the " + instanceId + " instance down");
			ask();
			EC2Service.stopInstance(instanceId);

			// test terminate instance
			CONSOLE.print("\nPress ENTER to terminate the " + instanceId + " instance");
			ask();
			EC2Service.terminateInstance(instanceId);
		}
		catch (AmazonServiceException ase)
		{
			LOGGER.severe("A " + ase.getClass().getSimpleName() + " exception has occurred!");
			ERROR_CONSOLE.println("Caught Exception: " + ase.getMessage());
			ERROR_CONSOLE.println("Reponse Status Code: " + ase.getStatusCode());
			ERROR_CONSOLE.println("Error Code: " + ase.getErrorCode());
			ERROR_CONSOLE.println("Request ID: " + ase.getRequestId());
		}

		LOGGER.info(this.getClass().getSimpleName() + " execution terminated!");
	} // end runClient(String[])
} // end AmazonServiceTest class
