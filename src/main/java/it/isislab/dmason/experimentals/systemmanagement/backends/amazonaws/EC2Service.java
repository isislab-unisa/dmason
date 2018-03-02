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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
//import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotPlacement;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model.LocalInstanceState;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.LocalInstanceStateManager;

/**
 *
 * Main class for connecting to EC2 service of Amazon AWS.
 *
 * @author Simone Bisogno
 *
 */
public class EC2Service
{
	// static variables
	/**
	 * The AMI is the <i>Amazon Machine Image</i> chosen to remotely instantiate.
	 * There are several kind of AMI and they all have an ID, here some
	 * examples:
	 * <ul>
	 *     <li><strong>ami-cd0f5cb6</strong>: Ubuntu Server 16.04 LTS HVM</lI>
	 *     <li><strong>ami-52a0c53b</strong>: StarCluster <code>?</code></li>
	 *     <li><strong>ami-841f46ff</strong>: Ubuntu Server 14.04 LTS (HVM)</li>
	 * </ul>
	 */
	private static String ami;
	private static String amiUser;
	private static boolean booted;
	private static String groupName;
	private static String region;
	private static int size;
	private static String type;
	private static AmazonEC2 ec2;
	/**
	 * The map of local instances states.
	 *
	 * @see #initializeLocalInstances()
	 */
	private static Map<String, LocalInstanceState> localInstances;
	/**
	 * Properties for <code>EC2Service</code> class.
	 *
	 * @see #loadProperties()
	 */
	private static Properties startProperties;

	// constants
	public static final String GROUP_PREFIX = "isislab-";
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String KEY_NAME = "dmason-key-2";
	private static final String PROPERTIES_FILE_PATH = "resources" + File.separator +
			"systemmanagement" + File.separator + "master" + File.separator + "conf" +
			File.separator + "config.properties";

	/**
	 * This method creates an AmazonEC2Client instance.
	 *
	 * @param aRegion - The region in which create the client.
	 */
	public static void buildEC2Client(String aRegion)
	{
		EC2Service.ec2 = AmazonEC2ClientBuilder.standard()
				.withRegion(aRegion)
				.withCredentials(new ProfileCredentialsProvider())
				.build();
	}

	public static void boot()
	{
		if (!EC2Service.booted)
		{
			// load properties
			EC2Service.loadProperties();
			LOGGER.info("Properties loaded from " + PROPERTIES_FILE_PATH + ".");

			// create an EC2 client
			EC2Service.buildEC2Client(EC2Service.region);
			LOGGER.info("A new EC2 client has been created!");

			// create security group and keypair
			try
			{
				EC2Service.createKeyPair();
			}
			catch (IOException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
			}
			EC2Service.createSecurityGroup();

			// initialize instance local state map
			EC2Service.initializeLocalInstances();
			LOGGER.info("Local instances states map populated!");

			EC2Service.booted = true;
			LOGGER.info("Amazon EC2 Service boot process completed!");
		}
		else
		{
			LOGGER.warning("Amazon EC2 Service is already booted!");
		}
	}

	/**
	 * This method creates a new instance into the specified group.
	 *
	 * @param maxNumberInstances - The number of instances to create
	 * @return result -
	 * @throws IOException - An error occurred while accessing the file
	 * @throws FileNotFoundException - Local instances file has not been found
	 */
	public static RunInstancesResult createInstance(int maxNumberInstances)
			throws FileNotFoundException, IOException
	{
		String groupName = EC2Service.GROUP_PREFIX.concat(EC2Service.getGroupName());
		String clusterType = "dmason";

		return EC2Service.createInstance(groupName, clusterType, maxNumberInstances);
	}

