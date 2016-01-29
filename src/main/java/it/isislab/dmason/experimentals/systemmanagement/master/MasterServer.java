package it.isislab.dmason.experimentals.systemmanagement.master;

import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;

public class MasterServer{

	private  String IP="";
	private  String PORT="";

	private BrokerService broker=null;
	private Properties prop = null;
	private ConnectionNFieldsWithActiveMQAPI conn=null;

	public MasterServer(){
		prop = new Properties();
		broker = new BrokerService();
		conn=new ConnectionNFieldsWithActiveMQAPI();
	}


	protected void sendJar(){

	}




	protected void startActivemq(){


		String address="tcp://"+IP+":"+PORT;

		try {
			broker.addConnector(address);
			System.out.println("Starting activemq "+address);
			broker.start();

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	protected void loadProperties(){



		//default 127.0.0.1:61616 else you have to change config.properties file
		String filePropPath="resources/systemmanagement/master/conf/config.properties";
		InputStream input=null;

		//load params from properties file 
		try {
			input=new FileInputStream(filePropPath);	
			prop.load(input);
			this.setIP(prop.getProperty("ipmaster"));
			this.setPORT(prop.getProperty("portmaster"));

		} catch (IOException e2) {
			System.err.println(e2.getMessage());
		}finally{try {
			input.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());

		}}
	}


	protected boolean createConnection(){

		Address address=new Address(IP, PORT);
		System.out.println("Creating connection to server "+address);
		return conn.setupConnection(address);

	}





	protected  void createInitialTopic(String topic){


		try {
			conn.createTopic(topic, 1);
			conn.subscribeToTopic(topic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	//	public static void main(String[] args) {
	//
	//
	//		loadProperties();
	//		startActivemq();
	//		createConnection();
	//		createStartTopic("Master");
	//
	//
	//
	//
	//
	//
	//
	//
	//		// 1. Creating the server on port 8080
	//		Server server = new Server(8080);
	//		//server.setHandler(handler);	
	//
	//
	//
	//		// 2. Creating the WebAppContext for the created content
	//		WebAppContext ctx = new WebAppContext();
	//		ctx.setResourceBase("resources/systemmanagement/master");
	//		ctx.setContextPath("/master");
	//
	//
	//		//3. Including the JSTL jars for the webapp.
	//		ctx.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*jstl.*\\.jar$");
	//
	//		//4. Enabling the Annotation based configuration
	//		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
	//		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
	//		classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");
	//
	//		//5. Setting the handler and starting the Server
	//		server.setHandler(ctx);
	//		try {
	//			server.start();
	//			server.join();
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//	}


	//getters and setters
	public String getIP() {return IP;}
	public void setIP(String iP) {IP = iP;}
	public String getPORT() {return PORT;}
	public void setPORT(String port) {PORT = port;}
}
