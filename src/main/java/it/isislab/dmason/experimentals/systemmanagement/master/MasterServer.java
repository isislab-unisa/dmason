package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class MasterServer{

	private static String IP;
	private static String PORT;

    private static void startActivemq(){
    	BrokerService broker=null;
		broker = new BrokerService();
		String address="tcp://"+IP+":"+PORT;
		//se remoto altrimente local:61616
		
		try {
			broker.addConnector(address);
			System.out.println("Starting activemq "+address);
			broker.start();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
	private static void loadProperties(){


		Properties prop = new Properties();
		String filePropPath="config.properties";
		InputStream input=null;

		//load params from properties file 
		try {
			input=new FileInputStream(filePropPath);	
			prop.load(input);
			IP=prop.getProperty("ipmaster");
			PORT=prop.getProperty("portmaster");
			
		} catch (IOException e2) {
			System.err.println(e2.getMessage());
		}finally{try {
			input.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());

		}}
	}


	private static boolean createConnection(){
	
		ConnectionNFieldsWithActiveMQAPI conn=new ConnectionNFieldsWithActiveMQAPI();
		Address address=new Address(IP, PORT);
		System.out.println("Creating connection to server "+address);
		return conn.setupConnection(address);
		
	}

	public static void main(String[] args) {


		loadProperties();
		startActivemq();
		createConnection();
		
		
	





		// 1. Creating the server on port 8080
		Server server = new Server(8080);
		//server.setHandler(handler);	



		// 2. Creating the WebAppContext for the created content
		WebAppContext ctx = new WebAppContext();
		ctx.setResourceBase("resources/systemmanagement/master");
		ctx.setContextPath("/master");


		//3. Including the JSTL jars for the webapp.
		ctx.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*jstl.*\\.jar$");

		//4. Enabling the Annotation based configuration
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		//5. Setting the handler and starting the Server
		server.setHandler(ctx);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
