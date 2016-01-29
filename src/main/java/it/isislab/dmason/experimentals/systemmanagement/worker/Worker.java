/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.systemmanagement.worker;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.experimentals.util.management.JarClassLoader;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.net.ftp.FTPClient;


/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class Worker {

	private String IP="";
	private String PORT="";
	private String TOPICPREFIX="";

	private ConnectionNFieldsWithActiveMQAPI conn=null;

	public Worker() {}

    /**
     * 
     * @param ipMaster
     * @param portMaster
     * @param topicPrefix
     */
	public Worker(String ipMaster,String portMaster,String topicPrefix) {
		this.IP=ipMaster;
		this.PORT=portMaster;
		this.TOPICPREFIX=topicPrefix;
		this.conn=new ConnectionNFieldsWithActiveMQAPI();

	}
	
	/**
	 * 
	 * @param path_jar_file
	 * @param params
	 * @param prefix
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	
	
	
	
	protected void downloadJar(Address address){
		
				
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

	/*protected void loadProperties(){



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
	}*/



	protected boolean createConnection(){
		Address address=new Address(this.getIP(), this.getPORT());
		System.out.println("connection to server "+address);
		return conn.setupConnection(address);

	}


	protected void subToInitialTopic(String initTopic){
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



	//getters and setters
	public String getIP() {return IP;}
	public void setIP(String iP) {IP = iP;}
	public String getPORT() {return PORT;}
	public void setPORT(String port) {PORT = port;}
	public String getTopicPrefix() {return TOPICPREFIX;}
	public void setTopicPrefix(String topicPrefix) {TOPICPREFIX = topicPrefix;}



}
