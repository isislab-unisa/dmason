package it.isislab.dmason.experimentals.systemmanagement.master;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class MasterServerMain {




	public static void main(String[] args) {

		MasterServer master=new MasterServer();
		
		// 1. Creating the server on port 8080
		Server server = new Server(8080);
		ServletContextHandler handler =new ServletContextHandler(server,"resources/systemmanagement/master");
		server.setHandler(handler);	

		
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
			e.printStackTrace();
		}



	}
}
