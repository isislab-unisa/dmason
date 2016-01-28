package it.isislab.dmason.experimentals.systemmanagement.worker;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



public class Worker {

	private static String IP="localhost";
	private static String PORT="61616";
	private Properties prop;
	private ConnectionNFieldsWithActiveMQAPI conn=null;

	public Worker() {
		conn=new ConnectionNFieldsWithActiveMQAPI();
		prop = new Properties();

	}
	protected DistributedState makeSimulation(String path_jar_file, GeneralParam params,String prefix) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{

		
		JarFile jar=new JarFile(new File(path_jar_file));
		Enumeration e=jar.entries();
		
		File file  = new File(path_jar_file);

		URL url = file.toURL(); 
		URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
         
        Class distributedState=null;
		while(e.hasMoreElements()){
			
			JarEntry je=(JarEntry)e.nextElement();
			String classPath = je.getName();
			if(!je.getName().contains(".class")) continue;
			
		    String[] nameclass = classPath.split("/");
		    nameclass[0]=((nameclass[nameclass.length-1]).split(".class"))[0];
		    
		    byte[] classBytes = new byte[(int) je.getSize()];
            InputStream input = jar.getInputStream(je);
            BufferedInputStream readInput=new BufferedInputStream(input);
           
            Class c=cl.loadClass(je.getName().replaceAll("/", ".").replaceAll(".class", ""));
            
            if(c.getSuperclass().equals(DistributedState.class))
            	distributedState=c;
            
		}
		if(distributedState==null) return null;
		JarClassLoader cload = new JarClassLoader(new URL("jar:file://"+path_jar_file+"!/"));

		cload.addToClassPath();
		return (DistributedState) cload.getInstance(distributedState.getName(), params,prefix);
//		Constructor constr = distributedState.getConstructor(new Class[]{ params.getClass() });
//		return (DistributedState) constr.newInstance(new Object[]{ params });
		
	}

	protected void loadProperties(){



		//default 127.0.0.1:61616 else you have to change config.properties file
		String filePropPath="resources/systemmanagement/master/conf/config.properties";
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



	protected boolean createConnection(){

		Address address=new Address(IP, PORT);
		System.out.println("connection to server "+address);
		return conn.setupConnection(address);

	}


	public void subToInitialTopic(String initTopic){
		try {
			conn.subscribeToTopic(initTopic);
			conn.asynchronousReceive("iniz");
			for (String x : conn.getTopicList()) {
				System.out.println(x);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 	

}
