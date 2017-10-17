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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
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
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.model.LocalInstanceState;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.LocalInstanceStateManager;
import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.VersionChooser;

/**
 * 
 * Main class for connecting to Amazon AWS
 * 
 * @author Simone Bisogno
 * 
 */
public class AmazonService // FIXME sort access descriptor for methods
{
	// static variables
	/**
	 * The AMI is the Amazon Machine Image chosen to remotely instantiate.
	 * There are several kind of AMI and they all have an ID:
	 * <ul>
	 *     <li><strong>ami-cd0f5cb6</strong>: Ubuntu Server 16.04 LTS HVM</lI>
	 *     <li><strong>ami-52a0c53b</strong>: StarCluster <code>?</code></li>
	 *     <li><strong>ami-841f46ff</strong>: Ubuntu Server 14.04 LTS (HVM)</li>
	 * </ul>
	 */
	private static String ami;
	private static String amiUser;
	private static boolean booted;
	private static String name;
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
	 * Properties for <code>AmazonService</code> class.
	 * 
	 * @see #loadProperties()
	 */
	private static Properties startProperties;

	// constants
	private static final int CONNECTION_TIMEOUT = 15000; // 15s
	public static final String GROUP_PREFIX = "isislab-";
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String MY_KEY = "dmason-key";
	private static final String PROPERTIES_FILE_PATH = "resources" + File.separator +
			"systemmanagement" + File.separator + "master" + File.separator + "conf" +
			File.separator + "config.properties";
	private static final int SESSION_TIMEOUT = 30000; // 30s

	/**
	 * This method creates an AmazonEC2Client instance.
	 * 
	 * @param aRegion - The region in which create the client.
	 */
	public static void buildEC2Client(String aRegion)
	{
		ec2 = AmazonEC2ClientBuilder.standard()
				.withRegion(aRegion)
				.withCredentials(new ProfileCredentialsProvider())
				.build();
	}

	public static void boot()
	{
		if (!AmazonService.booted)
		{
			// load properties
			AmazonService.loadProperties();
			LOGGER.info("Properties loaded from " + PROPERTIES_FILE_PATH + ".");

			// create an EC2 client
			AmazonService.buildEC2Client(AmazonService.region);
			LOGGER.info("A new EC2 client has been created!");

			// initialize instance local state map
			AmazonService.initializeLocalInstances();
			LOGGER.info("Local instances states map populated!");

			AmazonService.booted = true;
			LOGGER.info("Amazon Service boot process completed!");
		}
		else
		{
			LOGGER.warning("Amazon Service is already booted!");
		}
	}

