package it.isislab.dmason.experimentals.systemmanagement.master;


import it.isislab.dmason.experimentals.util.management.master.MasterDaemonStarter;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.broker.BrokerService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class MasterServer{


	public static void main(String[] args) {


		//Start activemq connection		
		BrokerService broker = new BrokerService();
		//broker.setBrokerName(brokerName);
	
		// configure the broker
		try {
			broker.addConnector("tcp://localhost:61616");
			broker.setBrokerName("pepp");
			broker.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		
		
		

		// 1. Creating the server on port 8080
		Server server = new Server(8080);
		//server.setHandler(handler);
		
		ConnectionNFieldsWithActiveMQAPI conn=new ConnectionNFieldsWithActiveMQAPI();
		Address address=new Address("localhost", "61616");
        conn.setupConnection(address);	
		MasterDaemonStarter n =new MasterDaemonStarter(conn, null);
		

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
