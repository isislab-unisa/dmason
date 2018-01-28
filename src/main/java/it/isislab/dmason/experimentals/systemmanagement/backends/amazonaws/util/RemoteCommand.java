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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.EC2Service;

/**
 * @author Bisogno Simone (05121/1177)
 *
 */
public class RemoteCommand
{
	/**
	 * This method executes a remote command on a ChannelExec channel
	 * and returns a status code.
	 *
	 * @param session - The session to use for command execution
	 * @param command - The command to execute on the remote channel
	 * @param printRemoteOutput - Toggle remote console
	 * @return exitStatus - Exit status for the command
	 * @throws JSchException - An error occurred while creating or accessing a secure shell object
	 * @throws IOException - An error occurred while accessing the file
	 * @throws InterruptedException - A thread has been interrupted while running
	 */
	public static int executeCommand(Session session, String command, boolean printRemoteOutput)
			throws JSchException, IOException, InterruptedException
	{
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
//		((Channel) channel).setOutputStream(System.out);
//		channel.setErrStream(System.err);

		channel.setCommand(command);
		channel.connect(RemoteCommand.CONNECTION_TIMEOUT);
		int exitStatus = RemoteCommand.readRemoteInput(channel, in, printRemoteOutput);
		channel.disconnect();
		in.close();

		return exitStatus;
	}

	public static Session retrieveSession(String instanceId)
	{
		Session session = EC2Service.getSession(
				instanceId,
				EC2Service.getAmiUser(), // username set according to AMI
				false
		);
		if (session == null)
		{
			LOGGER.severe("No session has been generated for instance " + instanceId + "!");
			return null;
		}

		session.setPort(22); // TODO attempt to make it connect
		boolean isConnected = false;
		final int MAX_ATTEMPTS = 5;
		int attempt = 0;

		// check if session was already connected or if
		// this method managed to connect it
		while (!session.isConnected() && !isConnected) {
			try
			{
				LOGGER.info("Attempt " + ++attempt + " to connect...");
				session.connect(RemoteCommand.SESSION_TIMEOUT); // FIXME why it doesn't connect?
				isConnected = true;
			}
			catch (JSchException e)
			{
				LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");

				// check whether max number of attempts has been reached
				if (attempt >= MAX_ATTEMPTS)
				{
					LOGGER.severe("Unable to connect to remote resource!");
					e.printStackTrace(); // TODO comment after debugging
					return null;
				}

				// (attempt times 1s) pause before reattempt
				LOGGER.warning("Connection failed, retrying in " + attempt + " second...");
				try
				{
					Thread.sleep(attempt*1000);
				}
				catch (InterruptedException ie) {
					LOGGER.severe(ie.getClass().getSimpleName() + ": " + ie.getMessage() + ".");
				}
			}
		}

		return session;
	}

	// helper methods
	/**
	 * This method processes remote input from a console and
	 * returns its status code.
	 *
	 * @param channel -
	 * @param in -
	 * @param printOutput -
	 * @return exitStatus - The exit status of the console
	 * @throws IOException - An error occurred while accessing the file
	 * @throws InterruptedException - A thread has been interrupted while running
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

	// constants
	private static final Logger LOGGER = Logger.getGlobal();
	public static final int CONNECTION_TIMEOUT = 15000; // 15s
	public static final int SESSION_TIMEOUT = 30000; // 30s
}
