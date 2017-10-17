package it.isislab.dmason.experimentals.systemmanagement.backends.amazonaws.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Simone Bisogno
 *
 */
public class VersionChooser
{
	public static String extract()
			throws ParserConfigurationException, SAXException, IOException
	{
		String version = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(new File(POM_PATH));

		Node projectNode = doc.getFirstChild();
		NodeList projectNodeList = projectNode.getChildNodes();
		for (int i = 0; i < projectNodeList.getLength(); i++)
		{
			Node childNode = projectNodeList.item(i);
			if (childNode.getNodeName().equals("version"))
			{
				version = childNode.getTextContent();
				break;
			}
		}

		return version;
	}

	// constants
	private static final String POM_PATH = "pom.xml";
}
