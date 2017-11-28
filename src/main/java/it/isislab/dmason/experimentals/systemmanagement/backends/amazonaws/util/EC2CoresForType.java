package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class EC2CoresForType
{
	// getters
	public static int getCores(String ec2Type)
	{
		return EC2CoresForType.ec2Types.get(ec2Type);
	}

	// helpers
	private static void initConfig()
	{
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
				new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
				.configure(
						params.properties().setFileName(EC2_TYPES_FILE)
				);
		try
		{
			Configuration tempConfig = builder.getConfiguration();
			startConfig = ConfigurationUtils.unmodifiableConfiguration(tempConfig);
		}
		catch (ConfigurationException e)
		{
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
	}

	// method for map initialization
	private static void initTypeMap()
	{
		// extract property names from configuration
		Iterator<String> configKeys = startConfig.getKeys();

		// import values from configuration into map
		while (configKeys.hasNext())
		{
			String type = configKeys.next();
			int cores = startConfig.getInt(type);
			EC2CoresForType.ec2Types.put(type, cores);
		}

		// prints for test
		Iterator<Map.Entry<String, Integer>> mapEntries = EC2CoresForType.ec2Types.entrySet().iterator();
		while (mapEntries.hasNext())
		{
			Entry<String, Integer> entry = mapEntries.next();
			System.out.println("Entry " + entry.getKey() + ": " + entry.getValue() + ";");
		}
	}

	// constants
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String EC2_TYPES_FILE = "resources" + File.separator + "systemmanagement" +
			File.separator + "master" + File.separator + "conf" + File.separator + "ec2types.properties";

	// static variables
	private static ImmutableConfiguration startConfig;
	private static Map<String, Integer> ec2Types;
	static
	{
		EC2CoresForType.ec2Types = new HashMap<>();
		LOGGER.setUseParentHandlers(false); // comment this line to enable logging
		LOGGER.info("Reading configurations from " + EC2_TYPES_FILE);
		EC2CoresForType.initConfig();
		LOGGER.info("Populating EC2 types map...");
		EC2CoresForType.initTypeMap();
		LOGGER.info("EC2 Types map is ready!");
	}
}
