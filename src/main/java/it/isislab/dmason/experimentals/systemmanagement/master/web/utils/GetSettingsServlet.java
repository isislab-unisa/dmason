package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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

import com.google.gson.Gson;

/**
 * Servlet implementation class LoadSettingsServlet
 */
public class GetSettingsServlet extends HttpServlet
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
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		this.provideSettings(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		this.provideSettings(request, response);
	}

	private void provideSettings(HttpServletRequest request, HttpServletResponse response)
	{
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

		// extract ActiveMQ parameters
		final String ACTIVEMQ_PREFIX = "activemq".concat(".");
		final String ACTIVEMQ_IP = config.getString(ACTIVEMQ_PREFIX.concat("ipmaster"));
		final String ACTIVEMQ_PORT = config.getString(ACTIVEMQ_PREFIX.concat("portmaster"));

		ActiveMQSettings activeMQSettings = new ActiveMQSettings(ACTIVEMQ_IP, ACTIVEMQ_PORT);
		LOGGER.info("ActiveMQ settings ready to be sent: " + activeMQSettings);;

		// extract Amazon AWS parameters
		final String AMAZONAWS_PREFIX = "amazonaws".concat(".");
		final String PRIKEY = config.getString(AMAZONAWS_PREFIX.concat("prikey"));
		final String PUBKEY = config.getString(AMAZONAWS_PREFIX.concat("pubkey"));
		final String REGION = config.getString(AMAZONAWS_PREFIX.concat("region"));

		AmazonAWSSettings amazonAWSSettings = new AmazonAWSSettings(PRIKEY, PUBKEY, REGION);
		LOGGER.info("Amazon AWS settings ready to be sent: " + amazonAWSSettings);

		Settings settings = new Settings(activeMQSettings, amazonAWSSettings);
		LOGGER.info("Settings ready to be sent: " + settings);

		// put settings in a JSON and send them into the response object
		String jsonObject = (new Gson()).toJson(settings);
		LOGGER.info("JSON ready to be sent: " + jsonObject);
		try
		{
			response.getWriter().write(jsonObject);
		}
		catch (IOException e)
		{
			LOGGER.severe(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
	} // end provideSettings(HttpServletRequest, HttpServletResponse)

	// define the class modelling the JSON for ActiveMQ settings
	static class ActiveMQSettings
			implements Serializable
	{
		// constructor
		public ActiveMQSettings(String ip, String port)
		{
			this.ip = ip;
			this.port = port;
		}

		@Override
		public String toString()
		{
			return "ActiveMQSettings@" + ip + ":" + port;
		}

		// variables
		private String ip;
		private String port;

		// constants
		private static final long serialVersionUID = 1L;
	} // end ActiveMQSettings

	// define the class modelling the JSON for Amazon AWS settings
	static class AmazonAWSSettings
			implements Serializable
	{
		// constructor
		public AmazonAWSSettings(String priKey, String pubKey, String region)
		{
			this.priKey = priKey;
			this.pubKey = pubKey;
			this.region = region;
		}

		@Override
		public String toString()
		{
			return "AmazonAWSSettings [priKey=" + priKey + ", pubKey=" + pubKey + ", region=" + region + "]";
		}

		// variables
		private String priKey;
		private String pubKey;
		private String region;

		// constants
		private static final long serialVersionUID = 1L;
	} // end AmazonAWSSettings

	// define the class modelling the JSON for settings
	static class Settings
			implements Serializable
	{
		// constructor
		public Settings(ActiveMQSettings activeMQSettings, AmazonAWSSettings amazonAWSSettings)
		{
			this.activeMQSettings = activeMQSettings;
			this.amazonAWSSettings = amazonAWSSettings;
		}

		@Override
		public String toString()
		{
			return "Settings " + activeMQSettings + ", " + amazonAWSSettings + "]";
		}

		// variables
		private ActiveMQSettings activeMQSettings;
		private AmazonAWSSettings amazonAWSSettings;

		// constants
		private static final long serialVersionUID = 1L;
	}
} // end GetSettingsServlet
