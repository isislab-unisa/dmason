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
package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 *
 *
 *
 * @author Simone Bisogno
 *
 */
public class UpdateSettingsServlet
		extends HttpServlet
{
	// constants
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String PROPERTIES_FILE_PATH = "resources" + File.separator +
			"systemmanagement" + File.separator + "master" + File.separator + "conf" +
			File.separator + "config.properties";
	private static final long serialVersionUID = 1L;

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		saveSettings(request);

		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		saveSettings(request);

		response.setStatus(HttpServletResponse.SC_OK);
	}

	// helper methods
	private void saveSettings(HttpServletRequest request)
	{
		// comment or properly edit following line to enable logging
//		LOGGER.setLevel(Level.OFF);

		String chooser = request.getParameter("setting");
		if (chooser == null)
		{
			LOGGER.warning("Illegal value for chooser!");
			return;
		}

		// use Apache Commons Configuration to
		// edit parameters in config file
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
				new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
					.configure(
							params.properties().setFileName(PROPERTIES_FILE_PATH)
					);
		Configuration config = null;
		try
		{
			config = builder.getConfiguration();
		}
		catch (ConfigurationException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		if (config == null)
		{
			LOGGER.severe("Error in reading properties file!");
			return;
		}

		chooser = chooser.toLowerCase();
		LOGGER.info("I received request to update " + chooser + ".");

		switch (chooser)
		{
			case "general":
			{
				final String PROPERTIES_PREFIX = "general".concat(".");
				String perfTrace = request.getParameter("enableperftrace"); // TODO is a value check needed?

				// persist data into file
				LOGGER.info("Enable performance trace: " + perfTrace);
				config.setProperty(PROPERTIES_PREFIX.concat("enableperftrace"), perfTrace);
				try
				{
					builder.save();
				}
				catch (ConfigurationException e)
				{
					LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
				}

				LOGGER.info("General settings have been set!");
				break;
			} // end case general

			case "activemq":
			{
				final String PROPERTIES_PREFIX = "activemq".concat(".");
				String ip = request.getParameter("activemqip");
				String port = request.getParameter("activemqport");

				// check whether parameters have been set
				if (ip == null || port == null || ip.isEmpty() || port.isEmpty())
				{
					LOGGER.warning("Empty values for ActiveMQ server!");
					return;
				}

				// persist data into file
				LOGGER.info("Setting Apache ActiveMQ on " + ip + ":" + port);
				config.setProperty(PROPERTIES_PREFIX.concat("ipmaster"), ip);
				config.setProperty(PROPERTIES_PREFIX.concat("portmaster"), port);
				try
				{
					builder.save();
				}
				catch (ConfigurationException e)
				{
					LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
				}

				LOGGER.info("ApacheMQ new address and port have been set!");
				break;
			} // end case activemq

			case "amazonec2":
			{
				final String PROPERTIES_PREFIX = "ec2".concat(".");
				String ec2PubKey = request.getParameter("ec2pubkey");
				String ec2PriKey = request.getParameter("ec2prikey");
				String region = request.getParameter("region");
				String securityGroup = request.getParameter("securitygroup");

				// check whether parameters have been set
				if (
						ec2PubKey == null ||
						ec2PriKey == null ||
						region == null ||
						securityGroup == null ||
						ec2PubKey.isEmpty() ||
						ec2PriKey.isEmpty() ||
						region.isEmpty() ||
						securityGroup.isEmpty()
				)
				{
					LOGGER.warning(
							"Empty values for Amazon AWS profile!\n" +
							"EC2 console public key: " + ec2PubKey + ", " +
							"EC2 console private key: " + ec2PriKey + ", " +
							"Region: " + region + ", " +
							"Security group: " + securityGroup + "."
					);
					return;
				}

				LOGGER.info("Setting Amazon AWS EC2 (region: " + region + ")...");
				config.setProperty(PROPERTIES_PREFIX.concat("consoleprikey"), ec2PriKey);
				config.setProperty(PROPERTIES_PREFIX.concat("consolepubkey"), ec2PubKey);
				config.setProperty(PROPERTIES_PREFIX.concat("region"), region);
				config.setProperty(PROPERTIES_PREFIX.concat("securitygroup"), securityGroup);
				try
				{
					builder.save();
				}
				catch (ConfigurationException e)
				{
					LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
				}

				LOGGER.info("Amazon EC2 paramaters have been set!");
				break;
			} // end case amazonec2
			default:
			{
				LOGGER.warning(chooser + " is not a valid value for /updateSettings servlet!");
				break;
			}
		}
	}
}