	/**
	 * This method creates a new instance into the specified group.
	 * 
	 * @param groupName
	 * @param clusterType
	 * @return result
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static RunInstancesResult createInstance(String groupName, String clusterType, int maxNumberInstances)
			throws FileNotFoundException, IOException
	{
		RunInstancesRequest istanceRequest = new RunInstancesRequest();

		// define the requested virtual machine
		istanceRequest.withImageId(AmazonService.ami)
				.withInstanceType(AmazonService.type)
				.withMinCount(1) // the minimum number of istances to launch
				.withMaxCount(maxNumberInstances) // the maximum number of istances to launch
				.withKeyName(AmazonService.MY_KEY)
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
			AmazonService.localInstances.put(istanceID, new LocalInstanceState(istanceID, instanceDns)); // here it creates a new local instance state
			LOGGER.info(clusterType + " node reservation: " + istanceID);

			// tag created instance
			int numInstances = AmazonService.localInstances.values().size(); // size already includes created instance
			AmazonService.tagInstance(
					istanceID,
					"Name",
					numInstances + "-" + AmazonService.name + "-" + clusterType,
					AmazonService.ec2
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
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void createKeyPair()
			throws IOException, FileNotFoundException
	{
		DescribeKeyPairsResult keyPairResult = ec2.describeKeyPairs();

		// check if the cluster already has a key pair
		boolean checkKey = false;
		for (KeyPairInfo key_pair: keyPairResult.getKeyPairs())
		{
			if (key_pair.getKeyName().equalsIgnoreCase(MY_KEY))
			{
				checkKey = true;
				LOGGER.warning("Key pair for current cluster already exists.");
				break;
			}
		}

		// create key pair to access cluster
		if (!checkKey)
		{
			// create key file
			String keysFilePath = System.getProperty("user.home") +
						"/.aws/" + MY_KEY + ".pem";
			File file = new File(keysFilePath);
			if (!file.exists())
			{
				file.createNewFile();
			}
			else
			{
				LOGGER.severe("Cannot create the key pair to access the cluster!");
				System.exit(1);
			}

			LOGGER.info("Create new key pair ~/.aws/" + MY_KEY + ".pem");
			CreateKeyPairRequest request = new CreateKeyPairRequest().withKeyName(MY_KEY);
			CreateKeyPairResult responseToCreate = ec2.createKeyPair(request);

			// save key into key file
			PrintWriter print = new PrintWriter(file);
			print.print(responseToCreate.getKeyPair().getKeyMaterial());
			print.close();

			// make file read-only
			try
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
							Paths.get(keysFilePath),
							AclFileAttributeView.class
					);
					view.setAcl(aclEntries);
				}
				else
				{
					Runtime.getRuntime().exec("chmod 0400 " + keysFilePath);
				}
				LOGGER.info("Permissions to key file changed to read-only!");
			}
			catch (IOException ioe)
			{
				LOGGER.severe(
						ioe.getClass().getSimpleName() + ioe.getMessage()
				);
			}
		}
	} // end createKeyPair()

	/**
	 * This method defines the security group for EC2 instances.
	 * if the specified group name doesn't exist, it gets created.
	 * 
	 * @param groupName - the security group name which the instances belongs to.
	 */
	public static void createSecurityGroupByClusterName(String groupName)
	{
		DescribeSecurityGroupsRequest groupRequest = new DescribeSecurityGroupsRequest();
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(groupRequest);

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

			CreateSecurityGroupResult csgresult = ec2.createSecurityGroup(csgr);
			LOGGER.info("Security group created for cluster " + groupName + " with id " + csgresult.getGroupId() + ".");

			IpPermission ipPermission =	new IpPermission();
			IpRange ipRange1 = new IpRange().withCidrIp("0.0.0.0/0"); // FIXME restrict IP interval
			ipPermission.withIpv4Ranges(Arrays.asList(new IpRange[] {ipRange1}))
					.withIpProtocol("tcp")
					.withFromPort(0)
					.withToPort(65535);

			AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
			authorizeSecurityGroupIngressRequest.withGroupName(groupName).withIpPermissions(ipPermission);
			ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

			LOGGER.info("Created security group " + groupName + " with /usr/bin/ssh enabled.");
		}
	} // end createSecurityGroupByClusterName(String)

	/**
	 * This method executes a remote command on a ChannelExec channel
	 * and returns a status code.
	 * 
	 * @param channel - The channel where the command has to be executed
	 * @param in - The input stream for the channel
	 * @param command - The command to execute on the remote channel
	 * @param printRemoteOutput - Toggle remote console
	 * @return exitStatus - Exit status for the command
	 * @throws JSchException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int executeCommand(Session session, String command, boolean printRemoteOutput)
			throws JSchException, IOException, InterruptedException
	{
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
//		((Channel) channel).setOutputStream(System.out);
//		channel.setErrStream(System.err);

		channel.setCommand(command);
		channel.connect(AmazonService.CONNECTION_TIMEOUT);
		int exitStatus = AmazonService.readRemoteInput(channel, in, printRemoteOutput);
		channel.disconnect();
		in.close();

		return exitStatus;
	}

	/**
	 * This method retrieves the public DNS for an instance on a EC2 client.
	 * 
	 * @param instanceId - The ID of an EC2 instance.
	 * @return The instance public DNS
	 */
	private static String getDns(String instanceId)
	{
		// try to retrieve DNS name from local map
		LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);
		if (localInstanceState != null)
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
	 * @param username - The username on the instance
	 * @return session - The session for instance specified by ID.
	 * @throws JSchException
	 */
	private static Session getSession(String instanceId, String username, boolean forceNewSession)
	{
		Session session = null;
		String publicDns = getDns(instanceId);

		// pick session for instance associated to instanceId
		if (!forceNewSession)
		{
			session = AmazonService.localInstances.get(instanceId).getSession();
		}

		if (forceNewSession)
		{
			LOGGER.warning("Existing session for instance " + instanceId + " will be discarded!");
		}

		// instantiate a new session
		if (session == null)
		{
			LOGGER.warning("There is no existing session for instance " + instanceId + "!");

			// instantiate a secure shell
			JSch jsch = new JSch();

			// specify private key
			String userHome = System.getProperty("user.home");
			try
			{
				// set private key for session
				jsch.addIdentity(userHome + "/.aws/" + AmazonService.MY_KEY + ".pem");

				// create a ssh session
				session = jsch.getSession(username, publicDns, 22);
			}
			catch (JSchException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
			}
			session.setConfig("StrictHostKeyChecking", "no"); // do not look for known_hosts file

			// save session in local map
			LocalInstanceState instanceState = AmazonService.localInstances.get(instanceId);
			instanceState.setSession(session);
			AmazonService.localInstances.put(instanceId, instanceState);
		}

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
		AmazonService.localInstances = new HashMap<>();

		LOGGER.warning("Downloading available instances...");

		DescribeInstancesRequest listRequest = new DescribeInstancesRequest();
		DescribeInstancesResult result = ec2.describeInstances();
		boolean done = false;

		while (!done)
		{
			for (Reservation reservation: result.getReservations())
			{
				for (Instance instance: reservation.getInstances())
				{
					String instanceId = instance.getInstanceId();
					String instanceDns = AmazonService.getDns(instanceId);
					String stateName = instance.getState().getName();
					boolean isRunning = stateName.equals(InstanceStateName.Running.toString());
					boolean terminated = stateName.equals(InstanceStateName.Terminated.toString());

					// put in the map only non-terminated instances
					if (!terminated)
					{
						LocalInstanceState localInstance = new LocalInstanceState(instanceId, instanceDns);
						localInstance.setRunning(isRunning);
						AmazonService.localInstances.put(instanceId, localInstance);						
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
				LocalInstanceStateManager.saveLocalInstanceStates(AmazonService.localInstances);
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
	 * @param instanceId
	 */
	public static void installDMason(String instanceId)
	{
		// check if DMASON is already installed on required instance machine
		LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);

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
			session = AmazonService.getSession(instanceId, AmazonService.amiUser, false); // username is set according to AMI
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
				AmazonService.localInstances.put(instanceId, localInstanceState);
				LOGGER.warning("DMASON was already installed on instance " + instanceId + "!");
				return;
			}

			// re-establish session because of SFTP request
			session = getSession(instanceId, AmazonService.amiUser, false);
			session.connect();

			// update remote repositories
			LOGGER.info("Update repositories...");
			exitStatus = executeCommand(session, "sudo apt-get update -q -y", true);
			LOGGER.info("Remote repositories have been updated!");
			LOGGER.info("Connection returned " + exitStatus);

			// install Java Development Kit
			LOGGER.info("Installing Java Development Kit...");
			exitStatus = executeCommand(session, "sudo apt-get install default-jdk -q -y", true); // -q quiet -y assert-all
			LOGGER.info("Connection returned " + exitStatus);
			exitStatus = executeCommand(session, "dpkg --get-selections | grep jdk", true);
			LOGGER.info("Java Development Kit has been installed!");
			LOGGER.info("Connection returned " + exitStatus);

			// install maven
			LOGGER.info("Installing Maven...");
			exitStatus = executeCommand(session, "sudo apt-get install maven -y", true);
			LOGGER.info("Maven has been installed!");
			LOGGER.info("Connection returned " + exitStatus);

			// install DMASON
			LOGGER.info("Downloading DMASON...");
			exitStatus = executeCommand(
					session,
					"mkdir isislab" + ";" +
					"cd isislab/" + ";" +
					"git clone https://github.com/isislab-unisa/dmason.git",
					true
			);
			LOGGER.info("DMASON has been downloaded to instance " + instanceId + "!");
			LOGGER.info("Connection returned " + exitStatus);

			// compile DMASON in 'maven' folder
			LOGGER.info("Compiling DMASON...");
			exitStatus = executeCommand(
					session,
					"cd ~/isislab/dmason/" + ";" + // every connection starts from home directory
					"mvn -Dmaven.test.skip=true clean package",
					true
			); // skip tests
			LOGGER.info("DMASON has been compiled and is ready to run!");
			LOGGER.info("Connection returned " + exitStatus);
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
				AmazonService.localInstances.put(instanceId, localInstanceState);
			}
		}

		// marking instance as DMASON-ready
		localInstanceState.setReady(true);
		AmazonService.localInstances.put(instanceId, localInstanceState);

		LOGGER.info("DMASON installation has been completed!");
	} // end installDMason(String, String)

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
			input = new FileInputStream(PROPERTIES_FILE_PATH);
			if (AmazonService.startProperties == null)
			{
				AmazonService.startProperties = new Properties();
			}
			startProperties.load(input);

			// set class parameters from config file
			AmazonService.setAmi(startProperties.getProperty("ami"));
			AmazonService.setAmiUser(startProperties.getProperty("amiuser"));
			AmazonService.setName(startProperties.getProperty("name"));
			AmazonService.setRegion(startProperties.getProperty("region"));
			AmazonService.setSize(Integer.parseInt(startProperties.getProperty("size")));
			AmazonService.setType(startProperties.getProperty("type"));
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

	public void persistInstanceStates()
			throws FileNotFoundException, IOException
	{
		LocalInstanceStateManager.saveLocalInstanceStates(AmazonService.localInstances);
	}

	public static void putFile(String instanceId, String localPath, String remotePath, String fileName)
	{
		LOGGER.info("Copying file " + localPath + "/" + fileName + " to " + remotePath + " directory of instance " + instanceId + "...");
		String absFilePath = "";
		boolean copied = false;
		if (localPath != null && !localPath.isEmpty())
		{
			absFilePath = localPath + "/" + fileName;
		}
		else
		{
			absFilePath = fileName;
		}
		LOGGER.info("Full file path: " + absFilePath);
		File localFile = new File(absFilePath);
		Session session = null;
		ChannelSftp channel = null;

		// establish a ssh session
		LOGGER.info("Establishing a new session...");
		try
		{
			// connect to session
			session = getSession(instanceId, AmazonService.amiUser, true);
			session.connect(AmazonService.SESSION_TIMEOUT);

			// start SFTP connection through session
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(AmazonService.CONNECTION_TIMEOUT);
			if (remotePath != null && !remotePath.isEmpty())
			{
				channel.cd(remotePath);
			}

			// send file to remote location
			channel.put(new FileInputStream(localFile), localFile.getName());
			copied = true;
		}
		catch (JSchException | SftpException | IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		finally
		{
			// close channel and session
			if (channel != null && channel.isConnected())
			{
				channel.disconnect();
			}
			if (session != null && session.isConnected())
			{
				session.disconnect();

				// discard session: this is a workaround for packet loss closing session after a SFTP channel connection
				LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);
				localInstanceState.setSession(null);
				AmazonService.localInstances.put(instanceId, localInstanceState);
			}
		}

		if (copied)
		{
			LOGGER.info("Copied file " + fileName + " to " + "\"" + remotePath + "\"" + " of instance " + instanceId + ".");
		}
	} // end putFile(String, String, String, String)

	/**
	 * This method processes remote input from a console and
	 * returns its status code.
	 * 
	 * @param channel
	 * @param in
	 * @param printOutput
	 * @return exitStatus - The exit status of the console
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int readRemoteInput(ChannelExec channel, InputStream in, boolean printOutput)
			throws IOException
	{
		byte[] tmp = new byte[1024];
		int exitStatus = -1;

		while (true)
		{
			while (in.available() > 0)
			{
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
				{
					break;
				}
				if (printOutput)
				{
					System.out.print(new String(tmp, 0, i));
				}
			}

			if (channel.isClosed())
			{
				exitStatus = channel.getExitStatus();
				break;
			}
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				LOGGER.severe(e.getClass().getName() + ": " + e.getMessage() + ".");
			}
		}

		return exitStatus;
	}

	/**
	 * This method reboots an existing running instance
	 * by specifying its ID as parameter.
	 * 
	 * @param instanceID
	 * @return
	 */
	public static RebootInstancesResult rebootInstance(String instanceID)
	{
		LOGGER.info("Request for instance " + instanceID + " to reboot.");
		Instance instance = retrieveInstance(instanceID);

		// check if the instance is running
		// check if instance is already stopping
		if (instance.getState().getName().equals(InstanceStateName.Stopped.toString()))
		{
			LOGGER.warning("Instance " + instanceID + " is not running!");
			return null;
		}

		RebootInstancesRequest rebootRequest = new RebootInstancesRequest();

		// define the reboot request
		rebootRequest.withInstanceIds(instanceID);
		RebootInstancesResult result = ec2.rebootInstances(rebootRequest);

		// looks like no state change occurs for rebooting
		LOGGER.info("Instance " + instanceID + " has been started again!");

		return result;
	}

	public static void retrieveFile(String instanceId, String localPath, String remotePath, String fileName)
	{
		LOGGER.info("Retrieving file " + remotePath + "/" + fileName + " from " + instanceId + "...");
		String absFilePath = "";
		boolean retrieved = false;
		if (localPath != null && !localPath.isEmpty())
		{
			absFilePath = localPath + "/" + fileName;
		}
		else
		{
			absFilePath = fileName;
		}
		LOGGER.info("Full file path: " + absFilePath);
		Session session = null;
		ChannelSftp channel = null;

		// establish a ssh session
		LOGGER.info("Establishing a new session...");
		try
		{
			// retrieve session for specified instance
			session = getSession(instanceId, AmazonService.amiUser, true);
			session.connect(AmazonService.SESSION_TIMEOUT); // 30s timeout

			// create sftp channel
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(AmazonService.CONNECTION_TIMEOUT);
			if (remotePath != null && !remotePath.isEmpty())
			{
				channel.cd(remotePath);
			}

			// retrieve file from remote location
			channel.get(remotePath.concat(fileName), localPath.concat(fileName));
			retrieved = true;
		}
		catch (JSchException | SftpException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		finally
		{
			// close channel and session
			if (channel != null && channel.isConnected())
			{
				channel.disconnect();
			}
			if (session != null && session.isConnected())
			{
				session.disconnect();

				// discard session: this is a workaround for packet loss closing session after a SFTP channel connection
				LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);
				localInstanceState.setSession(null);
				AmazonService.localInstances.put(instanceId, localInstanceState);
			}
		}

		if (retrieved)
		{
			LOGGER.info("Retrieved file " + fileName + " from " + "\"" + remotePath + "\"" + " of instance " + instanceId + ".");
		}
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
			LocalInstanceState localInstance = AmazonService.localInstances.get(instanceId);
			localInstance.setRunning(isRunning);
			AmazonService.localInstances.put(instanceId, localInstance);
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
	 * 
	 * @param instanceId
	 * @param isMaster
	 * @param numWorkers 
	 */
	public static void startDMason(String instanceId, boolean isMaster, int numWorkers)
	{
		LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);

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
		LOGGER.info("");
		Session session = null;
		try {
			session = getSession(instanceId, AmazonService.amiUser, false); // username is set according to AMI
			session.connect(AmazonService.SESSION_TIMEOUT); // 30s timeout
			int exitStatus = 0;
			final String DMASON_ABS_PATH = "~/isislab/dmason/target/";
			final String version = VersionChooser.extract(); // determine which DMASON version is running

			// check if actually DMASON is running on instance
			// if remote 'ps x' output contains 'java' then skip start command
			final String LOG_FILE_NAME = "ps.log";
			LOGGER.info("Check if DMASON is already running...");
			exitStatus = executeCommand(
					session,
					"ps x | grep 'java -jar DMASON' | grep -v 'grep' > " + LOG_FILE_NAME, // in case of ls error, an empty file gets generated
					true);
			LOGGER.info("");
			LOGGER.info("Connection returned " + exitStatus);
			session.disconnect();
			retrieveFile(instanceId, "", "", LOG_FILE_NAME);
			File logFile = new File(LOG_FILE_NAME);
			if (logFile.length() > 0)
			{
				localInstanceState.setBusy(true);
				AmazonService.localInstances.put(instanceId, localInstanceState);
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
			AmazonService.localInstances.put(instanceId, localInstanceState);
			if (isMaster)
			{
				LOGGER.info("DMASON is running as master on http://" + localInstanceState.getDns() + ":8080 !");
			}
			else
			{
				LOGGER.info("DMASON is running as worker on http://" + localInstanceState.getDns() + ":8080 !");
			}

			// re-establish session because of SFTP request
			session = getSession(instanceId, AmazonService.amiUser, false);
			session.connect();

			// 
			if (isMaster)
			{
				LOGGER.info("Running as master...");
				exitStatus = executeCommand(
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
				exitStatus = executeCommand(
						session,
						"cd " + DMASON_ABS_PATH + ";" +
						"java -jar DMASON-" + version + ".jar -m worker -ns " + numWorkers,
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
	 * This method starts an existing instance by specifying
	 * its ID as parameter.
	 * 
	 * @param instanceId
	 * @return result
	 */
	public static StartInstancesResult startInstance(String instanceId)
	{
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
	}

	public static void stopDMason(String instanceId)
	{
		LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceId);

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
			session = getSession(instanceId, AmazonService.amiUser, false); // username is set according to AMI
			session.connect(AmazonService.SESSION_TIMEOUT); // 30s timeout
			int exitStatus = 0;

			// check if actually DMASON is running on instance
			// if remote 'ps x' output does not contains 'java' then skip stop command
			final String LOG_FILE_NAME = "ps.log";
			LOGGER.info("Check if DMASON is running...");
			exitStatus = executeCommand(
					session,
					"ps x | grep 'java -jar DMASON' | grep -v 'grep' > " + LOG_FILE_NAME, // in case of ls error, an empty file gets generated
					true);
			LOGGER.info("");
			LOGGER.info("Connection returned " + exitStatus);
			session.disconnect();
			retrieveFile(instanceId, "", "", LOG_FILE_NAME);
			File logFile = new File(LOG_FILE_NAME);
			if (logFile.length() == 0)
			{
				// mark DMASON running for instance in local map and return control
				localInstanceState.setBusy(false);
				AmazonService.localInstances.put(instanceId, localInstanceState);
				LOGGER.warning("DMASON was not running on instance " + instanceId + "!");
				return;
			}

			// re-establish session because of SFTP request
			session = getSession(instanceId, AmazonService.amiUser, false);
			session.connect();

			LOGGER.info("Running as master...");
			exitStatus = executeCommand(
					session,
					"pkill java",
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
		AmazonService.localInstances.put(instanceId, localInstanceState);
		try
		{
			LocalInstanceStateManager.saveLocalInstanceStates(AmazonService.localInstances);
		}
		catch (IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		LOGGER.info("Stopped DMASON on " + localInstanceState.getDns() + "!");
	}

	/**
	 * This method stops an existing running instance
	 * by specifying its ID as parameter.
	 * 
	 * @param instanceID
	 * @return
	 */
	public static StopInstancesResult stopInstance(String instanceID)
	{
		LOGGER.info("Request for instance " + instanceID + " to stop.");
		Instance instance = retrieveInstance(instanceID);
		String instanceState = instance.getState().getName();

		// check if instance is already stopping
		if (instanceState.equals(InstanceStateName.Stopping.toString()))
		{
			LOGGER.warning("Instance " + instanceID + " is already stopping");
			return null;
		}

		// check if instance has been already stopped
		if (instanceState.equals(InstanceStateName.Stopped.toString()))
		{
			LOGGER.warning("Instance " + instanceID + " has been already stopped!");
			return null;
		}

		StopInstancesRequest stopRequest = new StopInstancesRequest();

		// define the stop request
		stopRequest.withInstanceIds(instanceID);
		StopInstancesResult result = ec2.stopInstances(stopRequest);

		// be sure the instance is stopped
		boolean stopped = false;
		do
		{
			instance = retrieveInstance(instanceID);
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
		LOGGER.info("Instance " + instanceID + " has been stopped!");

		return result;
	}

	/**
	 * This method tags an existing instance with its name
	 * and its cluster type.
	 * 
	 * @param istanceId - The instance to tag
	 * @param tag - The name of the tag
	 * @param value - The value of the tag
	 * @param ec2Client - The client containing the instance to tag
	 */
	private static void tagInstance(String istanceId, String tag, String value, AmazonEC2 ec2Client)
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
				.withTags(new Tag(tag, value));
		ec2Client.createTags(tagRequest);
	}

	/**
	 * This method terminates an existing running instance
	 * by specifying its ID as parameter.
	 * 
	 * @param instanceID
	 * @return
	 */
	public static TerminateInstancesResult terminateInstance(String instanceID)
	{
		Instance instance = retrieveInstance(instanceID);

		// check for instance not to be already terminated
		if (instance.getState().getName().equals(InstanceStateName.Terminated.toString()))
		{
			LOGGER.warning("Instance " + instanceID + " has already been terminated!");
			return null;
		}

		TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();

		// define the termination request
		terminateRequest.withInstanceIds(instanceID);
		TerminateInstancesResult result = ec2.terminateInstances(terminateRequest);

		// be sure the instance is terminated
		boolean terminated = false;
		do
		{
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
		LOGGER.info("Instance " + instanceID + " has been stopped!");

		// mark the instance as terminated in local map
		LocalInstanceState localInstanceState = AmazonService.localInstances.get(instanceID);
		localInstanceState.markTerminate();
		AmazonService.localInstances.put(instanceID, localInstanceState);

		return result;
	}

	public static String getAmi()
	{
		return ami;
	}

	public static void setAmi(String ami)
	{
		AmazonService.ami = ami;
	}

	public static String getAmiUser()
	{
		return amiUser;
	}

	public static void setAmiUser(String amiUser)
	{
		AmazonService.amiUser = amiUser;
	}

	public static String getName()
	{
		return name;
	}

	public static void setName(String name)
	{
		AmazonService.name = name;
	}

	public static String getRegion()
	{
		return region;
	}

	public static void setRegion(String region)
	{
		AmazonService.region = region;
	}

	public static int getSize()
	{
		return size;
	}

	public static void setSize(int size)
	{
		AmazonService.size = size;
	}

	public static String getType()
	{
		return type;
	}

	public static void setType(String type)
	{
		AmazonService.type = type;
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