	/**
	 * This method creates a new instance into the specified group.
	 *
	 * @param groupName -
	 * @param clusterType -
	 * @param maxNumberInstances -
	 * @return result -
	 * @throws IOException - An error occurred while accessing the file
	 * @throws FileNotFoundException - Local instances file has not been found
	 */
	public static RunInstancesResult createInstance(String groupName, String clusterType, int maxNumberInstances)
			throws FileNotFoundException, IOException
	{
		RunInstancesRequest istanceRequest = new RunInstancesRequest();

		// define the requested virtual machine
		String instanceType = EC2Service.type;
		istanceRequest.withImageId(EC2Service.ami)
				.withInstanceType(instanceType)
				.withMinCount(1) // the minimum number of istances to launch
				.withMaxCount(maxNumberInstances) // the maximum number of istances to launch
				.withKeyName(EC2Service.KEY_NAME)
				.withSecurityGroups(groupName);

		// request the virtual machine
		RunInstancesResult result = ec2.runInstances(istanceRequest);

		// add all created local instances to local map
		Iterator<Instance> instanceIterator = result.getReservation().getInstances().iterator();
		while (instanceIterator.hasNext())
		{
			// add created local instance state to local map
			Instance instance = instanceIterator.next();
			String istanceID = instance.getInstanceId();
			String instanceDns = instance.getPublicDnsName();
			EC2Service.localInstances.put(istanceID, new LocalInstanceState(istanceID, instanceDns, instanceType)); // here it creates a new local instance state
			LOGGER.info(clusterType + " node reservation: " + istanceID);

			// tag created instance
			int numInstances = EC2Service.localInstances.values().size(); // size already includes created instance
			EC2Service.tagInstance(
					istanceID,
					"Name",
					numInstances + "-" + EC2Service.groupName + "-" + clusterType
			);
		}

		// update local instances map to file
		LocalInstanceStateManager.saveLocalInstanceStates(localInstances);

		return result;
	}

	/**
	 * This method creates a key pair to access the cluster associated to
	 * {@link #ec2} client.
	 *
	 * @throws IOException - An error occurred while accessing the file
	 * @throws FileNotFoundException - The key pair file has not been found
	 */
	public static void createKeyPair()
			throws IOException, FileNotFoundException
	{
		// get a list with a single key pair
		KeyPairInfo dmasonKeyPair = new KeyPairInfo().withKeyName(EC2Service.KEY_NAME);
		DescribeKeyPairsResult keyPairResult = EC2Service.ec2.describeKeyPairs().withKeyPairs(dmasonKeyPair);

		// check if the cluster already has a key pair
		boolean existsRemoteKeyPair = false;
		for (KeyPairInfo keyPair: keyPairResult.getKeyPairs()) // keyPairResult only has a single keypair
		{
			String keyName = keyPair.getKeyName();
			LOGGER.info("Checking " + keyName + " in keyring...");
			if (keyName.equalsIgnoreCase(KEY_NAME))
			{
				existsRemoteKeyPair = true;
				dmasonKeyPair = keyPair;
				LOGGER.warning("Remote key pair " + keyName + " already exists.");
				break;
			}
		}

		// create key pair file to access cluster
		String keysFilePath = System.getProperty("user.home") +
				"/.aws/" + KEY_NAME + ".pem";
		File file = new File(keysFilePath);
		PrintWriter filePrinter = null;
		if (!existsRemoteKeyPair)
		{
			// create a new key pair on EC2
			LOGGER.info("Create new key pair " + EC2Service.KEY_NAME + " on EC2");
			CreateKeyPairRequest keyRequest = new CreateKeyPairRequest().withKeyName(EC2Service.KEY_NAME);
			CreateKeyPairResult responseToCreate = EC2Service.ec2.createKeyPair(keyRequest);

			// save key pair into key file
			LOGGER.info("Saving newly created key pair " + EC2Service.KEY_NAME + "...");
			filePrinter = new PrintWriter(file);
			filePrinter.print(responseToCreate.getKeyPair().getKeyMaterial());
			LOGGER.info("New keyring saved into file!");
			filePrinter.close();
		}
		else
		{
			LOGGER.info("Key pair file has to be created!");

			// create key file
			if (!file.exists())
			{
				// request remote key pair
				CreateKeyPairRequest keyRequest = new CreateKeyPairRequest().withKeyName(EC2Service.KEY_NAME);
				CreateKeyPairResult responseToCreate = EC2Service.ec2.createKeyPair(keyRequest);

				// write key into file
				LOGGER.info("Creating " + EC2Service.KEY_NAME + " in " + keysFilePath + " ...");
				file.createNewFile();
				filePrinter = new PrintWriter(file);
				filePrinter.print(responseToCreate.getKeyPair().getKeyMaterial());
				filePrinter.close();
				LOGGER.info("Created " + EC2Service.KEY_NAME + "key pair file!");

				// make new file read-only
				try
				{
					EC2Service.makeFileReadOnly(keysFilePath);
				}
				catch (IOException | IllegalStateException ioe)
				{
					LOGGER.severe(
							ioe.getClass().getSimpleName() + ": " + ioe.getMessage() + "."
					);
				}
			}
			else
			{
				LOGGER.info("Key pair file for " + EC2Service.KEY_NAME + " already exists.");
			}
		}
	} // end createKeyPair()

