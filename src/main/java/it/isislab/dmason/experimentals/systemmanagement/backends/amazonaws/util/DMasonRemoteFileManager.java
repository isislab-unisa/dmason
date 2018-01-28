/**
 * Copyright 2018 Universita' degli Studi di Salerno
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.EC2Service;

/**
 * @author Bisogno Simone (05121/1177)
 *
 */
public class DMasonRemoteFileManager
{
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
			session = RemoteCommand.retrieveSession(instanceId);
			session.connect(RemoteCommand.SESSION_TIMEOUT);

			// start SFTP connection through session
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(RemoteCommand.CONNECTION_TIMEOUT);
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

				// discard session: this is a workaround for
				// packet loss closing session after a SFTP channel connection
				EC2Service.discardSession(instanceId);
			}
		}

		if (copied)
		{
			LOGGER.info(
					"Copied file " + fileName +
					" to " + "\"" + remotePath + "\"" +
					" of instance " + instanceId + "."
			);
		}
	} // end putFile(String, String, String, String)

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
			session = EC2Service.getSession(instanceId, EC2Service.getAmiUser(), true);
			session.connect(RemoteCommand.SESSION_TIMEOUT); // 30s timeout

			// create sftp channel
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(RemoteCommand.CONNECTION_TIMEOUT);
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

				// discard session: this is a workaround for
				// packet loss closing session after a SFTP channel connection
				EC2Service.discardSession(instanceId);
			}
		}

		if (retrieved)
		{
			LOGGER.info(
					"Retrieved file " + fileName +
					" from " + "\"" + remotePath + "\"" +
					" of instance " + instanceId + "."
			);
		}
	} // end retrieveFile(String, String, String, String)

	// constants
	private static final Logger LOGGER = Logger.getGlobal();
}
