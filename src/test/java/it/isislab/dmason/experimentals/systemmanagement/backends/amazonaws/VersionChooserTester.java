package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util.VersionChooser;

public class VersionChooserTester
{
	public static void main(String[] args)
	{
		System.out.println("Version extraction...");
		String version = null;
		try
		{
			version = VersionChooser.extract();
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage() + ".");
		}
		System.out.println("DMASON current version is " + version + ".");
	}
}