	/**
	 * @throws IOException
	 *
	 */
	private static void makeFileReadOnly(String filePath)
			throws IOException
	{
		if (System.getProperty("os.name").contains("Windows"))
		{
			List<AclEntry> aclEntries = new Vector<>();
			AclEntry aclEntry1 = AclEntry.newBuilder()
					.setPermissions(AclEntryPermission.READ_ACL)
					.build();
			aclEntries.add(aclEntry1);
			AclEntry aclEntry2 = AclEntry.newBuilder()
					.setPermissions(AclEntryPermission.READ_ATTRIBUTES)
					.build();
			aclEntries.add(aclEntry2);
			AclEntry aclEntry3 = AclEntry.newBuilder()
					.setPermissions(AclEntryPermission.READ_DATA)
					.build();
			aclEntries.add(aclEntry3);
			AclEntry aclEntry4 = AclEntry.newBuilder()
					.setPermissions(AclEntryPermission.READ_NAMED_ATTRS)
					.build();
			aclEntries.add(aclEntry4);

			AclFileAttributeView view = Files.getFileAttributeView(
					Paths.get(filePath),
					AclFileAttributeView.class
			);
			view.setAcl(aclEntries);
		}
		else
		{
			Runtime.getRuntime().exec("chmod 0400 " + filePath);
		}
		LOGGER.info("Permissions to file " + filePath + " changed to read-only!");
	} // end makefileReadOnly(String)

	/**
	 * This method defines the security group for EC2 instances.
	 * Group name is defined into config.properties file.
	 *
	 */
	public static void createSecurityGroup()
	{
		EC2Service.createSecurityGroupByClusterName(EC2Service.groupName);
	}

	/**
	 * This method defines the security group for EC2 instances.
	 * if the specified group name doesn't exist, it gets created.
	 *
	 * @param groupName - the security group name which the instances belongs to.
	 */
	public static void createSecurityGroupByClusterName(String groupName)
	{
		DescribeSecurityGroupsRequest groupRequest = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult result = EC2Service.ec2.describeSecurityGroups(groupRequest);

		// check if group already exists
		List<SecurityGroup> groupList = result.getSecurityGroups();
		boolean groupExists = false;
		for (SecurityGroup sg: groupList)
		{
			if (sg.getGroupName().equals(groupName))
			{
				groupExists = true;
				LOGGER.warning("Security group " + groupName + " already exists.");
				break;
			}
		}

		if (!groupExists)
		{
			// security group doesn't exist
			LOGGER.info("Creating security group for cluster " + groupName);

			CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
			csgr.withGroupName(groupName).withDescription("Security group " + groupName + " created on " + LocalDateTime.now() + ".");

			CreateSecurityGroupResult csgresult = EC2Service.ec2.createSecurityGroup(csgr);
			LOGGER.info("Security group created for cluster " + groupName + " with id " + csgresult.getGroupId() + ".");

			IpPermission ipPermission =	new IpPermission();
			IpRange ipRange1 = new IpRange().withCidrIp("0.0.0.0/0"); // FIXME restrict IP interval
			ipPermission.withIpv4Ranges(Arrays.asList(new IpRange[] {ipRange1}))
					.withIpProtocol("tcp")
					.withFromPort(0) // FIXME restrict port interval as needed
					.withToPort(65535);

			AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
			authorizeSecurityGroupIngressRequest.withGroupName(groupName).withIpPermissions(ipPermission);
			ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

			LOGGER.info("Created security group " + groupName + " with /usr/bin/ssh enabled.");
		}
	} // end createSecurityGroupByClusterName(String)

//	public static void discardSession(String instanceId)
//	{
//		LocalInstanceState localInstanceState = EC2Service.getLocalInstances().get(instanceId);
//		localInstanceState.setSession(null);
//		EC2Service.getLocalInstances().put(instanceId, localInstanceState);
//	}

	/**
	 * This method retrieves the public DNS for an instance on a EC2 client.
	 *
	 * @param instanceId - The ID of an EC2 instance.
	 * @return The instance public DNS
	 */
	public static String getDns(String instanceId)
	{
		// try to retrieve DNS name from local map
		LocalInstanceState localInstanceState = EC2Service.localInstances.get(instanceId);
		if (
				localInstanceState != null &&
				localInstanceState.getDns() != null &&
				!localInstanceState.getDns().isEmpty()
		)
		{
			return localInstanceState.getDns();
		}

		// local retrieval failed, retrieve DNS name from EC2 client
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request = request.withInstanceIds(instanceId);
		DescribeInstancesResult result = ec2.describeInstances(request);

		return result.getReservations().get(0).getInstances().get(0).getPublicDnsName();
	}

