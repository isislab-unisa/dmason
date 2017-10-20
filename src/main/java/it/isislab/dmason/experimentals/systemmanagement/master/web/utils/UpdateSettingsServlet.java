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
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		saveSettings(request);
	}

	// helper methods
	private void saveSettings(HttpServletRequest request)
	{
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
			case "activemq":
			{
				final String PROPERTIES_PREFIX = "activemq.";
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

			case "amazonaws":
			{
				final String PROPERTIES_PREFIX = "amazonaws.";
				String region = request.getParameter("region");
				String pubkey = request.getParameter("pubkey");
				String prikey = request.getParameter("prikey");

				// check whether parameters have been set
				if (
						region == null ||
						pubkey == null ||
						prikey == null ||
						region.isEmpty() ||
						pubkey.isEmpty() ||
						prikey.isEmpty()
				)
				{
					LOGGER.warning("Empty values for Amazon AWS profile!");
					return;
				}

				LOGGER.info("Setting Amazon AWS EC2 (region: " + region + ")...");
				config.setProperty(PROPERTIES_PREFIX.concat("prikey"), prikey);
				config.setProperty(PROPERTIES_PREFIX.concat("pubkey"), pubkey);
				config.setProperty(PROPERTIES_PREFIX.concat("region"), region);
				try
				{
					builder.save();
				}
				catch (ConfigurationException e)
				{
					LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
				}

				LOGGER.info("Amazon AWS paramaters have been set!");
				break;
			}
			default:
			{
				LOGGER.warning(chooser + " is not a valid value for /updateSettings servlet!");
				break;
			}
		}
	}
}
