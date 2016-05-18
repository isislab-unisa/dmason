/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.experimentals.systemmanagement.master;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import it.isislab.dmason.experimentals.systemmanagement.utils.activemq.ActiveMQStarter;


/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class MasterServerMain {

	private boolean enableUI = false;
	public static MasterServer ms=null;
	

	public MasterServerMain() {
		enableUI = true;
	}

	public MasterServerMain(boolean WebUI) {
		this.enableUI = WebUI;
	}
	
	public MasterServer getMasterServer(){ return ms;} 
	
	public void start(){
		
        //set params of jvm
		
		
		// 1. Creating the server on port 8080
		Server server = null;
		if(enableUI){
			server = new Server(8080);
			ServletContextHandler handler =new ServletContextHandler(server,"resources/systemmanagement/master");	
			server.setHandler(handler);	
			
			// 2. Creating the WebAppContext for the created content
			WebAppContext ctx = new WebAppContext();
			ctx.setResourceBase("resources/systemmanagement/master");
			ctx.setContextPath("/");
	
			//3. Including the JSTL jars for the webapp.
			ctx.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*jstl.*\\.jar$");
	
			//4. Enabling the Annotation based configuration
			org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
			classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
			classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");
	
			
	
			//5. Setting the handler and starting the Server
			server.setHandler(ctx);
		}
		//Launch activeMq server
		ActiveMQStarter amqS = new ActiveMQStarter();		
		amqS.startActivemq();
		
		if(!enableUI){
			ms = new MasterServer();
		}
		
		if(enableUI){
			try {
				if(server != null){
					server.start();
					server.join();
				}else{
					System.err.println("Someting was wrong");
					System.exit(-1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		//System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*"); version under 5.12.2
		MasterServerMain msm = new MasterServerMain(); 
		msm.start();
	}
}