	/**
	 * This method retrieves the session related to specified instance ID.
	 * If no session is locally stored, a new one gets established.
	 *
	 * @param instanceId - The ID of the instance
	 * @return session - The session for instance specified by ID.
	 */
	public static Session getSession(String instanceId)
	{
		return EC2Service.getSession(instanceId, EC2Service.getAmiUser());
	}

	/**
	 * This method retrieves the session related to specified instance ID.
	 * If no session is locally stored, a new one gets established.
	 *
	 * @param instanceId - The ID of the instance
	 * @param username - The username on the instance
	 * @return session - The session for instance specified by ID.
	 */
	public static Session getSession(String instanceId, String username)
	{
		Session session = null;
		String publicDns = EC2Service.getDns(instanceId);
		if (publicDns == null || publicDns.isEmpty())
		{
			LOGGER.severe("No public DNS provided!");
			return null;
		}
		else
		{
			LOGGER.info("Public DNS: " + publicDns);
		}
		final int PORT = 22;

		// pick session for instance associated to instanceId
//		if (!forceNewSession)
//		{
//			session = EC2Service.localInstances.get(instanceId).getSession();
//		}
//		else
//		{
//			LOGGER.warning("Existing session for instance " + instanceId + " will be discarded!");
//			EC2Service.localInstances.get(instanceId).setSession(null);
//		}

		// instantiate a new session
		if (session == null)
		{
			LOGGER.warning(
					"There is no existing session for instance " + instanceId +
					" on " + publicDns + "!"
			);

			// instantiate a secure shell
			LOGGER.info("Instantiating a secure channel...");
			JSch jsch = new JSch();

			// specify private key
			String userHome = System.getProperty("user.home");
			boolean keyPairExists = false;
			try
			{
				// set private key for session
				String keyPath =
						userHome + File.separator +
						".aws" + File.separator +
						EC2Service.KEY_NAME + ".pem";
				LOGGER.info("Retrieving private key from " + keyPath + "...");
				jsch.addIdentity(keyPath);
				keyPairExists = true;
				LOGGER.info("Private key " + EC2Service.KEY_NAME + " for session has been set!");

				// create a ssh session
				LOGGER.info(
						"Creating SSH session on secure channel...\n" +
						username + "@" + publicDns + ":" + PORT
				);
				session = jsch.getSession(username, publicDns, PORT);
			}
			catch (JSchException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");

				if (!keyPairExists)
				{
					LOGGER.severe(
							"There is no " + EC2Service.KEY_NAME + ".pem keypair!\nUnable to establish a session!"
					);
					return null;
				}
			}
			session.setConfig("StrictHostKeyChecking", "no"); // do not look for known_hosts file
			LOGGER.info("A new session for instance " + instanceId + " has been created!");

			// save session in local map
//			LocalInstanceState instanceState = EC2Service.localInstances.get(instanceId);
//			instanceState.setSession(session);
//			EC2Service.localInstances.put(instanceId, instanceState);
		}
//		else
//		{
//			LOGGER.info("A session for " + instanceId + " already exists!");
//		}

		// check connection over session
//		try
//		{
//			session.connect(RemoteCommand.SESSION_TIMEOUT);
//			LOGGER.info("connection test over session: successful!");
//			session.disconnect();
//		}
//		catch (JSchException e) {
//			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
//			//e.printStackTrace();
//		}

		return session;
	} // end getSession(String)

	/**
	 * This method initializes a map of existing EC2 instance local states
	 * into the region selected in buildEC2Client(String) method.
	 *
	 * @see #buildEC2Client(String)
	 */
	private static void initializeLocalInstances()
	{
		EC2Service.localInstances = new HashMap<>();

		LOGGER.warning("Downloading available instances...");

		DescribeInstancesRequest listRequest = new DescribeInstancesRequest();
		DescribeInstancesResult result = EC2Service.ec2.describeInstances();
		boolean done = false;

		while (!done)
		{
			for (Reservation reservation: result.getReservations())
			{
				for (Instance instance: reservation.getInstances())
				{
					String instanceId = instance.getInstanceId();
					String instanceDns = EC2Service.getDns(instanceId);
					String instanceType = EC2Service.type;
					String stateName = instance.getState().getName();
					boolean isRunning = stateName.equals(InstanceStateName.Running.toString());
					boolean terminated = stateName.equals(InstanceStateName.Terminated.toString());

					// put in the map only non-terminated instances
					if (!terminated)
					{
						LocalInstanceState localInstance = new LocalInstanceState(instanceId, instanceDns, instanceType);
						localInstance.setRunning(isRunning);
						EC2Service.localInstances.put(instanceId, localInstance);
					}
					else
					{
						LOGGER.warning("Instance " + instanceId + " is terminated, discarded.");
					}
				}
			}

			listRequest.setNextToken(result.getNextToken());

			if (result.getNextToken() == null)
			{
				done = true;
			}
		} // end while

		// create local instances states file if there's none
		try
		{
			// try to read from file without actually loading its content
			LocalInstanceStateManager.loadLocalInstanceStates();
		}
		catch (FileNotFoundException fnfe)
		{
			LOGGER.warning("Local instances states file not found!");
			try
			{
				// try to write a new file: it doesn't actually write anything
				LocalInstanceStateManager.saveLocalInstanceStates(EC2Service.localInstances);
			}
			catch (IOException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
				System.exit(-1);
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
			System.exit(-1);
		}

		LocalInstanceStateManager.hookShutdown(localInstances);
		LOGGER.info("Local instances state map is ready!");
	}

	/**
	 *
	 * This helper method deals with loading properties from a specified
	 * file path.
	 *
	 */
	private static void loadProperties()
	{
		InputStream input = null;
		try
		{
			// read properties file
			LOGGER.info("Reading properties from " + PROPERTIES_FILE_PATH);
			input = new FileInputStream(PROPERTIES_FILE_PATH);
			if (EC2Service.startProperties == null)
			{
				EC2Service.startProperties = new Properties();
			}
			startProperties.load(input);

			// set class parameters from config file
			final String AWS_PREFIX = "ec2".concat(".");
			EC2Service.setAmi(startProperties.getProperty(AWS_PREFIX.concat("ami")));
			EC2Service.setAmiUser(startProperties.getProperty(AWS_PREFIX.concat("amiuser")));
			EC2Service.setRegion(startProperties.getProperty(AWS_PREFIX.concat("region")));
			EC2Service.setGroupName(startProperties.getProperty(AWS_PREFIX.concat("securitygroup")));
			EC2Service.setSize(Integer.parseInt(startProperties.getProperty(AWS_PREFIX.concat("size"))));
			EC2Service.setType(startProperties.getProperty(AWS_PREFIX.concat("type")));
		}
		catch (IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		finally
		{
			try
			{
				input.close();
			}
			catch (IOException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
			}
		}
	}

	public static void persistInstanceStates()
			throws FileNotFoundException, IOException
	{
		LocalInstanceStateManager.saveLocalInstanceStates(EC2Service.localInstances);
	}

	/**
	 * This method reboots an existing running instance
	 * by specifying its ID as parameter.
	 *
	 * @param instanceId - The instance ID of the instance to reboot
	 * @return Reboot instance result
	 */
	public static RebootInstancesResult rebootInstance(String instanceId)
	{
		// logging level
//		LOGGER.setLevel(Level.SEVERE);

		LOGGER.info("Request for instance " + instanceId + " to reboot.");
		Instance instance = retrieveInstance(instanceId);

		// check if the instance is running
		// check if instance is already stopping
		if (instance.getState().getName().equals(InstanceStateName.Stopped.toString()))
		{
			LOGGER.warning("Instance " + instanceId + " is not running!");
			return null;
		}

		RebootInstancesRequest rebootRequest = new RebootInstancesRequest();

		// define the reboot request
		rebootRequest.withInstanceIds(instanceId);
		RebootInstancesResult result = ec2.rebootInstances(rebootRequest);

		// looks like no state change occurs for rebooting
		LOGGER.info("Instance " + instanceId + " has been started again!");

		return result;
	}

	/**
	 * Ths method deals with requesting Spot Instances to Amazon EC2.<br />
	 * Minimal Spot Instances request must have a number of instances and
	 * a bid price specified. Other parameters may be whether request has
	 * to be persistent (i.e. if after all Spot Instances associated with
	 * it have been terminated it has to be terminated as well), whether a
	 * persistente storage has to be used, whether Spot Instance must have
	 * a specified duration (minutes from request submission and request
	 * duration), whether Spot Instances have all to be in specified
	 * Availability Zone, in some non-specified Availability Zone or just
	 * in the same launch group.
	 * 
	 * @param numInstances - The number of Spot Instances to run
	 * @param bid - The bid for Spot Instances
	 * @param isPersistent - If <code>true</code>, Spot Instances
	 * 			will be persistent
	 * @param deviceName - If not null and not empty, data will be
	 * 			persistent into specified device
	 * @param minutesStart - If greater than zero, this will be the
	 * 			minutes that have to pass from the moment request
	 * 			has been made for it to be active
	 * @param minutesExpire - If greater than <code>minutesStart</code>
	 * 			, this will be the minutes the request is valid
	 * @param availabilityZone - The name of Availability Zone which
	 * 			instances will be created in
	 * @param availabilityZoneGroup - The name of group for Spot
	 * 			Instances that have to be created in the same non-
	 * 			specified Availability Zone
	 * @param launchGroup - The name of the group of Spot Instances
	 * 			that have to be launched altogether and terminated
	 * 			in the same fashion as well
	 * @return result - Result for Spot Instances request
	 */
	public static RequestSpotInstancesResult requestSpotInstance(
			int numInstances,
			float bid,
			boolean isPersistent,
			String deviceName,
			int minutesStart,
			int minutesExpire,
			String availabilityZone,
			String availabilityZoneGroup,
			String launchGroup
	)
	{
		RequestSpotInstancesRequest request = new RequestSpotInstancesRequest();
		LaunchSpecification specs = new LaunchSpecification();

		// set bid price into request
		request.withSpotPrice(String.valueOf(bid))
				.withInstanceCount(Integer.valueOf(numInstances));

		// determine whether request is persistent
		if (isPersistent)
		{
			request.setType("persistent");
		}

		// determine whether data have to be persistent
		if (deviceName != null && !deviceName.isEmpty())
		{
			// Create the block device mapping to describe the root partition.
			BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
			blockDeviceMapping.setDeviceName(deviceName);
			
			// Set the delete on termination flag to false.
			EbsBlockDevice ebs = new EbsBlockDevice();
			ebs.setDeleteOnTermination(Boolean.FALSE);
			blockDeviceMapping.setEbs(ebs);
			
			// Add the block device mapping to the block list.
			List<BlockDeviceMapping> blockList = new ArrayList<>();
			blockList.add(blockDeviceMapping);
			
			// Set the block device mapping configuration in the launch specifications.
			specs.setBlockDeviceMappings(blockList);
		}		

		// set instance duration
		if (minutesStart > 0 && minutesExpire > minutesStart)
		{
			// convert minutes into milliseconds and add them to current time
			long millisStart = System.currentTimeMillis() + (1000*60*minutesStart);
			long millisExpire = System.currentTimeMillis() + (1000*60*minutesExpire);

			// set duration into request
			request.withValidFrom(new Date(millisStart))
					.withValidUntil(new Date(millisExpire));
		}

		// set availability zone
		// instances will be launched in specified Availability Zone
		if (availabilityZone != null && !availabilityZone.isEmpty())
		{
			specs.setPlacement(new SpotPlacement(availabilityZone));
		}
		// set availability zone group
		// instances will be launched in the same Availability Zone
		// (a subregion of defined region)
		else if (availabilityZoneGroup != null && !availabilityZoneGroup.isEmpty())
		{
			request.setAvailabilityZoneGroup(availabilityZoneGroup);
		}
		// set launch group
		// instances in group will be launched together
		// and terminate together as well, unless user intervention
		else if (launchGroup != null && !launchGroup.isEmpty())
		{
			request.setLaunchGroup(launchGroup);
		}

		

		// configure other request details
		specs.withImageId(EC2Service.getAmi())
				.withInstanceType(EC2Service.getType());
		request.setLaunchSpecification(specs);

		// send request to EC2
		RequestSpotInstancesResult result = EC2Service.ec2.requestSpotInstances(request);
		EC2Service.tagSpotInstancesRequest(
				result,
				"Name",
				"Request-" + new Long(System.currentTimeMillis()).hashCode() + "-" + EC2Service.groupName
		);
		return result;

		// spot instances cannot be added to local instance states map
		// because they may not be created right after request
	}

	private static Instance retrieveInstance(String instanceId)
	{
		LOGGER.info("Requesting instance " + instanceId + " status...");

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request = request.withInstanceIds(instanceId);
		DescribeInstancesResult result = ec2.describeInstances(request);

		// get the only instance possible, if ID is right
		List<Instance> instancesList = result.getReservations().get(0).getInstances();
		if (instancesList.size() == 0)
		{
			LOGGER.severe("No instance retrieved!");
			return null;
		}
		Instance instance = instancesList.get(0);

		// check for the instance and its ID
		if (instance != null && instanceId.equals(instance.getInstanceId()))
		{
			boolean isRunning = instance.getState().getName().equals(InstanceStateName.Running.toString());
			LocalInstanceState localInstance = EC2Service.localInstances.get(instanceId);
			localInstance.setRunning(isRunning);
			EC2Service.localInstances.put(instanceId, localInstance);
		}
		else
		{
			LOGGER.severe("Wrong instance ID!");
			return null;
		}

		LOGGER.info("Request of instance " + instanceId + " completed!");
		return instance;
	}

	/**
	 * This method starts an existing instance by specifying
	 * its ID as parameter.
	 *
	 * @param instanceId - The instance ID of the instance to start
	 * @return start instance result
	 */
	public static StartInstancesResult startInstance(String instanceId)
	{
		// logging level
//		LOGGER.setLevel(Level.SEVERE);

		LOGGER.info("Request for instance " + instanceId + " to start.");
		Instance instance = retrieveInstance(instanceId);

		// check if instance is already running
		if (instance.getState().getName().equals(InstanceStateName.Running.toString()))
		{
			LOGGER.warning("Instance " + instanceId + " is already running!");
			return null;
		}

		LOGGER.info("Instance " + instanceId + " is not already running!");
		StartInstancesRequest startRequest = new StartInstancesRequest();

		// define the start request
		startRequest.withInstanceIds(instanceId);
		StartInstancesResult result = ec2.startInstances(startRequest);

		// be sure the instance is running
		boolean started = false;
		do
		{
			instance = retrieveInstance(instanceId);
			if (instance.getState().getName().equals(InstanceStateName.Running.toString()))
			{
				started = true;
			}

			try
			{
				// wait 1 second before checking again
				Thread.sleep(1000);
			}
			catch (InterruptedException ie)
			{
				LOGGER.warning("Thread not ready to sleep!");
			}
		}
		while (!started);
		LOGGER.info("Instance " + instanceId + " has been started!");

		return result;
	} // end startInstance(String) method

	/**
	 * This method stops an existing running instance
	 * by specifying its ID as parameter.
	 *
	 * @param instanceId - The instance ID of the instance to stop
	 * @return stop instance result
	 */
	public static StopInstancesResult stopInstance(String instanceId)
	{
		// logging level
//		LOGGER.setLevel(Level.SEVERE);

		LOGGER.info("Request for instance " + instanceId + " to stop.");
		Instance instance = retrieveInstance(instanceId);
		String instanceState = instance.getState().getName();

		// check if instance is already stopping
		if (instanceState.equals(InstanceStateName.Stopping.toString()))
		{
			LOGGER.warning("Instance " + instanceId + " is already stopping");
			return null;
		}

		// check if instance has been already stopped
		if (instanceState.equals(InstanceStateName.Stopped.toString()))
		{
			LOGGER.warning("Instance " + instanceId + " has been already stopped!");
			return null;
		}

		StopInstancesRequest stopRequest = new StopInstancesRequest();

		// define the stop request
		stopRequest.withInstanceIds(instanceId);
		StopInstancesResult result = EC2Service.ec2.stopInstances(stopRequest);

		// be sure the instance is stopped
		boolean stopped = false;
		do
		{
			instance = EC2Service.retrieveInstance(instanceId);
			if (instance.getState().getName().equals(InstanceStateName.Stopped.toString()))
			{
				stopped = true;
			}
			try
			{
				Thread.sleep(2000); // stopping takes longer
			}
			catch (InterruptedException ie)
			{
				LOGGER.warning("Thread not ready to sleep!");
			}
		}
		while (!stopped);
		LOGGER.info("Instance " + instanceId + " has been stopped!");

		return result;
	}

	/**
	 * This method tags an existing instance with its name
	 * and its cluster type.
	 *
	 * @param istanceId - The instance to tag
	 * @param tagName - The name of the tag
	 * @param tagValue - The value of the tag
	 * @param ec2Client - The client containing the instance to tag
	 */
	private static void tagInstance(String istanceId, String tagName, String tagValue)
	{
		CreateTagsRequest tagRequest = new CreateTagsRequest();

		// wait 1 second before tagging the instance
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException ie)
		{
			LOGGER.severe(ie.getClass().getName() + ": " + ie.getMessage() + ".");
		}

		tagRequest = tagRequest
				.withResources(istanceId)
				.withTags(new Tag(tagName, tagValue));
		EC2Service.ec2.createTags(tagRequest);
	}

	private static void tagSpotInstancesRequest(RequestSpotInstancesResult result, String tagName, String tagValue)
	{
		List<SpotInstanceRequest> requestResponses = result.getSpotInstanceRequests();

		// A list of request IDs to tag
		List<String> spotInstanceRequestIds = new ArrayList<>();

		// Add the request ids to the hashset, so we can
		// determine when they hit the active state
		for (SpotInstanceRequest requestResponse: requestResponses)
		{
		    LOGGER.info("Created Spot Request: " + requestResponse.getSpotInstanceRequestId());
		    spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
		}

		// wait 1 second before tagging the spot instances request
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException ie)
		{
			LOGGER.severe(ie.getClass().getName() + ": " + ie.getMessage() + ".");
		}

		// Create the tag request
		CreateTagsRequest createTagsRequestRequests = new CreateTagsRequest()
				.withResources(spotInstanceRequestIds)
				.withTags(new Tag(tagName, tagValue));

		// Tag the spot request
		EC2Service.ec2.createTags(createTagsRequestRequests);
	}

	/**
	 * This method terminates an existing running instance
	 * by specifying its ID as parameter.
	 *
	 * @param instanceId - The instance ID of the instance to terminate
	 * @return terminate instance status
	 */
	public static TerminateInstancesResult terminateInstance(String instanceId)
	{
		// logging level
//		LOGGER.setLevel(Level.SEVERE);

		LOGGER.info("Request for instance " + instanceId + " to terminate.");
		Instance instance = retrieveInstance(instanceId);
		String instanceState = instance.getState().getName();

		// check for instance not to be already terminated
		if (instanceState.equals(InstanceStateName.Terminated.toString()))
		{
			LOGGER.warning("Instance " + instanceId + " has been already terminated!");
			return null;
		}

		TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();

		// define the termination request
		terminateRequest.withInstanceIds(instanceId);
		TerminateInstancesResult result = EC2Service.ec2.terminateInstances(terminateRequest);

		// be sure the instance is terminated
		boolean terminated = false;
		do
		{
			instance = EC2Service.retrieveInstance(instanceId);
			if (instance.getState().getName().equals(InstanceStateName.Terminated.toString()))
			{
				terminated = true;
			}
			try
			{
				Thread.sleep(2000); // termination requires shutdown, takes time as well
			}
			catch (InterruptedException ie)
			{
				LOGGER.warning("Thread not ready to sleep!");
			}
		}
		while (!terminated);
		LOGGER.info("Instance " + instanceId + " has been terminated!");

		// mark the instance as terminated in local map
		LocalInstanceState localInstanceState = EC2Service.localInstances.get(instanceId);
		localInstanceState.markTerminate();
		EC2Service.localInstances.put(instanceId, localInstanceState);

		return result;
	}

	// getters and setters
	public static String getAmi()
	{
		return ami;
	}

	public static void setAmi(String ami)
	{
		EC2Service.ami = ami;
	}

	public static String getAmiUser()
	{
		return amiUser;
	}

	public static void setAmiUser(String amiUser)
	{
		EC2Service.amiUser = amiUser;
	}

	public static String getGroupName()
	{
		return groupName;
	}

	public static void setGroupName(String groupName)
	{
		EC2Service.groupName = groupName;
	}

	public static String getRegion()
	{
		return region;
	}

	public static void setRegion(String region)
	{
		EC2Service.region = region;
	}

	public static int getSize()
	{
		return size;
	}

	public static void setSize(int size)
	{
		EC2Service.size = size;
	}

	public static String getType()
	{
		return type;
	}

	public static void setType(String type)
	{
		EC2Service.type = type;
	}

	public static Map<String, LocalInstanceState> getLocalInstances()
	{
		return localInstances;
	}

	public static AmazonEC2 getEc2()
	{
		return ec2;
	}
} // end AmazonService class
